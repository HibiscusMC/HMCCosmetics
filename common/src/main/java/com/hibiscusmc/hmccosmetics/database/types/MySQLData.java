package com.hibiscusmc.hmccosmetics.database.types;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.section.DatabaseSettings;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.SchedulerUtil;
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
            try (PreparedStatement preparedStatement =  connection.prepareStatement("CREATE TABLE IF NOT EXISTS `COSMETICDATABASE` " +
                    "(UUID varchar(36) PRIMARY KEY, " +
                    "COSMETICS MEDIUMTEXT " +
                    ");")) {
                preparedStatement.execute();
            }
        } catch (SQLException | IllegalStateException e) {
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
        SchedulerUtil.runTaskAsynchronously(HMCCosmeticsPlugin.getInstance(), () -> {
            try (PreparedStatement preparedSt = preparedStatement("DELETE FROM COSMETICDATABASE WHERE UUID=?;")) {
                preparedSt.setString(1, uniqueId.toString());
                preparedSt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void openConnection() throws SQLException {
        // Connection isn't null AND Connection isn't closed :: return
        try {
            if (isConnectionOpen()) return;
            if (connection != null) close(); // Close connection if still active
        } catch (RuntimeException e) {
            e.printStackTrace(); // If isConnectionOpen() throws error
        }

        // Connect to database host
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, setupProperties());
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    public void close() {
        SchedulerUtil.runTaskAsynchronously(HMCCosmeticsPlugin.getInstance(), () -> {
            try {
                if (connection == null) throw new IllegalStateException("Connection is null");
                connection.close();
            } catch (SQLException | NullPointerException e) {
                System.out.println(e.getMessage());
            }
        });
    }

    @NotNull
    private Properties setupProperties() {
        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", password);
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
            MessagesUtil.sendDebugMessages("The MySQL database connection is not open (Could the database been idle for to long?). Reconnecting...", Level.WARNING);
            try {
                openConnection();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        try {
            if (connection == null) throw new IllegalStateException("Connection is null");
            ps = connection.prepareStatement(query);
        } catch (SQLException | IllegalStateException e) {
            e.printStackTrace();
        }

        return ps;
    }
}