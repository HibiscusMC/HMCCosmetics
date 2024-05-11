package com.hibiscusmc.hmccosmetics.config;

import lombok.Getter;
import org.bukkit.inventory.EquipmentSlot;

public class SlotOptionConfig {

    @Getter
    private final EquipmentSlot slot;
    @Getter
    private final boolean addEnchantments;
    @Getter
    private final boolean requireEmpty;

    public SlotOptionConfig(EquipmentSlot slot, boolean addEnchantments, boolean requireEmpty) {
        this.slot = slot;
        this.addEnchantments = addEnchantments;
        this.requireEmpty = requireEmpty;
    }
}
