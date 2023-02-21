package com.hibiscusmc.hmccosmetics.user.manager;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBackpackType;
import com.hibiscusmc.hmccosmetics.nms.NMSHandlers;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class UserBackpackManager {

    private boolean hideBackpack;
    private List<ArmorStand> armorStandList;
    private CosmeticUser user;
    private BackpackType backpackType;

    public UserBackpackManager(CosmeticUser user, BackpackType backpackType) {
        this.user = user;
        hideBackpack = false;
        this.backpackType = backpackType;
        this.armorStandList = new ArrayList<>();
    }

    public int getFirstArmorstandId() {
        ArmorStand armorStand = getFirstArmorstand();
        return armorStand.getEntityId();
    }

    public ArmorStand getFirstArmorstand() {
        return armorStandList.get(0);
    }

    public ArmorStand getSecondArmorstand() {
        return armorStandList.get(1);
    }

    public void spawnBackpack(CosmeticBackpackType cosmeticBackpackType) {
        MessagesUtil.sendDebugMessages("spawnBackpack Bukkit - Start");

        if (getBackpackType().equals(BackpackType.NORMAL)) {
            spawnNormalBackpack(cosmeticBackpackType);
        }
        if (getBackpackType().equals(BackpackType.FIRST_PERSON)) {
            spawnFirstPersonBackpack(cosmeticBackpackType);
        }

        MessagesUtil.sendDebugMessages("spawnBackpack Bukkit - Finish");
    }

    private void spawnNormalBackpack(CosmeticBackpackType cosmeticBackpackType) {
        ArmorStand armorStand = (ArmorStand) NMSHandlers.getHandler().spawnBackpack(user, cosmeticBackpackType);

        if (cosmeticBackpackType.getModelName() != null && HMCCosmeticsPlugin.hasModelEngine()) {
            if (ModelEngineAPI.api.getModelRegistry().getBlueprint(cosmeticBackpackType.getModelName()) == null) {
                MessagesUtil.sendDebugMessages("Invalid Model Engine Blueprint " + cosmeticBackpackType.getModelName(), Level.SEVERE);
                return;
            }
            ModeledEntity modeledEntity = ModelEngineAPI.getOrCreateModeledEntity(armorStand);
            ActiveModel model = ModelEngineAPI.createActiveModel(ModelEngineAPI.getBlueprint(cosmeticBackpackType.getModelName()));
            model.setCanHurt(false);
            modeledEntity.addModel(model, false);
        }

        armorStandList.add(armorStand);
    }

    private void spawnFirstPersonBackpack(CosmeticBackpackType cosmeticBackpackType) {
        ArmorStand bottomArmorstand = (ArmorStand) NMSHandlers.getHandler().spawnBackpack(user, cosmeticBackpackType);
        ArmorStand topArmorstand = (ArmorStand) NMSHandlers.getHandler().spawnBackpack(user, cosmeticBackpackType);

        if (cosmeticBackpackType.getModelName() != null && HMCCosmeticsPlugin.hasModelEngine()) {
            if (ModelEngineAPI.api.getModelRegistry().getBlueprint(cosmeticBackpackType.getModelName()) == null) {
                MessagesUtil.sendDebugMessages("Invalid Model Engine Blueprint " + cosmeticBackpackType.getModelName(), Level.SEVERE);
                return;
            }
            ModeledEntity modeledEntity = ModelEngineAPI.getOrCreateModeledEntity(bottomArmorstand);
            ActiveModel model = ModelEngineAPI.createActiveModel(ModelEngineAPI.getBlueprint(cosmeticBackpackType.getModelName()));
            model.setCanHurt(false);
            modeledEntity.addModel(model, false);
        }

        armorStandList.add(bottomArmorstand);
        armorStandList.add(topArmorstand);
    }

    public void despawnBackpack() {
        for (ArmorStand armorStand : armorStandList) {
            if (armorStand == null) return;
            armorStand.setHealth(0);
            armorStand.remove();
        }
        armorStandList.clear();
    }

    public void hideBackpack() {
        if (user.getHidden() == true) return;
        for (ArmorStand armorStand : armorStandList) {
            armorStand.getEquipment().clear();
        }
        hideBackpack = true;
    }

    public void showBackpack() {
        if (hideBackpack == false) return;
        CosmeticBackpackType cosmeticBackpackType = (CosmeticBackpackType) user.getCosmetic(CosmeticSlot.BACKPACK);
        ItemStack item = user.getUserCosmeticItem(cosmeticBackpackType);
        setItem(item);
        hideBackpack = false;
    }

    public void setItem(ItemStack item) {
        if (getBackpackType().equals(BackpackType.NORMAL)) {
            getFirstArmorstand().getEquipment().setHelmet(item);
        }
        if (getBackpackType().equals(BackpackType.FIRST_PERSON)) {
            getSecondArmorstand().getEquipment().setHelmet(item);
        }
    }

    public void clearItems() {
        ItemStack item = new ItemStack(Material.AIR);
        if (getBackpackType().equals(BackpackType.NORMAL)) {
            getFirstArmorstand().getEquipment().setHelmet(item);
        }
        if (getBackpackType().equals(BackpackType.FIRST_PERSON)) {
            getSecondArmorstand().getEquipment().setHelmet(item);
        }
    }

    public void setVisibility(boolean shown) {
        hideBackpack = shown;
    }

    public List<ArmorStand> getArmorStandList() {
        return armorStandList;
    }

    public BackpackType getBackpackType() {
        return backpackType;
    }

    public enum BackpackType {
        NORMAL,
        FIRST_PERSON // First person not yet implemented
    }
}
