package com.hibiscusmc.hmccosmetics.user.manager;

import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBackpackType;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.packets.HMCCPacketManager;
import com.ticxo.modelengine.api.ModelEngineAPI;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.Getter;
import me.lojosho.hibiscuscommons.hooks.Hooks;
import me.lojosho.hibiscuscommons.nms.NMSHandlers;
import me.lojosho.hibiscuscommons.util.ServerUtils;
import me.lojosho.hibiscuscommons.util.packets.PacketManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class UserBackpackManager {

    @Getter
    private boolean backpackHidden;
    @Getter
    private int displayEntityId;
    private ArrayList<Integer> particleCloud = new ArrayList<>();
    @Getter
    private final CosmeticUser user;
    @Getter
    private UserEntity entityManager;

    public UserBackpackManager(CosmeticUser user) {
        this.user = user;
        this.backpackHidden = false;
        this.displayEntityId = ServerUtils.getNextEntityId();
        this.entityManager = new UserEntity(user.getUniqueId());
        if (user.getEntity() != null) this.entityManager.refreshViewers(user.getEntity().getLocation()); // Fixes an issue where a player, who somehow removes their potions, but doesn't have an entity produces an NPE (it's dumb)
    }

    @Deprecated
    public int getFirstArmorStandId() {
        return getDisplayEntityId();
    }

    public int getDisplayEntityId() {
        return displayEntityId;
    }

    public void spawnBackpack(CosmeticBackpackType cosmeticBackpackType) {
        MessagesUtil.sendDebugMessages("spawnBackpack Bukkit - Start");

        spawn(cosmeticBackpackType);
    }

    private void spawn(CosmeticBackpackType cosmeticBackpackType) {
        Location loc = user.getEntity().getLocation();

        getEntityManager().setIds(List.of(displayEntityId));
        getEntityManager().teleport(loc);
        List<Player> outsideViewers = getEntityManager().getViewers();
        ItemStack backpackItem = user.getUserCosmeticItem(CosmeticSlot.BACKPACK);

        HMCCPacketManager.sendEntitySpawnPacket(loc, getDisplayEntityId(), EntityType.ITEM_DISPLAY, UUID.randomUUID(), outsideViewers);
        HMCCPacketManager.sendItemDisplayMetadata(getDisplayEntityId(), backpackItem, false, outsideViewers);

        Entity entity = user.getEntity();
        int[] passengerIDs = new int[entity.getPassengers().size() + 1];

        for (int i = 0; i < entity.getPassengers().size(); i++) {
            passengerIDs[i] = entity.getPassengers().get(i).getEntityId();
        }

        passengerIDs[passengerIDs.length - 1] = this.getDisplayEntityId();

        ArrayList<Player> owner = new ArrayList<>();
        if (user.getPlayer() != null) owner.add(user.getPlayer());
        /*
        if (cosmeticBackpackType.isFirstPersonCompadible()) {
            for (int i = particleCloud.size(); i < cosmeticBackpackType.getHeight(); i++) {
                int entityId = ServerUtils.getNextEntityId();
                HMCCPacketManager.sendEntitySpawnPacket(user.getEntity().getLocation(), entityId, EntityType.AREA_EFFECT_CLOUD, UUID.randomUUID());
                HMCCPacketManager.sendCloudEffect(entityId, HMCCPacketManager.getViewers(user.getEntity().getLocation()));
                this.particleCloud.add(entityId);
            }
            // Copied code from updating the backpack
            for (int i = 0; i < particleCloud.size(); i++) {
                if (i == 0) HMCCPacketManager.sendRidingPacket(entity.getEntityId(), particleCloud.get(i), owner);
                else HMCCPacketManager.sendRidingPacket(particleCloud.get(i - 1), particleCloud.get(i) , owner);
            }
            HMCCPacketManager.sendRidingPacket(particleCloud.get(particleCloud.size() - 1), user.getUserBackpackManager().getFirstArmorStandId(), owner);
            if (!user.isHidden()) PacketManager.equipmentSlotUpdate(user.getUserBackpackManager().getFirstArmorStandId(), EquipmentSlot.HEAD, user.getUserCosmeticItem(cosmeticBackpackType, cosmeticBackpackType.getFirstPersonBackpack()), owner);
        }
         */
        HMCCPacketManager.sendRidingPacket(entity.getEntityId(), passengerIDs, outsideViewers);

        MessagesUtil.sendDebugMessages("spawnBackpack Bukkit - Finish");
    }

    public void despawnBackpack() {
        NMSHandlers.getHandler().getPacketHandler().sendEntityDestroyPacket(IntList.of(getDisplayEntityId()), getEntityManager().getViewers());
        if (particleCloud != null) {
            for (Integer entityId : particleCloud) {
                HMCCPacketManager.sendEntityDestroyPacket(entityId, getEntityManager().getViewers());
            }
            this.particleCloud = null;
        }
    }

    public void hideBackpack() {
        if (user.isHidden()) return;
        //getArmorStand().getEquipment().clear();
        backpackHidden = true;
    }

    public void showBackpack() {
        if (!backpackHidden) return;
        CosmeticBackpackType cosmeticBackpackType = (CosmeticBackpackType) user.getCosmetic(CosmeticSlot.BACKPACK);
        ItemStack item = user.getUserCosmeticItem(cosmeticBackpackType);
        //getArmorStand().getEquipment().setHelmet(item);
        backpackHidden = false;
    }

    public void setVisibility(boolean shown) {
        backpackHidden = shown;
    }

    public ArrayList<Integer> getAreaEffectEntityId() {
        return null;
    }

    public void setItem(ItemStack item) {
        PacketManager.equipmentSlotUpdate(getFirstArmorStandId(), EquipmentSlot.HEAD, item, getEntityManager().getViewers());
    }

    public void clearItems() {
        ItemStack item = new ItemStack(Material.AIR);
        PacketManager.equipmentSlotUpdate(getFirstArmorStandId(), EquipmentSlot.HEAD, item, getEntityManager().getViewers());
    }
}
