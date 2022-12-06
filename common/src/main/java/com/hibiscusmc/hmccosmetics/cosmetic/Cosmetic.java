package com.hibiscusmc.hmccosmetics.cosmetic;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.spongepowered.configurate.ConfigurationNode;

public class Cosmetic {

    private String id;
    private String permission;
    private CosmeticSlot slot;
    private boolean equipable; // This simply means if a player can put it on their body.
    private boolean dyable;

    protected Cosmetic(String id, ConfigurationNode config) {
        this.id = id;
        //this.permission = config.node("permission").getString(null);
        HMCCosmeticsPlugin.getInstance().getLogger().info("Slot: " + config.node("slot").getString());
        setSlot(CosmeticSlot.valueOf(config.node("slot").getString()));

        setEquipable(false);
        setDyable(config.node("dyable").getBoolean(false));

        Cosmetics.addCosmetic(this);
    }

    public String getId() {
        return this.id;
    }
    public String getPermission() {
        return this.permission;
    }

    public CosmeticSlot getSlot() {
        return this.slot;
    }

    public void setSlot(CosmeticSlot slot) {
        this.slot = slot;
    }
    public void setPermission(String permission) {
        this.permission = permission;
    }

    public boolean requiresPermission() {
        if (permission == null) return false;
        return true;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setEquipable(boolean equipable) {
        this.equipable = equipable;
    }

    public boolean isEquipable() {
        return equipable;
    }

    public void setDyable(boolean dyable) {
        this.dyable = dyable;
    }

    public boolean isDyable() {
        return this.dyable;
    }


    public void update(CosmeticUser user) {
        // Override
    }

}
