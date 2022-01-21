package io.github.fisher2911.hmccosmetics.database;

import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.logging.Logger;

public class DatabaseFactory {

    private static final String FILE_NAME = "database.yml";
    private static final String TYPE_PATH = "type";
    private static final String NAME_PATH = "name";
    private static final String USERNAME_PATH = "username";
    private static final String PASSWORD_PATH = "password";
    private static final String IP_PATH = "ip";
    private static final String PORT_PATH = "port";

    public static Database create(final HMCCosmetics plugin) throws SQLException {
        final File file = Path.of(plugin.getDataFolder().getPath(), FILE_NAME).toFile();

        if (!file.exists()) {
            plugin.saveResource(FILE_NAME, false);
        }

        final FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        final String type = config.getString(TYPE_PATH);

        final Logger logger = plugin.getLogger();

        if (type == null) {
            logger.severe("Database type was null, disabling plugin.");
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

                final HikariConfig hikari = new HikariConfig();

                final String jdbcUrl = "jdbc:mysql://" + ip + ":" + port + "/" + name;

                hikari.setJdbcUrl(jdbcUrl);
                hikari.setUsername(username);
                hikari.setPassword(password);
                hikari.setConnectionTimeout(1000000000);

                final HikariDataSource source = new HikariDataSource(hikari);

                yield new Database(plugin, new DataSourceConnectionSource(source, jdbcUrl));
            }
            case "sqlite" -> {
                final File folder = new File(plugin.getDataFolder().getPath(), "database");
                folder.mkdirs();
                yield new Database(plugin, new JdbcPooledConnectionSource("jdbc:sqlite:" + new File(
                        folder.getPath(),
                        "users.db"
                ).getPath()));
            }
            default -> null;
        };

        if (database == null) {
            logger.severe("Error loading database, type " + type + " is invalid! Disabling plugin.");
            Bukkit.getPluginManager().disablePlugin(plugin);
        }

        return database;
    }

}
