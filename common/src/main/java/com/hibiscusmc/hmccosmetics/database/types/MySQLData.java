package com.hibiscusmc.hmccosmetics.database.types;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.DatabaseSettings;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;

public class MySQLData extends SQLData {

    // Connection Information
    private String host;
    private String user;
    private String database;
    private String password;
    private int port;

    @Nullable
    private Connection connection;

    private final Object connectionLock = new Object();

    @Override
    public void setup() {
        host = DatabaseSettings.getHost();
        user = DatabaseSettings.getUsername();
        database = DatabaseSettings.getDatabase();
        password = DatabaseSettings.getPassword();
        port = DatabaseSettings.getPort();

        HMCCosmeticsPlugin plugin = HMCCosmeticsPlugin.getInstance();
        try {
            openConnection();
            if (connection == null) throw new IllegalStateException("Connection is null");
            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS `COSMETICDATABASE` (" +
                            "UUID varchar(36) PRIMARY KEY," +
                            "COSMETICS MEDIUMTEXT" +
                            ");")) {
                preparedStatement.execute();
            }
        } catch (SQLException | IllegalStateException e) {
            plugin.getLogger().severe("");
            plugin.getLogger().severe("MySQL DATABASE CAN NOT BE REACHED.");
            plugin.getLogger().severe("CHECK CONFIG FOR ERRORS");
            plugin.getLogger().severe("SAFETY SHUTTING DOWN SERVER");
            plugin.getLogger().severe("");

            // Ensure shutdown on the main/global context in Folia
            plugin.scheduler().runGlobal(() -> {
                plugin.getLogger().log(Level.SEVERE, "Shutting down due to database initialization failure.", e);
                Bukkit.shutdown();
            });

            throw new RuntimeException(e);
        }
    }

    @Override
    public void clear(UUID uniqueId) {
        HMCCosmeticsPlugin.getInstance().scheduler().runAsync(() -> {
            try (PreparedStatement preparedSt = preparedStatement("DELETE FROM COSMETICDATABASE WHERE UUID=?;")) {
                if (preparedSt == null) return;
                preparedSt.setString(1, uniqueId.toString());
                preparedSt.executeUpdate();
            } catch (SQLException e) {
                HMCCosmeticsPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to clear user row: " + uniqueId, e);
            }
        });
    }

    private void openConnection() throws SQLException {
        synchronized (connectionLock) {
            try {
                if (isConnectionOpen()) return;
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException ignored) {}
                    connection = null;
                }
            } catch (RuntimeException ex) {
                // ignore state check issues, we will try to open fresh
            }

            try {
                // Modern MySQL driver
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                HMCCosmeticsPlugin.getInstance().getLogger().log(Level.SEVERE, "MySQL driver not found (com.mysql.cj.jdbc.Driver).", e);
                throw new SQLException("MySQL driver not found", e);
            }

            final String url =
                    "jdbc:mysql://" + host + ":" + port + "/" + database + setupProperties();

            connection = DriverManager.getConnection(url, setupProperties());
        }
    }

    public void close() {
        HMCCosmeticsPlugin.getInstance().scheduler().runAsync(() -> {
            synchronized (connectionLock) {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        HMCCosmeticsPlugin.getInstance().getLogger().log(Level.WARNING, "Error while closing MySQL connection", e);
                    } finally {
                        connection = null;
                    }
                }
            }
        });
    }

    @NotNull
    private Properties setupProperties() {
        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", password);

        // Optional pool-friendly hints (no-op for plain DriverManager but harmless)
        props.setProperty("maxReconnects", "3");
        props.setProperty("useServerPrepStmts", "true");
        props.setProperty("cachePrepStmts", "true");
        props.setProperty("prepStmtCacheSize", "250");
        props.setProperty("prepStmtCacheSqlLimit", "2048");
        return props;
    }

    private boolean isConnectionOpen() {
        synchronized (connectionLock) {
            try {
                return connection != null && !connection.isClosed();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public PreparedStatement preparedStatement(String query) {
        if (!isConnectionOpen()) {
            MessagesUtil.sendDebugMessages(
                    "The MySQL database connection is not open (idle timeout?). Reconnecting...",
                    Level.WARNING
            );
            try {
                openConnection();
            } catch (SQLException e) {
                HMCCosmeticsPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to reopen MySQL connection.", e);
                return null;
            }
        }

        synchronized (connectionLock) {
            try {
                if (connection == null) throw new IllegalStateException("Connection is null");
                return connection.prepareStatement(query);
            } catch (SQLException | IllegalStateException e) {
                HMCCosmeticsPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to create PreparedStatement.", e);
                return null;
            }
        }
    }
}
