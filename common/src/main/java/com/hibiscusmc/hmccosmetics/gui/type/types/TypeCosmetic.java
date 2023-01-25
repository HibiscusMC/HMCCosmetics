package com.hibiscusmc.hmccosmetics.gui.type.types;

import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics;
import com.hibiscusmc.hmccosmetics.gui.action.Actions;
import com.hibiscusmc.hmccosmetics.gui.special.DyeMenu;
import com.hibiscusmc.hmccosmetics.gui.type.Type;
import com.hibiscusmc.hmccosmetics.hooks.PAPIHook;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.misc.StringUtils;
import com.hibiscusmc.hmccosmetics.util.misc.Utils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.meta.ItemMeta;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TypeCosmetic extends Type {

    public TypeCosmetic() {
        super("cosmetic");
    }

    @Override
    public void run(CosmeticUser user, ConfigurationNode config, ClickType clickType) {
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

            if (clickType != null) {
                if (clickType.isLeftClick()) {
                    if (!actionConfig.node("left-click").virtual()) actionStrings.addAll(actionConfig.node("left-click").getList(String.class));
                }
                if (clickType.isRightClick()) {
                    if (!actionConfig.node("right-click").virtual()) actionStrings.addAll(actionConfig.node("right-click").getList(String.class));
                }
            }

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

        user.updateCosmetic(cosmetic.getSlot());
    }

    @Override
    public ItemMeta setLore(CosmeticUser user, ConfigurationNode config, ItemMeta itemMeta) {
        List<String> processedLore = new ArrayList<>();

        if (config.node("cosmetic").virtual()) return processLoreLines(user, itemMeta);;
        String cosmeticName = config.node("cosmetic").getString();
        Cosmetic cosmetic = Cosmetics.getCosmetic(cosmeticName);
        if (cosmetic == null) {
            return processLoreLines(user, itemMeta);
        }

        if (user.canEquipCosmetic(cosmetic)) {
            return processLoreLines(user, itemMeta);
        } else {
            ConfigurationNode itemConfig = config.node("item");
            if (itemConfig.virtual()) return itemMeta;
            if (itemConfig.node("locked-name").virtual() && itemConfig.node("locked-name").virtual()) {
                return processLoreLines(user, itemMeta);
            }
            try {
                itemMeta.getLore().clear();

                List<String> lockedLore = Utils.replaceIfNull(itemConfig.node("locked-lore").getList(String.class),
                                new ArrayList<String>()).
                        stream().map(StringUtils::parseStringToString).collect(Collectors.toList());

                if (PAPIHook.isPAPIEnabled()) {
                    String lockedName = StringUtils.parseStringToString(Utils.replaceIfNull(itemConfig.node("locked-name").getString(), ""));
                    itemMeta.setDisplayName(PlaceholderAPI.setPlaceholders(user.getPlayer(), lockedName));
                    if (itemMeta.hasLore()) {
                        for (String loreLine : lockedLore) {
                            processedLore.add(PlaceholderAPI.setPlaceholders(user.getPlayer(), loreLine));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        itemMeta.setLore(processedLore);
        return itemMeta;
    }

    private ItemMeta processLoreLines(CosmeticUser user, ItemMeta itemMeta) {
        List<String> processedLore = new ArrayList<>();

        if (PAPIHook.isPAPIEnabled()) {
            if (itemMeta.hasLore()) {
                for (String loreLine : itemMeta.getLore()) {
                    processedLore.add(PlaceholderAPI.setPlaceholders(user.getPlayer(), loreLine));
                }
            }
        }

        itemMeta.setLore(processedLore);
        return itemMeta;
    }
}
