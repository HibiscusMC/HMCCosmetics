package com.hibiscusmc.hmccosmetics.database.types;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class SQLiteData extends Data {

    private Connection connection;

    @Override
    public void setup() {
        File dataFolder = new File(HMCCosmeticsPlugin.getInstance().getDataFolder(), "database.db");

        if (!dataFolder.exists()){
            try {
                dataFolder.createNewFile();
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
    public void save(CosmeticUser user) {
        Runnable run = () -> {
            try {
                PreparedStatement preparedSt = preparedStatement("REPLACE INTO COSMETICDATABASE(UUID,COSMETICS) VALUES(?,?);");
                preparedSt.setString(1, user.getUniqueId().toString());
                preparedSt.setString(2, serializeData(user));
                preparedSt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        };
        if (!HMCCosmeticsPlugin.isDisable()) {
            Bukkit.getScheduler().runTaskAsynchronously(HMCCosmeticsPlugin.getInstance(), run);
        } else {
            run.run();
        }
    }

    @Override
    public CosmeticUser get(UUID uniqueId) {
        CosmeticUser user = new CosmeticUser(uniqueId);

        Bukkit.getScheduler().runTaskAsynchronously(HMCCosmeticsPlugin.getInstance(), () -> {
            try {
                PreparedStatement preparedStatement = preparedStatement("SELECT * FROM COSMETICDATABASE WHERE UUID = ?;");
                preparedStatement.setString(1, uniqueId.toString());
                ResultSet rs = preparedStatement.executeQuery();
                if (rs.next()) {
                    String rawData = rs.getString("COSMETICS");
                    Map<CosmeticSlot, Map<Cosmetic, Color>> cosmetics = deserializeData(user, rawData);
                    for (Map<Cosmetic, Color> cosmeticColors : cosmetics.values()) {
                        for (Cosmetic cosmetic : cosmeticColors.keySet()) {
                            Bukkit.getScheduler().runTask(HMCCosmeticsPlugin.getInstance(), () -> {
                                // This can not be async.
                                user.addPlayerCosmetic(cosmetic, cosmeticColors.get(cosmetic));
                            });
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        return user;
    }

    @Override
    public void clear(UUID unqiueId) {
        Bukkit.getScheduler().runTaskAsynchronously(HMCCosmeticsPlugin.getInstance(), () -> {
            try {
                PreparedStatement preparedSt = preparedStatement("DELETE FROM COSMETICDATABASE WHERE UUID=?;");
                preparedSt.setString(1, unqiueId.toString());
                preparedSt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }


    private void openConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return;
        }
        //Bukkit.getScheduler().runTaskAsynchronously(HMCCosmeticsPlugin.getInstance(), () -> {

        //close Connection if still active

        File dataFolder = new File(HMCCosmeticsPlugin.getInstance().getDataFolder(), "database.db");

        //connect to database host
        try {
            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        //});
        //connection = DriverManager.getConnection("jdbc:mysql://" + DatabaseSettings.getHost() + ":" + DatabaseSettings.getPort() + "/" + DatabaseSettings.getDatabase(), setupProperties());
    }

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

    private boolean isConnectionOpen() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
