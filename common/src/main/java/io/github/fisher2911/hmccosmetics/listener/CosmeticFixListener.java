package io.github.fisher2911.hmccosmetics.listener;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.config.CosmeticSettings;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.inventory.PlayerArmor;
import io.github.fisher2911.hmccosmetics.message.Placeholder;
import io.github.fisher2911.hmccosmetics.message.Translation;
import io.github.fisher2911.hmccosmetics.packet.PacketManager;
import io.github.fisher2911.hmccosmetics.task.DelayedTask;
import io.github.fisher2911.hmccosmetics.task.TaskManager;
import io.github.fisher2911.hmccosmetics.user.Equipment;
import io.github.fisher2911.hmccosmetics.user.User;
import io.github.fisher2911.hmccosmetics.user.UserManager;
import io.github.fisher2911.hmccosmetics.util.Utils;
import io.github.fisher2911.hmccosmetics.util.builder.ItemBuilder;
import io.github.retrooper.packetevents.util.SpigotDataHelper;
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
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
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

    private void registerClickAirListener() {
//        PacketEvents.getAPI().getEventManager().registerListener(
//                new PacketListener() {
//                    @Override
//                    public void onPacketReceive(PacketReceiveEvent event) {
//
//                    }
//                }.asAbstract(PacketListenerPriority.HIGHEST, false, true)
//        );
//        ProtocolLibrary.getProtocolManager().addPacketListener(
//                new PacketAdapter(this.plugin, ListenerPriority.MONITOR, PacketType.Play.Client.BLOCK_PLACE) {
//                    @Override
//                    public void onPacketReceiving(PacketEvent event) {
//                        final Player player = event.getPlayer();
//                        final Optional<User> optionalUser = CosmeticFixListener.this.userManager.get(player.getUniqueId());
//                        if (optionalUser.isEmpty()) return;
//                        final ItemStack inHand = player.getInventory().getItemInMainHand();
//                        if (inHand == null || inHand.getType() == Material.AIR) return;
//                        final EquipmentSlot slot = getArmorSlot(inHand.getType());
//                        if (slot == null) {
//                            return;
//                        }
//                        final ArmorItem.Type type = ArmorItem.Type.fromEquipmentSlot(slot);
//                        if (type == null) {
//                            return;
//                        }
//                        final User user = optionalUser.get();
//                        final Location location = player.getLocation();
//                        taskManager.submit(() -> {
//                            final EntityEquipment entityEquipment = player.getEquipment();
//                            final Equipment equipment;
//                            if (entityEquipment == null) {
//                                equipment = Equipment.fromEntityEquipment(player.getEquipment());
//                            } else {
//                                equipment = new Equipment();
//                            }
//                            equipment.setItem(
//                                    slot,
//                                    getCosmeticItem(
//                                            user.getPlayerArmor().getItem(type),
//                                            inHand,
//                                            ArmorItem.Status.APPLIED,
//                                            slot
//                                    )
//                            );
//                            final List<Pair<EnumWrappers.ItemSlot, ItemStack>> items = getItemList(player, user, equipment, Set.of(type));
//                            for (final Player p : Bukkit.getOnlinePlayers()) {
//                                if (!settings.isInViewDistance(location, p.getLocation())) continue;
//                                sendUpdatePacket(
//                                        user.getEntityId(),
//                                        p,
//                                        items
//                                );
//                            }
//                        });
//                    }
//                }
//        );
    }

    @EventHandler
    public void onShiftClick(final InventoryClickEvent event) {
        if (event.getClick() != ClickType.SHIFT_LEFT && event.getClick() != ClickType.SHIFT_RIGHT) return;
        if (!(event.getWhoClicked() instanceof final Player player)) return;
        final int clickedSlot = event.getSlot();
        final ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;
        final EquipmentSlot slot = this.getArmorSlot(clicked.getType());
        if (slot == null) return;
        final Optional<User> user = this.userManager.get(player.getUniqueId());
        if (user.isEmpty()) return;
        final ArmorItem.Type type = ArmorItem.Type.fromWrapper(slot);
        if (type == null) return;
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
    }

    private void registerInventoryClickListener() {
        PacketEvents.getAPI().getEventManager().registerListener(
                new PacketListenerAbstract() {
                    @Override
                    public void onPacketReceive(PacketReceiveEvent event) {
                        if (event.getPacketType() != PacketType.Play.Client.CLICK_WINDOW) return;
                        final WrapperPlayClientClickWindow packet = new WrapperPlayClientClickWindow(event);
                        if (packet.getWindowId() != 0) return;
                        if (!(event.getPlayer() instanceof final Player player)) return;
                        final Optional<User> optionalUser = userManager.get(player.getUniqueId());
                        if (optionalUser.isEmpty()) return;
                        final User user = optionalUser.get();
                        int slotClicked = packet.getSlot();
                        final WrapperPlayClientClickWindow.WindowClickType clickType = packet.getWindowClickType();
                        EquipmentSlot slot = getPacketArmorSlot(slotClicked);
                        if (slot == null) return;
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
                    }
                }
        );
    }

    private void updateOnClick(final Player player, final EquipmentSlot slot, final User user, final ArmorItem.Type type, final ItemStack current) {
        taskManager.submit(() -> {
            final Location location = player.getLocation();
            final Equipment equipment = Equipment.fromEntityEquipment(player.getEquipment());
            final ItemStack cosmetic = getCosmeticItem(
                    user.getPlayerArmor().getItem(type),
                    current,
                    ArmorItem.Status.APPLIED,
                    slot
            );
            if (cosmetic != null && cosmetic.getType() != Material.AIR) equipment.setItem(slot, cosmetic);

            final List<com.github.retrooper.packetevents.protocol.player.Equipment> items =
                    getItemList(user, equipment, Set.of(type));
            for (final Player other : Bukkit.getOnlinePlayers()) {
                if (!settings.isInViewDistance(location, other.getLocation())) continue;
                sendUpdatePacket(
                        user.getEntityId(),
                        other,
                        items
                );
            }
        });
    }

    private void registerMenuChangeListener() {
        PacketEvents.getAPI().getEventManager().registerListener(
                new PacketListenerAbstract() {
                    @Override
                    public void onPacketSend(PacketSendEvent event) {
                        if (event.getPacketType() != PacketType.Play.Server.WINDOW_ITEMS) return;
                        final WrapperPlayServerWindowItems packet = new WrapperPlayServerWindowItems(event);
                        if (!(event.getPlayer() instanceof final Player player)) return;
                        final Optional<User> optionalUser = userManager.get(player.getUniqueId());
                        if (optionalUser.isEmpty()) return;
                        final User user = optionalUser.get();
                        if (packet.getWindowId() != 0) return;
                        final List<com.github.retrooper.packetevents.protocol.item.ItemStack> itemStacks = packet.getItems();
                        final int size = itemStacks.size();
                        final PlayerArmor playerArmor = user.getPlayerArmor();
                        for (final ArmorItem armorItem : playerArmor.getArmorItems()) {
                            final ArmorItem.Type type = armorItem.getType();
                            final EquipmentSlot slot = type.getSlot();
                            if (slot == null) continue;
                            final int packetSlot = getPacketArmorSlot(slot);
                            if (packetSlot == -1) continue;
                            if (packetSlot >= size) continue;
                            final ItemStack current = SpigotDataHelper.toBukkitItemStack(itemStacks.get(packetSlot));
                            final com.github.retrooper.packetevents.protocol.item.ItemStack setTo =
                                    SpigotDataHelper.fromBukkitItemStack(getCosmeticItem(
                                    armorItem,
                                    current,
                                    ArmorItem.Status.APPLIED,
                                    slot
                            ));
                            itemStacks.set(packetSlot, setTo);
                        }
                        packet.setItems(itemStacks);
                    }
                }
        );
    }

//    @EventHandler
//    public void onBlockClick(final PlayerInteractEvent event) {
//        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
//        final Player player = event.getPlayer();
//        final Block block = event.getClickedBlock();
//        if (block != null && block.getType().isInteractable() && !player.isSneaking()) return;
//        final ItemStack clickedWith = event.getItem();
//        if (clickedWith == null) return;
//        this.checkFix(player, -1, clickedWith);
//    }

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

//    @EventHandler
//    public void onEntityMount(final EntityMountEvent event) {
//        if (!(event.getEntity() instanceof final Player player)) {
//            return;
//        }
//        this.fixCosmetics(player);
//    }
//
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

    private void fixOffHand(final Player player, final ItemStack current) {
        final Optional<User> optionalUser = this.userManager.get(player.getUniqueId());
        if (optionalUser.isEmpty()) return;
        final User user = optionalUser.get();
        final ArmorItem.Type type = ArmorItem.Type.OFF_HAND;
        final ArmorItem armorItem = user.getPlayerArmor().getItem(type);
        this.taskManager.submit(new DelayedTask(() -> this.sendUpdatePacket(
                player.getEntityId(),
                player,
                armorItem,
                current,
                type
        ), 1));
    }

    private List<com.github.retrooper.packetevents.protocol.player.Equipment> getItemList(
            final User user,
            final Equipment equipment,
            final Set<ArmorItem.Type> ignored
    ) {
        final PlayerArmor armor = user.getPlayerArmor();
        final List<com.github.retrooper.packetevents.protocol.player.Equipment> items = new ArrayList<>();
        for (final ArmorItem.Type type : ArmorItem.Type.values()) {
            final EquipmentSlot slot = type.getSlot();
            if (slot == null) continue;
            if (ignored.contains(type)) {
                items.add(this.getEquipment(equipment.getItem(slot), slot));
                continue;
            }
            final ItemStack wearing = Utils.replaceIfNull(equipment.getItem(slot), new ItemStack(Material.AIR));
            final ItemStack itemStack = this.getCosmeticItem(
                    armor.getItem(type),
                    wearing,
                    ArmorItem.Status.APPLIED,
                    slot
            );
            if (itemStack.getType() != Material.AIR) items.add(this.getEquipment(itemStack, slot));
        }
        return items;
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

    private void sendUpdatePacket(
            final int entityId,
            final Player other,
            final ArmorItem armorItem,
            final ItemStack wearing,
            final ArmorItem.Type type) {
        final EquipmentSlot slot = type.getSlot();
        final ItemStack itemStack = this.getCosmeticItem(armorItem, wearing, ArmorItem.Status.APPLIED, slot);
        final List<com.github.retrooper.packetevents.protocol.player.Equipment> itemList = new ArrayList<>();
        itemList.add(this.getEquipment(itemStack, slot));
        this.sendUpdatePacket(entityId, other, itemList);
    }

    private void sendUpdatePacket(
            final int entityId,
            final Player other,
            List<com.github.retrooper.packetevents.protocol.player.Equipment> items) {
        PacketManager.sendEquipmentPacket(
                items,
                entityId,
                other
        );

    }

    private com.github.retrooper.packetevents.protocol.player.Equipment getEquipment(
            final ItemStack itemStack,
            final EquipmentSlot slot
    ) {
        return new com.github.retrooper.packetevents.protocol.player.Equipment(
                PacketManager.fromBukkitSlot(slot),
                SpigotDataHelper.fromBukkitItemStack(itemStack)
        );
    }

    private ItemStack getCosmeticItem(
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
        final boolean requireEmpty = settings.requireEmpty(slot);

        if (!isAir && requireEmpty) {
            return wearing;
        }

        return itemStack;
    }

}
