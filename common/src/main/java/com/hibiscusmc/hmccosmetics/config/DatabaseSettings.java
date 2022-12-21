package com.hibiscusmc.hmccosmetics.config;

import org.spongepowered.configurate.ConfigurationNode;

public class DatabaseSettings {

    //private static final String DATABASE_SETTINGS_PATH = "cosmetic-settings";
    private static final String DATABASE_TYPE_PATH = "type";
    private static final String MYSQL_DATABASE_SETTINGS = "mysql";

    private static final String MYSQL_DATABASE = "database";
    private static final String MYSQL_PASSWORD = "password";
    private static final String MYSQL_HOST = "host";
    private static final String MYSQL_USER = "user";
    private static final String MYSQL_PORT = "port";
    private static final String DELAY_PATH = "delay";
    private static final String ENABLE_DELAY = "enabled";
    private static final String DELAY_LENGTH = "delay";

    private static String databaseType;
    private static String database;
    private static String password;
    private static String host;
    private static String username;
    private static int port;
    private static boolean enabledDelay;
    private static int delayLength;

    public static void load(ConfigurationNode source) {
        //ConfigurationNode databaseSettings = source.node(DATABASE_SETTINGS_PATH);

        databaseType = source.node(DATABASE_TYPE_PATH).getString();

        ConfigurationNode mySql = source.node(MYSQL_DATABASE_SETTINGS);

        database = mySql.node(MYSQL_DATABASE).getString();
        password = mySql.node(MYSQL_PASSWORD).getString();
        host = mySql.node(MYSQL_HOST).getString();
        username = mySql.node(MYSQL_USER).getString();
        port = mySql.node(MYSQL_PORT).getInt();

        ConfigurationNode delay = source.node(DELAY_PATH);

        enabledDelay = delay.node(ENABLE_DELAY).getBoolean(false);
        delayLength = delay.node(DELAY_LENGTH).getInt(2);
    }

    public static String getDatabaseType() {
        return databaseType;
    }

    public static String getDatabase() {
        return database;
    }

    public static String getPassword() {
        return password;
    }

    public static String getHost() {
        return host;
    }

    public static String getUsername() {
        return username;
    }

    public static int getPort() {
        return port;
    }

    public static boolean isEnabledDelay() {
        return enabledDelay;
    }

    public static int getDelayLength() {
        return delayLength;
    }
}
