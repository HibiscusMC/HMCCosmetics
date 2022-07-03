package io.github.fisher2911.hmccosmetics.cosmetic;

import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.config.ArmorItemSerializer;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.gui.Token;
import io.github.fisher2911.hmccosmetics.gui.WrappedGuiItem;
import io.github.fisher2911.hmccosmetics.util.Keys;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

public class CosmeticManager {

    private final Path ITEMS_PATH = Path.of(HMCCosmetics.getPlugin(HMCCosmetics.class).getDataFolder().getPath(), "items.yml");

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

    public void clearItems() {
        this.armorItemMap.clear();
    }

    public void load() {
        this.clearItems();
        try {
            final File file = ITEMS_PATH.toFile();
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            final YamlConfigurationLoader loader = YamlConfigurationLoader.
                    builder().
                    path(ITEMS_PATH).
                    defaultOptions(opts ->
                            opts.serializers(build ->
                                    build.register(WrappedGuiItem.class, ArmorItemSerializer.INSTANCE))
                    )
                    .build();
            for (var node : loader.load().childrenMap().values()) {
                final WrappedGuiItem item = ArmorItemSerializer.INSTANCE.deserialize(WrappedGuiItem.class, node);
                if (item instanceof ArmorItem armorItem) {
                    armorItem.setAction(null);
                    this.armorItemMap.put(armorItem.getId(), armorItem);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
