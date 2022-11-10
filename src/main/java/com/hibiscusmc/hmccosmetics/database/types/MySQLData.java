package com.hibiscusmc.hmccosmetics.database.types;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;

import java.sql.Connection;
import java.util.UUID;

public class MySQLData extends Data {

    private Connection connection;

    @Override
    public void setup() {
        // TODO
        HMCCosmeticsPlugin plugin = HMCCosmeticsPlugin.getInstance();
    }

    @Override
    public void save(CosmeticUser user) {
        // TODO
    }

    @Override
    public CosmeticUser get(UUID uniqueId) {
        // TODO
        return null;
    }

}
