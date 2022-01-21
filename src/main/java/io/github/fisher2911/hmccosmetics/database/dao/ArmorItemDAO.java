package io.github.fisher2911.hmccosmetics.database.dao;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import io.github.fisher2911.hmccosmetics.cosmetic.CosmeticManager;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@DatabaseTable(tableName = "armor_item")
public class ArmorItemDAO {

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "user", uniqueCombo = true)
    private UserDAO user;

    @DatabaseField
    private String id;

    @DatabaseField(id = true, uniqueCombo = true)
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

    @Nullable
    public ArmorItem toArmorItem(final CosmeticManager cosmeticManager) {
        final ArmorItem armorItem = cosmeticManager.getArmorItem(this.id);
        if (armorItem == null) return null;
        final ArmorItem copy = new ArmorItem(armorItem);
        copy.setDye(this.rgbDye);
        return copy;
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ArmorItemDAO that = (ArmorItemDAO) o;
        return Objects.equals(getUser(), that.getUser()) && Objects.equals(getType(), that.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUser(), getType());
    }
}
