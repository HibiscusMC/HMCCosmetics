package io.github.fisher2911.hmccosmetics.database.dao;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import io.github.fisher2911.hmccosmetics.cosmetic.CosmeticManager;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;

@DatabaseTable(tableName = "armor_items")
public class ArmorItemDAO {

    @DatabaseField(columnName = "uuid", useGetSet = true, uniqueCombo = true)
    private String uuid;

    @DatabaseField
    private String id;

    @DatabaseField(id = true, useGetSet = true, columnName = "artificial_id")
    private String artificialId;

    @DatabaseField(uniqueCombo = true)
    private String type;

    @DatabaseField(columnName = "color")
    private int rgbDye;

    public ArmorItemDAO(final String id, final String type, final int rgbDye) {
        this.id = id;
        this.artificialId = this.getArtificialId();
        this.type = type;
        this.rgbDye = rgbDye;
    }

    public ArmorItemDAO() {
    }

    public static ArmorItemDAO fromArmorItem(final ArmorItem armorItem) {
        return new ArmorItemDAO(armorItem.getId(), armorItem.getType().toString(), armorItem.getDye());
    }

    @Nullable
    public ArmorItem toArmorItem(final CosmeticManager cosmeticManager) {
        final ArmorItem armorItem = cosmeticManager.getArmorItem(this.id);
        if (armorItem == null) {
            return null;
        }
        final ArmorItem copy = armorItem.copy();
        copy.setDye(this.rgbDye);
        return copy;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    /**
     * ORMLite does not allow more than one primary key (WHYYYY???????????)
     *
     * @return
     */
    public String getArtificialId() {
        return this.uuid + "-" + this.type;
    }

    public void setArtificialId(final String artificialId) {
        this.artificialId = artificialId;
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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ArmorItemDAO that = (ArmorItemDAO) o;
        return Objects.equals(getUuid(), that.getUuid()) && Objects.equals(getType(),
                that.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUuid(), getType());
    }

    @Override
    public String toString() {
        return "ArmorItemDAO{" +
                "uuid='" + uuid + '\'' +
                ", id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", rgbDye=" + rgbDye +
                '}';
    }

}
