package com.hibiscusmc.hmccosmetics.database;

import com.hibiscusmc.hmccosmetics.database.types.Data;
import com.hibiscusmc.hmccosmetics.database.types.InternalData;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;

import java.util.UUID;

public class Database {

    private static Data data;

    private static InternalData INTERNAL_DATA  = new InternalData();

    public Database() {
        this.data = INTERNAL_DATA;
        // To be replaced when multiple systems exist
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
