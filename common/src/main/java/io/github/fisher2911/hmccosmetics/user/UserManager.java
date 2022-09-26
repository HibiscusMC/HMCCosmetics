package io.github.fisher2911.hmccosmetics.user;

import com.comphenix.protocol.wrappers.EnumWrappers;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.api.CosmeticItem;
import io.github.fisher2911.hmccosmetics.api.event.CosmeticChangeEvent;
import io.github.fisher2911.hmccosmetics.concurrent.Threads;
import io.github.fisher2911.hmccosmetics.config.Settings;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.inventory.PlayerArmor;
import io.github.fisher2911.hmccosmetics.message.Message;
import io.github.fisher2911.hmccosmetics.message.MessageHandler;
import io.github.fisher2911.hmccosmetics.message.Placeholder;
import io.github.fisher2911.hmccosmetics.message.Translation;
import io.github.fisher2911.hmccosmetics.packet.PacketManager;
import io.github.fisher2911.hmccosmetics.task.InfiniteTask;
import io.github.fisher2911.hmccosmetics.util.PlayerUtils;
import io.github.fisher2911.hmccosmetics.util.Utils;
import io.github.fisher2911.hmccosmetics.util.builder.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager {

    private final HMCCosmetics plugin;
    private final Settings settings;
    private final MessageHandler messageHandler;

    private final Map<UUID, User> userMap = new ConcurrentHashMap<>();

    public UserManager(final HMCCosmetics plugin) {
        this.plugin = plugin;
        this.settings = this.plugin.getSettings();
        this.messageHandler = this.plugin.getMessageHandler();
    }

    public void add(final User user) {
        this.userMap.put(user.getId(), user);
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

        final PlayerArmor copy = user.getPlayerArmor().copy();

        user.removeAllCosmetics();
        this.updateCosmetics(user);
        user.despawnAttached();

        user.setPlayerArmor(copy);

        Threads.getInstance().execute(() -> this.plugin.getDatabase().saveUser(user));
    }

    public void startTeleportTask() {
        this.plugin.getTaskManager().submit(new InfiniteTask(
                () -> {
                    for (final User user : this.userMap.values()) {
                        user.updateOutsideCosmetics(this.plugin.getSettings());
                    }
                }
        ));
    }

//    public void resendCosmetics(final Player player) {
//        for (final User user : this.userMap.values()) {
//            final Player p = user.getPlayer();
//            if (p == null) continue;
//            user.updateOutsideCosmetics(player, p.getLocation(), this.settings);
//        }
//    }

    public void updateCosmetics(final UUID uuid) {
        this.get(uuid).ifPresent(this::updateCosmetics);
    }

    public void updateCosmetics(final UUID uuid, final Player other) {
        this.get(uuid).ifPresent(user -> this.updateCosmetics(user, other));
    }

    public void updateCosmetics(final BaseUser<?> user, final Player other) {
        this.sendUpdatePacket(
                user,
                other,
                getItemList(user)
        );
    }

    public void updateCosmetics(final BaseUser<?> user) {
        this.sendUpdatePacket(
                user,
                getItemList(user)
        );
    }

    public ItemStack getCosmeticItem(
            final ArmorItem armorItem,
            final ItemStack wearing,
            final ArmorItem.Status status,
            final EquipmentSlot slot
    ) {
        final Map<String, String> placeholders = Map.of(Placeholder.ALLOWED, Translation.TRUE,
                Placeholder.ENABLED, Translation.TRUE);

        if (armorItem.isEmpty()) return wearing;

        ItemStack itemStack = ItemBuilder.from(armorItem.getItemStack(status)).
                namePlaceholders(placeholders).
                lorePlaceholders(placeholders).
                build();

        if (wearing == null) return itemStack;

        final boolean isAir = wearing.getType().isAir();
        final boolean requireEmpty = settings.getCosmeticSettings().requireEmpty(slot);

        if (!isAir && requireEmpty) {
            return wearing;
        }

        return itemStack;
    }

    public Equipment getItemList(
            final BaseUser<?> user
    ) {
        return getItemList(user, user.getEquipment(), Collections.emptySet());
    }

    public Equipment getItemList(
            final BaseUser<?> user,
            final Equipment equipment,
            final Set<ArmorItem.Type> ignored
    ) {
        final PlayerArmor armor = user.getPlayerArmor();
        for (final ArmorItem.Type type : ArmorItem.Type.values()) {
            final EquipmentSlot slot = type.getSlot();
            if (slot == null) continue;
            if (ignored.contains(type)) {
                Equipment item = PlayerUtils.getEquipment(equipment.getItem(slot), slot);
                equipment.setItem(slot, item.getItem(slot));
                continue;
            }
            final ItemStack wearing = Utils.replaceIfNull(equipment.getItem(slot), new ItemStack(Material.AIR));
            final ItemStack itemStack = this.getCosmeticItem(
                    armor.getItem(type),
                    wearing,
                    ArmorItem.Status.APPLIED,
                    slot
            );
            equipment.setItem(slot, itemStack);
        }
        return equipment;
    }

    public void setItem(final BaseUser<?> user, final ArmorItem armorItem, final boolean sendPacket) {
        ArmorItem previous = user.getPlayerArmor().getItem(armorItem.getType());

        final CosmeticChangeEvent event =
                new CosmeticChangeEvent(
                        !Bukkit.isPrimaryThread(),
                        new CosmeticItem(armorItem.copy()),
                        new CosmeticItem(previous.copy()),
                        user
                );
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        final ArmorItem.Type type = armorItem.getType();
        if (type == ArmorItem.Type.BALLOON) user.despawnBalloon();
        user.setItem(event.getCosmeticItem().getArmorItem());
        if (armorItem.getType() == ArmorItem.Type.BACKPACK && armorItem.isEmpty()) {
            user.removeItem(ArmorItem.Type.SELF_BACKPACK);
        }
        if (!sendPacket) {
            user.setArmorUpdated(false);
            return;
        }
        user.setArmorUpdated(true);
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            this.updateCosmetics(user);
            if (user instanceof Wardrobe) user.updateOutsideCosmetics(settings);
        });
    }

    public void removeItem(final User user, final ArmorItem.Type type, final boolean sendPacket) {
        this.setItem(user, ArmorItem.empty(type), sendPacket);
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
            final Message setMessage,
            final boolean sendPacket) {
        final Player player = user.getPlayer();

        final ArmorItem.Type type = armorItem.getType();

        final ArmorItem empty = ArmorItem.empty(type);

        if (player == null) {
            return empty;
        }

        final ArmorItem check = user.getPlayerArmor().getItem(type);

        if (armorItem.getId().equals(check.getId())) {
            this.setItem(user, ArmorItem.empty(type), sendPacket);

            messageHandler.sendMessage(
                    player,
                    removeMessage
            );

            return empty;
        }

        this.setItem(user, armorItem, sendPacket);
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

    @Nullable
    private EquipmentSlot slotFromInventorySlot(final int slot) {
        return switch (slot) {
            case 8 -> EquipmentSlot.FEET;
            case 7 -> EquipmentSlot.LEGS;
            case 6 -> EquipmentSlot.CHEST;
            case 5 -> EquipmentSlot.HEAD;
            default -> null;
        };
    }

    public void sendUpdatePacket(
            final User user,
            final ArmorItem armorItem,
            final ItemStack wearing,
            final ArmorItem.Type type) {
        final EquipmentSlot slot = type.getSlot();
        final ItemStack itemStack = this.getCosmeticItem(armorItem, wearing, ArmorItem.Status.APPLIED, slot);
        //final List<Equipment> itemList = new ArrayList<>();
        //itemList.add(PacketManager.getEquipment(itemStack, slot));
        Equipment equip = user.getEquipment();
        equip.setItem(slot, itemStack);
        this.sendUpdatePacket(user, equip);
    }

    /**
     * Sends a packet to a player "updating" their equipment.
     * @param user The user having their equipment being updated.
     * @param equipment The equipment that is being set on the player.
     */
    public void sendUpdatePacket(
            final BaseUser<?> user,
            Equipment equipment
    ) {
//        final Player player = user.getPlayer();
//        if (player == null) return;
        final Location location = user.getLocation();
        if (location == null) return;
        final int entityId = user.getEntityId();
        if (equipment == null) return;
        for (final User otherUser : this.userMap.values()) {
            final Player other = otherUser.getPlayer();
            if (other == null) continue;
            if (!user.shouldShow(other)) continue;
            if (!this.settings.getCosmeticSettings().isInViewDistance(location, other.getLocation())) continue;
            user.updateBackpack(other, this.settings);
            PacketManager.sendEquipmentPacket(equipment, entityId, other);
        }
    }

    public void sendUpdatePacket(
            final BaseUser<?> user,
            final Player other,
            Equipment equipment
    ) {
//        final Player player = user.getPlayer();
//        if (player == null) return;
        final Location location = user.getLocation();
        if (location == null) return;
        final int entityId = user.getEntityId();
        if (other == null) return;
        if (!user.shouldShow(other)) return;
        if (!this.settings.getCosmeticSettings().isInViewDistance(location, other.getLocation())) return;
        PacketManager.sendEquipmentPacket(equipment, entityId, other);
    }
}
