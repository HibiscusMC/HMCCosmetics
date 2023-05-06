package com.hibiscusmc.hmccosmetics.gui.type.types;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.serializer.ItemSerializer;
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
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.lang.invoke.TypeDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TypeCosmetic extends Type {

    public TypeCosmetic() {
        super("cosmetic");
    }

    @Override
    public void run(CosmeticUser user, @NotNull ConfigurationNode config, ClickType clickType) {
        if (config.node("cosmetic").virtual()) return;
        String cosmeticName = config.node("cosmetic").getString();
        Cosmetic cosmetic = Cosmetics.getCosmetic(cosmeticName);
        Player player = user.getPlayer();
        if (cosmetic == null) {
            MessagesUtil.sendMessage(player, "invalid-cosmetic");
            return;
        }

        if (!user.canEquipCosmetic(cosmetic)) {
            MessagesUtil.sendMessage(player, "no-cosmetic-permission");
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
    public ItemStack setItem(CosmeticUser user, @NotNull ConfigurationNode config, ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (config.node("cosmetic").virtual()) {
            itemStack.setItemMeta(processLoreLines(user, itemMeta));
            return itemStack;
        };
        String cosmeticName = config.node("cosmetic").getString();
        Cosmetic cosmetic = Cosmetics.getCosmetic(cosmeticName);
        if (cosmetic == null) {
            itemStack.setItemMeta(processLoreLines(user, itemMeta));
            return itemStack;
        }

        if (user.hasCosmeticInSlot(cosmetic) && !config.node("equipped-item").virtual()) {
            ConfigurationNode equippedItem = config.node("equipped-item");
            try {
                if (equippedItem.node("material").virtual()) equippedItem.node("material").set(config.node("item", "material").getString());
            } catch (SerializationException e) {
                // Nothing >:)
            }
            try {
                itemStack = ItemSerializer.INSTANCE.deserialize(ItemStack.class, equippedItem);
            } catch (SerializationException e) {
                throw new RuntimeException(e);
            }
            return itemStack;
        }

        if (!user.canEquipCosmetic(cosmetic) && !config.node("locked-item").virtual()) {
            ConfigurationNode lockedItem = config.node("locked-item");
            try {
                if (lockedItem.node("material").virtual()) lockedItem.node("material").set(config.node("item", "material").getString());
            } catch (SerializationException e) {
                // Nothing >:)
            }
            try {
                itemStack = ItemSerializer.INSTANCE.deserialize(ItemStack.class, lockedItem);
                //item = config.node("item").get(ItemStack.class);
            } catch (SerializationException e) {
                throw new RuntimeException(e);
            }
            return itemStack;
        }
        return itemStack;
    }

    @Contract("_, _ -> param2")
    @NotNull
    @SuppressWarnings("Duplicates")
    private ItemMeta processLoreLines(CosmeticUser user, @NotNull ItemMeta itemMeta) {
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
