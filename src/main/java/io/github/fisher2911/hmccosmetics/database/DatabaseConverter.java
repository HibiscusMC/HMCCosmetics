package io.github.fisher2911.hmccosmetics.database;

import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.cosmetic.CosmeticManager;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.inventory.PlayerArmor;
import io.github.fisher2911.hmccosmetics.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class DatabaseConverter {

    private final HMCCosmetics plugin;
    private final Database database;
    private static final int CURRENT_VERSION = 2;

    public DatabaseConverter(final HMCCosmetics plugin, final Database database) {
        this.database = database;
        this.plugin = plugin;
    }

    private static final String FILE_NAME = "info.yml";

    public void convert() {
        final File folder = new File(this.plugin.getDataFolder(), "database");

        final File file = Path.of(
                folder.getPath(),
                FILE_NAME
        ).toFile();

        final boolean fileExists = file.exists();

        if (!file.exists()) {
            this.plugin.saveResource("database" + File.separator + FILE_NAME, true);
        }

        final YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        final int version = fileExists ? config.getInt("version") : 1;

        final Set<User> users = new HashSet<>();

        this.convert(version, users::add);

        try {
            config.set("version", CURRENT_VERSION);
            config.save(file);
        } catch (final IOException exception) {
            exception.printStackTrace();
        }

        this.database.createTables();

        for (final User user : users) database.saveUser(user);
    }

    private void convert(final int version, final Consumer<User> consumer) {
        switch (version) {
            case 1 -> this.convertVersionOne(consumer);
        }
    }

    private void convertVersionOne(final Consumer<User> consumer) {
        final String query = "SELECT * from user";

        try (final PreparedStatement statement = this.database.getDataSource().getReadOnlyConnection("user").
                getUnderlyingConnection().prepareStatement(query)) {
            final ResultSet results = statement.executeQuery();
            try {

                final Map<String, ArmorItem> armorItems = new ConcurrentHashMap<>(this.plugin.getCosmeticManager().getArmorItemMap());

                while (results.next()) {
                    final PlayerArmor playerArmor = PlayerArmor.empty();
                    final User user = new User
                            (UUID.fromString(results.getString(1)),
                                    playerArmor,
                                    this.database.ARMOR_STAND_ID.getAndDecrement()
                            );
                    final String backpackId = results.getString(2);
                    final String hatId = results.getString(3);
                    final int hatDye = results.getInt(4);

                    final ArmorItem backpack = armorItems.get(backpackId);
                    final ArmorItem hat = armorItems.get(hatId);
                    if (backpack != null) playerArmor.setItem(backpack);
                    if (hat != null) {
                        hat.setDye(hatDye);
                        playerArmor.setItem(hat);
                    }

                    consumer.accept(user);
                }
            } catch (final SQLException exception) {
                exception.printStackTrace();
            }
        } catch (final SQLException exception) {
            exception.printStackTrace();
        }

        try (final PreparedStatement dropStatement = this.database.getDataSource().getReadWriteConnection("user").
                getUnderlyingConnection().prepareStatement("DROP TABLE user")) {
            dropStatement.executeUpdate();
        } catch (final SQLException exception) {
            exception.printStackTrace();
        }
    }
}
