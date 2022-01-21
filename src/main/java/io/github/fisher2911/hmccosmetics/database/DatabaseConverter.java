package io.github.fisher2911.hmccosmetics.database;

import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class DatabaseConverter {

    private final HMCCosmetics plugin;
    private final Database database;

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


        if (!file.exists()) {
            this.plugin.saveResource("database" + File.separator + FILE_NAME, true);
        }

        final int version = YamlConfiguration.loadConfiguration(file).getInt("version");

        this.convert(version);
    }

    private void convert(final int version) {

    }

    private void convertVersionOne() {
//        switch (this.database.getDatabaseType()) {
//
//        }
        final String query = "SELECT * from user";
        try (final PreparedStatement statement = this.database.getDataSource().getReadOnlyConnection("user").
                getUnderlyingConnection().prepareStatement(query)) {
            final ResultSet results = statement.executeQuery();
            while (results.next()) {
                System.out.println(results.getObject(1, UUID.class));
                System.out.println(results.getString(2));
                System.out.println(results.getString(3));
                System.out.println(results.getInt(4));
            }
        } catch (final SQLException exception) {
            exception.printStackTrace();
        }
    }
}
