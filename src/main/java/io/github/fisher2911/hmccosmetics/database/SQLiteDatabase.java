package io.github.fisher2911.hmccosmetics.database;

import com.j256.ormlite.support.ConnectionSource;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;

import java.sql.Connection;
import java.sql.SQLException;

public class SQLiteDatabase extends Database {

    private Connection conn;

    public SQLiteDatabase(final HMCCosmetics plugin, final ConnectionSource connectionSource) throws SQLException {
        super(plugin, connectionSource, DatabaseType.SQLITE);
    }

    String SAVE_STATEMENT =
            "INSERT INTO " + TABLE_NAME + "(" +
                    PLAYER_UUID_COLUMN + ", " +
                    BACKPACK_COLUMN + ", " +
                    HAT_COLUMN + ", " +
                    DYE_COLOR_COLUMN +
                    ") " +
                    "VALUES (?,?,?,?) " +
                    "ON CONFLICT (" +
                    PLAYER_UUID_COLUMN + ") " +
                    "DO UPDATE SET " +
                    BACKPACK_COLUMN + "=?," +
                    HAT_COLUMN + "=?, " +
                    DYE_COLOR_COLUMN + "=?";

    String LOAD_STATEMENT =
            "SELECT " +
                    BACKPACK_COLUMN + ", " +
                    HAT_COLUMN + ", " +
                    DYE_COLOR_COLUMN + " " +
                    "FROM " + TABLE_NAME + " " +
                    "WHERE " +
                    PLAYER_UUID_COLUMN + "=? ";

    @Override
    public void close() {
        try {
            this.conn.close();
        } catch (final SQLException exception) {
            exception.printStackTrace();
        }
    }
}
