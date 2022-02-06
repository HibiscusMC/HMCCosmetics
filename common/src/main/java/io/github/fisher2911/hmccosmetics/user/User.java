package io.github.fisher2911.hmccosmetics.user;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Serializer;
import io.github.fisher2911.hmccosmetics.config.Settings;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.inventory.PlayerArmor;
import io.github.fisher2911.hmccosmetics.packet.PacketManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class User {

    private final UUID uuid;
    private final PlayerArmor playerArmor;

    protected Wardrobe wardrobe;

    private ArmorItem lastSetItem = ArmorItem.empty(ArmorItem.Type.HAT);

    private boolean hasArmorStand;
    private final int armorStandId;

    public User(final UUID uuid, final PlayerArmor playerArmor, final Wardrobe wardrobe, final int armorStandId) {
        this.uuid = uuid;
        this.playerArmor = playerArmor;
        this.wardrobe = wardrobe;
        this.armorStandId = armorStandId;
    }

    protected User(final UUID uuid, final PlayerArmor playerArmor, final int armorStandId) {
        this.uuid = uuid;
        this.playerArmor = playerArmor;
        this.armorStandId = armorStandId;
    }

    public @Nullable Player getPlayer() {
        return Bukkit.getPlayer(this.uuid);
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public PlayerArmor getPlayerArmor() {
        return playerArmor;
    }

    public Wardrobe getWardrobe() {
        return wardrobe;
    }

    protected void setPlayerArmor(final PlayerArmor playerArmor) {
        for (final ArmorItem armorItem : playerArmor.getArmorItems()) {
            this.playerArmor.setItem(armorItem);
        }
    }

    protected void removeAllCosmetics() {
        for (final ArmorItem.Type type : ArmorItem.Type.values()) {
            this.removeItem(type);
        }
    }

    public int getArmorStandId() {
        return armorStandId;
    }

    protected ArmorItem setItem(final ArmorItem armorItem) {
        this.lastSetItem = armorItem;
        return this.playerArmor.setItem(armorItem);
    }

    protected ArmorItem removeItem(final ArmorItem.Type type) {
        return this.setItem(ArmorItem.empty(type));
    }

    public void spawnArmorStand(final Player other) {
        final Player player = this.getPlayer();

        if (player == null) return;

        final Location location = player.getLocation();

        final PacketContainer packet = PacketManager.getEntitySpawnPacket(location, this.armorStandId, EntityType.ARMOR_STAND);

        PacketManager.sendPacket(other, packet);
    }

    public void spawnArmorStand(final Settings settings) {
        if (this.hasArmorStand) {
            this.updateArmorStand(settings);
            return;
        }

        for (final Player p : Bukkit.getOnlinePlayers()) {
            this.spawnArmorStand(p);
        }

        this.hasArmorStand = true;
    }

    public void updateArmorStand(final Settings settings) {
        if (!this.hasArmorStand) {
            this.spawnArmorStand(settings);
        }
        for (final Player player : Bukkit.getOnlinePlayers()) {
            this.updateArmorStand(player, settings);
        }
    }

    public void updateArmorStand(final Player other, final Settings settings) {
        final Player player = this.getPlayer();

        if (player == null) return;

        this.updateArmorStand(other, settings, player.getLocation());
    }

    public void updateArmorStand(final Player other, final Settings settings, final Location location) {
        final List<Pair<EnumWrappers.ItemSlot, ItemStack>> equipmentList = new ArrayList<>();
        final boolean hidden = !this.shouldShow(other);
        if (hidden) {
            equipmentList.add(new Pair<>(EnumWrappers.ItemSlot.HEAD,
                    new ItemStack(Material.AIR)
            ));
        } else {
            equipmentList.add(new Pair<>(EnumWrappers.ItemSlot.HEAD,
                    this.playerArmor.getBackpack().getColored()
            ));
        }

        final PacketContainer armorPacket = PacketManager.getEquipmentPacket(equipmentList, this.armorStandId);
        final PacketContainer rotationPacket = PacketManager.getRotationPacket(this.armorStandId, location);
        final PacketContainer ridingPacket = PacketManager.getRidingPacket(this.getEntityId(), this.armorStandId);

        final PacketContainer metaContainer = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);

        WrappedDataWatcher metaData = new WrappedDataWatcher();

        final Serializer byteSerializer = WrappedDataWatcher.Registry.get(Byte.class);

        metaData.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, byteSerializer), (byte) (0x20));
        metaData.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(15, byteSerializer), (byte) (0x10));

        metaContainer.getIntegers().write(0, this.armorStandId);
        metaContainer.getWatchableCollectionModifier().write(0, metaData.getWatchableObjects());

        PacketManager.sendPacket(other, armorPacket, metaContainer, rotationPacket, ridingPacket);

        if (hidden) return;

        final int lookDownPitch = settings.getCosmeticSettings().getLookDownPitch();

        if (lookDownPitch != -1 &&
                this.isFacingDown(location, lookDownPitch)) {
            equipmentList.set(0, new Pair<>(EnumWrappers.ItemSlot.HEAD,
                    new ItemStack(Material.AIR)
            ));

            if (!this.uuid.equals(other.getUniqueId())) return;
            PacketManager.sendPacket(other, PacketManager.getEquipmentPacket(equipmentList, this.armorStandId));
        }
    }

    public boolean shouldShow(final Player other) {
        final Player player = this.getPlayer();
        return player == null ||
                (!player.hasPotionEffect(PotionEffectType.INVISIBILITY) &&
                        other.canSee(player) &&
                        !player.isSwimming());
    }

    private boolean isFacingDown(final Location location, final int pitchLimit) {
        return location.getPitch() > pitchLimit;
    }

    public void despawnAttached() {
        PacketManager.sendPacketToOnline(PacketManager.getEntityDestroyPacket(this.armorStandId));
        this.hasArmorStand = false;
    }

    public boolean hasArmorStand() {
        return hasArmorStand;
    }

    public ArmorItem getLastSetItem() {
        return lastSetItem;
    }

    public int getEntityId() {
        final Player player = this.getPlayer();
        if (player == null) return -1;
        return player.getEntityId();
    }

    public boolean hasPermissionToUse(final ArmorItem armorItem) {
        final Player player = this.getPlayer();
        if (player == null) return false;
        return player.hasPermission(armorItem.getPermission());
    }
}
