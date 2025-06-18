package com.hibiscusmc.hmccosmetics.gui;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.api.events.PlayerMenuCloseEvent;
import com.hibiscusmc.hmccosmetics.api.events.PlayerMenuOpenEvent;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticHolder;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics;
import com.hibiscusmc.hmccosmetics.gui.type.Type;
import com.hibiscusmc.hmccosmetics.gui.type.Types;
import com.hibiscusmc.hmccosmetics.gui.type.types.TypeCosmetic;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.GuiType;
import dev.triumphteam.gui.components.InventoryProvider;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import lombok.Getter;
import me.lojosho.hibiscuscommons.config.serializer.ItemSerializer;
import me.lojosho.hibiscuscommons.hooks.Hooks;
import me.lojosho.hibiscuscommons.util.AdventureUtils;
import me.lojosho.hibiscuscommons.util.StringUtils;
import me.lojosho.shaded.configurate.ConfigurationNode;
import me.lojosho.shaded.configurate.serialize.SerializationException;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Menu {

    @Getter
    private final String id;
    @Getter
    private final String title;
    @Getter
    private final int rows;
    @Getter
    private final Long cooldown;
    @Getter
    private final ConfigurationNode config;
    @Getter
    private final String permissionNode;
    private final HashMap<Integer, List<MenuItem>> items;
    @Getter
    private final int refreshRate;
    @Getter
    private final boolean shading;

    public Menu(String id, @NotNull ConfigurationNode config) {
        this.id = config.node("id").getString(id);
        this.config = config;

        title = config.node("title").getString("chest");
        rows = config.node("rows").getInt(1);
        cooldown = config.node("click-cooldown").getLong(Settings.getDefaultMenuCooldown());
        permissionNode = config.node("permission").getString("");
        refreshRate = config.node("refresh-rate").getInt(-1);
        shading = config.node("shading").getBoolean(Settings.isDefaultShading());

        items = new HashMap<>();
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

            if (slots.isEmpty()) {
                MessagesUtil.sendDebugMessages("Slot is empty for " + config.key().toString());
                continue;
            }

            ItemStack item;
            try {
                item = ItemSerializer.INSTANCE.deserialize(ItemStack.class, config.node("item"));
            } catch (SerializationException e) {
                MessagesUtil.sendDebugMessages("Unable to get valid item for " + config.key().toString() + " " + e.getMessage());
                continue;
            }

            if (item == null) {
                MessagesUtil.sendDebugMessages("Something went wrong with the item creation for " + config.key().toString());
                continue;
            }

            int priority = config.node("priority").getInt(1);

            Type type = Types.getDefaultType();
            if (!config.node("type").virtual()) {
                String typeId = config.node("type").getString("");
                if (Types.isType(typeId)) type = Types.getType(typeId);
            }

            for (Integer slot : slots) {
                MenuItem menuItem = new MenuItem(slots, item, type, priority, config);
                if (items.containsKey(slot)) {
                    List<MenuItem> menuItems = items.get(slot);
                    menuItems.add(menuItem);
                    menuItems.sort(priorityCompare);
                    items.put(slot, menuItems);
                } else {
                    items.put(slot, new ArrayList<>(List.of(menuItem)));
                }
            }
        }
    }

    public void openMenu(CosmeticUser user) {
        openMenu(user, false);
    }

    public void openMenu(@NotNull CosmeticUser user, boolean ignorePermission) {
        Player player = user.getPlayer();
        if (player == null) return;
        openMenu(player, user, ignorePermission);
    }

    public void openMenu(@NotNull Player viewer, @NotNull CosmeticHolder cosmeticHolder) {
        openMenu(viewer, cosmeticHolder, false);
    }

    public void openMenu(@NotNull Player viewer, @NotNull CosmeticHolder cosmeticHolder, boolean ignorePermission) {
        if (!ignorePermission && !permissionNode.isEmpty()) {
            if (!viewer.hasPermission(permissionNode) && !viewer.isOp()) {
                MessagesUtil.sendMessage(viewer, "no-permission");
                return;
            }
        }
        final Component component = AdventureUtils.MINI_MESSAGE.deserialize(Hooks.processPlaceholders(viewer, this.title));
        Gui gui = Gui.gui()
                .title(component)
                .type(GuiType.CHEST)
                .inventory((title, owner, type) -> Bukkit.createInventory(owner, rows * 9, title))
                .create();

        gui.setDefaultClickAction(event -> event.setCancelled(true));

        AtomicInteger taskid = new AtomicInteger(-1);
        gui.setOpenGuiAction(event -> {
            Runnable run = () -> {
                if (gui.getInventory().getViewers().isEmpty() && taskid.get() != -1) {
                    Bukkit.getScheduler().cancelTask(taskid.get());
                }

                updateMenu(viewer, cosmeticHolder, gui);
            };

            if (refreshRate != -1) {
                taskid.set(Bukkit.getScheduler().scheduleSyncRepeatingTask(HMCCosmeticsPlugin.getInstance(), run, 0, refreshRate));
            } else {
                run.run();
            }
        });

        gui.setCloseGuiAction(event -> {
            if (cosmeticHolder instanceof CosmeticUser user) {
                PlayerMenuCloseEvent closeEvent = new PlayerMenuCloseEvent(user, this, event.getReason());
                Bukkit.getScheduler().runTask(HMCCosmeticsPlugin.getInstance(), () -> Bukkit.getPluginManager().callEvent(closeEvent));
            }

            if (taskid.get() != -1) Bukkit.getScheduler().cancelTask(taskid.get());
        });

        // API
        if (cosmeticHolder instanceof CosmeticUser user) {
            PlayerMenuOpenEvent event = new PlayerMenuOpenEvent(user, this);
            Bukkit.getScheduler().runTask(HMCCosmeticsPlugin.getInstance(), () -> Bukkit.getPluginManager().callEvent(event));
            if (event.isCancelled()) return;
        }
        // Internal

        Bukkit.getScheduler().runTask(HMCCosmeticsPlugin.getInstance(), () -> {
            gui.open(viewer);
            updateMenu(viewer, cosmeticHolder, gui); // fixes shading? I know I do this twice but it's easier than writing a whole new class to deal with this shit
        });
    }

    private void updateMenu(Player viewer, CosmeticHolder cosmeticHolder, Gui gui) {
        StringBuilder title = new StringBuilder(this.title);

        int row = 0;
        if (shading) {
            for (int i = 0; i < gui.getInventory().getSize(); i++) {
                // Handles the title
                if (i % 9 == 0) {
                    if (row == 0) {
                        title.append(Settings.getFirstRowShift()); // Goes back to the start of the gui
                    } else {
                        title.append(Settings.getSequentRowShift());
                    }
                    row += 1;
                } else {
                    title.append(Settings.getIndividualColumnShift()); // Goes to the next slot
                }

                boolean occupied = false;

                if (items.containsKey(i)) {
                    // Handles the items
                    List<MenuItem> menuItems = items.get(i);
                    MenuItem item = menuItems.get(0);
                    updateItem(viewer, cosmeticHolder, gui, i);

                    if (item.type() instanceof TypeCosmetic) {
                        Cosmetic cosmetic = Cosmetics.getCosmetic(item.itemConfig().node("cosmetic").getString(""));
                        if (cosmetic == null) continue;
                        if (cosmeticHolder.hasCosmeticInSlot(cosmetic)) {
                            title.append(Settings.getEquippedCosmeticColor());
                        } else {
                            if (cosmeticHolder.canEquipCosmetic(cosmetic, true)) {
                                title.append(Settings.getEquipableCosmeticColor());
                            } else {
                                title.append(Settings.getLockedCosmeticColor());
                            }
                        }
                        occupied = true;
                    }
                }
                if (occupied) {
                    title.append(Settings.getBackground().replaceAll("<row>", String.valueOf(row)));
                } else {
                    title.append(Settings.getClearBackground().replaceAll("<row>", String.valueOf(row)));
                }
            }
            MessagesUtil.sendDebugMessages("Updated menu with title " + title);
            gui.updateTitle(AdventureUtils.MINI_MESSAGE.deserialize(Hooks.processPlaceholders(viewer, title.toString())));
        } else {
            for (int i = 0; i < gui.getInventory().getSize(); i++) {
                if (items.containsKey(i)) {
                    updateItem(viewer, cosmeticHolder, gui, i);
                }
            }
        }
    }

    private void updateItem(Player viewer, CosmeticHolder cosmeticHolder, Gui gui, int slot) {
        if (!items.containsKey(slot)) return;
        List<MenuItem> menuItems = items.get(slot);
        if (menuItems.isEmpty()) return;

        for (MenuItem item : menuItems) {
            Type type = item.type();
            ItemStack modifiedItem = getMenuItem(viewer, cosmeticHolder, type, item.itemConfig(), item.item().clone(), slot);
            if (modifiedItem.getType().isAir()) continue;
            GuiItem guiItem = ItemBuilder.from(modifiedItem).asGuiItem();
            guiItem.setAction(event -> {
                UUID uuid = viewer.getUniqueId();
                if (Settings.isMenuClickCooldown()) {
                    Long userCooldown = Menus.getCooldown(uuid);
                    if (userCooldown != 0 && (System.currentTimeMillis() - Menus.getCooldown(uuid) <= getCooldown())) {
                        MessagesUtil.sendDebugMessages("Cooldown for " + viewer.getUniqueId() + " System time: " + System.currentTimeMillis() + " Cooldown: " + Menus.getCooldown(viewer.getUniqueId()) + " Difference: " + (System.currentTimeMillis() - Menus.getCooldown(viewer.getUniqueId())));
                        MessagesUtil.sendMessage(viewer, "on-click-cooldown");
                        return;
                    } else {
                        Menus.addCooldown(uuid, System.currentTimeMillis());
                    }
                }
                MessagesUtil.sendDebugMessages("Updated Menu Item in slot number " + slot);
                final ClickType clickType = event.getClick();
                if (type != null) type.run(viewer, cosmeticHolder, item.itemConfig(), clickType);
                updateMenu(viewer, cosmeticHolder, gui);
            });

            MessagesUtil.sendDebugMessages("Set an item in slot " + slot + " in the menu of " + getId());
            gui.updateItem(slot, guiItem);
            break;
        }
    }

    @NotNull
    private List<Integer> getSlots(@NotNull List<String> slotString) {
        List<Integer> slots = new ArrayList<>();

        for (String a : slotString) {
            if (a.contains("-")) {
                String[] split = a.split("-");
                int min = Integer.parseInt(split[0]);
                int max = Integer.parseInt(split[1]);
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

    @Contract("_, _, _, _, _, _ -> param4")
    @NotNull
    private ItemStack getMenuItem(Player viewer, CosmeticHolder cosmeticHolder, Type type, ConfigurationNode config, ItemStack itemStack, int slot) {
        if (!itemStack.hasItemMeta()) return itemStack;
        return type.setItem(viewer, cosmeticHolder, config, itemStack, slot);
    }

    public boolean canOpen(Player player) {
        if (permissionNode.isEmpty()) return true;
        return player.isOp() || player.hasPermission(permissionNode);
    }

    public static Comparator<MenuItem> priorityCompare = Comparator.comparing(MenuItem::priority).reversed();
}
