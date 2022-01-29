package io.github.fisher2911.hmccosmetics.user;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Serializer;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.inventory.PlayerArmor;
import io.github.fisher2911.hmccosmetics.packet.PacketManager;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class User {

    private final UUID uuid;
    private final PlayerArmor playerArmor;
    private final int armorStandId;
    private ArmorItem lastSetItem = ArmorItem.empty(ArmorItem.Type.HAT);
    private boolean hasArmorStand;

    public User(final UUID uuid, final PlayerArmor playerArmor, final int armorStandId) {
        this.uuid = uuid;
        this.playerArmor = playerArmor;
        this.armorStandId = armorStandId;
    }

    public @Nullable Player getPlayer() {
        return Bukkit.getPlayer(this.uuid);
    }

    public UUID getUuid() {
        return uuid;
    }

    public PlayerArmor getPlayerArmor() {
        return playerArmor;
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

        if (player == null) {
            return;
        }

        final Location location = player.getLocation();

        final PacketContainer packet = PacketManager.getEntitySpawnPacket(location,
                this.armorStandId,
                EntityType.ARMOR_STAND);

        PacketManager.sendPacket(other, packet);
    }

    public void spawnArmorStand() {
        if (this.hasArmorStand) {
            this.updateArmorStand();
            return;
        }

        for (final Player p : Bukkit.getOnlinePlayers()) {
            this.spawnArmorStand(p);
        }

        this.hasArmorStand = true;
    }

    public void updateArmorStand() {
        if (!this.hasArmorStand) {
            this.spawnArmorStand();
//            return;
        }

        final Player player = this.getPlayer();

        if (player == null) {
            return;
        }

        final List<Pair<EnumWrappers.ItemSlot, ItemStack>> equipmentList = new ArrayList<>();
        equipmentList.add(new Pair<>(EnumWrappers.ItemSlot.HEAD,
                this.playerArmor.getBackpack().getColored()
        ));

        final Location location = player.getLocation();

        final PacketContainer armorPacket = PacketManager.getEquipmentPacket(equipmentList,
                this.armorStandId);
        final PacketContainer rotationPacket = PacketManager.getRotationPacket(this.armorStandId,
                location);
        final PacketContainer ridingPacket = PacketManager.getRidingPacket(player.getEntityId(),
                this.armorStandId);

        final PacketContainer metaContainer = new PacketContainer(
                PacketType.Play.Server.ENTITY_METADATA);

        WrappedDataWatcher metaData = new WrappedDataWatcher();

        final Serializer byteSerializer = WrappedDataWatcher.Registry.get(Byte.class);

        metaData.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, byteSerializer),
                (byte) (0x20));
        metaData.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(15, byteSerializer),
                (byte) (0x10));

        metaContainer.getIntegers().write(0, this.armorStandId);
        metaContainer.getWatchableCollectionModifier().write(0, metaData.getWatchableObjects());

        PacketManager.sendPacketToOnline(armorPacket, metaContainer, rotationPacket, ridingPacket);
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

}
