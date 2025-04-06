package com.hibiscusmc.hmccosmetics.cosmetic.types;

import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.manager.UserBackpackManager;
import com.hibiscusmc.hmccosmetics.user.manager.UserEntity;
import com.hibiscusmc.hmccosmetics.user.manager.UserBackpackManager;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.packets.HMCCPacketManager;
import lombok.Getter;
import me.lojosho.hibiscuscommons.nms.NMSHandlers;
import me.lojosho.hibiscuscommons.util.packets.PacketManager;
import me.lojosho.shaded.configurate.ConfigurationNode;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class CosmeticBackpackType extends Cosmetic {

    @Getter
    private int height = -1;
    private ItemStack firstPersonBackpack;

    public CosmeticBackpackType(String id, ConfigurationNode config) {
        super(id, config);

        if (!config.node("firstperson-item").virtual()) {
            this.firstPersonBackpack = generateItemStack(config.node("firstperson-item"));
            this.height = config.node("height").getInt(5);
        }
    }

    @Override
    public void update(@NotNull CosmeticUser user) {
        Entity entity = user.getEntity();
        if (entity == null) return;

        if (user.isInWardrobe() || !user.isBackpackSpawned()) return;
        if (user.isHidden()) {
            // Sometimes the backpack is not despawned when the player is hidden (weird ass logic happening somewhere)
            user.despawnBackpack();
            return;
        }

        UserBackpackManager backpackManager = user.getUserBackpackManager();
        Location loc = entity.getLocation();
        loc.setPitch(0);

        List<Player> addedViewers = user.getUserBackpackManager().getEntityManager().refreshViewers(loc);
        int entityId = user.getUserBackpackManager().getDisplayEntityId();
        ItemStack backpackItem = user.getUserCosmeticItem(CosmeticSlot.BACKPACK);

        user.getUserBackpackManager().getEntityManager().teleport(loc);

        HMCCPacketManager.sendEntitySpawnPacket(user.getEntity().getLocation(), entityId, EntityType.ITEM_DISPLAY, UUID.randomUUID(), addedViewers);
        HMCCPacketManager.sendDisplayEntityMetadataPacket(backpackManager.getDisplayEntityId(), backpackItem, addedViewers);
        // If true, it will send the riding packet to all players. If false, it will send the riding packet only to new players
        if (Settings.isBackpackForceRidingEnabled()) HMCCPacketManager.sendRidingPacket(entity.getEntityId(), user.getUserBackpackManager().getDisplayEntityId(), user.getUserBackpackManager().getEntityManager().getViewers());
        else HMCCPacketManager.sendRidingPacket(entity.getEntityId(), user.getUserBackpackManager().getDisplayEntityId(), addedViewers);

        /*
        if (!user.isInWardrobe() && isFirstPersonCompadible() && user.getPlayer() != null) {
            List<Player> owner = List.of(user.getPlayer());

            ArrayList<Integer> particleCloud = backpackManager.getAreaEffectEntityId();
            for (int i = 0; i < particleCloud.size(); i++) {
                if (i == 0) {
                    HMCCPacketManager.sendRidingPacket(entity.getEntityId(), particleCloud.get(i), owner);
                } else {
                    HMCCPacketManager.sendRidingPacket(particleCloud.get(i - 1), particleCloud.get(i) , owner);
                }
            }
            HMCCPacketManager.sendRidingPacket(particleCloud.get(particleCloud.size() - 1), firstArmorStandId, owner);
            if (!user.isHidden()) {
                //if (loc.getPitch() < -70) NMSHandlers.getHandler().equipmentSlotUpdate(user.getUserBackpackManager().getFirstArmorStandId(), EquipmentSlot.HEAD, new ItemStack(Material.AIR), owner);
                //else NMSHandlers.getHandler().equipmentSlotUpdate(user.getUserBackpackManager().getFirstArmorStandId(), EquipmentSlot.HEAD, firstPersonBackpack, owner);
                PacketManager.equipmentSlotUpdate(firstArmorStandId, EquipmentSlot.HEAD, user.getUserCosmeticItem(this, firstPersonBackpack), owner);
            }
            //MessagesUtil.sendDebugMessages("First Person Backpack Update[owner=" + user.getUniqueId() + ",player_location=" + loc + "]!", Level.INFO);
        }
         */

        backpackManager.showBackpack();
    }

    public boolean isFirstPersonCompadible() {
        return firstPersonBackpack != null;
    }

    public ItemStack getFirstPersonBackpack() {
        return firstPersonBackpack;
    }
}
