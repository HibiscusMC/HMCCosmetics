package com.hibiscusmc.hmccosmetics.database.types;

import com.hibiscusmc.hmccosmetics.user.CosmeticUser;

import java.util.UUID;

public class Data {

    public void save(CosmeticUser user) {
        // Override
    }

    public CosmeticUser get(UUID uniqueId) {
        // Override
        return null;
    }

}
