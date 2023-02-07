package com.hibiscusmc.hmccosmetics.gui.type.types;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticArmorType;
import com.hibiscusmc.hmccosmetics.gui.action.Actions;
import com.hibiscusmc.hmccosmetics.gui.special.DyeMenu;
import com.hibiscusmc.hmccosmetics.gui.type.Type;
import com.hibiscusmc.hmccosmetics.hooks.Hooks;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.misc.StringUtils;
import com.hibiscusmc.hmccosmetics.util.misc.Utils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.EquipmentSlot;
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
        // Fixes issue with offhand cosmetics not appearing. Yes, I know this is dumb
        Runnable run = () -> user.updateCosmetic(cosmetic.getSlot());
        if (cosmetic instanceof CosmeticArmorType) {
            if (((CosmeticArmorType) cosmetic).getEquipSlot().equals(EquipmentSlot.OFF_HAND)) {
                Bukkit.getScheduler().runTaskLater(HMCCosmeticsPlugin.getInstance(), run, 1);
                return;
            }
        }
        run.run();
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
            if (itemConfig.node("locked-name").virtual() && itemConfig.node("locked-lore").virtual()) {
                return processLoreLines(user, itemMeta);
            }
            try {
                List<String> lockedLore = itemMeta.getLore();
                String lockedName = itemMeta.getDisplayName();

                if (!itemConfig.node("locked-lore").virtual()) {
                    lockedLore = Utils.replaceIfNull(itemConfig.node("locked-lore").getList(String.class),
                                    new ArrayList<String>()).
                            stream().map(StringUtils::parseStringToString).collect(Collectors.toList());
                }
                if (!itemConfig.node("locked-name").virtual()) {
                    lockedName = StringUtils.parseStringToString(Utils.replaceIfNull(itemConfig.node("locked-name").getString(), ""));
                }

                if (Hooks.isActiveHook("PlaceHolderAPI")) {
                    lockedName = PlaceholderAPI.setPlaceholders(user.getPlayer(), lockedName);
                }
                itemMeta.setDisplayName(lockedName);
                if (itemMeta.hasLore()) {
                    itemMeta.getLore().clear();
                    for (String loreLine : lockedLore) {
                        if (Hooks.isActiveHook("PlaceHolderAPI")) loreLine = PlaceholderAPI.setPlaceholders(user.getPlayer(), loreLine);
                        processedLore.add(loreLine);
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

        if (itemMeta.hasLore()) {
            for (String loreLine : itemMeta.getLore()) {
                if (Hooks.isActiveHook("PlaceholderAPI"))
                    loreLine = PlaceholderAPI.setPlaceholders(user.getPlayer(), loreLine);
                processedLore.add(loreLine);
            }
        }

        itemMeta.setLore(processedLore);
        return itemMeta;
    }
}
