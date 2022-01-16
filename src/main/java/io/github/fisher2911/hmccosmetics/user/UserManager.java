package io.github.fisher2911.hmccosmetics.user;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.inventory.PlayerArmor;
import io.github.fisher2911.hmccosmetics.message.Placeholder;
import io.github.fisher2911.hmccosmetics.util.Keys;
import io.github.fisher2911.hmccosmetics.util.builder.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class UserManager {

    private int currentArmorStandId = Integer.MAX_VALUE;
    private final HMCCosmetics plugin;

    private final Map<UUID, User> userMap = new HashMap<>();
    private final Map<Integer, User> armorStandIdMap = new HashMap<>();

    private BukkitTask teleportTask;

    public UserManager(final HMCCosmetics plugin) {
        this.plugin = plugin;
        this.registerPacketListener();
    }

    public void add(final Player player) {
        final UUID uuid = player.getUniqueId();
        final int armorStandId = this.currentArmorStandId;
        final User user = new User(
                uuid,
                PlayerArmor.empty(),
                armorStandId);
        this.userMap.put(uuid, user);
        this.armorStandIdMap.put(armorStandId, user);
        this.currentArmorStandId--;
    }

    public Optional<User> get(final UUID uuid) {
        return Optional.ofNullable(this.userMap.get(uuid));
    }

    public void updateHat(final User user) {
        this.setFakeHelmet(user);
    }

    public void remove(final UUID uuid) {
        final User user = this.userMap.remove(uuid);

        if (user == null) return;

        user.removeAllCosmetics();
        this.setFakeHelmet(user);
        user.despawnAttached();

        this.armorStandIdMap.remove(user.getArmorStandId());
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

    public void setFakeHelmet(final User user) {

        ItemStack hat = user.getPlayerArmor().getHat().getItemStack();
        final Player player = user.getPlayer();

        if (player == null || hat == null) {
            return;
        }

        if (hat.getType() == Material.AIR) {
            final EntityEquipment equipment = player.getEquipment();
            if (equipment != null) {
                hat = equipment.getHelmet() == null ? hat : equipment.getHelmet();
            }
        }

        final List<Pair<EnumWrappers.ItemSlot, ItemStack>> equipmentList = new ArrayList<>();

        final Map<String, String> placeholders = Map.of(Placeholder.ALLOWED, "true",
                Placeholder.ENABLED, "true");

        equipmentList.add(new Pair<>(EnumWrappers.ItemSlot.HEAD,
                ItemBuilder.from(hat).
                        namePlaceholders(placeholders).
                        lorePlaceholders(placeholders).
                        build()
        ));

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
