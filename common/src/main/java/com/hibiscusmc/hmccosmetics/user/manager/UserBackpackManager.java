package com.hibiscusmc.hmccosmetics.user.manager;

import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBackpackType;
import com.hibiscusmc.hmccosmetics.hooks.Hooks;
import com.hibiscusmc.hmccosmetics.nms.NMSHandlers;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.packets.PacketManager;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;

public class UserBackpackManager {

    private boolean hideBackpack;
    private ArmorStand invisibleArmorStand;
    private ArrayList<Integer> particleCloud = new ArrayList<>();
    private final CosmeticUser user;
    private boolean firstPerson;
    private UserBackpackCloudManager cloudManager;

    public UserBackpackManager(CosmeticUser user, boolean firstPersonView) {
        this.user = user;
        this.hideBackpack = false;
        this.firstPerson = firstPersonView;
        this.cloudManager = new UserBackpackCloudManager(user.getUniqueId());
    }

    public int getFirstArmorStandId() {
        return invisibleArmorStand.getEntityId();
    }

    public ArmorStand getArmorStand() {
        return invisibleArmorStand;
    }

    public boolean IsValidBackpackEntity() {
        if (invisibleArmorStand == null) {
            MessagesUtil.sendDebugMessages("InvisibleArmorStand is Null!");
            return false;
        }
        return getArmorStand().isValid();
    }

    public void spawnBackpack(CosmeticBackpackType cosmeticBackpackType) {
        MessagesUtil.sendDebugMessages("spawnBackpack Bukkit - Start");

        spawn(cosmeticBackpackType);
    }

    private void spawn(CosmeticBackpackType cosmeticBackpackType) {
        if (this.invisibleArmorStand != null) return;
        this.invisibleArmorStand = (ArmorStand) NMSHandlers.getHandler().spawnBackpack(user, cosmeticBackpackType);

        if (cosmeticBackpackType.isFirstPersonCompadible()) {
            for (int i = particleCloud.size(); i < 5; i++) {
                int entityId = NMSHandlers.getHandler().getNextEntityId();
                PacketManager.sendEntitySpawnPacket(user.getPlayer().getLocation(), entityId, EntityType.AREA_EFFECT_CLOUD, UUID.randomUUID());
                PacketManager.sendCloudEffect(entityId, PacketManager.getViewers(user.getPlayer().getLocation()));
                this.particleCloud.add(entityId);
            }
        }

        // No one should be using ME because it barely works but some still use it, so it's here
        if (cosmeticBackpackType.getModelName() != null && Hooks.isActiveHook("ModelEngine")) {
            if (ModelEngineAPI.api.getModelRegistry().getBlueprint(cosmeticBackpackType.getModelName()) == null) {
                MessagesUtil.sendDebugMessages("Invalid Model Engine Blueprint " + cosmeticBackpackType.getModelName(), Level.SEVERE);
                return;
            }
            ModeledEntity modeledEntity = ModelEngineAPI.getOrCreateModeledEntity(invisibleArmorStand);
            ActiveModel model = ModelEngineAPI.createActiveModel(ModelEngineAPI.getBlueprint(cosmeticBackpackType.getModelName()));
            model.setCanHurt(false);
            modeledEntity.addModel(model, false);
        }

        cosmeticBackpackType.update(user);

        MessagesUtil.sendDebugMessages("spawnBackpack Bukkit - Finish");
    }

    public void despawnBackpack() {
        if (invisibleArmorStand != null) {
            invisibleArmorStand.setHealth(0);
            invisibleArmorStand.remove();
            this.invisibleArmorStand = null;
        }
        if (particleCloud != null) {
            for (int i = 0; i < particleCloud.size(); i++) {
                PacketManager.sendEntityDestroyPacket(particleCloud.get(i), getCloudManager().getViewers());
            }
            this.particleCloud = null;
        }
    }

    public void hideBackpack() {
        if (user.getHidden()) return;
        getArmorStand().getEquipment().clear();
        hideBackpack = true;
    }

    public void showBackpack() {
        if (!hideBackpack) return;
        CosmeticBackpackType cosmeticBackpackType = (CosmeticBackpackType) user.getCosmetic(CosmeticSlot.BACKPACK);
        ItemStack item = user.getUserCosmeticItem(cosmeticBackpackType);
        getArmorStand().getEquipment().setHelmet(item);
        hideBackpack = false;
    }

    public void setVisibility(boolean shown) {
        hideBackpack = shown;
    }

    public ArrayList<Integer> getAreaEffectEntityId() {
        return particleCloud;
    }

    public void setItem(ItemStack item) {
            getArmorStand().getEquipment().setHelmet(item);
    }

    public void clearItems() {
        ItemStack item = new ItemStack(Material.AIR);
        getArmorStand().getEquipment().setHelmet(item);
    }

    public UserBackpackCloudManager getCloudManager() {
        return cloudManager;
    }
}
