package com.hibiscusmc.hmccosmetics.database.types;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.section.DatabaseSettings;
import com.hibiscusmc.hmccosmetics.database.UserData;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PostgreSQL storage, meant to be shared by several servers.
 * <p>
 * All database work runs on one dedicated thread, in the order it was requested: the main thread never waits for the
 * database, and a load queued after a save of the same player always sees that save. Because of that, the bookkeeping
 * below is only ever touched by that thread and needs no locking.
 * <p>
 * Stale writes: every row has a {@code version}. A load remembers the version it read and a save only replaces that
 * exact version, so a late save made from an old state is discarded instead of overwriting what another server saved
 * since. After a discarded save, nothing more is written for that player until the next load.
 * <p>
 * The table has the columns of the MySQL one ({@code uuid}, {@code cosmetics}) plus {@code version}, so existing data
 * can be copied over as it is.
 */
public class PostgreSQLData extends Data {

    private static final int ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 2000;
    private static final long SHUTDOWN_WAIT_SECONDS = 30;
    private static final String UNIQUE_VIOLATION = "23505";

    /** What this server last read or wrote for a player; version 0 means there is no row. */
    private record Row(long version, String cosmetics) {
        static final Row NONE = new Row(0, "");
    }

    @FunctionalInterface
    private interface SqlTask<T> {
        T run(Connection connection) throws SQLException;
    }

    private HikariDataSource pool;
    private ExecutorService executor;
    private Logger logger;
    private long retryDelayMs = RETRY_DELAY_MS;

    // Only used on the executor thread.
    private final Map<UUID, Row> known = new HashMap<>();
    private final Set<UUID> stale = new HashSet<>();

    @Override
    public void setup() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://" + DatabaseSettings.getPostgresHost() + ":" + DatabaseSettings.getPostgresPort()
                + "/" + DatabaseSettings.getPostgresDatabase());
        config.setUsername(DatabaseSettings.getPostgresUsername());
        config.setPassword(DatabaseSettings.getPostgresPassword());
        config.setPoolName("HMCCosmetics");
        // One thread uses the database, so one connection is enough.
        config.setMaximumPoolSize(1);
        // Start even when the database is down; the first operations retry.
        config.setInitializationFailTimeout(-1);
        config.setConnectionTimeout(5000);
        config.addDataSourceProperty("socketTimeout", "15");
        init(new HikariDataSource(config), HMCCosmeticsPlugin.getInstance().getLogger());
    }

    /** Used by {@link #setup()}, and by the tests with their own pool. */
    void init(@NotNull HikariDataSource dataSource, @NotNull Logger logger) {
        this.pool = dataSource;
        this.logger = logger;
        this.executor = Executors.newSingleThreadExecutor(runnable -> new Thread(runnable, "HMCCosmetics-PostgreSQL"));
        executor.execute(this::createTable);
    }

    /** Returns when everything queued so far has run; used by the tests. */
    void awaitIdle() throws InterruptedException, ExecutionException {
        executor.submit(() -> {}).get();
    }

    void setRetryDelayMs(long retryDelayMs) {
        this.retryDelayMs = retryDelayMs;
    }

    private void createTable() {
        try {
            withRetry(connection -> {
                try (Statement statement = connection.createStatement()) {
                    statement.execute("CREATE TABLE IF NOT EXISTS COSMETICDATABASE (UUID varchar(36) PRIMARY KEY, COSMETICS text, VERSION bigint NOT NULL DEFAULT 1)");
                    // A table copied from another storage has no version column; its rows start at version 1.
                    statement.execute("ALTER TABLE COSMETICDATABASE ADD COLUMN IF NOT EXISTS VERSION bigint NOT NULL DEFAULT 1");
                }
                return null;
            });
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Could not create the PostgreSQL table; loading and saving will fail until it exists.", e);
        }
    }

    @Override
    public CompletableFuture<UserData> get(UUID uniqueId) {
        return load(uniqueId).thenApply(cosmetics -> {
            UserData data = new UserData(uniqueId);
            data.setCosmetics(deserializeData(cosmetics));
            return data;
        });
    }

    /** Reads the stored cosmetics and remembers the row as the base of this server's next save. */
    CompletableFuture<String> load(UUID uniqueId) {
        CompletableFuture<String> future = new CompletableFuture<>();
        executor.execute(() -> {
            try {
                Row row = withRetry(connection -> read(connection, uniqueId));
                known.put(uniqueId, row);
                stale.remove(uniqueId);
                future.complete(row.cosmetics());
            } catch (SQLException e) {
                // Completing exceptionally (not with empty data) keeps the caller from saving over the stored cosmetics.
                future.completeExceptionally(new CompletionException("Could not load cosmetics of " + uniqueId, e));
            }
        });
        return future;
    }

    @Override
    public void save(CosmeticUser user) {
        // Serialized on the calling (main) thread; the write happens later on the database thread.
        save(user.getUniqueId(), serializeData(user));
    }

    void save(UUID uniqueId, String cosmetics) {
        executor.execute(() -> {
            try {
                withRetry(connection -> write(connection, uniqueId, cosmetics));
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Could not save cosmetics of " + uniqueId, e);
            }
        });
    }

    @Override
    public void clear(UUID uniqueId) {
        executor.execute(() -> {
            try {
                withRetry(connection -> {
                    try (PreparedStatement statement = connection.prepareStatement("DELETE FROM COSMETICDATABASE WHERE UUID = ?")) {
                        statement.setString(1, uniqueId.toString());
                        statement.executeUpdate();
                    }
                    return null;
                });
                // The row is gone, and whatever this server held is no longer a reason to discard a later save.
                known.put(uniqueId, Row.NONE);
                stale.remove(uniqueId);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Could not clear cosmetics of " + uniqueId, e);
            }
        });
    }

    @Override
    public void shutdown() {
        if (executor == null) return;
        executor.shutdown();
        try {
            if (!executor.awaitTermination(SHUTDOWN_WAIT_SECONDS, TimeUnit.SECONDS)) {
                logger.warning("Some cosmetic data was still being written on shutdown.");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        pool.close();
    }

    /**
     * Writes the cosmetics unless the row changed since this server last saw it.
     * A write that is not applied is checked against the row: equal means an earlier attempt of this same write got
     * through (a retry after a lost answer), anything else means a newer state won and this one is discarded.
     */
    private Void write(Connection connection, UUID uniqueId, String cosmetics) throws SQLException {
        if (stale.contains(uniqueId)) {
            logger.warning("Discarded the cosmetics save of " + uniqueId + ": it lost to a newer saved state and the player was not loaded again since.");
            return null;
        }
        Row base = known.getOrDefault(uniqueId, Row.NONE);
        // Nothing changed, or nothing to store for a player without a row.
        if (base.version() == 0 ? cosmetics.isEmpty() : base.cosmetics().equals(cosmetics)) return null;

        if (applyWrite(connection, uniqueId, cosmetics, base.version())) {
            known.put(uniqueId, new Row(base.version() + 1, cosmetics));
            return null;
        }
        Row current = read(connection, uniqueId);
        known.put(uniqueId, current);
        if (current.version() != 0 && current.cosmetics().equals(cosmetics)) return null;
        stale.add(uniqueId);
        logger.warning("Discarded the cosmetics save of " + uniqueId + ": the stored data changed since it was loaded here (version "
                + base.version() + " -> " + current.version() + ").");
        return null;
    }

    /** Inserts the first row, or replaces the row of the given version. */
    private boolean applyWrite(Connection connection, UUID uniqueId, String cosmetics, long baseVersion) throws SQLException {
        // Standard SQL, so the tests can run on a PostgreSQL-compatible in-memory database.
        String sql = baseVersion == 0
                ? "INSERT INTO COSMETICDATABASE (UUID, COSMETICS, VERSION) SELECT ?, ?, 1 WHERE NOT EXISTS (SELECT 1 FROM COSMETICDATABASE WHERE UUID = ?)"
                : "UPDATE COSMETICDATABASE SET COSMETICS = ?, VERSION = VERSION + 1 WHERE UUID = ? AND VERSION = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (baseVersion == 0) {
                statement.setString(1, uniqueId.toString());
                statement.setString(2, cosmetics);
                statement.setString(3, uniqueId.toString());
            } else {
                statement.setString(1, cosmetics);
                statement.setString(2, uniqueId.toString());
                statement.setLong(3, baseVersion);
            }
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            // Another server inserted the same row between the check and the insert: the row exists, so it was not applied.
            if (UNIQUE_VIOLATION.equals(e.getSQLState())) return false;
            throw e;
        }
    }

    private static Row read(Connection connection, UUID uniqueId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT COSMETICS, VERSION FROM COSMETICDATABASE WHERE UUID = ?")) {
            statement.setString(1, uniqueId.toString());
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) return Row.NONE;
                String cosmetics = rs.getString("COSMETICS");
                return new Row(rs.getLong("VERSION"), cosmetics == null ? "" : cosmetics);
            }
        }
    }

    /** Runs the task, trying again after a pause when the database cannot be reached. Retries keep the executor's order. */
    private <T> T withRetry(SqlTask<T> task) throws SQLException {
        for (int attempt = 1; ; attempt++) {
            try (Connection connection = pool.getConnection()) {
                return task.run(connection);
            } catch (SQLException e) {
                if (attempt == ATTEMPTS) throw e;
                logger.warning("PostgreSQL failed (attempt " + attempt + "/" + ATTEMPTS + "): " + e.getMessage());
                try {
                    Thread.sleep(retryDelayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
            }
        }
    }
}
