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

public class MySQLData extends SQLData {

    // Connection Information
    private String host;
    private String user;
    private String database;
    private String password;
    private int port;

    @Nullable
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
            if (connection == null) throw new NullPointerException("Connection is null");
            connection.prepareStatement("CREATE TABLE IF NOT EXISTS `COSMETICDATABASE` " +
                    "(UUID varchar(36) PRIMARY KEY, " +
                    "COSMETICS MEDIUMTEXT " +
                    ");").execute();
        } catch (SQLException | NullPointerException e) {
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
            PreparedStatement preparedSt = null;
            try {
                preparedSt = preparedStatement("DELETE FROM COSMETICDATABASE WHERE UUID=?;");
                preparedSt.setString(1, uniqueId.toString());
                preparedSt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (preparedSt != null) preparedSt.close();
                } catch (SQLException e) {}
            }
        });
    }

    private void openConnection() throws SQLException {
        // Bukkit.getScheduler().runTaskAsynchronously(HMCCosmeticsPlugin.getInstance(), () -> {
        // ...
        // });
        // connection = DriverManager.getConnection("jdbc:mysql://" + DatabaseSettings.getHost() + ":" + DatabaseSettings.getPort() + "/" + DatabaseSettings.getDatabase(), setupProperties());

        // Connection isn't null AND Connection isn't closed :: return
        try {
            if (isConnectionOpen()) {
                return;
            } else if (connection != null) close(); // Close connection if still active
        } catch (RuntimeException e) {
            e.printStackTrace(); // If isConnectionOpen() throws error
        }

        // Connect to database host
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, setupProperties());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        Bukkit.getScheduler().runTaskAsynchronously(HMCCosmeticsPlugin.getInstance(), () -> {
            try {
                if (connection == null) throw new NullPointerException("Connection is null");
                connection.close();
            } catch (SQLException | NullPointerException e) {
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

    private boolean isConnectionOpen() throws RuntimeException {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PreparedStatement preparedStatement(String query) {
        PreparedStatement ps = null;

        if (!isConnectionOpen()) MessagesUtil.sendDebugMessages("Connection is not open");

        try {
            if (connection == null) throw new NullPointerException("Connection is null");
            ps = connection.prepareStatement(query);
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
        }

        return ps;
    }
}
