package io.github.fisher2911.hmccosmetics.cosmetic;

import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import java.util.Collection;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

public class CosmeticManager {

    private final Map<String, ArmorItem> armorItemMap;

    public CosmeticManager(final Map<String, ArmorItem> armorItemMap) {
        this.armorItemMap = armorItemMap;
    }

    @Nullable
    public ArmorItem getArmorItem(final String id) {
        return this.armorItemMap.get(id);
    }

    public void addArmorItem(final ArmorItem armorItem) {
        this.armorItemMap.put(armorItem.getId(), armorItem);
    }

    public Collection<ArmorItem> getAll() {
        return this.armorItemMap.values();
    }

    public Map<String, ArmorItem> getArmorItemMap() {
        return armorItemMap;
    }

}
