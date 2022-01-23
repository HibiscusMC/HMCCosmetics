package io.github.fisher2911.hmccosmetics.user;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Serializer;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.inventory.PlayerArmor;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class User {

    private final UUID uuid;
    private final PlayerArmor playerArmor;

    private ArmorItem lastSetItem = ArmorItem.empty(ArmorItem.Type.HAT);

    private boolean hasArmorStand;
    private final int armorStandId;

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

    // return true if backpack was set

    public void spawnArmorStand(final Player other) {
        final Player player = this.getPlayer();

        if (player == null) return;

        final Location location = player.getLocation();

        final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

        final PacketContainer packet = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);

        // Entity ID
        packet.getIntegers().write(0, this.armorStandId);
        // Entity Type
//        packet.getIntegers().write(6, 78);
        // Set yaw pitch
        packet.getIntegers().write(4, (int) location.getPitch());
        packet.getIntegers().write(5, (int) location.getYaw());
        // Set location
        packet.getDoubles().write(0, location.getX());
        packet.getDoubles().write(1, location.getY());
        packet.getDoubles().write(2, location.getZ());
        // Set UUID
        packet.getUUIDs().write(0, UUID.randomUUID());

        packet.getEntityTypeModifier().write(0, EntityType.ARMOR_STAND);

        try {
            protocolManager.sendServerPacket(other, packet);
        } catch (final InvocationTargetException exception) {
            exception.printStackTrace();
        }
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
            return;
        }

        final Player player = this.getPlayer();

        if (player == null) return;

        final List<Pair<EnumWrappers.ItemSlot, ItemStack>> equipmentList = new ArrayList<>();

        equipmentList.add(new Pair<>(EnumWrappers.ItemSlot.HEAD,
                this.playerArmor.getBackpack().getColored()
        ));

        final PacketContainer armorPacket = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);
        armorPacket.getIntegers().write(0, this.armorStandId);
        armorPacket.getSlotStackPairLists().write(0, equipmentList);

        final Location location = player.getLocation();

        final PacketContainer metaContainer = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);

        WrappedDataWatcher metaData = new WrappedDataWatcher();

        final Serializer byteSerializer = WrappedDataWatcher.Registry.get(Byte.class);

        metaData.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, byteSerializer), (byte) (0x20));
        metaData.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(15, byteSerializer), (byte) (0x10));

        final PacketContainer rotationPacket = new PacketContainer(PacketType.Play.Server.ENTITY_HEAD_ROTATION);

        rotationPacket.getIntegers().write(0, this.armorStandId);
        rotationPacket.getBytes().write(0, (byte) (location.getYaw() * 256 / 360));

        metaContainer.getIntegers().write(0, this.armorStandId);
        metaContainer.getWatchableCollectionModifier().write(0, metaData.getWatchableObjects());

        final PacketContainer ridingPacket = new PacketContainer(PacketType.Play.Server.MOUNT);
        ridingPacket.
                getIntegers().
                write(0, player.getEntityId());
        ridingPacket.getIntegerArrays().write(0, new int[]{this.armorStandId});

        final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

        try {
            for (final Player p : Bukkit.getOnlinePlayers()) {
                protocolManager.sendServerPacket(p, armorPacket);
                protocolManager.sendServerPacket(p, metaContainer);
                protocolManager.sendServerPacket(p, rotationPacket);
                protocolManager.sendServerPacket(p, ridingPacket);
            }
        } catch (final InvocationTargetException exception) {
            exception.printStackTrace();
        }
    }

    public void despawnAttached() {
        final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

        final PacketContainer destroyPacket = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        destroyPacket.getModifier().write(0, new IntArrayList(new int[]{this.armorStandId}));

        for (final Player p : Bukkit.getOnlinePlayers()) {
            try {
                protocolManager.sendServerPacket(p, destroyPacket);
                this.hasArmorStand = false;
            } catch (final InvocationTargetException exception) {
                exception.printStackTrace();
            }
        }
    }

    public boolean hasArmorStand() {
        return hasArmorStand;
    }

    public ArmorItem getLastSetItem() {
        return lastSetItem;
    }
}
