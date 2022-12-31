package com.hibiscusmc.hmccosmetics.gui.type.types;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics;
import com.hibiscusmc.hmccosmetics.gui.action.Actions;
import com.hibiscusmc.hmccosmetics.gui.special.DyeMenu;
import com.hibiscusmc.hmccosmetics.gui.type.Type;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.List;

public class TypeCosmetic extends Type {

    public TypeCosmetic() {
        super("cosmetic");
    }

    @Override
    public void run(CosmeticUser user, ConfigurationNode config) {
        if (config.node("cosmetic").virtual()) return;
        String cosmeticName = config.node("cosmetic").getString();
        Cosmetic cosmetic = Cosmetics.getCosmetic(cosmeticName);
        if (cosmetic == null) {
            MessagesUtil.sendMessage(user.getPlayer(), "invalid-cosmetic");
            return;
        }

        if (!user.canEquipCosmetic(cosmetic)) {
            MessagesUtil.sendMessage(user.getPlayer(), "no-cosmetic-permission");
            return;
        }

        List<String> actionStrings = new ArrayList<>();
        ConfigurationNode actionConfig = config.node("actions");

        try {
            if (!actionConfig.node("any").virtual()) actionStrings.addAll(actionConfig.node("any").getList(String.class));

            if (user.getCosmetic(cosmetic.getSlot()) == cosmetic) {
                if (!actionConfig.node("on-unequip").virtual()) actionStrings.addAll(actionConfig.node("on-unequip").getList(String.class));
                MessagesUtil.sendDebugMessages("on-unequip");
                user.removeCosmeticSlot(cosmetic);
            } else {
                if (!actionConfig.node("on-equip").virtual()) actionStrings.addAll(actionConfig.node("on-equip").getList(String.class));
                MessagesUtil.sendDebugMessages("on-equip");
                // TODO: Redo this
                if (cosmetic.isDyable()) {
                    DyeMenu.openMenu(user, cosmetic);
                } else {
                    user.addPlayerCosmetic(cosmetic);
                }
            }

            Actions.runActions(user, actionStrings);

        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }

        //user.toggleCosmetic(cosmetic);
        user.updateCosmetic(cosmetic.getSlot());
    }
}
