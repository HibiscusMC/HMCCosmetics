package io.github.fisher2911.hmccosmetics.database.dao;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import io.github.fisher2911.hmccosmetics.cosmetic.CosmeticManager;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.inventory.PlayerArmor;
import io.github.fisher2911.hmccosmetics.packet.EntityIds;
import io.github.fisher2911.hmccosmetics.user.BaseUser;
import io.github.fisher2911.hmccosmetics.user.NPCUser;
import io.github.fisher2911.hmccosmetics.user.User;
import io.github.fisher2911.hmccosmetics.user.Wardrobe;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

@DatabaseTable(tableName = "citizen")
public class CitizenDAO {

        @DatabaseField(id = true)
        private int citizensId;

        public CitizenDAO() {
        }

        public CitizenDAO(final int citizensId) {
            this.citizensId = citizensId;
        }

        public void setCitizensId(final int citizensId) {
            this.citizensId = citizensId;
        }

        @Nullable
        public NPCUser toUser(
                final CosmeticManager cosmeticManager,
                final EntityIds entityIds,
                final List<ArmorItemDAO> armorItems
        ) {
            final PlayerArmor playerArmor = PlayerArmor.empty();

            for (final ArmorItemDAO armorItemDao : armorItems) {
                final ArmorItem armorItem = armorItemDao.toArmorItem(cosmeticManager);
                if (armorItem == null) {
                    continue;
                }
                playerArmor.setItem(armorItem);
            }

            return new NPCUser(this.citizensId, playerArmor, entityIds);
        }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final CitizenDAO that = (CitizenDAO) o;
        return citizensId == that.citizensId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(citizensId);
    }
}
