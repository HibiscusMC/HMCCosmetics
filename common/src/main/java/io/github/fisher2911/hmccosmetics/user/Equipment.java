package io.github.fisher2911.hmccosmetics.user;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import io.github.fisher2911.hmccosmetics.packet.PacketManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Equipment {

    private static final EquipmentSlot[] VALUES = EquipmentSlot.values();
    private final HashMap<EquipmentSlot, ItemStack> equipment = new HashMap<>();

    public Equipment() {
    }

    public static Equipment fromEntityEquipment(@Nullable final EntityEquipment entityEquipment) {
        if (entityEquipment == null) return new Equipment();
        final Equipment equipment = new Equipment();
        for (final EquipmentSlot slot : VALUES) {
            equipment.setItem(slot, entityEquipment.getItem(slot));
        }
        return equipment;
    }

    public static Equipment fromEntityEquipment(@Nullable final Player player) {
        if (player == null) return new Equipment();
        final Equipment equipment = new Equipment();
        for (final EquipmentSlot slot : VALUES) {
            equipment.setItem(slot, player.getInventory().getItem(slot));
        }
        return equipment;
    }

    public static Equipment fromEntityEquipment(@Nullable final User user) {
        if (user == null) return new Equipment();
        final Equipment equipment = new Equipment();
        for (final EquipmentSlot slot : VALUES) {
            equipment.setItem(slot, user.getPlayer().getInventory().getItem(slot));
        }
        return equipment;
    }

    @Nullable
    public ItemStack getItem(final EquipmentSlot slot) {
        return this.equipment.get(slot);
    }

    public void setItem(final EquipmentSlot slot, @Nullable final ItemStack itemStack) {
        this.equipment.put(slot, itemStack);
    }

    public EquipmentSlot[] values() {
        return VALUES;
    }

    public Set<EquipmentSlot> keys() {
        return equipment.keySet();
    }

    public void removeSlot(EquipmentSlot slot) {
        equipment.remove(slot);
    }

    public void clear() {
        equipment.clear();
    }
}
