package io.github.fisher2911.hmccosmetics.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.table.TableUtils;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.database.dao.ArmorItemDAO;
import io.github.fisher2911.hmccosmetics.database.dao.UserDAO;
import io.github.fisher2911.hmccosmetics.user.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Database {

    AtomicInteger ARMOR_STAND_ID = new AtomicInteger(Integer.MAX_VALUE);

    String TABLE_NAME = "user";
    String PLAYER_UUID_COLUMN = "uuid";
    String BACKPACK_COLUMN = "backpack";
    String HAT_COLUMN = "hat";
    String DYE_COLOR_COLUMN = "dye";
    String CREATE_TABLE_STATEMENT =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                    PLAYER_UUID_COLUMN + " CHAR(36), " +
                    BACKPACK_COLUMN + " CHAR(50), " +
                    HAT_COLUMN + " CHAR(50), " +
                    DYE_COLOR_COLUMN + " INT, " +
                    "UNIQUE (" +
                    PLAYER_UUID_COLUMN +
                    "))";

    protected final HMCCosmetics plugin;
    private final DataSourceConnectionSource dataSource;

    public Database(final HMCCosmetics plugin, final DataSourceConnectionSource dataSource) {
        this.plugin = plugin;
        this.dataSource = dataSource;
    }

    public abstract Connection getConnection();

    public void load() {
        this.createTables();
    }
    
    private void createTables() {
        try {
            TableUtils.createTable(this.dataSource, ArmorItemDAO.class);
            TableUtils.createTable(this.dataSource, UserDAO.class);
        } catch (final SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void saveUser(final User u) {
        try {
            final UserDAO user = new UserDAO(u.getUuid());

            final Dao<UserDAO, UUID> userDao = DaoManager.createDao(this.dataSource, UserDAO.class);
            userDao.create(user);
            userDao.assignEmptyForeignCollection(user, "armorItems");
            userDao.create(user);
    } catch (final SQLException exception) {
            exception.printStackTrace();
        }
    }

//    public void saveUser(final User user) {
//
//        try (final PreparedStatement statement = this.getConnection().prepareStatement(this.getSaveStatement())) {
//            final PlayerArmor playerArmor = user.getPlayerArmor();
//            final String hat = playerArmor.getHat().getId();
//            final String backpack = playerArmor.getBackpack().getId();
//
//            statement.setString(1, user.getUuid().toString());
//            statement.setString(2, backpack);
//            statement.setString(3, hat);
//            statement.setString(5, backpack);
//            statement.setString(6, hat);
//
//            statement.executeUpdate();
//        } catch (final SQLException exception) {
//            this.plugin.getLogger().severe("There was in issue saving the player!");
//            exception.printStackTrace();
//        }
//    }
//
//    public User loadUser(final UUID uuid) {
//        final int armorStandId = ARMOR_STAND_ID.getAndDecrement();
//
//        final User blankUser = new User(
//                uuid,
//                PlayerArmor.empty(),
//                armorStandId
//        );
//
//        try (final PreparedStatement statement = this.getConnection().prepareStatement(this.getLoadStatement())) {
//            statement.setString(1, uuid.toString());
//
//            final ResultSet results = statement.executeQuery();
//
//            if (!results.next()) {
//                return blankUser;
//            }
//
//            final String backpackId = results.getString(1);
//            final String hatId = results.getString(2);
//            final int dye = results.getInt(3);
//
//            final CosmeticManager manager = this.plugin.getCosmeticManager();
//
//            ArmorItem backpack = manager.getArmorItem(backpackId);
//            ArmorItem hat = manager.getArmorItem(hatId);
//
//            if (backpack == null) backpack = ArmorItem.empty(ArmorItem.Type.BACKPACK);
//            if (hat == null) hat = ArmorItem.empty(ArmorItem.Type.HAT);
//
//            return new User(
//                    uuid,
//                    new PlayerArmor(
//                            hat,
//                            backpack,
//                            // todo
//                            null
//                    ),
//                    armorStandId
//            );
//
//        } catch (final SQLException exception) {
//            exception.printStackTrace();
//            return blankUser;
//        }
//    }


    public abstract void close();

    public abstract String getSaveStatement();

    public abstract String getLoadStatement();
}
