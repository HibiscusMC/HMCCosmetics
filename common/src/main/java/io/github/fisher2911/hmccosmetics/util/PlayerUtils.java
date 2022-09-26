package io.github.fisher2911.hmccosmetics.util;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import io.github.fisher2911.hmccosmetics.user.Equipment;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class PlayerUtils {

    public static WrappedSignedProperty getSkin(Player player) {
        WrappedSignedProperty skinData = WrappedGameProfile.fromPlayer(player).getProperties()
                .get("textures").stream().findAny().orElse(null);

        if (skinData == null)
            throw new RuntimeException("Missing skin data");

        return new WrappedSignedProperty("textures", skinData.getValue(), skinData.getSignature());
    }

    /**
     * Gets the equipment of a slot
     * @param itemStack the ItemStack of a slot
     * @param slot the slot to look at
     * @return returns the equipment at a slot
     */
    // It works now, need to redo this sytem sometime in the future...
    public static Equipment getEquipment(
            final ItemStack itemStack,
            final org.bukkit.inventory.EquipmentSlot slot
    ) {
        Equipment equip = new Equipment();
        equip.setItem(slot, itemStack);
        return equip;
    }
    /*
    public static EquipmentSlot fromBukkitSlot(final org.bukkit.inventory.EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> EquipmentSlot.HELMET;
            case CHEST -> EquipmentSlot.CHEST_PLATE;
            case LEGS -> EquipmentSlot.LEGGINGS;
            case FEET -> EquipmentSlot.BOOTS;
            case HAND -> EquipmentSlot.MAIN_HAND;
            case OFF_HAND -> EquipmentSlot.OFF_HAND;
        };
    }
     */

    /**
     * Converts from the Bukkit item slots to ProtocolLib item slots. Will produce a null if an improper bukkit item slot is sent through
     * @param slot The BUKKIT item slot to convert.
     * @return The ProtocolLib item slot that is returned
     */
    public static EnumWrappers.ItemSlot itemBukkitSlot(final EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> EnumWrappers.ItemSlot.HEAD;
            case CHEST -> EnumWrappers.ItemSlot.CHEST;
            case LEGS -> EnumWrappers.ItemSlot.LEGS;
            case FEET -> EnumWrappers.ItemSlot.FEET;
            case HAND -> EnumWrappers.ItemSlot.MAINHAND;
            case OFF_HAND -> EnumWrappers.ItemSlot.OFFHAND;
        };
    }

    /**
     * Converts a bukkit gamemode into an integer for use in packets
     * @param gamemode Bukkit gamemode to convert.
     * @return int of the gamemode
     */
    public static int convertGamemode(final GameMode gamemode) {
        return switch (gamemode) {
            case SURVIVAL -> 0;
            case CREATIVE -> 1;
            case ADVENTURE -> 2;
            case SPECTATOR -> 3;
        };
    }
}
