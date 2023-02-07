package com.hibiscusmc.hmccosmetics.gui;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.api.PlayerMenuOpenEvent;
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
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.List;

public class Menu {

    private String id;
    private String title;
    private int rows;
    private ConfigurationNode config;
    private String permissionNode;

    public Menu(String id, ConfigurationNode config) {
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

    public void openMenu(CosmeticUser user, boolean ignorePermission) {
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

        gui = getItems(user, gui);

        Gui finalGui = gui;

        PlayerMenuOpenEvent event = new PlayerMenuOpenEvent(user, this);

        Bukkit.getScheduler().runTask(HMCCosmeticsPlugin.getInstance(), () -> {
            Bukkit.getPluginManager().callEvent(event);
        });

        if (event.isCancelled()) {
            return;
        }

        Bukkit.getScheduler().runTask(HMCCosmeticsPlugin.getInstance(), () -> {
            finalGui.open(player);
        });

        //gui.open(player);

    }

    private Gui getItems(CosmeticUser user, Gui gui) {
        Player player = user.getPlayer();

        for (ConfigurationNode config : config.node("items").childrenMap().values()) {

            List<String> slotString = null;
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
                //item = config.node("item").get(ItemStack.class);
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

            ItemStack originalItem = item.clone();
            item = updateLore(user, item, type, config);

            GuiItem guiItem = ItemBuilder.from(item).asGuiItem();

            Type finalType = type;
            guiItem.setAction(event -> {
                final ClickType clickType = event.getClick();
                if (finalType != null) finalType.run(user, config, clickType);

                for (int i : slots) {
                    gui.updateItem(i, updateLore(user, originalItem.clone(), finalType, config));
                    MessagesUtil.sendDebugMessages("Updated slot " + i);
                }
            });

            MessagesUtil.sendDebugMessages("Added " + slots + " as " + guiItem + " in the menu");
            gui.setItem(slots, guiItem);
        }
        return gui;
    }

    private List<Integer> getSlots(List<String> slotString) {
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

    private List<Integer> getSlots(int small, int max) {
        List<Integer> slots = new ArrayList<>();

        for (int i = small; i <= max; i++) slots.add(i);
        return slots;
    }

    private ItemStack updateLore(CosmeticUser user, ItemStack itemStack, Type type, ConfigurationNode config) {
        if (itemStack.hasItemMeta()) {
            itemStack.setItemMeta(type.setLore(user, config, itemStack.getItemMeta()));
        }
        return itemStack;
    }

    public String getPermissionNode() {
        return permissionNode;
    }

    public boolean canOpen(Player player) {
        if (permissionNode.isEmpty()) return true;
        if (player.isOp() || player.hasPermission(permissionNode)) return true;
        return false;
    }
}
