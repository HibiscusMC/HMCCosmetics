package com.hibiscusmc.hmccosmetics.cosmetic.types;

import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.nms.NMSHandlers;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.manager.UserBackpackManager;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.packets.PacketManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class CosmeticBackpackType extends Cosmetic {

    private final String modelName;
    private ItemStack firstPersonBackpack;

    public CosmeticBackpackType(String id, ConfigurationNode config) {
        super(id, config);

        modelName = config.node("model").getString();

        if (!config.node("firstperson-item").virtual()) {
            this.firstPersonBackpack = generateItemStack(config.node("firstperson-item"));
        }
    }

    @Override
    public void update(@NotNull CosmeticUser user) {
        Entity entity = Bukkit.getEntity(user.getUniqueId());
        if (entity == null) return;

        Location loc = entity.getLocation().clone().add(0, 2, 0);

        if (user.isInWardrobe() || !user.isBackpackSpawned()) return;
        // This needs to be moved to purely packet based, there are far to many plugin doing dumb stuff that prevents spawning armorstands ignoring our spawn reason.
        if (!user.getUserBackpackManager().IsValidBackpackEntity()) {
            MessagesUtil.sendDebugMessages("Invalid Backpack Entity[owner=" + user.getUniqueId() + ",player_location=" + loc + "]!");
            user.respawnBackpack();
            return;
        }
        if (loc.getWorld() != user.getUserBackpackManager().getArmorStand().getWorld()) {
            user.getUserBackpackManager().getArmorStand().teleport(loc);
        }

        user.getUserBackpackManager().getArmorStand().teleport(loc);
        user.getUserBackpackManager().getArmorStand().setRotation(loc.getYaw(), loc.getPitch());

        List<Player> outsideViewers = user.getUserBackpackManager().getCloudManager().refreshViewers(loc);
        if (!user.isInWardrobe() && isFirstPersonCompadible() && user.getPlayer() != null) {
            List<Player> owner = List.of(user.getPlayer());

            ArrayList<Integer> particleCloud = user.getUserBackpackManager().getAreaEffectEntityId();
            for (int i = 0; i < particleCloud.size(); i++) {
                if (i == 0) {
                    PacketManager.sendRidingPacket(entity.getEntityId(), particleCloud.get(i), owner);
                } else {
                    PacketManager.sendRidingPacket(particleCloud.get(i - 1), particleCloud.get(i) , owner);
                }
            }
            PacketManager.sendRidingPacket(particleCloud.get(particleCloud.size() - 1), user.getUserBackpackManager().getFirstArmorStandId(), owner);
            if (!user.getHidden()) {
                //if (loc.getPitch() < -70) NMSHandlers.getHandler().equipmentSlotUpdate(user.getUserBackpackManager().getFirstArmorStandId(), EquipmentSlot.HEAD, new ItemStack(Material.AIR), owner);
                //else NMSHandlers.getHandler().equipmentSlotUpdate(user.getUserBackpackManager().getFirstArmorStandId(), EquipmentSlot.HEAD, firstPersonBackpack, owner);
                NMSHandlers.getHandler().equipmentSlotUpdate(user.getUserBackpackManager().getFirstArmorStandId(), EquipmentSlot.HEAD, user.getUserCosmeticItem(this, firstPersonBackpack), owner);
            }
            MessagesUtil.sendDebugMessages("First Person Backpack Update[owner=" + user.getUniqueId() + ",player_location=" + loc + "]!", Level.INFO);
        }
        PacketManager.sendRidingPacket(entity.getEntityId(), user.getUserBackpackManager().getFirstArmorStandId(), outsideViewers);

        user.getUserBackpackManager().showBackpack();
    }

    public String getModelName() {
        return modelName;
    }

    public boolean isFirstPersonCompadible() {
        return firstPersonBackpack != null;
    }

    public ItemStack getFirstPersonBackpack() {
        return firstPersonBackpack;
    }
}
