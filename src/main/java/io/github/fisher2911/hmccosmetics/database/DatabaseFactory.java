package io.github.fisher2911.hmccosmetics.database;

import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.nio.file.Path;
import java.util.logging.Logger;

public class DatabaseFactory {

    private static final String FILE_NAME = "database.yml";
    private static final String TYPE_PATH = "type";
    private static final String NAME_PATH = "name";
    private static final String USERNAME_PATH = "username";
    private static final String PASSWORD_PATH = "password";
    private static final String IP_PATH = "ip";
    private static final String PORT_PATH = "port";

    public static Database create(final HMCCosmetics plugin) {
        final File file = Path.of(plugin.getDataFolder().getPath(), FILE_NAME).toFile();

        if (!file.exists()) {
            plugin.saveResource(FILE_NAME, false);
        }

        final FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        final String type = config.getString(TYPE_PATH);

        final Logger logger = plugin.getLogger();

        if (type == null) {
            Bukkit.getPluginManager().disablePlugin(plugin);
            throw new NullPointerException();
        }

        final Database database = switch (type.toLowerCase()) {
            case "mysql" -> {
                final String name = config.getString(NAME_PATH);
                final String username = config.getString(USERNAME_PATH);
                final String password = config.getString(PASSWORD_PATH);
                final String ip = config.getString(IP_PATH);
                final String port = config.getString(PORT_PATH);

                yield new MySQLDatabase(
                        plugin,
                        name,
                        username,
                        password,
                        ip,
                        port
                );
            }
            case "sqlite" -> new SQLiteDatabase(plugin);
            default -> null;
        };

        if (database == null) {
            logger.severe("Error loading database, type " + type + " is invalid!");
            Bukkit.getPluginManager().disablePlugin(plugin);
        }

        return database;
    }

}
