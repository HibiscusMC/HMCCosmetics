package com.hibiscusmc.hmccosmetics.database;

import com.hibiscusmc.hmccosmetics.config.section.DatabaseSettings;
import com.hibiscusmc.hmccosmetics.database.types.Data;
import com.hibiscusmc.hmccosmetics.database.types.MySQLData;
import com.hibiscusmc.hmccosmetics.database.types.NoneData;
import com.hibiscusmc.hmccosmetics.database.types.PostgreSQLData;
import com.hibiscusmc.hmccosmetics.database.types.SQLiteData;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class Database {

    @Getter
    private static Data data;
    private static final MySQLData MYSQL_DATA = new MySQLData();
    private static final SQLiteData SQLITE_DATA = new SQLiteData();
    private static final PostgreSQLData POSTGRESQL_DATA = new PostgreSQLData();
    private static final NoneData NONE_DATA = new NoneData();

    public Database() {
        String databaseType = DatabaseSettings.getDatabaseType();
        data = SQLITE_DATA; // default to SQLite, then check if it's anything different
        switch (databaseType.toLowerCase()) {
            case "mysql":
                data = MYSQL_DATA;
                break;
            case "postgresql", "postgres":
                data = POSTGRESQL_DATA;
                break;
            case "sqlite":
                // already the default
                break;
            case "none":
                data = NONE_DATA;
                MessagesUtil.sendDebugMessages("Database is set to none. Data will not be saved.", Level.WARNING);
                break;
            default:
                MessagesUtil.sendDebugMessages("Invalid database type. Defaulting to SQLite.", Level.WARNING);
        }
        MessagesUtil.sendDebugMessages("Database is " + data);

        setup();
    }

    public static void setup() {
        data.setup();
    }

    public static void save(CosmeticUser user) {
        data.save(user);
    }

    public static void save(Player player) {
        data.save(CosmeticUsers.getUser(player));
    }

    public static CompletableFuture<UserData> get(UUID uniqueId) {
        return data.get(uniqueId);
    }

    /** Waits for the pending writes and releases the storage's resources. Call once on plugin shutdown, after the final saves. */
    public static void shutdown() {
        data.shutdown();
    }

    public static void clearData(UUID uniqueId) {
        data.clear(uniqueId);
    }
}
