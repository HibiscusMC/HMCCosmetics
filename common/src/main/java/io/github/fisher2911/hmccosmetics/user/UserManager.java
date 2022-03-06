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
import io.github.fisher2911.hmccosmetics.task.InfiniteTask;
import io.github.fisher2911.hmccosmetics.util.builder.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        this.plugin.getTaskManager().submit(new InfiniteTask(
                () -> {
                    for (final User user : this.userMap.values()) {
                        user.updateOutsideCosmetics(this.plugin.getSettings());
                    }
                }
        ));
    }

    public void resendCosmetics(final Player player) {
        for (final User user : this.userMap.values()) {
            final Player p = user.getPlayer();
            if (p == null) continue;
            user.updateOutsideCosmetics(player, p.getLocation(), this.settings);
            this.updateCosmetics(user, player);
        }
    }

    public void updateCosmetics(final UUID uuid) {
        this.get(uuid).ifPresent(this::updateCosmetics);
    }

    public void updateCosmetics(final BaseUser user) {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            this.updateCosmetics(user, player);
        }
    }

    public void updateCosmetics(final BaseUser user, final Player other) {
//        final Player player = user.getPlayer();
        final Equipment equipment = user.getEquipment();

        for (final ArmorItem.Type type : ArmorItem.Type.values()) {
            if (type.getSlot() == null) continue;
            this.sendUpdatePacket(
                    user,
                    other,
                    equipment,
                    type
            );
        }
    }

    private void sendUpdatePacket(
            final BaseUser user,
            final Player other,
            final Equipment equipment,
            final ArmorItem.Type type) {
        final PlayerArmor playerArmor = user.getPlayerArmor();
        final EquipmentSlot slot = type.getSlot();
        final ItemStack itemStack = this.getCosmeticItem(user, equipment, playerArmor.getItem(type), ArmorItem.Status.APPLIED, slot);
        if (itemStack != null && itemStack.equals(equipment.getItem(slot))) return;
        final List<Pair<EnumWrappers.ItemSlot, ItemStack>> itemList = new ArrayList<>();
        itemList.add(new Pair<>(EnumWrappers.ItemSlot.valueOf(slot.toString().replace("_", "")), itemStack));
        PacketManager.sendPacket(
                other,
                PacketManager.getEquipmentPacket(
                        itemList,
                        user.getEntityId()
                )
        );
    }

    private ItemStack getCosmeticItem(
            final BaseUser user,
            final Equipment equipment,
            final ArmorItem armorItem,
            final ArmorItem.Status status,
            final EquipmentSlot slot
    ) {
        final CosmeticSettings cosmeticSettings = this.settings.getCosmeticSettings();

        final Map<String, String> placeholders = Map.of(Placeholder.ALLOWED, Translation.TRUE,
                Placeholder.ENABLED, Translation.TRUE);

        ItemStack itemStack = ItemBuilder.from(armorItem.getItemStack(status)).
                namePlaceholders(placeholders).
                lorePlaceholders(placeholders).
                build();


        final boolean isAir = itemStack.getType().isAir();
        final boolean requireEmpty = cosmeticSettings.requireEmpty(slot);


        if (!isAir && (!requireEmpty || user instanceof Wardrobe)) {
            return itemStack;
        }

        if (equipment == null) {
            return itemStack;
        }

        final ItemStack equipped = equipment.getItem(slot);

        if (equipped != null && (equipped.getType() != Material.AIR && !user.isWardrobeActive())) {
            return equipped;
        }

        return itemStack;
    }

    public void setItem(final BaseUser user, final ArmorItem armorItem) {
        ArmorItem previous = user.getPlayerArmor().getItem(armorItem.getType());

        final CosmeticChangeEvent event =
                new CosmeticChangeEvent(new CosmeticItem(armorItem.copy()), new CosmeticItem(previous.copy()), user);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        user.setItem(event.getCosmeticItem().getArmorItem());
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            switch (armorItem.getType()) {
                case HAT, OFF_HAND, CHEST_PLATE, PANTS, BOOTS -> {
                    this.updateCosmetics(user);
                }
                case BACKPACK -> {
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
}
