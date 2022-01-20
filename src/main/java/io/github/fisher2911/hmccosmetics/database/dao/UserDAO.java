package io.github.fisher2911.hmccosmetics.database.dao;

import com.j256.ormlite.dao.EagerForeignCollection;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import io.github.fisher2911.hmccosmetics.user.User;

import java.util.List;
import java.util.UUID;

@DatabaseTable(tableName = "user")
public class UserDAO {

    @DatabaseField(id = true)
    private UUID uuid;

    @ForeignCollectionField(eager = true)
    private ForeignCollection<ArmorItemDAO> armorItems;

    public UserDAO() {
    }

    public UserDAO(final UUID uuid, final ForeignCollection<ArmorItemDAO> armorItems) {
        this.uuid = uuid;
        this.armorItems = armorItems;
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
}
