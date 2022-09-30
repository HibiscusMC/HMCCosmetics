package io.github.fisher2911.hmccosmetics.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.config.CosmeticSettings;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.inventory.PlayerArmor;
import io.github.fisher2911.hmccosmetics.packet.PacketManager;
import io.github.fisher2911.hmccosmetics.packet.wrappers.WrapperPlayClientWindowClick;
import io.github.fisher2911.hmccosmetics.packet.wrappers.WrapperPlayServerWindowItems;
import io.github.fisher2911.hmccosmetics.task.DelayedTask;
import io.github.fisher2911.hmccosmetics.task.TaskManager;
import io.github.fisher2911.hmccosmetics.user.Equipment;
import io.github.fisher2911.hmccosmetics.user.User;
import io.github.fisher2911.hmccosmetics.user.UserManager;
import io.github.fisher2911.hmccosmetics.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;

public class CosmeticFixListener implements Listener {

    private final HMCCosmetics plugin;
    private final UserManager userManager;
    private final CosmeticSettings settings;
    private final TaskManager taskManager;

    public CosmeticFixListener(final HMCCosmetics plugin) {
        this.plugin = plugin;
        this.userManager = this.plugin.getUserManager();
        this.settings = this.plugin.getSettings().getCosmeticSettings();
        this.taskManager = this.plugin.getTaskManager();
        this.registerPacketListeners();
    }

    final Map<EquipmentSlot, Set<Material>> ARMOR_ITEMS = Map.of(
            EquipmentSlot.HEAD, EnumSet.of(
                    Material.LEATHER_HELMET,
                    Material.CHAINMAIL_HELMET,
                    Material.IRON_HELMET,
                    Material.GOLDEN_HELMET,
                    Material.DIAMOND_HELMET,
                    Material.NETHERITE_HELMET,
                    Material.TURTLE_HELMET
            ),
            EquipmentSlot.CHEST, EnumSet.of(
                    Material.LEATHER_CHESTPLATE,
                    Material.CHAINMAIL_CHESTPLATE,
                    Material.IRON_CHESTPLATE,
                    Material.GOLDEN_CHESTPLATE,
                    Material.DIAMOND_CHESTPLATE,
                    Material.NETHERITE_CHESTPLATE,
                    Material.ELYTRA
            ),
            EquipmentSlot.LEGS, EnumSet.of(
                    Material.LEATHER_LEGGINGS,
                    Material.CHAINMAIL_LEGGINGS,
                    Material.IRON_LEGGINGS,
                    Material.GOLDEN_LEGGINGS,
                    Material.DIAMOND_LEGGINGS,
                    Material.NETHERITE_LEGGINGS
            ),
            EquipmentSlot.FEET, EnumSet.of(
                    Material.LEATHER_BOOTS,
                    Material.CHAINMAIL_BOOTS,
                    Material.IRON_BOOTS,
                    Material.GOLDEN_BOOTS,
                    Material.DIAMOND_BOOTS,
                    Material.NETHERITE_BOOTS
            )
    );

    public void registerPacketListeners() {
        this.registerMenuChangeListener();
        this.registerInventoryClickListener();
    }

    @EventHandler
    public void onHoldItem(final PlayerItemHeldEvent event) {
        this.userManager.get(event.getPlayer().getUniqueId()).ifPresent(user -> {
            if (user.isWardrobeActive()) event.setCancelled(true);
        });
    }

    @EventHandler
    public void onShiftClick(final InventoryClickEvent event) {
        if (event.getClick() != ClickType.SHIFT_LEFT && event.getClick() != ClickType.SHIFT_RIGHT) return;
        if (!(event.getWhoClicked() instanceof final Player player)) return;
        final ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;
        final EquipmentSlot slot = this.getArmorSlot(clicked.getType());
        if (slot == null) return;
        final Optional<User> user = this.userManager.get(player.getUniqueId());
        if (user.isEmpty()) return;
        final ArmorItem.Type type = ArmorItem.Type.fromWrapper(slot);
        if (type == null) return;

        this.taskManager.submit(() -> {

            final EntityEquipment equipment = player.getEquipment();
            final ItemStack current;
            if (equipment == null) {
                current = new ItemStack(Material.AIR);
            } else {
                current = equipment.getItem(slot);
            }
            updateOnClick(
                    player,
                    slot,
                    user.get(),
                    type,
                    current
            );
        });
    }

    private void registerInventoryClickListener() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(HMCCosmetics.getPlugin(HMCCosmetics.class), ListenerPriority.NORMAL, PacketType.Play.Client.WINDOW_CLICK) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                Player player = event.getPlayer();
                int invTypeClicked = event.getPacket().getIntegers().read(0);
                int slotClicked = event.getPacket().getIntegers().read(2);

                // Must be a player inventory.
                if (invTypeClicked != 0) return;
                // -999 is when a player clicks outside their inventory. https://wiki.vg/Inventory#Player_Inventory
                if (slotClicked == -999) return;
                if (!(event.getPlayer() instanceof Player)) return;
                taskManager.submit(() -> {
                    final Optional<User> optionalUser = userManager.get(player.getUniqueId());
                    if (optionalUser.isEmpty()) return;
                    final User user = optionalUser.get();
                    EquipmentSlot slot = getPacketArmorSlot(slotClicked);
                    if (slot == null) {
                        return;
                    }
                    final EntityEquipment entityEquipment = player.getEquipment();
                    if (entityEquipment == null) return;
                    final ItemStack current = Utils.replaceIfNull(entityEquipment.getItem(slot), new ItemStack(Material.AIR));
                    final ArmorItem.Type type = ArmorItem.Type.fromEquipmentSlot(slot);
                    if (type == null) return;
                    updateOnClick(
                            player,
                            slot,
                            user,
                            type,
                            current
                    );
                });
            }
        });
    }

    private void updateOnClick(final Player player, final EquipmentSlot slot, final User user, final ArmorItem.Type type, final ItemStack current) {
        //plugin.getLogger().log(Level.INFO, "updateOnClick (171)");

        final Location location = player.getLocation();
        final Equipment equipment = new Equipment();
        final ItemStack cosmetic = userManager.getCosmeticItem(
                user.getPlayerArmor().getItem(type),
                current,
                ArmorItem.Status.APPLIED,
                slot
        );
        //plugin.getLogger().log(Level.INFO, "Set cosmetic in " + slot + " to " + cosmetic);
        if (cosmetic != null && cosmetic.getType() != Material.AIR) equipment.setItem(slot, cosmetic);
        //plugin.getLogger().log(Level.INFO, "Set cosmetic in " + slot + " to " + cosmetic + "(done)");

        /*
        final Equipment items = userManager.getItemList(user, equipment, Collections.emptySet());
        for (EquipmentSlot e : items.keys()) {
            //final EquipmentSlot s = e.getSlot();
            final ArmorItem.Type t = ArmorItem.Type.fromWrapper(e);
            ItemStack air = new ItemStack(Material.AIR);
            if (t == null) {
                //plugin.getLogger().log(Level.INFO, "T is null");
                equipment.setItem(e, air);
                return;
            }
            final ArmorItem armorItem = user.getPlayerArmor().getItem(t);
            final ItemStack i = equipment.getItem(e);
            if (i == null) {
                //plugin.getLogger().log(Level.INFO, "I is null");
                equipment.setItem(e, air);
                return;
            }
            Boolean remove = armorItem.isEmpty() && i.equals(equipment.getItem(t.getSlot()));
            if (remove) {
                //plugin.getLogger().log(Level.INFO, "Boolean is true");
                equipment.setItem(e, air);
                return;
            }
            return;
        }
         */
        for (final Player other : Bukkit.getOnlinePlayers()) {
            if (!settings.isInViewDistance(location, other.getLocation())) continue;
            userManager.sendUpdatePacket(
                    user,
                    equipment
            );
        }
    }

    private void registerMenuChangeListener() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(HMCCosmetics.getPlugin(HMCCosmetics.class), ListenerPriority.NORMAL, PacketType.Play.Server.WINDOW_ITEMS) {
            @Override
            public void onPacketSending(PacketEvent event) {
                Player player = event.getPlayer();
                if (event.getPlayer() == null) return;
                WrapperPlayServerWindowItems wrapper = new WrapperPlayServerWindowItems(event.getPacket());
                if (!(event.getPlayer() instanceof Player)) return;
                final int windowId = wrapper.getWindowId();

                List<ItemStack> items = wrapper.getSlotData();
                taskManager.submit(() -> {
                    final Optional<User> optionalUser = userManager.get(player.getUniqueId());
                    if (optionalUser.isEmpty()) return;
                    final User user = optionalUser.get();
                    if (windowId != 0) return;
                    final int size = items.size();
                    final PlayerArmor playerArmor = user.getPlayerArmor();
                    final Equipment equip = userManager.getItemList(user);
                    for (final ArmorItem armorItem : playerArmor.getArmorItems()) {
                        final ArmorItem.Type type = armorItem.getType();
                        final EquipmentSlot slot = type.getSlot();
                        if (slot == null) continue;
                        final int packetSlot = getPacketArmorSlot(slot);
                        if (packetSlot == -1) continue;
                        if (packetSlot >= size) continue;

                        final ItemStack current = (items.get(packetSlot));
                        final ItemStack setTo =
                                (userManager.getCosmeticItem(
                                        armorItem,
                                        current,
                                        ArmorItem.Status.APPLIED,
                                        slot
                                ));
                        if ((current).equals(setTo)) continue;
                        equip.setItem(slot, setTo);
                    }
                    userManager.sendUpdatePacket(
                            user,
                            equip
                    );
                });
            }
        });
    }

    private int getPacketArmorSlot(final EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> 5;
            case CHEST -> 6;
            case LEGS -> 7;
            case FEET -> 8;
            case OFF_HAND -> 45;
            default -> -1;
        };
    }

    @Nullable
    public EquipmentSlot getPacketArmorSlot(final int slot) {
        return switch (slot) {
            case 5 -> EquipmentSlot.HEAD;
            case 6 -> EquipmentSlot.CHEST;
            case 7 -> EquipmentSlot.LEGS;
            case 8 -> EquipmentSlot.FEET;
            case 45 -> EquipmentSlot.OFF_HAND;
            default -> null;
        };
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onOffhandSwap(final PlayerSwapHandItemsEvent event) {
        final ItemStack offHand = event.getOffHandItem();
        if (offHand != null && offHand.getType() != Material.AIR) {
            return;
        }
        final Player player = event.getPlayer();
        this.fixOffHand(player, new ItemStack(Material.AIR));
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockPlace(final BlockPlaceEvent event) {
        if (event.getHand() != EquipmentSlot.OFF_HAND) {
            return;
        }
        final ItemStack itemStack = event.getItemInHand();

        if (itemStack.getAmount() > 1) {
            return;
        }

        this.fixOffHand(event.getPlayer(), new ItemStack(Material.AIR));
    }

//    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
//    public void onShiftInventoryClick(final InventoryClickEvent event) {
//        final ClickType clickType = event.getClick();
//        if (clickType != ClickType.SHIFT_LEFT && clickType != ClickType.SHIFT_RIGHT) return;
//        final ItemStack clicked = event.getCurrentItem();
//        if (clicked == null || clicked.getType() == Material.AIR) return;
//        final EquipmentSlot slot = this.getArmorSlot(clicked.getType());
//        if (slot == null) return;
//        if (!(event.getWhoClicked() instanceof final Player player)) return;
//        final EntityEquipment entityEquipment = player.getEquipment();
//        if (entityEquipment == null) return;
//        final Equipment equipment = Equipment.fromEntityEquipment(entityEquipment);
//        final Optional<User> optionalUser = this.userManager.get(player.getUniqueId());
//        if (optionalUser.isEmpty()) return;
//        final User user = optionalUser.get();
//        player.sendMessage("Sent update packet in listener");
//        final var itemList = this.getItemList(user, equipment, Collections.emptySet());
//        this.taskManager.submit(() -> this.userManager.sendUpdatePacket(
//                player.getEntityId(),
//                player,
//                itemList
//        ));
//    }

    private void fixOffHand(final Player player, final ItemStack current) {
        final Optional<User> optionalUser = this.userManager.get(player.getUniqueId());
        if (optionalUser.isEmpty()) return;
        final User user = optionalUser.get();
        final ArmorItem.Type type = ArmorItem.Type.OFF_HAND;
        final ArmorItem armorItem = user.getPlayerArmor().getItem(type);
        this.taskManager.submit(new DelayedTask(() -> this.userManager.sendUpdatePacket(
                user,
                armorItem,
                current,
                type
        ), 1));
    }

    @Nullable
    private EquipmentSlot getArmorSlot(final Material material) {
        for (final EquipmentSlot slot : EquipmentSlot.values()) {
            final Set<Material> armorItems = ARMOR_ITEMS.get(slot);
            if (armorItems == null) continue;
            if (material == null) continue;
            if (armorItems.contains(material)) return slot;
        }
        return null;
    }
    /*
    private void fixCosmetics(final Player player) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(this.plugin,
                () -> this.userManager.updateCosmetics(player.getUniqueId()), 2);
    }
     */

}
