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

    private static String databaseType;
    private static String database;
    private static String password;
    private static String host;
    private static String username;
    private static int port;

    public static void load(ConfigurationNode source) {
        //ConfigurationNode databaseSettings = source.node(DATABASE_SETTINGS_PATH);

        databaseType = source.node(DATABASE_TYPE_PATH).getString();

        ConfigurationNode mySql = source.node(MYSQL_DATABASE_SETTINGS);

        database = mySql.node(MYSQL_DATABASE).getString();
        password = mySql.node(MYSQL_PASSWORD).getString();
        host = mySql.node(MYSQL_HOST).getString();
        username = mySql.node(MYSQL_USER).getString();
        port = mySql.node(MYSQL_PORT).getInt();
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
}
