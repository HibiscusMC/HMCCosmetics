package com.hibiscusmc.hmccosmetics.gui;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.api.events.PlayerMenuOpenEvent;
import com.hibiscusmc.hmccosmetics.config.serializer.ItemSerializer;
import com.hibiscusmc.hmccosmetics.gui.type.Type;
import com.hibiscusmc.hmccosmetics.gui.type.Types;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.misc.Adventure;
import com.hibiscusmc.hmccosmetics.util.misc.Placeholder;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.List;

public class Menu {

    private final String id;
    private final String title;
    private final int rows;
    private final ConfigurationNode config;
    private final String permissionNode;

    public Menu(String id, @NotNull ConfigurationNode config) {
        this.id = id;
        this.config = config;

        title = config.node("title").getString("chest");
        rows = config.node("rows").getInt(1);
        permissionNode = config.node("permission").getString("");

        Menus.addMenu(this);
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return this.title;
    }

    public int getRows() {
        return this.getRows();
    }

    public void openMenu(CosmeticUser user) {
        openMenu(user, false);
    }

    public void openMenu(@NotNull CosmeticUser user, boolean ignorePermission) {
        Player player = user.getPlayer();
        if (player == null) return;
        if (!ignorePermission && !permissionNode.isEmpty()) {
            if (!player.hasPermission(permissionNode) && !player.isOp()) {
                MessagesUtil.sendMessage(player, "no-permission");
                return;
            }
        }
        final Component component = Adventure.MINI_MESSAGE.deserialize(Placeholder.applyPapiPlaceholders(player, this.title));
        Gui gui = Gui.gui().
                title(component).
                rows(this.rows).
                create();

        gui.setDefaultClickAction(event -> event.setCancelled(true));

        // TODO: Redo this whole gui creation process to allow for all items, possibly implement caching
        gui = getItems(user, gui);
        final Gui finalGui = gui; // Need to make it final for the runtask

        // API
        PlayerMenuOpenEvent event = new PlayerMenuOpenEvent(user, this);
        Bukkit.getScheduler().runTask(HMCCosmeticsPlugin.getInstance(), () -> {
            Bukkit.getPluginManager().callEvent(event);
        });
        if (event.isCancelled()) return;
        // Internal

        Bukkit.getScheduler().runTask(HMCCosmeticsPlugin.getInstance(), () -> {
            finalGui.open(player);
        });
    }

    @Contract("_, _ -> param2")
    private Gui getItems(@NotNull CosmeticUser user, Gui gui) {
        Player player = user.getPlayer();

        for (ConfigurationNode config : config.node("items").childrenMap().values()) {

            List<String> slotString;
            try {
                slotString = config.node("slots").getList(String.class);
            } catch (SerializationException e) {
                continue;
            }
            if (slotString == null) {
                MessagesUtil.sendDebugMessages("Unable to get valid slot for " + config.key().toString());
                continue;
            }

            List<Integer> slots = getSlots(slotString);

            if (slots == null) {
                MessagesUtil.sendDebugMessages("Slot is null for " + config.key().toString());
                continue;
            }

            ItemStack item;
            try {
                item = ItemSerializer.INSTANCE.deserialize(ItemStack.class, config.node("item"));
            } catch (SerializationException e) {
                throw new RuntimeException(e);
            }

            if (item == null) {
                MessagesUtil.sendDebugMessages("something went wrong! " + item);
                continue;
            }

            Type type = null;

            if (!config.node("type").virtual()) {
                String typeId = config.node("type").getString();
                if (Types.isType(typeId)) type = Types.getType(typeId);
            }

            for (int slot : slots) {
                ItemStack modifiedItem = getMenuItem(user, type, config, item.clone(), slot).clone();
                GuiItem guiItem = ItemBuilder.from(modifiedItem).asGuiItem();

                Type finalType = type;
                guiItem.setAction(event -> {
                    MessagesUtil.sendDebugMessages("Selected slot " + slot);
                    final ClickType clickType = event.getClick();
                    if (finalType != null) finalType.run(user, config, clickType);
                    // Need to delay the update by a tick so it will actually update with new values
                    for (int guiSlot : slots) {
                        gui.updateItem(guiSlot, getMenuItem(user, finalType, config, item.clone(), guiSlot));
                    }
                    MessagesUtil.sendDebugMessages("Updated slot " + slot);
                });

                MessagesUtil.sendDebugMessages("Added " + slots + " as " + guiItem + " in the menu");
                gui.setItem(slot, guiItem);
            }
        }
        return gui;
    }

    @NotNull
    private List<Integer> getSlots(@NotNull List<String> slotString) {
        List<Integer> slots = new ArrayList<>();

        for (String a : slotString) {
            if (a.contains("-")) {
                String[] split = a.split("-");
                int min = Integer.valueOf(split[0]);
                int max = Integer.valueOf(split[1]);
                slots.addAll(getSlots(min, max));
            } else {
                slots.add(Integer.valueOf(a));
            }
        }

        return slots;
    }

    @NotNull
    private List<Integer> getSlots(int small, int max) {
        List<Integer> slots = new ArrayList<>();

        for (int i = small; i <= max; i++) slots.add(i);
        return slots;
    }

    @Contract("_, _, _, _ -> param2")
    @NotNull
    private ItemStack getMenuItem(CosmeticUser user, Type type, ConfigurationNode config, ItemStack itemStack, int slot) {
        if (!itemStack.hasItemMeta()) return itemStack;
        return type.setItem(user, config, itemStack, slot);
    }

    public String getPermissionNode() {
        return permissionNode;
    }

    public boolean canOpen(Player player) {
        if (permissionNode.isEmpty()) return true;
        return player.isOp() || player.hasPermission(permissionNode);
    }
}
