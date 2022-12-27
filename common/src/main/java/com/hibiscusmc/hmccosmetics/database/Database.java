package com.hibiscusmc.hmccosmetics.database;

import com.hibiscusmc.hmccosmetics.config.DatabaseSettings;
import com.hibiscusmc.hmccosmetics.database.types.Data;
import com.hibiscusmc.hmccosmetics.database.types.InternalData;
import com.hibiscusmc.hmccosmetics.database.types.MySQLData;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Database {

    private static Data data;

    private static InternalData INTERNAL_DATA  = new InternalData();
    private static MySQLData MYSQL_DATA = new MySQLData();

    public Database() {
        String databaseType = DatabaseSettings.getDatabaseType();
        data = INTERNAL_DATA; // default
        if (databaseType.equalsIgnoreCase("INTERNAL")) {
            data = INTERNAL_DATA;
            MessagesUtil.sendDebugMessages("Datatype set to internal data");
        }
        if (databaseType.equalsIgnoreCase("MySQL")) {
            data = MYSQL_DATA;
            MessagesUtil.sendDebugMessages("Datatype set to MySQL data");
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

    public static CosmeticUser get(UUID uniqueId) {
        return data.get(uniqueId);
    }

    public static Data getData() {
        return data;
    }

    public static void clearData(UUID uniqueId) {
        data.clear(uniqueId);
    }
}
