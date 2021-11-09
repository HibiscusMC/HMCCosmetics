package io.github.fisher2911.hmccosmetics.gui;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import io.github.fisher2911.hmccosmetics.config.ItemSerializer;
import io.github.fisher2911.hmccosmetics.inventory.PlayerArmor;
import io.github.fisher2911.hmccosmetics.user.User;
import io.github.fisher2911.hmccosmetics.util.builder.LeatherArmorBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.checkerframework.checker.units.qual.A;

import java.util.HashMap;
import java.util.Map;

public class DyeSelectorGui {

    private final String title;
    private final int rows;
    private final Map<Integer, Color> itemColors;
    private final ArmorItem armorItem;

    public DyeSelectorGui(
            final String title,
            final int rows,
            final Map<Integer, Color> itemColors,
            final ArmorItem armorItem) {
        this.title = title;
        this.rows = rows;
        this.itemColors = itemColors;
        this.armorItem = armorItem;
    }

    public Gui getGui(final User user) {
        final Gui gui = Gui.gui().
                title(Component.text(this.title)).
                rows(rows).
                create();

        for (final var entry : itemColors.entrySet()) {
            gui.setItem(entry.getKey(), ItemBuilder.from(Material.BLACK_DYE).
                    name(Component.text(
                            String.valueOf(entry.getValue().asRGB()))).asGuiItem());
        }

        gui.setDefaultClickAction(event -> {
            event.setCancelled(true);

            final ArmorItem.Type type = this.armorItem.getType();

            final PlayerArmor playerArmor = user.getPlayerArmor();

            if (playerArmor == null) {
                return;
            }

            final ItemStack itemStack = switch (type) {
                case HAT -> {
                    final ArmorItem hatItem = playerArmor.getHat();

                    if (hatItem == null) {
                        yield null;
                    }
                    yield hatItem.getItemStack();
                }
                case BACKPACK -> {
                    final ArmorItem backpackItem = playerArmor.getBackpack();

                    if (backpackItem == null) {
                        yield null;
                    }
                    yield backpackItem.getItemStack();
                }
            };

            if (itemStack == null) {
                return;
            }

            if (!(itemStack.getItemMeta() instanceof final LeatherArmorMeta itemMeta)) {
                return;
            }

            final LeatherArmorBuilder leatherArmorBuilder =
                    LeatherArmorBuilder.from(itemStack);

            final Color color = this.itemColors.get(event.getSlot());

            if (color == null) {
                return;
            }

            leatherArmorBuilder.color(color);

            final ArmorItem armorItem = new ArmorItem(
                    leatherArmorBuilder.build(),
                    this.armorItem.getAction(),
                    this.armorItem.getId(),
                    this.armorItem.getPermission(),
                    this.armorItem.getType(),
                    this.armorItem.isDyeable()
            );

            switch (type) {
                case HAT -> user.setHat(armorItem);
                case BACKPACK -> user.setBackpack(armorItem);
            }
        });

        return gui;
    }

    public String getTitle() {
        return title;
    }

    public int getRows() {
        return rows;
    }

    public Map<Integer, Color> getItemColors() {
        return itemColors;
    }

    public ArmorItem getArmorItem() {
        return armorItem;
    }
}
