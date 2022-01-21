package io.github.fisher2911.hmccosmetics.gui;

import com.google.common.collect.BiMap;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.database.dao.ArmorItemDAO;
import io.github.fisher2911.hmccosmetics.inventory.PlayerArmor;
import io.github.fisher2911.hmccosmetics.user.User;
import io.github.fisher2911.hmccosmetics.util.StringUtils;
import io.github.fisher2911.hmccosmetics.util.builder.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

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
                title(Component.text(StringUtils.applyPapiPlaceholders(user.getPlayer(), this.title))).
                rows(rows).
                create();

        final Player player = user.getPlayer();
        player.sendMessage("Getting Gui");

        if (type != null) {
            final Integer selected = this.cosmeticsSlots.inverse().get(type);
            this.selectedCosmetic = selected == null ? this.selectedCosmetic : selected;
        }

        for (final var entry : this.cosmeticsSlots.entrySet()) {
            gui.setItem(entry.getKey(), user.getPlayerArmor().getItem(entry.getValue()));
        }

        for (final var entry : this.guiItemMap.entrySet()) {

            final GuiItem guiItem = entry.getValue();

            final ItemStack itemStack = this.itemStackMap.get(entry.getKey());

            if (itemStack == null) continue;

            guiItem.setItemStack(
                    ItemBuilder.from(itemStack.clone()).papiPlaceholders(player).build()
            );

            gui.setItem(entry.getKey(), guiItem);
        }

        final PlayerArmor playerArmor = user.getPlayerArmor();

        final ArmorItem armorItem = playerArmor.getItem(
                this.cosmeticsSlots.get(this.selectedCosmetic)
        );

        this.select(this.selectedCosmetic);

        gui.setDefaultClickAction(event -> {
            event.setCancelled(true);

            if (armorItem == null) {
                return;
            }

            final ItemStack itemStack = playerArmor.getItem(type).getItemStack();

            if (itemStack == null) {
                return;
            }

            if (!armorItem.isDyeable()) {
                return;
            }

            final int slot = event.getSlot();

            final ArmorItem.Type clickedType = this.cosmeticsSlots.get(slot);

            if (clickedType != null) {
                this.select(slot);
                return;
            }

            final GuiItem guiItem = this.guiItemMap.get(slot);

            if (!(guiItem instanceof ColorItem colorItem)) {
                return;
            }

            armorItem.setDye(colorItem.getColor().asRGB());

            this.userManager.setItem(user, armorItem);
        });

        return gui;
    }

    private void select(final int slot) {
        ItemStack itemStack = this.itemStackMap.get(slot);
        ItemStack previous = this.itemStackMap.get(this.selectedCosmetic);

        if (itemStack == null) return;

        itemStack =
                dev.triumphteam.gui.builder.item.ItemBuilder.from(
                        itemStack).glow().build();

        if (previous != null && this.selectedCosmetic != slot) {
            previous = dev.triumphteam.gui.builder.item.ItemBuilder.from(
                    previous).glow(false).build();
            this.gui.updateItem(this.selectedCosmetic, previous);
        }

        this.gui.updateItem(slot, itemStack);

        this.selectedCosmetic = slot;
    }

    @Override
    public void open(final HumanEntity player) {
        final Optional<User> optionalUser = this.plugin.getUserManager().get(player.getUniqueId());
        optionalUser.ifPresent(user -> this.getGui(user, user.getLastSetItem().getType()).open(player));
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
