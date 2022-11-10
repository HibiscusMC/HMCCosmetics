package com.hibiscusmc.hmccosmetics.database.types;

import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class Data {

    public void setup() {
        // Override
    }

    public void save(CosmeticUser user) {
        // Override
    }

    @Nullable
    public CosmeticUser get(UUID uniqueId) {
        // Override
        return null;
    }

}
