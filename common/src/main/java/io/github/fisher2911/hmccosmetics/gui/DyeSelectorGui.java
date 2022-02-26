package io.github.fisher2911.hmccosmetics.gui;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.inventory.PlayerArmor;
import io.github.fisher2911.hmccosmetics.message.Placeholder;
import io.github.fisher2911.hmccosmetics.user.User;
import io.github.fisher2911.hmccosmetics.user.Wardrobe;
import io.github.fisher2911.hmccosmetics.util.builder.ItemBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class DyeSelectorGui extends CosmeticGui {

    private final BiMap<Integer, ArmorItem.Type> cosmeticsSlots;
    private int selectedCosmetic;

    public DyeSelectorGui(
            final HMCCosmetics plugin,
            final String title,
            final int rows,
            final Map<Integer, GuiItem> guiItemMap,
            final BiMap<Integer, ArmorItem.Type> cosmeticsSlots,
            final int selectedCosmetic
    ) {
        super(plugin, title, rows, guiItemMap);
        this.cosmeticsSlots = cosmeticsSlots;
        this.selectedCosmetic = selectedCosmetic;
    }

    public Gui getGui(final User user) {
        return this.getGui(user, null);
    }

    public Gui getGui(final User user, @Nullable final ArmorItem.Type type) {
        this.gui = Gui.gui().
                title(Component.text(
                        Placeholder.applyPapiPlaceholders(user.getPlayer(), this.title))).
                rows(rows).
                create();

        final Player player = user.getPlayer();

        if (type != null) {
            final Integer selected = this.cosmeticsSlots.inverse().get(type);
            this.selectedCosmetic = selected == null ? this.selectedCosmetic : selected;
        }

        for (final var entry : this.guiItemMap.entrySet()) {

            final GuiItem guiItem = entry.getValue();

            final ItemStack itemStack = this.itemStackMap.get(entry.getKey());

            if (itemStack == null) {
                continue;
            }

            guiItem.setItemStack(
                    ItemBuilder.from(itemStack.clone()).papiPlaceholders(player).build()
            );

            gui.setItem(entry.getKey(), guiItem);
        }

        for (final var entry : this.cosmeticsSlots.entrySet()) {

            final ArmorItem guiItem = user.getPlayerArmor().getItem(entry.getValue()).copy();

            final ItemStack itemStack = guiItem.getItemStack();

            if (itemStack == null || guiItem.isEmpty()) continue;

            guiItem.setItemStack(
                    ItemBuilder.from(
                            this.applyPlaceholders(
                                    user, player, guiItem, true
                            )
                    ).build()
            );

            gui.setItem(entry.getKey(), guiItem);
        }

        final PlayerArmor playerArmor = user.getPlayerArmor();

        this.select(this.selectedCosmetic, user, player);

        gui.setDefaultClickAction(event -> {
            event.setCancelled(true);

            final ArmorItem armorItem = playerArmor.getItem(
                    this.cosmeticsSlots.get(this.selectedCosmetic)
            );

            if (armorItem == null) {
                return;
            }

            final ItemStack itemStack = playerArmor.getItem(type).getItemStack();

            if (itemStack == null) {
                return;
            }

            final int slot = event.getSlot();

            final ArmorItem.Type clickedType = this.cosmeticsSlots.get(slot);

            if (clickedType != null) {
                this.select(slot, user, player);
                return;
            }

            if (!armorItem.isDyeable()) {
                return;
            }

            final GuiItem guiItem = this.guiItemMap.get(slot);

            if (!(guiItem instanceof ColorItem colorItem)) {
                return;
            }

            armorItem.setDye(colorItem.getColor().asRGB());

            if (user.isWardrobeActive()) {
                this.plugin.getUserManager().setItem(user.getWardrobe(), armorItem);
            } else {
                this.plugin.getUserManager().setItem(user, armorItem);
            }
            colorItem.getAction().execute(event);
            this.updateSelected(user, player);
        });

        return gui;
    }

    private void select(final int slot, final User user, final Player player) {

        final PlayerArmor playerArmor = user.getPlayerArmor();

        final ArmorItem previousArmorItem = playerArmor.getItem(this.cosmeticsSlots.get(this.selectedCosmetic));

        final ItemStack previous = this.applyPlaceholders(
                user,
                player,
                previousArmorItem,
                true
        );

        if (previous != null && !previousArmorItem.isEmpty()) {
            final ItemStack previousItem = dev.triumphteam.gui.builder.item.ItemBuilder.from(
                    previous
            ).build();

            this.gui.updateItem(this.selectedCosmetic, previousItem);
        } else {
            final GuiItem guiItem = this.guiItemMap.get(this.selectedCosmetic);
            final ItemStack itemStack = this.itemStackMap.get(this.selectedCosmetic);
            if (itemStack != null && guiItem != null) {
                final GuiItem setItem = this.getGuiItem(
                        user,
                        player,
                        guiItem,
                        itemStack
                );
                if (setItem != null) this.gui.updateItem(this.selectedCosmetic, setItem);
            }
        }

        this.selectedCosmetic = slot;

        this.updateSelected(user, player);
    }

    private void updateSelected(final User user, final Player player) {
        final ArmorItem.Type type = this.cosmeticsSlots.get(this.selectedCosmetic);

        if (type == null) {
            return;
        }

        final ArmorItem armorItem = user.getPlayerArmor().getItem(type);
        if (armorItem.isEmpty()) return;

        this.gui.updateItem(
                this.selectedCosmetic,
                ItemBuilder.from(
                        this.applyPlaceholders(
                                user, player, armorItem, true
                        )
                ).build());
    }

    @Override
    public void open(final User user, final Player player) {
        this.getGui(user, user.getLastSetItem().getType()).open(player);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public DyeSelectorGui copy() {
        return new DyeSelectorGui(
                this.plugin,
                super.title,
                super.rows,
                new HashMap<>(super.guiItemMap),
                HashBiMap.create(this.cosmeticsSlots),
                this.selectedCosmetic
        );
    }

}
