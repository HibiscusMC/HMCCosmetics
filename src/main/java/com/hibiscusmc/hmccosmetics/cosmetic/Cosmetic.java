package com.hibiscusmc.hmccosmetics.cosmetic;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.spongepowered.configurate.ConfigurationNode;

public class Cosmetic {

    private String id;
    private String permission;
    private CosmeticSlot slot;

    protected Cosmetic(String id, ConfigurationNode config) {
        this.id = id;
        //this.permission = config.node("permission").getString(null);
        HMCCosmeticsPlugin.getInstance().getLogger().info("Slot: " + config.node("slot").getString());
        this.slot = CosmeticSlot.valueOf(config.node("slot").getString());

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

    public void update(CosmeticUser user) {
        // overide
    }

}
