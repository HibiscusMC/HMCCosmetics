package com.hibiscusmc.hmccosmetics.cosmetic.types;

import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.manager.UserBackpackManager;
import com.hibiscusmc.hmccosmetics.util.packets.PacketManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.Locale;

public class CosmeticBackpackType extends Cosmetic {

    private String modelName;
    private ConfigurationNode config;
    private UserBackpackManager.BackpackType backpackType;

    public CosmeticBackpackType(String id, ConfigurationNode config) {
        super(id, config);
        this.config = config;
        modelName = config.node("model").getString();
        backpackType = UserBackpackManager.BackpackType.valueOf(config.node("type").getString("NORMAL").toUpperCase());
    }

    @Override
    public void update(CosmeticUser user) {
        Player player = Bukkit.getPlayer(user.getUniqueId());
        Location loc = player.getLocation().clone().add(0, 2, 0);

        if (user.isInWardrobe() || !user.isBackupSpawned()) return;
        if (loc.getWorld() != user.getUserBackpackManager().getFirstArmorstand().getWorld()) {
            user.getUserBackpackManager().getFirstArmorstand().teleport(loc);
        }

        user.getUserBackpackManager().getFirstArmorstand().teleport(loc);

        PacketManager.sendRidingPacket(player.getEntityId(), user.getUserBackpackManager().getFirstArmorstandId(), loc);

        user.getUserBackpackManager().getFirstArmorstand().setRotation(loc.getYaw(), loc.getPitch());

        if (user.getUserBackpackManager().getBackpackType().equals(UserBackpackManager.BackpackType.FIRST_PERSON)) {
            user.getUserBackpackManager().getSecondArmorstand().teleport(loc);
            user.getUserBackpackManager().getSecondArmorstand().setRotation(loc.getYaw(), loc.getPitch());

            PacketManager.sendRidingPacket(user.getUserBackpackManager().getFirstArmorstandId(), user.getUserBackpackManager().getSecondArmorstand().getEntityId(), loc);
        }
        user.getUserBackpackManager().showBackpack();
    }

    public String getModelName() {
        return modelName;
    }

    public UserBackpackManager.BackpackType getBackpackType() {
        return backpackType;
    }
}
