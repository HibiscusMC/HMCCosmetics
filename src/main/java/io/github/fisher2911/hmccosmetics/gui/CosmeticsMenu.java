package io.github.fisher2911.hmccosmetics.gui;

import dev.triumphteam.gui.guis.GuiItem;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.config.DyeGuiSerializer;
import io.github.fisher2911.hmccosmetics.config.GuiSerializer;
import io.github.fisher2911.hmccosmetics.config.ItemSerializer;
import io.github.fisher2911.hmccosmetics.cosmetic.CosmeticManager;
import io.github.fisher2911.hmccosmetics.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class CosmeticsMenu {

    public static final String DEFAULT_MAIN_MENU = "main";
    public static final String DEFAULT_DYE_MENU = "dye-menu";

    private final HMCCosmetics plugin;
    private final CosmeticManager cosmeticManager;

    private final Map<String, CosmeticGui> guiMap = new HashMap<>();

    public CosmeticsMenu(final HMCCosmetics plugin) {
        this.plugin = plugin;
        this.cosmeticManager = this.plugin.getCosmeticManager();
    }

    public void openMenu(final String id, final HumanEntity humanEntity) {
        final CosmeticGui cosmeticGui = this.getGui(id);

        if (cosmeticGui instanceof final DyeSelectorGui dyeSelectorGui) {
            final Optional<User> optionalUser = this.plugin.getUserManager().get(humanEntity.getUniqueId());
            optionalUser.ifPresent(user -> dyeSelectorGui.getGui(user, user.getLastSetItem().getType()).open(humanEntity));
            return;
        }

        if (cosmeticGui != null) {
            cosmeticGui.open(humanEntity);
        }
    }

    public void openDefault(final HumanEntity humanEntity) {
        this.openMenu(DEFAULT_MAIN_MENU, humanEntity);
    }

    public void reload() {
        for (final ArmorItem armorItem : this.cosmeticManager.getAll()) {
            Bukkit.getPluginManager().removePermission(new Permission(armorItem.getPermission()));
        }
        this.load();
    }

    public void openDyeSelectorGui(
            final User user,
            final ArmorItem.Type type) {

        final Player player = user.getPlayer();

        if (player == null) {
            return;
        }

        final CosmeticGui gui = this.getGui(DEFAULT_DYE_MENU);

        if (gui instanceof final DyeSelectorGui dyeSelectorGui) {
            dyeSelectorGui.getGui(user, type).open(player);
        }
    }

    @Nullable
    private CosmeticGui getGui(final String id) {
        final CosmeticGui gui = this.guiMap.get(id);
        if (gui == null) return null;
        return gui.copy();
    }

    private static final String GUI_TYPE = "gui-type";
    private static final String DYE_TYPE = "dye";

    public void load() {
        this.guiMap.clear();
        final File file = Path.of(this.plugin.getDataFolder().getPath(),
                "menus").toFile();

        if (!Path.of(this.plugin.getDataFolder().getPath(),
                "menus",
                DEFAULT_MAIN_MENU + ".yml").toFile().exists()) {
            this.plugin.saveResource(
                    new File("menus", DEFAULT_MAIN_MENU + ".yml").getPath(),
                    false
            );
        }

        if (!Path.of(this.plugin.getDataFolder().getPath(),
        "menus",
                DEFAULT_DYE_MENU + ".yml").toFile().exists()) {
            this.plugin.saveResource(
                    new File("menus", DEFAULT_DYE_MENU + ".yml").getPath(),
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
                                build.register(DyeSelectorGui.class, DyeGuiSerializer.INSTANCE);
                            }))
                    .build();

            try {
                final ConfigurationNode source = loader.load();
                final ConfigurationNode typeNode = source.node(GUI_TYPE);

                final String type;

                if (typeNode != null) {
                    type = typeNode.getString();
                } else {
                    type = "";
                }

                if (id.equals(DEFAULT_DYE_MENU) || DYE_TYPE.equals(type)) {
                    this.guiMap.put(id, DyeGuiSerializer.INSTANCE.deserialize(DyeSelectorGui.class, source));
                    this.plugin.getLogger().info("Loaded dye gui: " + id);
                    continue;
                }

                final CosmeticGui gui = source.get(CosmeticGui.class);

                if (gui == null) continue;

                for (final GuiItem guiItem : gui.guiItemMap.values()) {
                    if (guiItem instanceof final ArmorItem item) {
                        final ArmorItem copy = new ArmorItem(item);
                        copy.setAction(null);
                        this.cosmeticManager.addArmorItem(copy);
                        if (copy.getPermission().isBlank()) continue;
                        Bukkit.getPluginManager().addPermission(new Permission(copy.getPermission()));
                    }
                }

                this.guiMap.put(id, source.get(CosmeticGui.class));
                this.plugin.getLogger().info("Loaded gui: " + id);
            } catch (final ConfigurateException exception) {
                exception.printStackTrace();
            }

        }
    }
}
