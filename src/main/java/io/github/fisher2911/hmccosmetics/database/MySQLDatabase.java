package io.github.fisher2911.hmccosmetics.database;

import com.j256.ormlite.support.ConnectionSource;
import com.zaxxer.hikari.HikariDataSource;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;

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

    public MySQLDatabase(final HMCCosmetics plugin, final ConnectionSource dataSource, final String SAVE_STATEMENT, final String LOAD_STATEMENT, final HikariDataSource dataSource1) throws SQLException {
        super(plugin, dataSource, DatabaseType.MYSQL);
        this.SAVE_STATEMENT = SAVE_STATEMENT;
        this.LOAD_STATEMENT = LOAD_STATEMENT;
        this.dataSource = dataSource1;
    }

    public void getAll() {

    }

    @Override
    public void close() {
        this.dataSource.close();
    }
}
