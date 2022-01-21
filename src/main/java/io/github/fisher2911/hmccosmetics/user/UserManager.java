package io.github.fisher2911.hmccosmetics.user;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import com.google.common.xml.XmlEscapers;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.database.dao.UserDAO;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.inventory.PlayerArmor;
import io.github.fisher2911.hmccosmetics.message.Message;
import io.github.fisher2911.hmccosmetics.message.MessageHandler;
import io.github.fisher2911.hmccosmetics.message.Messages;
import io.github.fisher2911.hmccosmetics.message.Placeholder;
import io.github.fisher2911.hmccosmetics.util.Keys;
import io.github.fisher2911.hmccosmetics.util.builder.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftEntityEquipment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class UserManager {

    private final HMCCosmetics plugin;
    private final MessageHandler messageHandler;

    private final Map<UUID, User> userMap = new HashMap<>();
    private final Map<Integer, User> armorStandIdMap = new HashMap<>();

    private BukkitTask teleportTask;

    public UserManager(final HMCCosmetics plugin) {
        this.plugin = plugin;
        this.messageHandler = this.plugin.getMessageHandler();
        this.registerPacketListener();
    }

    public void add(final User user) {
        this.userMap.put(user.getUuid(), user);
        this.armorStandIdMap.put(user.getArmorStandId(), user);
        this.updateCosmetics(user);
    }

    public Optional<User> get(final UUID uuid) {
        return Optional.ofNullable(this.userMap.get(uuid));
    }

    public void remove(final UUID uuid) {
        final User user = this.userMap.remove(uuid);

        if (user == null) return;

        this.armorStandIdMap.remove(user.getArmorStandId());

        user.removeAllCosmetics();
        this.updateCosmetics(user);
        user.despawnAttached();
    }

    public void startTeleportTask() {
        this.teleportTask = Bukkit.getScheduler().runTaskTimer(
                this.plugin,
                () -> this.userMap.values().forEach(User::updateArmorStand),
                1,
                1
        );
    }

    public void resendCosmetics(final Player player) {
        for (final User user : this.userMap.values()) {
            user.spawnArmorStand(player);
        }
    }

    private void registerPacketListener() {
        final ProtocolManager protocolManager = this.plugin.getProtocolManager();
        protocolManager.addPacketListener(new PacketAdapter(
                this.plugin,
                ListenerPriority.NORMAL,
                PacketType.Play.Server.ENTITY_EQUIPMENT) {
            @Override
            public void onPacketReceiving(PacketEvent event) {

            }

            @Override
            public void onPacketSending(final PacketEvent event) {
                if (event.getPacketType() == PacketType.Play.Server.ENTITY_EQUIPMENT) {
                    final int id = event.getPacket().getIntegers().read(0);

                    for (final var entry : event.getPacket().getSlotStackPairLists().getValues().get(0)) {
                        if (entry.getFirst() != EnumWrappers.ItemSlot.HEAD) {
                            continue;
                        }
                        for (final Player p : Bukkit.getOnlinePlayers()) {
                            if (p.getEntityId() != id) {
                                continue;
                            }

                            final User user = userMap.get(p.getUniqueId());

                            if (user == null) {
                                break;
                            }

                            final ItemStack hat = user.getPlayerArmor().getHat().getItemStack();
                            final ItemStack second = entry.getSecond();

                            if (hat != null && hat
                                    .getType() != Material.AIR &&
                                    second != null &&
                                    !Keys.hasKey(second)) {
                                event.setCancelled(true);
                            }
                        }
                    }
                }
            }
        });
    }

    public void updateCosmetics(final UUID uuid, final boolean ignoreRestrictions) {
        this.get(uuid).ifPresent(user -> this.updateCosmetics(user, ignoreRestrictions));

    }

    public void updateCosmetics(final UUID uuid) {
        this.updateCosmetics(uuid, false);
    }

    public void updateCosmetics(final User user) {
        this.updateCosmetics(user, false);
    }

    public void updateCosmetics(final User user, final boolean ignoreRestrictions) {
        final Player player = user.getPlayer();

        if (player == null) {
            return;
        }
        final PlayerArmor playerArmor = user.getPlayerArmor();

        final List<Pair<EnumWrappers.ItemSlot, ItemStack>> equipmentList = new ArrayList<>();

        equipmentList.add(
                new Pair<>(EnumWrappers.ItemSlot.HEAD, this.getCosmeticItem(player, playerArmor.getHat(), EquipmentSlot.HEAD, ignoreRestrictions))
        );
        equipmentList.add(
                new Pair<>(EnumWrappers.ItemSlot.OFFHAND, this.getCosmeticItem(player, playerArmor.getOffHand(), EquipmentSlot.OFF_HAND, ignoreRestrictions))
        );

        final PacketContainer fake = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);

        fake.getIntegers().write(0, player.getEntityId());
        fake.getSlotStackPairLists().write(0, equipmentList);

        for (final Player p : Bukkit.getOnlinePlayers()) {
            try {
                this.plugin.getProtocolManager().sendServerPacket(p, fake);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private ItemStack getCosmeticItem(
            final Player player,
            final ArmorItem armorItem,
            final EquipmentSlot slot,
            final boolean ignoreRestrictions) {
        final EntityEquipment equipment = player.getEquipment();

        final Map<String, String> placeholders = Map.of(Placeholder.ALLOWED, "true",
                Placeholder.ENABLED, "true");

        ItemStack itemStack = ItemBuilder.from(armorItem.getColored()).
                namePlaceholders(placeholders).
                lorePlaceholders(placeholders).
                build();

        if (itemStack.getType() != Material.AIR &&
                (slot != EquipmentSlot.OFF_HAND || ignoreRestrictions)) return itemStack;

        if (equipment == null) return itemStack;

        final ItemStack equipped = equipment.getItem(slot);

        if (equipped != null && equipped.getType() != Material.AIR) return equipped;

        return itemStack;
    }

    public void setItem(final User user, final ArmorItem armorItem) {
        user.setItem(armorItem);
        switch (armorItem.getType()) {
            case HAT, OFF_HAND -> this.updateCosmetics(user);
        }
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> this.plugin.getDatabase().saveUser(user));
    }

    public void removeItem(final User user, final ArmorItem.Type type) {
        this.setItem(user, ArmorItem.empty(type));
    }

    /**
     *
     * @param user
     * @param armorItem
     * @param removeMessage
     * @param setMessage
     * @return the item that was set
     */
    public ArmorItem setOrUnset(
            final User user,
            final ArmorItem armorItem,
            final Message removeMessage,
            final Message setMessage) {
        final Player player = user.getPlayer();

        final ArmorItem.Type type = armorItem.getType();

        final ArmorItem empty = ArmorItem.empty(type);

        if (player == null) {
            return empty;
        }

        final ArmorItem check = user.getPlayerArmor().getItem(type);

        if (armorItem.getId().equals(check.getId())) {
            this.setItem(user, ArmorItem.empty(type));

            messageHandler.sendMessage(
                    player,
                    removeMessage
            );

            return empty;
        }

        this.setItem(user, armorItem);
        messageHandler.sendMessage(
                player,
                setMessage
        );
        return armorItem;
    }

    public void removeAll() {
        for (final var user : this.userMap.values()) {
            user.despawnAttached();
        }

        this.userMap.clear();
    }

    public void cancelTeleportTask() {
        this.teleportTask.cancel();
    }
}
