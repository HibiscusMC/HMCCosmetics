package io.github.fisher2911.hmccosmetics.user;

import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.inventory.PlayerArmor;
import io.github.fisher2911.hmccosmetics.message.MessageHandler;
import io.github.fisher2911.hmccosmetics.message.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.UUID;

public class User {

    private final UUID uuid;
    private final PlayerArmor playerArmor;
    private ArmorStand attached;
    private ArmorItem lastSetItem;

    public User(final UUID uuid, final PlayerArmor playerArmor) {
        this.uuid = uuid;
        this.playerArmor = playerArmor;
    }

    public @Nullable Player getPlayer() {
        return Bukkit.getPlayer(this.uuid);
    }

    public UUID getUuid() {
        return uuid;
    }

    public PlayerArmor getPlayerArmor() {
        return playerArmor;
    }

    public void setBackpack(final ArmorItem backpack) {
        this.playerArmor.setBackpack(backpack);
        this.lastSetItem = backpack;
    }

    // return true if backpack was set
    public boolean setOrUnsetBackpack(
            final ArmorItem backpack,
            final MessageHandler messageHandler) {

        final Player player = this.getPlayer();

        if (player == null) {
            return false;
        }

        if (backpack.getId().equals(this.playerArmor.getBackpack().getId())) {
            this.setBackpack(new ArmorItem(
                    new ItemStack(Material.AIR),
                    "",
                    new ArrayList<>(),
                    "",
                    ArmorItem.Type.BACKPACK
            ));

            messageHandler.sendMessage(
                    player,
                    Messages.REMOVED_BACKPACK
            );

            return false;
        }

        this.setBackpack(backpack);
        messageHandler.sendMessage(
                player,
                Messages.SET_BACKPACK
        );

        return true;
    }


    public void setHat(final ArmorItem hat, final UserManager userManager) {
        this.playerArmor.setHat(hat);
        this.lastSetItem = hat;
        userManager.updateHat(this);
    }

    // return true if hat was set
    public boolean setOrUnsetHat(
            final ArmorItem hat,
            final MessageHandler messageHandler,
            final UserManager userManager) {

        final Player player = this.getPlayer();

        if (player == null) {
            return false;
        }

        if (hat.getId().equals(this.playerArmor.getHat().getId())) {
            this.setHat(new ArmorItem(
                    new ItemStack(Material.AIR),
                    "",
                    new ArrayList<>(),
                    "",
                    ArmorItem.Type.HAT
            ),
                    userManager);

            messageHandler.sendMessage(
                    player,
                    Messages.REMOVED_HAT
            );

            return false;
        }

        this.setHat(hat, userManager);
        messageHandler.sendMessage(
                player,
                Messages.SET_HAT
        );

        return true;
    }

    public void detach() {
        if (this.attached != null) {
            this.attached.remove();
        }
    }

    // teleports armor stand to the correct position
    public void updateArmorStand() {
        final ArmorItem backpackArmorItem = this.playerArmor.getBackpack();
        if (backpackArmorItem == null ) {
            return;
        }

        final ItemStack backpackItem = backpackArmorItem.getItemStack();

        if (backpackItem == null) {
            return;
        }

        final Player player = this.getPlayer();

        if (player == null) {
            return;
        }

        if (this.attached == null) {
            this.attached = player.getWorld().spawn(player.getLocation(),
                    ArmorStand.class,
                    armorStand -> {
                        armorStand.setVisible(false);
                        armorStand.setMarker(true);
                        player.addPassenger(armorStand);
                    });
        }

        if (!player.getPassengers().contains(this.attached)) {
            player.addPassenger(this.attached);
        }

        final EntityEquipment equipment = this.attached.getEquipment();

        if (!backpackItem.equals(equipment.getHelmet())) {
            equipment.setHelmet(backpackItem);
        }

        this.attached.
                setRotation(
                        player.getLocation().getYaw(),
                        player.getLocation().getPitch());
    }

    public ArmorItem getLastSetItem() {
        return lastSetItem;
    }
}
