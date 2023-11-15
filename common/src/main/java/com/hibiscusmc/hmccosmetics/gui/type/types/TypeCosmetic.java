package com.hibiscusmc.hmccosmetics.gui.type.types;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.Settings;
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
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.List;

public class TypeCosmetic extends Type {

    public TypeCosmetic() {
        super("cosmetic");
    }

    @Override
    public void run(CosmeticUser user, @NotNull ConfigurationNode config, ClickType clickType) {
        MessagesUtil.sendDebugMessages("Running Cosmetic Click Type");
        if (config.node("cosmetic").virtual()) {
            MessagesUtil.sendDebugMessages("Cosmetic Config Field Virtual");
            return;
        }
        String cosmeticName = config.node("cosmetic").getString();
        Cosmetic cosmetic = Cosmetics.getCosmetic(cosmeticName);
        Player player = user.getPlayer();
        if (cosmetic == null) {
            MessagesUtil.sendDebugMessages("No Cosmetic Found");
            MessagesUtil.sendMessage(player, "invalid-cosmetic");
            return;
        }

        if (!user.canEquipCosmetic(cosmetic)) {
            MessagesUtil.sendDebugMessages("No Cosmetic Permission");
            MessagesUtil.sendMessage(player, "no-cosmetic-permission");
            return;
        }

        boolean isUnEquippingCosmetic = false;
        if (user.getCosmetic(cosmetic.getSlot()) == cosmetic) isUnEquippingCosmetic = true;

        String requiredClick;
        if (isUnEquippingCosmetic) requiredClick = Settings.getCosmeticUnEquipClickType();
        else requiredClick = Settings.getCosmeticEquipClickType();

        MessagesUtil.sendDebugMessages("Required click type: " + requiredClick);
        MessagesUtil.sendDebugMessages("Click type: " + clickType.name());
        if (!requiredClick.equalsIgnoreCase("ANY") && !requiredClick.equalsIgnoreCase(clickType.name())) {
            MessagesUtil.sendMessage(user.getPlayer(), "invalid-click-type");
            return;
        }

        List<String> actionStrings = new ArrayList<>();
        ConfigurationNode actionConfig = config.node("actions");

        MessagesUtil.sendDebugMessages("Running Actions");

        try {
            if (!actionConfig.node("any").virtual()) actionStrings.addAll(actionConfig.node("any").getList(String.class));

            if (clickType != null) {
                if (clickType.isLeftClick()) {
                    if (!actionConfig.node("left-click").virtual()) actionStrings.addAll(actionConfig.node("left-click").getList(String.class));
                }
                if (clickType.isRightClick()) {
                    if (!actionConfig.node("right-click").virtual()) actionStrings.addAll(actionConfig.node("right-click").getList(String.class));
                }
                if (clickType.equals(ClickType.SHIFT_LEFT)) {
                    if (!actionConfig.node("shift-left-click").virtual()) actionStrings.addAll(actionConfig.node("shift-left-click").getList(String.class));
                }
                if (clickType.equals(ClickType.SHIFT_RIGHT)) {
                    if (!actionConfig.node("shift-right-click").virtual()) actionStrings.addAll(actionConfig.node("shift-right-click").getList(String.class));
                }
            }

            if (isUnEquippingCosmetic) {
                if (!actionConfig.node("on-unequip").virtual()) actionStrings.addAll(actionConfig.node("on-unequip").getList(String.class));
                MessagesUtil.sendDebugMessages("on-unequip");
                user.removeCosmeticSlot(cosmetic);
            } else {
                if (!actionConfig.node("on-equip").virtual()) actionStrings.addAll(actionConfig.node("on-equip").getList(String.class));
                MessagesUtil.sendDebugMessages("on-equip");
                // TODO: Redo this
                if (cosmetic.isDyable() && Hooks.isActiveHook("HMCColor")) {
                    DyeMenu.openMenu(user, cosmetic);
                } else {
                    user.addPlayerCosmetic(cosmetic);
                }
            }

            Actions.runActions(user, actionStrings);

        } catch (SerializationException e) {
            e.printStackTrace();
        }
        // Fixes issue with offhand cosmetics not appearing. Yes, I know this is dumb
        Runnable run = () -> user.updateCosmetic(cosmetic.getSlot());
        if (cosmetic instanceof CosmeticArmorType) {
            if (((CosmeticArmorType) cosmetic).getEquipSlot().equals(EquipmentSlot.OFF_HAND)) {
                Bukkit.getScheduler().runTaskLater(HMCCosmeticsPlugin.getInstance(), run, 1);
            }
        }
        run.run();
        MessagesUtil.sendDebugMessages("Finished Type Click Run");
    }

    @Override
    public ItemStack setItem(CosmeticUser user, @NotNull ConfigurationNode config, ItemStack itemStack, int slot) {
        itemStack.setItemMeta(processLoreLines(user, itemStack.getItemMeta()));

        if (config.node("cosmetic").virtual()) {
            return itemStack;
        }
        String cosmeticName = config.node("cosmetic").getString();
        Cosmetic cosmetic = Cosmetics.getCosmetic(cosmeticName);
        if (cosmetic == null) {
            return itemStack;
        }

        if (user.hasCosmeticInSlot(cosmetic) && !config.node("equipped-item").virtual()) {
            MessagesUtil.sendDebugMessages("GUI Equipped Item");
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
            itemStack.setItemMeta(processLoreLines(user, itemStack.getItemMeta()));
            return itemStack;
        }

        if (!user.canEquipCosmetic(cosmetic) && !config.node("locked-item").virtual()) {
            MessagesUtil.sendDebugMessages("GUI Locked Item");
            ConfigurationNode lockedItem = config.node("locked-item");
            try {
                if (lockedItem.node("material").virtual()) lockedItem.node("material").set(config.node("item", "material").getString());
            } catch (SerializationException e) {
                // Nothing >:)
            }
            try {
                itemStack = ItemSerializer.INSTANCE.deserialize(ItemStack.class, lockedItem);
            } catch (SerializationException e) {
                throw new RuntimeException(e);
            }
            itemStack.setItemMeta(processLoreLines(user, itemStack.getItemMeta()));
            return itemStack;
        }
        return itemStack;
    }

    @Contract("_, _ -> param2")
    @NotNull
    @SuppressWarnings("Duplicates")
    private ItemMeta processLoreLines(CosmeticUser user, @NotNull ItemMeta itemMeta) {
        List<String> processedLore = new ArrayList<>();

        if (itemMeta.hasDisplayName()) {
            itemMeta.setDisplayName(Hooks.processPlaceholders(user.getPlayer(), itemMeta.getDisplayName()));
        }

        if (itemMeta.hasLore()) {
            for (String loreLine : itemMeta.getLore()) {
                processedLore.add(Hooks.processPlaceholders(user.getPlayer(), loreLine));
            }
        }

        if (itemMeta instanceof SkullMeta skullMeta) {
            if (skullMeta.hasOwner()) {
                skullMeta.setOwner(Hooks.processPlaceholders(user.getPlayer(), skullMeta.getOwner()));
            }
        }
        itemMeta.setLore(processedLore);
        return itemMeta;
    }
}
