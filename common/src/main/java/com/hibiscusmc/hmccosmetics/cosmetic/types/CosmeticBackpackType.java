package com.hibiscusmc.hmccosmetics.cosmetic.types;

import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.manager.UserBackpackManager;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.packets.HMCCPacketManager;
import lombok.Getter;
import me.lojosho.hibiscuscommons.util.packets.PacketManager;
import me.lojosho.shaded.configurate.ConfigurationNode;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class CosmeticBackpackType extends Cosmetic {

    @Getter
    private final String modelName;
    @Getter
    private int height = -1;
    private ItemStack firstPersonBackpack;

    public CosmeticBackpackType(String id, ConfigurationNode config) {
        super(id, config);

        modelName = config.node("model").getString();

        if (!config.node("firstperson-item").virtual()) {
            this.firstPersonBackpack = generateItemStack(config.node("firstperson-item"));
            this.height = config.node("height").getInt(5);
        }
    }

    @Override
    public void update(@NotNull CosmeticUser user) {
        Entity entity = Bukkit.getEntity(user.getUniqueId());
        if (entity == null) return;

        Location loc = entity.getLocation().clone().add(0, 2, 0);

        if (user.isInWardrobe() || !user.isBackpackSpawned()) return;
        if (user.isHidden()) {
            // Sometimes the backpack is not despawned when the player is hidden (weird ass logic happening somewhere)
            user.despawnBackpack();
            return;
        }
        List<Player> outsideViewers = user.getUserBackpackManager().getEntityManager().refreshViewers(loc);

        UserBackpackManager backpackManager = user.getUserBackpackManager();
        backpackManager.getEntityManager().teleport(loc);
        backpackManager.getEntityManager().setRotation((int) loc.getYaw(), isFirstPersonCompadible());

        HMCCPacketManager.sendEntitySpawnPacket(user.getEntity().getLocation(), backpackManager.getFirstArmorStandId(), EntityType.ARMOR_STAND, UUID.randomUUID(), outsideViewers);
        //HMCCPacketManager.sendArmorstandMetadata(backpackManager.getFirstArmorStandId(), outsideViewers);
        PacketManager.equipmentSlotUpdate(backpackManager.getFirstArmorStandId(), EquipmentSlot.HEAD, user.getUserCosmeticItem(this, getItem()), outsideViewers);
        // If true, it will send the riding packet to all players. If false, it will send the riding packet only to new players
        if (Settings.isBackpackForceRidingEnabled()) HMCCPacketManager.sendRidingPacket(entity.getEntityId(), backpackManager.getFirstArmorStandId(), backpackManager.getEntityManager().getViewers());
        else HMCCPacketManager.sendRidingPacket(entity.getEntityId(), user.getUserBackpackManager().getFirstArmorStandId(), outsideViewers);

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
            HMCCPacketManager.sendRidingPacket(particleCloud.get(particleCloud.size() - 1), backpackManager.getFirstArmorStandId(), owner);
            if (!user.isHidden()) {
                //if (loc.getPitch() < -70) NMSHandlers.getHandler().equipmentSlotUpdate(user.getUserBackpackManager().getFirstArmorStandId(), EquipmentSlot.HEAD, new ItemStack(Material.AIR), owner);
                //else NMSHandlers.getHandler().equipmentSlotUpdate(user.getUserBackpackManager().getFirstArmorStandId(), EquipmentSlot.HEAD, firstPersonBackpack, owner);
                PacketManager.equipmentSlotUpdate(backpackManager.getFirstArmorStandId(), EquipmentSlot.HEAD, user.getUserCosmeticItem(this, firstPersonBackpack), owner);
            }
            MessagesUtil.sendDebugMessages("First Person Backpack Update[owner=" + user.getUniqueId() + ",player_location=" + loc + "]!", Level.INFO);
        }

        MessagesUtil.sendDebugMessages("TTTTTTT " + backpackManager.refreshBlock(backpackManager.getEntityManager().getViewers()));
        backpackManager.showBackpack();
    }

    public boolean isFirstPersonCompadible() {
        return firstPersonBackpack != null;
    }

    public ItemStack getFirstPersonBackpack() {
        return firstPersonBackpack;
    }
}
