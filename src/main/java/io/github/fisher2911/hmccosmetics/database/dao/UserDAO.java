package io.github.fisher2911.hmccosmetics.database.dao;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import io.github.fisher2911.hmccosmetics.cosmetic.CosmeticManager;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.inventory.PlayerArmor;
import io.github.fisher2911.hmccosmetics.user.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

@DatabaseTable(tableName = "user")
public class UserDAO {

    @DatabaseField(id = true)
    private UUID uuid;

    @ForeignCollectionField(eager = true)
    private ForeignCollection<ArmorItemDAO> armorItems;

    public UserDAO() {
    }

    public void setUuid(final UUID uuid) {
        this.uuid = uuid;
    }

    public UserDAO(final UUID uuid) {
        this.uuid = uuid;
    }

    public ForeignCollection<ArmorItemDAO> getArmorItems() {
        return armorItems;
    }

    public void setArmorItems(final ForeignCollection<ArmorItemDAO> armorItems) {
        this.armorItems = armorItems;
    }

    public User toUser(final CosmeticManager cosmeticManager, final int armorStandId) {
        final PlayerArmor playerArmor = PlayerArmor.empty();

        for (final ArmorItemDAO armorItemDao : this.armorItems) {
            final ArmorItem armorItem = armorItemDao.toArmorItem(cosmeticManager);
            if (armorItem == null) continue;
            playerArmor.setItem(armorItem);
        }

        return new User(this.uuid, playerArmor, armorStandId);
    }

    @Override
    public String toString() {
        return "UserDAO{" +
                "uuid=" + uuid +
                ", armorItems=" + armorItems +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final UserDAO userDAO = (UserDAO) o;
        return Objects.equals(uuid, userDAO.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
