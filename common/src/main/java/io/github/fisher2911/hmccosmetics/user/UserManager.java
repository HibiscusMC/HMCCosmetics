package io.github.fisher2911.hmccosmetics.user;

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
                this.getItemList(
                        user,
                        user.getEquipment(),
                        Collections.emptySet()
                )
        );
    }

    public void updateCosmetics(final BaseUser<?> user) {
        this.sendUpdatePacket(
                user,
                this.getItemList(user, user.getEquipment(), Collections.emptySet())
        );
//        for (final Player player : Bukkit.getOnlinePlayers()) {
//            this.updateCosmetics(user, player);
//        }
    }

//    public void updateCosmetics(final BaseUser<?> user, final Player other) {
//        final Equipment equipment = user.getEquipment();
//        for (final ArmorItem.Type type : ArmorItem.Type.values()) {
//            if (type.getSlot() == null) continue;
//            this.sendUpdatePacket(
//                    user,
//                    other,
//                    equipment,
//                    type
//            );
//        }
//    }

//    private void sendUpdatePacket(
//            final BaseUser<?> user,
//            final Player other,
//            final Equipment equipment,
//            final ArmorItem.Type type) {
//        final PlayerArmor playerArmor = user.getPlayerArmor();
//        final EquipmentSlot slot = type.getSlot();
//        final ItemStack itemStack = this.getCosmeticItem(playerArmor.getItem(type), equipment.getItem(type.getSlot()), ArmorItem.Status.APPLIED, slot);
//        if (itemStack != null && itemStack.equals(equipment.getItem(slot))) return;
//        final List<com.github.retrooper.packetevents.protocol.player.Equipment> itemList = new ArrayList<>();
//        itemList.add(new com.github.retrooper.packetevents.protocol.player.Equipment(
//                PacketManager.fromBukkitSlot(slot), SpigotDataHelper.fromBukkitItemStack(itemStack)
//        ));
//        PacketManager.sendEquipmentPacket(
//                itemList,
//                user.getEntityId(),
//                other
//        );
//    }

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

    public List<com.github.retrooper.packetevents.protocol.player.Equipment> getItemList(
            final BaseUser<?> user,
            final Equipment equipment,
            final Set<ArmorItem.Type> ignored
    ) {
        final PlayerArmor armor = user.getPlayerArmor();
        final List<com.github.retrooper.packetevents.protocol.player.Equipment> items = new ArrayList<>();
        for (final ArmorItem.Type type : ArmorItem.Type.values()) {
            final EquipmentSlot slot = type.getSlot();
            if (slot == null) continue;
            if (ignored.contains(type)) {
                items.add(PacketManager.getEquipment(equipment.getItem(slot), slot));
                continue;
            }
            final ItemStack wearing = Utils.replaceIfNull(equipment.getItem(slot), new ItemStack(Material.AIR));
            final ItemStack itemStack = this.getCosmeticItem(
                    armor.getItem(type),
                    wearing,
                    ArmorItem.Status.APPLIED,
                    slot
            );
            if (itemStack.getType() != Material.AIR) items.add(PacketManager.getEquipment(itemStack, slot));
        }
        return items;
    }

    public void setItem(final BaseUser<?> user, final ArmorItem armorItem) {
        ArmorItem previous = user.getPlayerArmor().getItem(armorItem.getType());

        final CosmeticChangeEvent event =
                new CosmeticChangeEvent(new CosmeticItem(armorItem.copy()), new CosmeticItem(previous.copy()), user);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        final ArmorItem.Type type = armorItem.getType();
        if (type == ArmorItem.Type.BALLOON) user.despawnBalloon();
        user.setItem(event.getCosmeticItem().getArmorItem());
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            switch (type) {
                case HAT, OFF_HAND, CHEST_PLATE, PANTS, BOOTS -> this.updateCosmetics(user);
                case BACKPACK -> {
                    user.despawnAttached();
                    if (user instanceof Wardrobe) user.updateOutsideCosmetics(settings);
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
        final List<com.github.retrooper.packetevents.protocol.player.Equipment> itemList = new ArrayList<>();
        itemList.add(PacketManager.getEquipment(itemStack, slot));
        this.sendUpdatePacket(user, itemList);
    }

    public void sendUpdatePacket(
            final BaseUser<?> user,
            List<com.github.retrooper.packetevents.protocol.player.Equipment> items
    ) {
//        final Player player = user.getPlayer();
//        if (player == null) return;
        final Location location = user.getLocation();
        if (location == null) return;
        final int entityId = user.getEntityId();
        for (final User otherUser : this.userMap.values()) {
            final Player other = otherUser.getPlayer();
            if (other == null) continue;
            if (!user.shouldShow(other)) continue;
            if (!this.settings.getCosmeticSettings().isInViewDistance(location, other.getLocation())) continue;
            PacketManager.sendEquipmentPacket(
                    items,
                    entityId,
                    other
            );
        }
    }

    public void sendUpdatePacket(
            final BaseUser<?> user,
            final Player other,
            List<com.github.retrooper.packetevents.protocol.player.Equipment> items
    ) {
//        final Player player = user.getPlayer();
//        if (player == null) return;
        final Location location = user.getLocation();
        if (location == null) return;
        final int entityId = user.getEntityId();
        if (other == null) return;
        if (!user.shouldShow(other)) return;
        if (!this.settings.getCosmeticSettings().isInViewDistance(location, other.getLocation())) return;
        PacketManager.sendEquipmentPacket(
                items,
                entityId,
                other
        );
    }
}
