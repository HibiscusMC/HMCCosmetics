package com.hibiscusmc.hmccosmetics.database.types;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.DatabaseSettings;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.UUID;

public class MySQLData extends Data {

    private Connection connection;

    @Override
    public void setup() {
        // TODO
        HMCCosmeticsPlugin plugin = HMCCosmeticsPlugin.getInstance();
        try {
            openConnection();
            connection.prepareStatement("CREATE TABLE IF NOT EXISTS 'COSMETICDATABASE' " +
                    "(" +
                    ");");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void save(CosmeticUser user) {
        // TODO
    }

    @Override
    public CosmeticUser get(UUID uniqueId) {
        // TODO
        return null;
    }

    private void openConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return;
        }
        connection = DriverManager.getConnection("jdbc:mysql://" + DatabaseSettings.getHost() + ":" + DatabaseSettings.getPort() + "/" + DatabaseSettings.getDatabase(), setupProperties());
    }

    private Properties setupProperties() {
        Properties props = new Properties();
        props.put("user", DatabaseSettings.getUsername());
        props.put("password", DatabaseSettings.getPassword());
        props.put("autoReconnect", "true");
        return props;
    }

    private boolean isConnectionOpen() throws SQLException{
        if (connection == null || connection.isClosed()) {
            return false;
        } else {
            return true;
        }
    }

}
