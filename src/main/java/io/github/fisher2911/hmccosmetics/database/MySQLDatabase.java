package io.github.fisher2911.hmccosmetics.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;

import java.sql.Connection;
import java.sql.SQLException;

public class MySQLDatabase extends Database {

    String SAVE_STATEMENT =
            "INSERT INTO " + TABLE_NAME + "(" +
                    PLAYER_UUID_COLUMN + ", " +
                    BACKPACK_COLUMN + ", " +
                    HAT_COLUMN + ", " +
                    DYE_COLOR_COLUMN + ") " +
                    "VALUES (?,?,?,?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    BACKPACK_COLUMN + "=?, " +
                    HAT_COLUMN + "=?, " +
                    DYE_COLOR_COLUMN + "=?";

    String LOAD_STATEMENT =
            "SELECT " +
                    BACKPACK_COLUMN + ", " +
                    HAT_COLUMN + ", " +
                    DYE_COLOR_COLUMN + " " +
                    "FROM " + TABLE_NAME + " " +
                    "WHERE " +
                    PLAYER_UUID_COLUMN + "=?";

    private final HikariDataSource dataSource;

    MySQLDatabase(
            final HMCCosmetics plugin,
            final String name,
            final String username,
            final String password,
            final String ip,
            final String port) {
        super(plugin, null);
        final HikariConfig config = new HikariConfig();

        config.setJdbcUrl("jdbc:mysql://" + ip + ":" + port + "/" + name);
        config.setUsername(username);
        config.setPassword(password);
        config.setConnectionTimeout(1000000000);

        this.dataSource = new HikariDataSource(config);
    }

    @Override
    public Connection getConnection() {
        try {
            return this.dataSource.getConnection();
        } catch (final SQLException exception) {
            return null;
        }
    }

    @Override
    public void close() {
        this.dataSource.close();
    }

    @Override
    public String getSaveStatement() {
        return SAVE_STATEMENT;
    }

    @Override
    public String getLoadStatement() {
        return LOAD_STATEMENT;
    }
}
