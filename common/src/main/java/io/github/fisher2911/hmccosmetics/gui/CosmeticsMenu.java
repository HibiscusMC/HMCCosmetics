package io.github.fisher2911.hmccosmetics.gui;

import dev.triumphteam.gui.guis.GuiItem;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.config.ArmorItemSerializer;
import io.github.fisher2911.hmccosmetics.config.DyeGuiSerializer;
import io.github.fisher2911.hmccosmetics.config.GuiSerializer;
import io.github.fisher2911.hmccosmetics.config.ItemSerializer;
import io.github.fisher2911.hmccosmetics.config.TokenGuiSerializer;
import io.github.fisher2911.hmccosmetics.cosmetic.CosmeticManager;
import io.github.fisher2911.hmccosmetics.user.User;
import io.github.fisher2911.hmccosmetics.user.Wardrobe;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class CosmeticsMenu {

    public static final String DEFAULT_MAIN_MENU = "main";
    public static final String DEFAULT_DYE_MENU = "dye-menu";
    public static final String DEFAULT_TOKEN_MENU = "token-menu";

    private final HMCCosmetics plugin;
    private final CosmeticManager cosmeticManager;

    private final Map<String, CosmeticGui> guiMap = new HashMap<>();

    private final Set<String> registeredPermissions = new HashSet<>();

    public CosmeticsMenu(final HMCCosmetics plugin) {
        this.plugin = plugin;
        this.cosmeticManager = this.plugin.getCosmeticManager();
    }

    public void openMenu(final String id, final HumanEntity humanEntity) {
        if (!(humanEntity instanceof final Player player)) return;
        final CosmeticGui cosmeticGui = this.getGui(id);

        final Optional<User> optionalUser = this.plugin.getUserManager().get(humanEntity.getUniqueId());

        if (optionalUser.isEmpty()) return;

        User user = optionalUser.get();

        final Wardrobe wardrobe = user.getWardrobe();
        if (wardrobe.isActive()) user = wardrobe;

        if (cosmeticGui instanceof final DyeSelectorGui dyeSelectorGui) {
            dyeSelectorGui.getGui(user, user.getLastSetItem().getType()).open(humanEntity);
            user.setOpenGui(dyeSelectorGui);
            return;
        }

        if (cosmeticGui != null) {
            user.setOpenGui(cosmeticGui);
            cosmeticGui.open(user, player);
        }
    }

    public Set<String> getMenus() {
        return this.guiMap.keySet();
    }

    public void openDefault(final HumanEntity humanEntity) {
        this.openMenu(DEFAULT_MAIN_MENU, humanEntity);
    }

    public void reload() {
        for (final ArmorItem armorItem : this.cosmeticManager.getAllArmorItems()) {
            Bukkit.getPluginManager().removePermission(new Permission(armorItem.getPermission()));
        }
        this.load();
    }

    public void openDyeSelectorGui(
            User user,
            final ArmorItem.Type type) {

        final Player player = user.getPlayer();

        if (player == null) {
            return;
        }

        final Wardrobe wardrobe = user.getWardrobe();
        if (wardrobe.isActive()) user = wardrobe;

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
    private static final String TOKEN_TYPE = "token";

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

        if (!Path.of(this.plugin.getDataFolder().getPath(),
                "menus",
                DEFAULT_TOKEN_MENU + ".yml").toFile().exists()) {
            this.plugin.saveResource(
                    new File("menus", DEFAULT_TOKEN_MENU + ".yml").getPath(),
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
                                build.register(WrappedGuiItem.class, ArmorItemSerializer.INSTANCE);
                                build.register(CosmeticGui.class, GuiSerializer.INSTANCE);
                                build.register(DyeSelectorGui.class, DyeGuiSerializer.INSTANCE);
                                build.register(TokenGui.class, TokenGuiSerializer.INSTANCE);
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

                if (TOKEN_TYPE.equals(type)) {
                    this.guiMap.put(id, TokenGuiSerializer.INSTANCE.deserialize(TokenGui.class, source));
                    this.plugin.getLogger().info("Loaded token gui: " + id);
                    continue;
                }

                final CosmeticGui gui = source.get(CosmeticGui.class);

                if (gui == null) continue;

                for (final GuiItem guiItem : gui.guiItemMap.values()) {
                    if (guiItem instanceof final ArmorItem item) {
                        final ArmorItem copy = item.copy();
                        copy.setAction(null);
                        this.cosmeticManager.addArmorItem(copy);
                        final String perm = copy.getPermission();
                        if (perm.isBlank() || this.registeredPermissions.contains(perm)) continue;
                        this.registeredPermissions.add(perm);
                        Bukkit.getPluginManager().addPermission(new Permission(perm));
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
