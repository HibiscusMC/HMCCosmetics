package com.hibiscusmc.hmccosmetics.database.types;

import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class NoneData extends Data {
    @Override
    public void setup() {
        // Nothing
    }

    @Override
    public void save(CosmeticUser user) {
        // Nothing
    }

    @Override
    public @Nullable CosmeticUser get(UUID uniqueId) {
        return new CosmeticUser(uniqueId);
    }

    @Override
    public void clear(UUID uniqueId) {
        // Nothing
    }
}
