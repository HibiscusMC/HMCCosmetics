package io.github.fisher2911.hmccosmetics.listener;

import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.config.CosmeticSettings;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.task.DelayedTask;
import io.github.fisher2911.hmccosmetics.task.TaskManager;
import io.github.fisher2911.hmccosmetics.user.User;
import io.github.fisher2911.hmccosmetics.user.UserManager;
import org.bukkit.Bukkit;
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

import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
        /*
        PacketEvents.getAPI().getEventManager().registerListener(
                new PacketListenerAbstract() {
                    @Override
                    public void onPacketReceive(PacketReceiveEvent event) {
                        if (event.getPacketType() != PacketType.Play.Client.CLICK_WINDOW) return;
                        final WrapperPlayClientClickWindow packet = new WrapperPlayClientClickWindow(event);
                        if (packet.getWindowId() != 0) return;
                        if (!(event.getPlayer() instanceof final Player player)) return;
                        int slotClicked = packet.getSlot();
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
                }
        );
         */
    }

    private void updateOnClick(final Player player, final EquipmentSlot slot, final User user, final ArmorItem.Type type, final ItemStack current) {
        /*
        final Location location = player.getLocation();
        final Equipment equipment = Equipment.fromEntityEquipment(player.getEquipment());
        final ItemStack cosmetic = userManager.getCosmeticItem(
                user.getPlayerArmor().getItem(type),
                current,
                ArmorItem.Status.APPLIED,
                slot
        );
        if (cosmetic != null && cosmetic.getType() != Material.AIR) equipment.setItem(slot, cosmetic);

        final List<Equipment> items =
                userManager.getItemList(user, equipment, Collections.emptySet());
        items.removeIf(e -> {
            //final EquipmentSlot s = e.getSlot();
            final ArmorItem.Type t = ArmorItem.Type.fromPacketSlot(slot);
            if (t == null) return false;
            final ArmorItem armorItem = user.getPlayerArmor().getItem(t);
            final ItemStack i = e.getItem(slot);
            return armorItem.isEmpty() && i.equals(equipment.getItem(t.getSlot()));
        });
        for (final Player other : Bukkit.getOnlinePlayers()) {
            if (!settings.isInViewDistance(location, other.getLocation())) continue;
            userManager.sendUpdatePacket(
                    user,
                    items
            );
        }
         */
    }

    private void registerMenuChangeListener() {
        /*
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(HMCCosmetics.getPlugin(HMCCosmetics.class), ListenerPriority.NORMAL) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (!event.getPacketType().equals(PacketType.Play.Server.WINDOW_ITEMS)) return;
                if (event.getPlayer() == null) return;
                WrapperPlayServerWindowItems wrapper = new WrapperPlayServerWindowItems(event.getPacket());
                final int windowId = wrapper.getWindowId();

                List<ItemStack> items = new ArrayList<>();
                int count = items.size();
                for (int i = 0; i < count; i++) {
                    items.add(wrapper.getSlotData().get(count));
                }


                //final WrapperPlayServerWindowItems packet = new WrapperPlayServerWindowItems(event);



                //final int windowId = packet.getWindowId();
            }
        });
         */
        /*
        PacketEvents.getAPI().getEventManager().registerListener(
                new PacketListenerAbstract() {
                    @Override
                    public void onPacketSend(PacketSendEvent event) {
                        if (event.getPacketType() != PacketType.Play.Server.WINDOW_ITEMS) return;
                        final WrapperPlayServerWindowItems packet = new WrapperPlayServerWindowItems(event);
                        if (!(event.getPlayer() instanceof final Player player)) return;
                        final int windowId = packet.getWindowId();
                        final List<ItemStack> itemStacks = packet.getItems();
                        taskManager.submit(() -> {
                            final Optional<User> optionalUser = userManager.get(player.getUniqueId());
                            if (optionalUser.isEmpty()) return;
                            final User user = optionalUser.get();
                            if (windowId != 0) return;
                            final int size = itemStacks.size();
                            final PlayerArmor playerArmor = user.getPlayerArmor();
                            final List<Equipment> equipmentList = new ArrayList<>();
                            for (final ArmorItem armorItem : playerArmor.getArmorItems()) {
                                final ArmorItem.Type type = armorItem.getType();
                                final EquipmentSlot slot = type.getSlot();
                                if (slot == null) continue;
                                final int packetSlot = getPacketArmorSlot(slot);
                                if (packetSlot == -1) continue;
                                if (packetSlot >= size) continue;

                                final ItemStack current = (itemStacks.get(packetSlot));
                                final ItemStack setTo =
                                        (userManager.getCosmeticItem(
                                                armorItem,
                                                current,
                                                ArmorItem.Status.APPLIED,
                                                slot
                                        ));
                                if ((current).equals(setTo)) continue;
                                equipmentList.add(PacketManager.getEquipment(setTo, slot));
                            }
                            userManager.sendUpdatePacket(
                                    user,
                                    equipmentList
                            );
                        });
                        packet.setItems(itemStacks);
                    }
                }
        );
         */
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

    private void fixCosmetics(final Player player) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(this.plugin,
                () -> this.userManager.updateCosmetics(player.getUniqueId()), 2);
    }

}
