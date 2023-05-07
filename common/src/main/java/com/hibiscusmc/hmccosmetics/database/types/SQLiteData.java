package com.hibiscusmc.hmccosmetics.database.types;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;

public class SQLiteData extends SQLData {

    private Connection connection;

    @Override
    public void setup() {
        File dataFolder = new File(HMCCosmeticsPlugin.get().getDataFolder(), "database.db");
        boolean exists = dataFolder.exists();

        if (!exists) {
            try {
                boolean created = dataFolder.createNewFile();
                if (!created) throw new IOException("File didn't exist but now does");
            } catch (IOException e) {
                MessagesUtil.sendDebugMessages("File write error. Database will not work properly", Level.SEVERE);
            }
        }

        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);

            openConnection();
            connection.prepareStatement("CREATE TABLE IF NOT EXISTS `COSMETICDATABASE` " +
                    "(UUID varchar(36) PRIMARY KEY, " +
                    "COSMETICS MEDIUMTEXT " +
                    ");").execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @SuppressWarnings("resource")
    public void clear(UUID uniqueId) {
        Bukkit.getScheduler().runTaskAsynchronously(HMCCosmeticsPlugin.get(), () -> {
            try {
                PreparedStatement preparedSt = preparedStatement("DELETE FROM COSMETICDATABASE WHERE UUID=?;");
                preparedSt.setString(1, uniqueId.toString());
                preparedSt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void openConnection() throws SQLException {
        // Bukkit.getScheduler().runTaskAsynchronously(HMCCosmeticsPlugin.getInstance(), () -> {
        // ...
        // });
        // connection = DriverManager.getConnection("jdbc:mysql://" + DatabaseSettings.getHost() + ":" + DatabaseSettings.getPort() + "/" + DatabaseSettings.getDatabase(), setupProperties());

        if (connection != null && !connection.isClosed()) return;

        // Close Connection if still active
        File dataFolder = new File(HMCCosmeticsPlugin.get().getDataFolder(), "database.db");

        // Connect to database host
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PreparedStatement preparedStatement(String query) {
        PreparedStatement ps = null;
        if (!isConnectionOpen()) {
            HMCCosmeticsPlugin.get().getLogger().info("Connection is not open");
        }

        try {
            ps = connection.prepareStatement(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ps;
    }

    private boolean isConnectionOpen() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
