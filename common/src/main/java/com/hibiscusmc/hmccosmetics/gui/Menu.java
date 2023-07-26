package com.hibiscusmc.hmccosmetics.gui;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.api.events.PlayerMenuOpenEvent;
import com.hibiscusmc.hmccosmetics.config.serializer.ItemSerializer;
import com.hibiscusmc.hmccosmetics.gui.type.Type;
import com.hibiscusmc.hmccosmetics.gui.type.Types;
import com.hibiscusmc.hmccosmetics.hooks.Hooks;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.misc.Adventure;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Menu {

    private final String id;
    private final String title;
    private final int rows;
    private final ConfigurationNode config;
    private final String permissionNode;
    private final ArrayList<MenuItem> items;
    private final int refreshRate;

    public Menu(String id, @NotNull ConfigurationNode config) {
        this.id = id;
        this.config = config;

        title = config.node("title").getString("chest");
        rows = config.node("rows").getInt(1);
        permissionNode = config.node("permission").getString("");
        refreshRate = config.node("refresh-rate").getInt(-1);

        items = new ArrayList<>();
        setupItems();

        Menus.addMenu(this);
    }

    private void setupItems() {
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

            items.add(new MenuItem(slots, item, type, config));
        }
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
        final Component component = Adventure.MINI_MESSAGE.deserialize(Hooks.processPlaceholders(player, this.title));
        Gui gui = Gui.gui().
                title(component).
                rows(this.rows).
                create();

        gui.setDefaultClickAction(event -> event.setCancelled(true));

        AtomicInteger taskid = new AtomicInteger(-1);
        gui.setOpenGuiAction(event -> {
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    if (gui.getInventory().getViewers().size() == 0 && taskid.get() != -1) {
                        Bukkit.getScheduler().cancelTask(taskid.get());
                    }

                    updateMenu(user, gui);
                }
            };

            if (refreshRate != -1) {
                taskid.set(Bukkit.getScheduler().scheduleSyncRepeatingTask(HMCCosmeticsPlugin.getInstance(), run, 0, refreshRate));
            } else {
                run.run();
            }
        });

        gui.setCloseGuiAction(event -> {
            if (taskid.get() != -1) Bukkit.getScheduler().cancelTask(taskid.get());
        });

        // API
        PlayerMenuOpenEvent event = new PlayerMenuOpenEvent(user, this);
        Bukkit.getScheduler().runTask(HMCCosmeticsPlugin.getInstance(), () -> {
            Bukkit.getPluginManager().callEvent(event);
        });
        if (event.isCancelled()) return;
        // Internal

        Bukkit.getScheduler().runTask(HMCCosmeticsPlugin.getInstance(), () -> {
            gui.open(player);
        });
    }

    private void updateMenu(CosmeticUser user, Gui gui) {
        for (MenuItem item : items) {
            Type type = item.getType();
            for (int slot : item.getSlots()) {
                ItemStack modifiedItem = getMenuItem(user, type, item.getItemConfig(), item.getItem().clone(), slot);
                GuiItem guiItem = ItemBuilder.from(modifiedItem).asGuiItem();
                guiItem.setAction(event -> {
                    MessagesUtil.sendDebugMessages("Selected slot " + slot);
                    final ClickType clickType = event.getClick();
                    if (type != null) type.run(user, item.getItemConfig(), clickType);
                    updateMenu(user, gui);
                });

                MessagesUtil.sendDebugMessages("Added " + slot + " as " + guiItem + " in the menu");
                gui.updateItem(slot, guiItem);
            }
        }
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
