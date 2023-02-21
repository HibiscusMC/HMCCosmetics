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
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Level;

public class UserBackpackManager {

    private boolean hideBackpack;
    private ArmorStand invisibleArmorstand;
    private CosmeticUser user;
    private BackpackType backpackType;

    public UserBackpackManager(CosmeticUser user) {
        this.user = user;
        hideBackpack = false;
        backpackType = BackpackType.NORMAL;
    }

    public int getFirstArmorstandId() {
        return invisibleArmorstand.getEntityId();
    }

    public ArmorStand getArmorstand() {
        return invisibleArmorstand;
    }

    public void spawnBackpack(CosmeticBackpackType cosmeticBackpackType) {
        MessagesUtil.sendDebugMessages("spawnBackpack Bukkit - Start");

        if (this.invisibleArmorstand != null) return;

        this.invisibleArmorstand = (ArmorStand) NMSHandlers.getHandler().spawnBackpack(user, cosmeticBackpackType);

        if (cosmeticBackpackType.getModelName() != null && HMCCosmeticsPlugin.hasModelEngine()) {
            if (ModelEngineAPI.api.getModelRegistry().getBlueprint(cosmeticBackpackType.getModelName()) == null) {
                MessagesUtil.sendDebugMessages("Invalid Model Engine Blueprint " + cosmeticBackpackType.getModelName(), Level.SEVERE);
                return;
            }
            ModeledEntity modeledEntity = ModelEngineAPI.getOrCreateModeledEntity(invisibleArmorstand);
            ActiveModel model = ModelEngineAPI.createActiveModel(ModelEngineAPI.getBlueprint(cosmeticBackpackType.getModelName()));
            model.setCanHurt(false);
            modeledEntity.addModel(model, false);
        }

        MessagesUtil.sendDebugMessages("spawnBackpack Bukkit - Finish");
    }

    public void despawnBackpack() {
        if (invisibleArmorstand == null) return;
        invisibleArmorstand.setHealth(0);
        invisibleArmorstand.remove();
        this.invisibleArmorstand = null;
    }

    public void hideBackpack() {
        if (user.getHidden() == true) return;
        getArmorstand().getEquipment().clear();
        hideBackpack = true;
    }

    public void showBackpack() {
        if (hideBackpack == false) return;
        CosmeticBackpackType cosmeticBackpackType = (CosmeticBackpackType) user.getCosmetic(CosmeticSlot.BACKPACK);
        ItemStack item = user.getUserCosmeticItem(cosmeticBackpackType);
        getArmorstand().getEquipment().setHelmet(item);
        hideBackpack = false;
    }

    public void setVisibility(boolean shown) {
        hideBackpack = shown;
    }

    public enum BackpackType {
        NORMAL,
        FIRST_PERSON // First person not yet implemented
    }
}
