package io.github.fisher2911.hmccosmetics.user;

import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class Equipment {

    private static final EquipmentSlot[] VALUES = EquipmentSlot.values();
    private final Map<EquipmentSlot, ItemStack> equipment = new EnumMap<>(EquipmentSlot.class);

    public Equipment() {
    }

    public static Equipment fromEntityEquipment(final EntityEquipment entityEquipment) {
        final Equipment equipment = new Equipment();
        for (final EquipmentSlot slot : VALUES) {
            equipment.setItem(slot, entityEquipment.getItem(slot));
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
}
