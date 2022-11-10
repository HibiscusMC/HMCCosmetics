package com.hibiscusmc.hmccosmetics.database;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.DatabaseSettings;
import com.hibiscusmc.hmccosmetics.database.types.Data;
import com.hibiscusmc.hmccosmetics.database.types.InternalData;
import com.hibiscusmc.hmccosmetics.database.types.MySQLData;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;

import java.util.UUID;

public class Database {

    private static Data data;

    private static InternalData INTERNAL_DATA  = new InternalData();
    private static MySQLData MYSQL_DATA = new MySQLData();

    public Database() {
        String databaseType = DatabaseSettings.getDatabaseType();
        switch (databaseType) {
            case "INTERNAL_DATA":
                data = INTERNAL_DATA;
            case "MYSQL":
                data = MYSQL_DATA;
            default:
                data = INTERNAL_DATA;
                HMCCosmeticsPlugin.getInstance().getLogger().severe("No Valid Datatype detected! Defaulting to internal data...");
        }
        setup();
    }

    public static void setup() {
        data.setup();
    }

    public static void save(CosmeticUser user) {
        data.save(user);
    }

    public static CosmeticUser get(UUID uniqueId) {
        return data.get(uniqueId);
    }

    public static Data getData() {
        return data;
    }

}
