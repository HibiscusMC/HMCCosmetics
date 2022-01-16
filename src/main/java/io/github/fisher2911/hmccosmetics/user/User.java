package io.github.fisher2911.hmccosmetics.user;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import com.comphenix.protocol.wrappers.Vector3F;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.sun.jdi.InvocationException;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.inventory.PlayerArmor;
import io.github.fisher2911.hmccosmetics.message.MessageHandler;
import io.github.fisher2911.hmccosmetics.message.Messages;
import io.github.fisher2911.hmccosmetics.message.Placeholder;
import io.github.fisher2911.hmccosmetics.util.Keys;
import io.github.fisher2911.hmccosmetics.util.builder.ItemBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.core.Vector3f;
import net.minecraft.network.protocol.game.PacketPlayOutEntityHeadRotation;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.syncher.DataWatcher;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftArmorStand;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class User {

    private final UUID uuid;
    private final PlayerArmor playerArmor;
    private ArmorStand attached;
    private ArmorItem lastSetItem;

    private final int armorStandId;
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

    public void setPlayerArmor(final PlayerArmor playerArmor) {
        this.playerArmor.setBackpack(playerArmor.getBackpack());
        this.playerArmor.setHat(playerArmor.getHat());
    }

    public void removeAllCosmetics() {
        this.setPlayerArmor(PlayerArmor.empty());
    }

    public int getArmorStandId() {
        return armorStandId;
    }

    public void setBackpack(final ArmorItem backpack) {
        this.playerArmor.setBackpack(backpack);
        this.lastSetItem = backpack;
    }

    // return true if backpack was set
    public boolean setOrUnsetBackpack(
            final ArmorItem backpack,
            final MessageHandler messageHandler) {

        final Player player = this.getPlayer();

        if (player == null) {
            return false;
        }

        if (backpack.getId().equals(this.playerArmor.getBackpack().getId())) {
            this.setBackpack(new ArmorItem(
                    new ItemStack(Material.AIR),
                    "",
                    new ArrayList<>(),
                    "",
                    ArmorItem.Type.BACKPACK
            ));

            messageHandler.sendMessage(
                    player,
                    Messages.REMOVED_BACKPACK
            );

            return false;
        }

        this.setBackpack(backpack);
        messageHandler.sendMessage(
                player,
                Messages.SET_BACKPACK
        );

        return true;
    }


    public void setHat(final ArmorItem hat, final UserManager userManager) {
        this.playerArmor.setHat(hat);
        this.lastSetItem = hat;
        userManager.updateHat(this);
    }

    // return true if hat was set
    public boolean setOrUnsetHat(
            final ArmorItem hat,
            final MessageHandler messageHandler,
            final UserManager userManager) {

        final Player player = this.getPlayer();

        if (player == null) {
            return false;
        }

        if (hat.getId().equals(this.playerArmor.getHat().getId())) {
            this.setHat(new ArmorItem(
                            new ItemStack(Material.AIR),
                            "",
                            new ArrayList<>(),
                            "",
                            ArmorItem.Type.HAT
                    ),
                    userManager);

            messageHandler.sendMessage(
                    player,
                    Messages.REMOVED_HAT
            );

            return false;
        }

        this.setHat(hat, userManager);
        messageHandler.sendMessage(
                player,
                Messages.SET_HAT
        );

        return true;
    }

    public void detach() {
        if (this.attached != null) {
            this.attached.remove();
        }
    }

    // teleports armor stand to the correct position
    public void updateArmorStand() {
        if (true) {
            this.updatePacketArmorStand();
            return;
        }

        final ArmorItem backpackArmorItem = this.playerArmor.getBackpack();
        if (backpackArmorItem == null) {
            this.despawnAttached();
            return;
        }

        final ItemStack backpackItem = backpackArmorItem.getItemStack();

        if (backpackItem == null || backpackItem.getType() == Material.AIR) {
            this.despawnAttached();
            return;
        }

        final Player player = this.getPlayer();

        if (player == null) {
            this.despawnAttached();
            return;
        }

        if (this.attached == null) {
            this.attached = player.getWorld().spawn(player.getLocation(),
                    ArmorStand.class,
                    armorStand -> {
                        armorStand.setVisible(false);
                        armorStand.setMarker(true);
                        armorStand.getPersistentDataContainer().set(
                                Keys.ARMOR_STAND_KEY,
                                PersistentDataType.BYTE,
                                (byte) 1
                        );
                        player.addPassenger(armorStand);
                    });
        }

        if (!player.getPassengers().contains(this.attached)) {
            player.addPassenger(this.attached);
        }

        final EntityEquipment equipment = this.attached.getEquipment();

        if (equipment == null) {
            this.despawnAttached();
            return;
        }

        if (!backpackItem.equals(equipment.getHelmet())) {
            equipment.setHelmet(backpackItem);
        }

        this.attached.
                setRotation(
                        player.getLocation().getYaw(),
                        player.getLocation().getPitch());
    }

    public void spawnPacketArmorStand() {

        final Player player = this.getPlayer();

        if (player == null) {
            this.updatePacketArmorStand();
            return;
        }

        this.hasArmorStand = true;

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
        packet.getDoubles().write(0, 0d);
        packet.getDoubles().write(1, -5d);
        packet.getDoubles().write(2, 0d);
        // Set UUID
        packet.getUUIDs().write(0, UUID.randomUUID());

        packet.getEntityTypeModifier().write(0, EntityType.ZOMBIE);

        for (final Player p : Bukkit.getOnlinePlayers()) {
            try {
                protocolManager.sendServerPacket(p, packet);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    public void updatePacketArmorStand() {
        if (!this.hasArmorStand) {
            this.spawnPacketArmorStand();
            return;
        }

        final Player player = this.getPlayer();

        if (player == null) return;

        final List<Pair<EnumWrappers.ItemSlot, ItemStack>> equipmentList = new ArrayList<>();

        final Map<String, String> placeholders = Map.of(Placeholder.ALLOWED, "true",
                Placeholder.ENABLED, "true");

        equipmentList.add(new Pair<>(EnumWrappers.ItemSlot.HEAD,
                ItemBuilder.from(this.playerArmor.getBackpack().getItemStack()).
                        namePlaceholders(placeholders).
                        lorePlaceholders(placeholders).
                        build()
        ));

        final PacketContainer armorPacket = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);
        armorPacket.getIntegers().write(0, this.armorStandId);
        armorPacket.getSlotStackPairLists().write(0, equipmentList);

//        final PacketContainer rotationContainer = new PacketContainer(PacketType.Play.Server.ENTITY_LOOK);
        final Location location = player.getLocation();
//        rotationContainer.
//                getIntegers().
//                write(0, this.armorStandId);
//        rotationContainer.
//                getBytes().
//                write(0, (byte) (location.getYaw() * 256.0F / 360.0F));
//
//        final PacketContainer rotationContainer2 = new PacketContainer(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
//        rotationContainer2.
//                getIntegers().
//                write(0, this.armorStandId);
//        rotationContainer2.
//                getBytes().
//                write(0, (byte) (location.getPitch() * 256.0F / 360.0F));

        final PacketContainer metaContainer = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
//        metaContainer.
//                getBytes().
//                write(15, (byte) 0x01);



        WrappedDataWatcher metaData = new WrappedDataWatcher();
        metaData.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class)), (byte) (0x20)); //isSmall, noBasePlate, set Marker
        metaData.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(15, WrappedDataWatcher.Registry.get(Byte.class)), (byte) (0x01)); //isSmall, noBasePlate, set Marker

  /*      metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(8, WrappedDataWatcher.Registry.get(Vector3F.getMinecraftClass())), new Vector3f(
                location.getPitch(),
                location.getYaw(),
                0
        ));*/ //isSmall, noBasePlate, set Marker
//        metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(16, WrappedDataWatcher.Registry.(), new Vector()));

        final PacketContainer rotationPacket = new PacketContainer(PacketType.Play.Server.REL_ENTITY_MOVE_LOOK);

        rotationPacket.getIntegers().write(0, this.armorStandId);
        rotationPacket.
                getShorts().
                write(0, (short) 0).
                write(1, (short) -5).
                write(2, (short) 0);
        rotationPacket.
                getBytes().
                write(0, (byte) (location.getYaw() * 256.0F / 360.0F)).
                write(1, (byte) (location.getPitch() * 256.0F / 360.0F));

        metaContainer.getIntegers().write(0, this.armorStandId);
//        metaContainer.getWatchableCollectionModifier().write(0, metaData.getWatchableObjects());

//        final WrappedDataWatcher dataWatcher = new WrappedDataWatcher();
//        dataWatcher.setObject(15, (byte) 0x01);
//
//        metaContainer.getDataWatcherModifier().write(
//                0, dataWatcher
//        );

        final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

        final PacketContainer teleportPacket = new PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT);

        teleportPacket.getIntegers().write(0, this.armorStandId);
        teleportPacket.getDoubles().
                write(0, location.getX()).
                write(1, location.getY()).
                write(2, location.getZ());

        teleportPacket.getBytes().
                write(0, (byte) (location.getYaw() * 256.0F / 360.0F)).
                write(1, (byte) (location.getPitch() * 256.0F / 360.0F));

        final PacketContainer destroyPacket = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
//        destroyPacket.getIntegerArrays().write(0, new int[]{this.armorStandId});
        destroyPacket.getModifier().write(0, new IntArrayList(new int[]{this.armorStandId}));

        final PacketContainer ridingPacket = new PacketContainer(PacketType.Play.Server.MOUNT);
        ridingPacket.
                getIntegers().
                write(0, player.getEntityId());
        ridingPacket.getIntegerArrays().write(0, new int[]{this.armorStandId});

        for (final Player p : Bukkit.getOnlinePlayers()) {
            try {
//                protocolManager.sendServerPacket(p, destroyPacket);
                if (i == 0) {
                    i++;
//                    protocolManager.sendServerPacket(p, ridingPacket);
                }
                protocolManager.sendServerPacket(p, armorPacket);
//                protocolManager.sendServerPacket(p, metaContainer);
                protocolManager.sendServerPacket(p, rotationPacket);
//                protocolManager.sendServerPacket(p, teleportPacket);
//                protocolManager.sendServerPacket(p, rotationPacket2);
//                protocolManager.sendServerPacket(p, removeRiderPacket);
//                protocolManager.sendServerPacket(p, rotationPacket2);
//                i++;
            } catch (final InvocationTargetException exception) {
                exception.printStackTrace();
            }
        }
    }

    int i = 0;

    public void addArmorStandPassenger(final Entity entity) {
        final Player player = this.getPlayer();

        if (player == null) return;

        if (!player.getPassengers().contains(entity)) {
            player.addPassenger(entity);
        }
    }

    public void despawnAttached() {
        if (this.attached == null) {
            return;
        }

        final Player player = this.getPlayer();

        if (player != null) {
            player.removePassenger(this.attached);
        }

        this.attached.remove();
        this.attached = null;
    }

    public ArmorItem getLastSetItem() {
        return lastSetItem;
    }
}
