package com.hibiscusmc.hmccosmetics.gui.types;

import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics;
import com.hibiscusmc.hmccosmetics.gui.actions.Actions;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.List;

public class CosmeticType extends Type {

    public CosmeticType() {
        super("cosmetic");
    }

    @Override
    public void run(CosmeticUser user, ConfigurationNode config) {
        if (config.node("cosmetic").virtual()) return;
        String cosmeticName = config.node("cosmetic").getString();
        Cosmetic cosmetic = Cosmetics.getCosmetic(cosmeticName);
        if (cosmetic == null) return;

        List<String> actionStrings = new ArrayList<>();
        ConfigurationNode actionConfig = config.node("actions");

        try {
            if (!actionConfig.node("any").virtual()) actionStrings.addAll(actionConfig.node("any").getList(String.class));

            if (!user.hasCosmetic(cosmetic)) {
                if (!actionConfig.node("on-equip").virtual()) actionStrings.addAll(actionConfig.node("on-equip").getList(String.class));
            } else {
                if (!actionConfig.node("on-unequip").virtual()) actionStrings.addAll(actionConfig.node("on-unequip").getList(String.class));
            }

            Actions.runActions(user, actionStrings);

            user.toggleCosmetic(cosmetic);
            user.updateCosmetic(cosmetic.getSlot());

        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }
}
