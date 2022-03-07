package io.github.fisher2911.hmccosmetics.config;

import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.cosmetic.CosmeticManager;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.gui.Token;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class TokenLoader {

    private static final String FILE_NAME = "tokens.yml";
    private static final String TOKEN_PATH = "tokens";
    private static final String ID_PATH = "id";
    private static final String COMMANDS_PATH = "commands";
    private final HMCCosmetics plugin;
    private final CosmeticManager cosmeticManager;

    public TokenLoader(final HMCCosmetics plugin) {
        this.plugin = plugin;
        this.cosmeticManager = this.plugin.getCosmeticManager();
    }

    public void load() {
        final Path path = Path.of(this.plugin.getDataFolder().getPath(), FILE_NAME);
        final File file = path.toFile();
        if (!file.exists()) {
            this.plugin.saveResource(FILE_NAME, false);
        }
        final YamlConfigurationLoader loader = YamlConfigurationLoader.
                builder().
                defaultOptions(opts -> opts.serializers(build -> build.register(ItemStack.class, ItemSerializer.INSTANCE))).
                path(path).
                build();
        try {
            final ConfigurationNode source = loader.load().node(TOKEN_PATH);
            for (final var entry : source.childrenMap().entrySet()) {
                final var node = entry.getValue();
                final String id = node.node(ID_PATH).getString();
                final ItemStack itemStack = node.get(ItemStack.class);
                final ArmorItem armorItem = this.cosmeticManager.getArmorItem(id);
                if (armorItem == null) {
                    this.plugin.getLogger().severe("Could not find armor item for token: " + id + " with id: " + id);
                    continue;
                }
                final List<String> commands = node.node(COMMANDS_PATH).getList(String.class);
                this.cosmeticManager.addToken(new Token(itemStack, armorItem, commands));
            }
        } catch (final ConfigurateException exception) {
            this.plugin.getLogger().severe("Error loading tokens!");
        }
    }
}
