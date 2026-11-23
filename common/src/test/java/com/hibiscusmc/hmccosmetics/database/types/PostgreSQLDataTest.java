package com.hibiscusmc.hmccosmetics.database.types;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Two "servers" (two {@link PostgreSQLData} with their own pool) share one table. The database is H2 in its PostgreSQL
 * compatibility mode, so no server is needed. Every call queues work in order, so waiting for a load drains the
 * queue before it.
 */
class PostgreSQLDataTest {

    private final String url = "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1";
    private final UUID player = UUID.randomUUID();
    private final List<PostgreSQLData> servers = new java.util.ArrayList<>();

    private PostgreSQLData server() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setMaximumPoolSize(1);
        PostgreSQLData data = new PostgreSQLData();
        data.init(new HikariDataSource(config), Logger.getAnonymousLogger());
        data.setRetryDelayMs(10);
        servers.add(data);
        return data;
    }

    @AfterEach
    void shutdownServers() {
        servers.forEach(PostgreSQLData::shutdown);
        if (direct != null) direct.close();
    }

    private void join(PostgreSQLData server) {
        server.load(player).join();
    }

    private void save(PostgreSQLData server, String cosmetics) throws Exception {
        server.save(player, cosmetics);
        server.awaitIdle();
    }

    private HikariDataSource direct;

    private Connection direct() throws SQLException {
        if (direct == null) direct = new HikariDataSource(cfg());
        return direct.getConnection();
    }

    private String stored() throws SQLException {
        try (Connection c = direct(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT COSMETICS FROM COSMETICDATABASE WHERE UUID = '" + player + "'")) {
            return rs.next() ? rs.getString(1) : null;
        }
    }

    private HikariConfig cfg() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setMaximumPoolSize(1);
        return config;
    }

    private void update(String sql) throws SQLException {
        try (Connection c = direct(); Statement st = c.createStatement()) {
            st.execute(sql);
        }
    }

    @Test
    void savedCosmeticsAreLoadedBack() throws Exception {
        PostgreSQLData a = server();
        join(a);
        save(a, "HELMET=hat&16711680");
        assertEquals("HELMET=hat&16711680", server().load(player).join());
    }

    @Test
    void anEmptyPlayerWithoutRowStoresNothing() throws Exception {
        PostgreSQLData a = server();
        join(a);
        save(a, "");
        assertNull(stored());
    }

    @Test
    void laterSavesReplaceEarlierOnesInOrder() throws Exception {
        PostgreSQLData a = server();
        join(a);
        a.save(player, "HELMET=a");
        a.save(player, "HELMET=b");
        a.save(player, "");
        join(a);
        assertEquals("", stored());
    }

    @Test
    void aSaveFromAnOlderStateDoesNotOverwriteANewerOne() throws Exception {
        PostgreSQLData a = server();
        PostgreSQLData b = server();
        join(a);
        join(b);
        save(b, "HELMET=fromB");
        save(a, "HELMET=fromA"); // A still works from the state it loaded
        assertEquals("HELMET=fromB", stored());
    }

    @Test
    void afterLosingAConflictNothingIsWrittenUntilTheNextLoad() throws Exception {
        PostgreSQLData a = server();
        PostgreSQLData b = server();
        join(a);
        join(b);
        save(b, "HELMET=fromB");
        save(a, "HELMET=fromA");
        save(a, "HELMET=again"); // same stale state, must be discarded too
        assertEquals("HELMET=fromB", stored());

        join(a);
        save(a, "HELMET=afterReload");
        assertEquals("HELMET=afterReload", stored());
    }

    @Test
    void twoServersWithoutARowKeepTheFirstSave() throws Exception {
        PostgreSQLData a = server();
        PostgreSQLData b = server();
        join(a);
        join(b);
        save(a, "HELMET=fromA");
        save(b, "HELMET=fromB");
        assertEquals("HELMET=fromA", stored());
    }

    @Test
    void aServerThatLoadedBeforeADataClearCannotBringTheOldStateBack() throws Exception {
        PostgreSQLData a = server();
        PostgreSQLData b = server();
        join(a);
        save(a, "HELMET=hat");
        join(b);
        a.clear(player);
        a.awaitIdle();
        save(b, "HELMET=late");
        assertEquals("", stored());
    }

    @Test
    void aStaleServerCannotMatchTheVersionOfARowRecreatedAfterAClear() throws Exception {
        PostgreSQLData a = server();
        PostgreSQLData b = server();
        join(a);
        save(a, "HELMET=hat");
        join(b); // B holds the version of the first row
        a.clear(player);
        save(a, "BACKPACK=new"); // the player's next change on A
        save(b, "HELMET=late");
        assertEquals("BACKPACK=new", stored());
    }

    @Test
    void afterADataClearTheNextLoadAndSaveCreateTheRowAgain() throws Exception {
        PostgreSQLData a = server();
        PostgreSQLData b = server();
        join(a);
        save(a, "HELMET=hat");
        join(b);
        a.clear(player);
        a.awaitIdle();
        join(b); // the player comes back on B with a fresh load
        save(b, "HELMET=new");
        assertEquals("HELMET=new", stored());
    }

    @Test
    void clearingOnTheSameServerLetsItSaveAgain() throws Exception {
        PostgreSQLData a = server();
        join(a);
        save(a, "HELMET=hat");
        a.clear(player);
        save(a, "BACKPACK=bag");
        assertEquals("BACKPACK=bag", stored());
    }

    @Test
    void aTableCopiedWithoutVersionColumnIsUpgraded() throws Exception {
        update("CREATE TABLE COSMETICDATABASE (UUID varchar(36) PRIMARY KEY, COSMETICS text)");
        update("INSERT INTO COSMETICDATABASE VALUES ('" + player + "', 'HELMET=old')");
        PostgreSQLData a = server();
        assertEquals("HELMET=old", a.load(player).join());
        save(a, "HELMET=new");
        assertEquals("HELMET=new", stored());
    }

    @Test
    void anUnreachableDatabaseFailsTheLoadInsteadOfReturningEmptyData() {
        PostgreSQLData a = server();
        join(a);
        a.shutdown(); // closes the pool: every attempt fails
        PostgreSQLData down = new PostgreSQLData();
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:tcp://127.0.0.1:1/none");
        config.setInitializationFailTimeout(-1);
        config.setConnectionTimeout(250);
        down.init(new HikariDataSource(config), Logger.getAnonymousLogger());
        down.setRetryDelayMs(10);
        servers.add(down);
        assertThrows(CompletionException.class, () -> down.load(player).join());
    }
}
