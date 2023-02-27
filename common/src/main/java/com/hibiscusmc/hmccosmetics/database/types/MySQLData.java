package com.hibiscusmc.hmccosmetics.database.types;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.DatabaseSettings;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.Properties;
import java.util.UUID;

public class MySQLData extends SQLData {

    // Connection Information
    private String host;
    private String user;
    private String database;
    private String password;
    private int port;

    private Connection connection;

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
            connection.prepareStatement("CREATE TABLE IF NOT EXISTS `COSMETICDATABASE` " +
                    "(UUID varchar(36) PRIMARY KEY, " +
                    "COSMETICS MEDIUMTEXT " +
                    ");").execute();
        } catch (SQLException e) {
            plugin.getLogger().severe("");
            plugin.getLogger().severe("");
            plugin.getLogger().severe("MySQL DATABASE CAN NOT BE REACHED.");
            plugin.getLogger().severe("CHECK CONFIG FOR ERRORS");
            plugin.getLogger().severe("");
            plugin.getLogger().severe("SAFETY SHUTTING DOWN SERVER");
            plugin.getLogger().severe("");
            plugin.getLogger().severe("");
            Bukkit.shutdown();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void clear(UUID uniqueId) {
        Bukkit.getScheduler().runTaskAsynchronously(HMCCosmeticsPlugin.getInstance(), () -> {
            try {
                PreparedStatement preparedSt = preparedStatement("DELETE FROM COSMETICDATABASE WHERE UUID=?;");
                preparedSt.setString(1, uniqueId.toString());
                preparedSt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        // TODO (Something)
    }

    private void openConnection() throws SQLException {
        // Bukkit.getScheduler().runTaskAsynchronously(HMCCosmeticsPlugin.getInstance(), () -> {
        // ...
        // });
        // connection = DriverManager.getConnection("jdbc:mysql://" + DatabaseSettings.getHost() + ":" + DatabaseSettings.getPort() + "/" + DatabaseSettings.getDatabase(), setupProperties());

        if (connection != null && !connection.isClosed()) return;

        // Close connection if still active
        if (connection != null) {
            close();
        }

        // Connect to database host
        try {
            // Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, setupProperties());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void close() {
        Bukkit.getScheduler().runTaskAsynchronously(HMCCosmeticsPlugin.getInstance(), () -> {
            try {
                connection.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        });
    }

    @NotNull
    private Properties setupProperties() {
        Properties props = new Properties();
        props.put("user", user);
        props.put("password", password);
        props.put("autoReconnect", "true");
        return props;
    }

    private boolean isConnectionOpen() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PreparedStatement preparedStatement(String query) {
        PreparedStatement ps = null;

        if (!isConnectionOpen()) {
            HMCCosmeticsPlugin.getInstance().getLogger().info("Connection is not open");
        }

        try {
            ps = connection.prepareStatement(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ps;
    }
}
