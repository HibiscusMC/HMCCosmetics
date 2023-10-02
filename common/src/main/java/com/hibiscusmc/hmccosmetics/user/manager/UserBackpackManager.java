package com.hibiscusmc.hmccosmetics.user.manager;

import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBackpackType;
import com.hibiscusmc.hmccosmetics.hooks.Hooks;
import com.hibiscusmc.hmccosmetics.nms.NMSHandlers;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.packets.PacketManager;
import com.ticxo.modelengine.api.ModelEngineAPI;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class UserBackpackManager {

    @Getter
    private boolean backpackHidden;
    @Getter
    private int invisibleArmorStand;
    private ArrayList<Integer> particleCloud = new ArrayList<>();
    @Getter
    private final CosmeticUser user;
    @Getter
    private UserEntity entityManager;

    public UserBackpackManager(CosmeticUser user) {
        this.user = user;
        this.backpackHidden = false;
        this.invisibleArmorStand = NMSHandlers.getHandler().getNextEntityId();
        this.entityManager = new UserEntity(user.getUniqueId());
        this.entityManager.refreshViewers(user.getEntity().getLocation());
    }

    public int getFirstArmorStandId() {
        return invisibleArmorStand;
    }

    public void spawnBackpack(CosmeticBackpackType cosmeticBackpackType) {
        MessagesUtil.sendDebugMessages("spawnBackpack Bukkit - Start");

        spawn(cosmeticBackpackType);
    }

    private void spawn(CosmeticBackpackType cosmeticBackpackType) {
        getEntityManager().setIds(List.of(invisibleArmorStand));
        getEntityManager().teleport(user.getEntity().getLocation());
        List<Player> outsideViewers = getEntityManager().getViewers();
        PacketManager.sendEntitySpawnPacket(user.getEntity().getLocation(), getFirstArmorStandId(), EntityType.ARMOR_STAND, UUID.randomUUID(), getEntityManager().getViewers());
        PacketManager.sendArmorstandMetadata(getFirstArmorStandId(), outsideViewers);

        Entity entity = user.getEntity();

        int[] passengerIDs = new int[entity.getPassengers().size() + 1];

        for (int i = 0; i < entity.getPassengers().size(); i++) {
            passengerIDs[i] = entity.getPassengers().get(i).getEntityId();
        }

        passengerIDs[passengerIDs.length - 1] = this.getFirstArmorStandId();

        ArrayList<Player> owner = new ArrayList<>();
        if (user.getPlayer() != null) owner.add(user.getPlayer());

        if (cosmeticBackpackType.isFirstPersonCompadible()) {
            for (int i = particleCloud.size(); i < cosmeticBackpackType.getHeight(); i++) {
                int entityId = NMSHandlers.getHandler().getNextEntityId();
                PacketManager.sendEntitySpawnPacket(user.getEntity().getLocation(), entityId, EntityType.AREA_EFFECT_CLOUD, UUID.randomUUID());
                PacketManager.sendCloudEffect(entityId, PacketManager.getViewers(user.getEntity().getLocation()));
                this.particleCloud.add(entityId);
            }
            // Copied code from updating the backpack
            for (int i = 0; i < particleCloud.size(); i++) {
                if (i == 0) PacketManager.sendRidingPacket(entity.getEntityId(), particleCloud.get(i), owner);
                else PacketManager.sendRidingPacket(particleCloud.get(i - 1), particleCloud.get(i) , owner);
            }
            PacketManager.sendRidingPacket(particleCloud.get(particleCloud.size() - 1), user.getUserBackpackManager().getFirstArmorStandId(), owner);
            if (!user.getHidden()) NMSHandlers.getHandler().equipmentSlotUpdate(user.getUserBackpackManager().getFirstArmorStandId(), EquipmentSlot.HEAD, user.getUserCosmeticItem(cosmeticBackpackType, cosmeticBackpackType.getFirstPersonBackpack()), owner);
        }
        NMSHandlers.getHandler().equipmentSlotUpdate(getFirstArmorStandId(), EquipmentSlot.HEAD, user.getUserCosmeticItem(cosmeticBackpackType), outsideViewers);
        PacketManager.sendRidingPacket(entity.getEntityId(), passengerIDs, outsideViewers);

        // No one should be using ME because it barely works but some still use it, so it's here
        if (cosmeticBackpackType.getModelName() != null && Hooks.isActiveHook("ModelEngine")) {
            if (ModelEngineAPI.getBlueprint(cosmeticBackpackType.getModelName()) == null) {
                MessagesUtil.sendDebugMessages("Invalid Model Engine Blueprint " + cosmeticBackpackType.getModelName(), Level.SEVERE);
                return;
            }
            /* TODO: Readd ModelEngine support
            ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(new PacketBaseEntity(getFirstArmorStandId(), UUID.randomUUID(), entity.getLocation()));
            ActiveModel model = ModelEngineAPI.createActiveModel(ModelEngineAPI.getBlueprint(cosmeticBackpackType.getModelName()));
            model.setCanHurt(false);
            modeledEntity.addModel(model, false);
             */
        }

        MessagesUtil.sendDebugMessages("spawnBackpack Bukkit - Finish");
    }

    public void despawnBackpack() {
        PacketManager.sendEntityDestroyPacket(invisibleArmorStand, getEntityManager().getViewers());
        if (particleCloud != null) {
            for (Integer entityId : particleCloud) {
                PacketManager.sendEntityDestroyPacket(entityId, getEntityManager().getViewers());
            }
            this.particleCloud = null;
        }
    }

    public void hideBackpack() {
        if (user.getHidden()) return;
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
        return particleCloud;
    }

    public void setItem(ItemStack item) {
        NMSHandlers.getHandler().equipmentSlotUpdate(getFirstArmorStandId(), EquipmentSlot.HEAD, item, getEntityManager().getViewers());
    }

    public void clearItems() {
        ItemStack item = new ItemStack(Material.AIR);
        NMSHandlers.getHandler().equipmentSlotUpdate(getFirstArmorStandId(), EquipmentSlot.HEAD, item, getEntityManager().getViewers());
    }
}
