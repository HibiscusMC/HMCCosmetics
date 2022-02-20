package io.github.fisher2911.hmccosmetics.user;

import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.gui.CosmeticGui;
import io.github.fisher2911.hmccosmetics.inventory.PlayerArmor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class User extends BaseUser<UUID> {

    protected Wardrobe wardrobe;
    private CosmeticGui openGui;

    public User(final UUID uuid, final int entityId, final PlayerArmor playerArmor, final Wardrobe wardrobe, final int armorStandId) {
        super(uuid, entityId, playerArmor, armorStandId);
        this.wardrobe = wardrobe;
    }

    public User(final UUID uuid, final int entityId, final PlayerArmor playerArmor, final int armorStandId) {
        super(uuid, entityId, playerArmor, armorStandId);
    }

    public @Nullable Player getPlayer() {
        return Bukkit.getPlayer(this.getId());
    }

    public Wardrobe getWardrobe() {
        return wardrobe;
    }

    public boolean shouldShow(final Player other) {
        final Player player = this.getPlayer();
        if (player == null) return false;
        return player.getGameMode() != GameMode.SPECTATOR &&
                (!player.hasPotionEffect(PotionEffectType.INVISIBILITY) &&
                        other.canSee(player) &&
                        !player.isSwimming());
    }

    @Nullable
    public CosmeticGui getOpenGui() {
        return openGui;
    }

    public void setOpenGui(final CosmeticGui openGui) {
        this.openGui = openGui;
    }

    public boolean hasPermissionToUse(final ArmorItem armorItem) {
        final Player player = this.getPlayer();
        if (player == null) return false;
        return player.hasPermission(armorItem.getPermission());
    }

    @Override
    @Nullable
    public Location getLocation() {
        final Player player = this.getPlayer();
        if (player == null) return null;
        return player.getLocation();
    }

    @Override
    public Equipment getEquipment() {
        final Player player = this.getPlayer();
        if (player == null) return new Equipment();
        return Equipment.fromEntityEquipment(player.getEquipment());
    }

    @Override
    public boolean isWardrobeActive() {
        return this.wardrobe.isActive();
    }
}
