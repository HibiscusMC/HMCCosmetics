package io.github.fisher2911.hmccosmetics.user;

import java.util.EnumMap;
import java.util.Map;

public class EntityIds {

    private final Map<Type, Integer> ids;

    public EntityIds(final Map<Type, Integer> ids) {
        this.ids = ids;
    }

    public EntityIds(final int self, final int armorStand, final int balloon) {
        final Map<Type, Integer> ids = new EnumMap<>(Type.class);
        ids.put(Type.SELF, self);
        ids.put(Type.ARMOR_STAND, armorStand);
        ids.put(Type.BALLOON, balloon);
        this.ids = ids;
    }

    public int self() {
        return ids.getOrDefault(Type.SELF, -1);
    }

    public int armorStand() {
        return ids.getOrDefault(Type.ARMOR_STAND, -1);
    }

    public int balloon() {
        return ids.getOrDefault(Type.BALLOON, -1);
    }

    public enum Type {

        SELF,
        ARMOR_STAND,
        BALLOON

    }

}
