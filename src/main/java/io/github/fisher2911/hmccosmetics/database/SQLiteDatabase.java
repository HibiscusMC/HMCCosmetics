package io.github.fisher2911.hmccosmetics.database;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.inventory.PlayerArmor;
import io.github.fisher2911.hmccosmetics.user.User;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SQLiteDatabase extends Database {

    private Connection conn;

    public SQLiteDatabase(final HMCCosmetics plugin) throws SQLException {
        super(plugin, new JdbcPooledConnectionSource("jdbc:sqlite:" + Path.of(
                plugin.getDataFolder().getPath(),
                "database",
                "users.db"
        ).toFile().getPath()));
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
    public Connection getConnection() {
        if (this.conn != null) {
            return this.conn;
        }
        try {
            final File folder = Path.of(
                    this.plugin.getDataFolder().getPath(),
                    "database"
            ).toFile();

            folder.mkdirs();

            final File file = Path.of(
                    folder.getPath(),
                    "users.db"
            ).toFile();

            this.conn = DriverManager.getConnection("jdbc:sqlite:" +
                    file.getPath());
            return this.conn;
        } catch (final SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    @Override
    public void close() {
        try {
            this.conn.close();
        } catch (final SQLException exception) {
            exception.printStackTrace();
        }
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
