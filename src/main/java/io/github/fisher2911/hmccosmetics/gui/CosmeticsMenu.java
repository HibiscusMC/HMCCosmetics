package io.github.fisher2911.hmccosmetics.gui;

import dev.triumphteam.gui.guis.GuiItem;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.config.GuiSerializer;
import io.github.fisher2911.hmccosmetics.config.ItemSerializer;
import org.bukkit.entity.HumanEntity;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class CosmeticsMenu {

    public static final String MAIN_MENU = "main";

    private final HMCCosmetics plugin;

    private final Map<String, CosmeticGui> guiMap = new HashMap<>();

    public CosmeticsMenu(final HMCCosmetics plugin) {
        this.plugin = plugin;
    }

    public void openMenu(final String id, final HumanEntity humanEntity) {
        final CosmeticGui cosmeticGui = this.guiMap.get(id);

        if (cosmeticGui != null) {
            cosmeticGui.open(humanEntity);
        }
    }

    public void openDefault(final HumanEntity humanEntity) {
        this.openMenu(MAIN_MENU, humanEntity);
    }

    public void load() {
        final File file = Path.of(this.plugin.getDataFolder().getPath(),
                "menus").toFile();

        if (!Path.of(this.plugin.getDataFolder().getPath(),
                "menus",
                "main").toFile().exists()) {
            this.plugin.saveResource(
                    new File("menus", "main.yml").getPath(),
                    false
            );
        }

        if (!file.exists() ||
                !file.isDirectory()) {
            this.plugin.getLogger().severe("No directory found");
            return;
        }

        final File[] files = file.listFiles();

        if (files == null) {
            this.plugin.getLogger().severe("Files are null");
            return;
        }

        for (final File guiFile : files) {
            final String id = guiFile.getName().replace(".yml", "");

            final YamlConfigurationLoader loader = YamlConfigurationLoader.
                    builder().
                    path(Path.of(guiFile.getPath())).
                    defaultOptions(opts ->
                            opts.serializers(build -> {
                                build.register(GuiItem.class, ItemSerializer.INSTANCE);
                                build.register(CosmeticGui.class, GuiSerializer.INSTANCE);
                            }))
                    .build();

            try {
                final ConfigurationNode source = loader.load();

                this.guiMap.put(id, source.get(CosmeticGui.class));
                this.plugin.getLogger().severe("Loaded gui: " + id);
            } catch (final ConfigurateException exception) {
                exception.printStackTrace();
            }

        }
    }
}
