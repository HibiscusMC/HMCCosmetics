package io.github.fisher2911.hmccosmetics.database.dao;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;

@DatabaseTable(tableName = "armor_item")
public class ArmorItemDAO {

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private UserDAO user;

    @DatabaseField
    private String id;

    @DatabaseField
    private String type;

    @DatabaseField(columnName = "color")
    private int rgbDye;

    public ArmorItemDAO(final String id, final String type, final int rgbDye) {
        this.id = id;
        this.type = type;
        this.rgbDye = rgbDye;
    }

    public static ArmorItemDAO fromArmorItem(final ArmorItem armorItem) {
        return new ArmorItemDAO(armorItem.getId(), armorItem.getType().toString(), armorItem.getDye());
    }

    public ArmorItemDAO() {
    }

    public UserDAO getUser() {
        return user;
    }

    public void setUser(final UserDAO user) {
        this.user = user;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public int getRgbDye() {
        return rgbDye;
    }

    public void setRgbDye(final int rgbDye) {
        this.rgbDye = rgbDye;
    }
}
