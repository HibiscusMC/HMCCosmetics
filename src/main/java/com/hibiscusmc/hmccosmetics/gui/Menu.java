package com.hibiscusmc.hmccosmetics.gui;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.serializer.ItemSerializer;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.misc.Adventure;
import com.hibiscusmc.hmccosmetics.util.misc.Placeholder;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Menu {

    private String id;
    private String title;
    private int rows;
    private ConfigurationNode config;

    public Menu(String id, ConfigurationNode config) {
        this.id = id;
        this.config = config;

        title = config.node("title").getString("chest");
        rows = config.node("rows").getInt(1);

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
        Player player = user.getPlayer();
        final Component component = Adventure.MINI_MESSAGE.deserialize(Placeholder.applyPapiPlaceholders(player, this.title));
        Gui gui = Gui.gui().
                title(component).
                rows(this.rows).
                create();

        gui.setDefaultClickAction(event -> event.setCancelled(true));

        gui = getItems(user, gui);

        gui.open(player);

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
                HMCCosmeticsPlugin.getInstance().getLogger().info("Unable to get valid slot for " + config.key().toString());
                continue;
            }

            List<Integer> slots = getSlots(slotString);


            if (slots == null) {
                HMCCosmeticsPlugin.getInstance().getLogger().info("Slot is null for " + config.key().toString());
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
                HMCCosmeticsPlugin.getInstance().getLogger().info("something went wrong! " + item);
                continue;
            }

            GuiItem guiItem = ItemBuilder.from(item).asGuiItem();
            guiItem.setAction(event -> {
                if (config.node("action").virtual()) return;
                /*
                // This is in dire need of an overhaul
                // Idea: [ActionType] string ~Trigger
                // Ex: [EQUIP] niftyHat
                // Ex: [SEND_MESSAGE] You have equipped the nifty hat! ~equip

                List<String> actionStrings = null;
                try {
                    actionStrings = config.node("action").getList(String.class);
                } catch (SerializationException e) {
                    throw new RuntimeException(e);
                }

                for (String actionString : actionStrings) {
                    String[] types = actionString.split("(?<=\\[)(.*?)(?=\\])");
                    if (types.length >= 2 || types.length == 0) continue;
                    String type = types[0];

                    switch (type) {

                    }

                    String[] triggers = actionString.split("(?<=\\~)(.*?)(?=\\ )");
                    if (triggers.length >= 2 || triggers.length == 0) continue;
                    String trigger = triggers[0];

                    switch (trigger) {
                        case ("equip"):
                            if (user.hasCosmeticInSlot())
                    }
                }
                 */

                String action = config.node("action").getString();
                List<String> processedAction = Arrays.asList(action.split(" "));
                if (processedAction.isEmpty()) return;
                if (processedAction.get(0).equalsIgnoreCase("equip")) {
                    Cosmetic cosmetic = Cosmetics.getCosmetic(processedAction.get(1));
                    if (cosmetic == null) return;
                    user.toggleCosmetic(cosmetic);
                    user.updateCosmetic(cosmetic.getSlot());
                    for (int i : slots) {
                        gui.updateItem(i, guiItem);
                    }
                }
            });
            HMCCosmeticsPlugin.getInstance().getLogger().info("Added " + slots + " as " + guiItem + " in the menu");
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
}
