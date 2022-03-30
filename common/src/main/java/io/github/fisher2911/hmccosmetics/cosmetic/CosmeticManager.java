package io.github.fisher2911.hmccosmetics.cosmetic;

import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.gui.Token;
import io.github.fisher2911.hmccosmetics.util.Keys;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

public class CosmeticManager {

    private final Map<String, Token> tokenMap;
    private final Map<String, ArmorItem> armorItemMap;

    public CosmeticManager(final Map<String, Token> tokenMap, final Map<String, ArmorItem> armorItemMap) {
        this.tokenMap = tokenMap;
        this.armorItemMap = armorItemMap;
    }

    @Nullable
    public ArmorItem getArmorItem(final String id) {
        return this.armorItemMap.get(id);
    }

    public void addArmorItem(final ArmorItem armorItem) {
        this.armorItemMap.put(armorItem.getId(), armorItem);
    }

    public Collection<ArmorItem> getAllArmorItems() {
        return this.armorItemMap.values();
    }

    public Map<String, ArmorItem> getArmorItemMap() {
        return armorItemMap;
    }

    @Nullable
    public Token getToken(final String id) {
        return this.tokenMap.get(id);
    }

    public void addToken(final Token token) {
        this.tokenMap.put(token.getId(), token);
    }

    public Collection<Token> getAllTokens() {
        return this.tokenMap.values();
    }

    public Map<String, Token> getTokenMap() {
        return this.tokenMap;
    }

    @Nullable
    public Token getToken(final ItemStack itemStack) {
        final String id = Keys.getValue(itemStack, Keys.TOKEN_KEY, PersistentDataType.STRING);
        if (id == null) return null;
        return this.tokenMap.get(id);
    }

    public boolean isToken(final ItemStack itemStack) {
        return Keys.hasKey(itemStack, Keys.TOKEN_KEY, PersistentDataType.STRING);
    }

    public void clearTokens() {
        this.tokenMap.clear();
    }
}
