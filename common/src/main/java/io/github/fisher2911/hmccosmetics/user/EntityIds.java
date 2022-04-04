package io.github.fisher2911.hmccosmetics.user;

import java.util.EnumMap;
import java.util.Map;

public class EntityIds {

    private final Map<Type, Integer> ids;

    public EntityIds(final Map<Type, Integer> ids) {
        this.ids = ids;
    }

    public EntityIds(final int self, final int armorStand, final int balloon, final int wardrobeViewer) {
        final Map<Type, Integer> ids = new EnumMap<>(Type.class);
        ids.put(Type.SELF, self);
        ids.put(Type.ARMOR_STAND, armorStand);
        ids.put(Type.BALLOON, balloon);
        ids.put(Type.WARDROBE_VIEWER, wardrobeViewer);
        this.ids = ids;
    }

    public int self() {
        return this.ids.getOrDefault(Type.SELF, -1);
    }

    public int armorStand() {
        return this.ids.getOrDefault(Type.ARMOR_STAND, -1);
    }

    public int balloon() {
        return this.ids.getOrDefault(Type.BALLOON, -1);
    }

    public int wardrobeViewer() {
        return this.ids.getOrDefault(Type.WARDROBE_VIEWER, -1);
    }

    public enum Type {

        SELF,
        ARMOR_STAND,
        BALLOON,
        WARDROBE_VIEWER

    }

}
