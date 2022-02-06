package io.github.fisher2911.hmccosmetics.user;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.api.CosmeticItem;
import io.github.fisher2911.hmccosmetics.api.event.CosmeticChangeEvent;
import io.github.fisher2911.hmccosmetics.concurrent.Threads;
import io.github.fisher2911.hmccosmetics.config.CosmeticSettings;
import io.github.fisher2911.hmccosmetics.config.Settings;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.inventory.PlayerArmor;
import io.github.fisher2911.hmccosmetics.message.Message;
import io.github.fisher2911.hmccosmetics.message.MessageHandler;
import io.github.fisher2911.hmccosmetics.message.Placeholder;
import io.github.fisher2911.hmccosmetics.message.Translation;
import io.github.fisher2911.hmccosmetics.packet.PacketManager;
import io.github.fisher2911.hmccosmetics.util.builder.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class UserManager {

    private final HMCCosmetics plugin;
    private final Settings settings;
    private final MessageHandler messageHandler;

    private final Map<UUID, User> userMap = new HashMap<>();
    private final Map<Integer, User> armorStandIdMap = new HashMap<>();

    private BukkitTask teleportTask;

    public UserManager(final HMCCosmetics plugin) {
        this.plugin = plugin;
        this.settings = this.plugin.getSettings();
        this.messageHandler = this.plugin.getMessageHandler();
    }

    public void add(final User user) {
        this.userMap.put(user.getUuid(), user);
        this.armorStandIdMap.put(user.getArmorStandId(), user);
        this.updateCosmetics(user);
    }

    public Optional<User> get(final UUID uuid) {
        return Optional.ofNullable(this.userMap.get(uuid));
    }

    public Collection<User> getAll() {
        return this.userMap.values();
    }

    public void remove(final UUID uuid) {
        final User user = this.userMap.remove(uuid);

        if (user == null) return;

        this.armorStandIdMap.remove(user.getArmorStandId());

        final PlayerArmor copy = user.getPlayerArmor().copy();

        user.removeAllCosmetics();
        this.updateCosmetics(user);
        user.despawnAttached();

        user.setPlayerArmor(copy);

        Threads.getInstance().execute(() -> this.plugin.getDatabase().saveUser(user));
    }

    public void startTeleportTask() {
        // throws an error on first load of registry if this isn't here
        WrappedDataWatcher.Registry.get(Byte.class);
        this.teleportTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
                this.plugin,
                () -> {
                    for (final User user : this.userMap.values()) {
                        user.updateArmorStand(this.plugin.getSettings());
                    }
                },
                1,
                1
        );
    }

    public void resendCosmetics(final Player player) {
        for (final User user : this.userMap.values()) {
            user.spawnArmorStand(player);
            this.updateCosmetics(user, false, player);
        }
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
        for (final Player player : Bukkit.getOnlinePlayers()) {
            this.updateCosmetics(user, ignoreRestrictions, player);
        }
    }

    public void updateCosmetics(final User user, final boolean ignoreRestrictions, final Player other) {
        final Player player = user.getPlayer();

        final Equipment equipment;
        if (player == null) {
            equipment = new Equipment();
        } else {
            equipment = Equipment.fromEntityEquipment(player.getEquipment());
        }

        final PlayerArmor playerArmor = user.getPlayerArmor();

        final List<Pair<EnumWrappers.ItemSlot, ItemStack>> equipmentList = new ArrayList<>();
        final boolean hidden = !user.shouldShow(other);

        equipmentList.add(
                new Pair<>(
                        EnumWrappers.ItemSlot.HEAD,
                        this.getCosmeticItem(equipment, playerArmor.getHat(), EquipmentSlot.HEAD, ignoreRestrictions, hidden)
                )
        );
        equipmentList.add(
                new Pair<>(
                        EnumWrappers.ItemSlot.OFFHAND,
                        this.getCosmeticItem(equipment, playerArmor.getOffHand(), EquipmentSlot.OFF_HAND, ignoreRestrictions, hidden)
                )
        );

        PacketManager.sendPacket(
                other,
                PacketManager.getEquipmentPacket(
                        equipmentList,
                        user.getEntityId()
                )
        );
    }

    private ItemStack getCosmeticItem(
            final Equipment equipment,
            final ArmorItem armorItem,
            final EquipmentSlot slot,
            final boolean ignoreRestrictions,
            final boolean hidden) {
        if (hidden) return new ItemStack(Material.AIR);
        final CosmeticSettings cosmeticSettings = this.settings.getCosmeticSettings();

        final Map<String, String> placeholders = Map.of(Placeholder.ALLOWED, Translation.TRUE,
                Placeholder.ENABLED, Translation.TRUE);

        ItemStack itemStack = ItemBuilder.from(armorItem.getColored()).
                namePlaceholders(placeholders).
                lorePlaceholders(placeholders).
                build();

        final boolean isAir = itemStack.getType().isAir();
        final boolean requireEmpty = cosmeticSettings.requireEmpty(slot);

        if (!isAir && (!requireEmpty || ignoreRestrictions)) return itemStack;

        if (equipment == null) return itemStack;

        final ItemStack equipped = equipment.getItem(slot);

        if (equipped != null && equipped.getType() != Material.AIR) return equipped;

        return itemStack;
    }

    public void setItem(final User user, final ArmorItem armorItem) {
        final Wardrobe wardrobe = user.getWardrobe();
        final User setUser;
        if (wardrobe.isActive()) {
            setUser = wardrobe;
        } else {
            setUser = user;
        }
        ArmorItem previous = setUser.getPlayerArmor().getItem(armorItem.getType());

        final CosmeticChangeEvent event =
                new CosmeticChangeEvent(new CosmeticItem(armorItem.copy()), new CosmeticItem(previous.copy()), setUser);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        setUser.setItem(event.getCosmeticItem().getArmorItem());
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            switch (armorItem.getType()) {
                case HAT, OFF_HAND -> this.updateCosmetics(setUser);
                case BACKPACK -> {
                    if (wardrobe.isActive()) setUser.updateArmorStand(settings);
                }
            }
        });
    }

    public void removeItem(final User user, final ArmorItem.Type type) {
        this.setItem(user, ArmorItem.empty(type));
    }

    /**
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
