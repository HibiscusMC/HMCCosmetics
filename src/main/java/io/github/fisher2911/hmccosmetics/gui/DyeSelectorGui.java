package io.github.fisher2911.hmccosmetics.gui;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.inventory.PlayerArmor;
import io.github.fisher2911.hmccosmetics.message.Placeholder;
import io.github.fisher2911.hmccosmetics.user.User;
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

        for (final var entry : this.cosmeticsSlots.entrySet()) {
            gui.setItem(
                    entry.getKey(),
                    new GuiItem(
                            this.applyPlaceholders(
                                    user,
                                    player,
                                    user.getPlayerArmor().getItem(entry.getValue()),
                                    true
                            )
                    )
            );
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

            this.plugin.getUserManager().setItem(user, armorItem);
            this.updateSelected(user, player);
        });

        return gui;
    }

    private void select(final int slot, final User user, final Player player) {

        final PlayerArmor playerArmor = user.getPlayerArmor();

        final ItemStack previous = this.applyPlaceholders(
                user,
                player,
                playerArmor.getItem(this.cosmeticsSlots.get(this.selectedCosmetic)),
                true
        );

        if (previous != null && previous.getType() != Material.AIR) {
            final ItemStack previousItem = dev.triumphteam.gui.builder.item.ItemBuilder.from(
                    previous
            ).glow(false).build();

            this.gui.updateItem(this.selectedCosmetic, previousItem);
        }

        this.selectedCosmetic = slot;

        this.updateSelected(user, player);
    }

    private void updateSelected(final User user, final Player player) {
        final ArmorItem.Type type = this.cosmeticsSlots.get(this.selectedCosmetic);

        if (type == null) {
            return;
        }

        this.gui.updateItem(this.selectedCosmetic,

                ItemBuilder.from(
                        this.applyPlaceholders(
                                user, player, user.getPlayerArmor().getItem(type), true
                        )
                ).glow(true).build());
    }

    @Override
    public void open(final HumanEntity player) {
        final Optional<User> optionalUser = this.plugin.getUserManager().get(player.getUniqueId());
        optionalUser.ifPresent(
                user -> this.getGui(user, user.getLastSetItem().getType()).open(player));
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
