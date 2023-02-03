package com.hibiscusmc.hmccosmetics.database.types;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.DatabaseSettings;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.bukkit.Bukkit;
import org.bukkit.Color;

import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

public class MySQLData extends Data {

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
    public void save(CosmeticUser user) {
        Runnable run = () -> {
            try {
                PreparedStatement preparedSt = preparedStatement("REPLACE INTO COSMETICDATABASE(UUID,COSMETICS) VALUES(?,?);");
                preparedSt.setString(1, user.getUniqueId().toString());
                preparedSt.setString(2, steralizeData(user));
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
                    Map<CosmeticSlot, Map<Cosmetic, Color>> cosmetics = desteralizedata(user, rawData);
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
        // TODO
    }

    private void openConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return;
        }
        //Bukkit.getScheduler().runTaskAsynchronously(HMCCosmeticsPlugin.getInstance(), () -> {

            //close Connection if still active
            if (connection != null) {
                close();
            }

            //connect to database host
            try {
                Class.forName("com.mysql.jdbc.Driver");

                connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, setupProperties());

            } catch (SQLException e) {
                System.out.println(e.getMessage());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

        //});
        //connection = DriverManager.getConnection("jdbc:mysql://" + DatabaseSettings.getHost() + ":" + DatabaseSettings.getPort() + "/" + DatabaseSettings.getDatabase(), setupProperties());
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

    private Properties setupProperties() {
        Properties props = new Properties();
        props.put("user", user);
        props.put("password", password);
        props.put("autoReconnect", "true");
        return props;
    }

    private boolean isConnectionOpen() {
        try {
            if (connection == null || connection.isClosed()) {
                return false;
            } else {
                return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
}
