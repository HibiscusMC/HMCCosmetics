package com.hibiscusmc.hmccosmetics.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticArmorType;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import com.hibiscusmc.hmccosmetics.util.InventoryUtils;
import com.hibiscusmc.hmccosmetics.util.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PlayerGameListener implements Listener {

    public PlayerGameListener() {
        registerInventoryClickListener();
        registerMenuChangeListener();
        registerPlayerEquipmentListener();
    }

    @EventHandler
    public void onPlayerClick(@NotNull InventoryClickEvent event) {
        if (event.getClick() != ClickType.SHIFT_LEFT && event.getClick() != ClickType.SHIFT_RIGHT) return;
        //if (event.getSlotType() != InventoryType.SlotType.ARMOR) return;
        CosmeticUser user = CosmeticUsers.getUser(event.getWhoClicked().getUniqueId());
        if (user == null) return;
        EquipmentSlot slot = getArmorSlot(event.getCurrentItem().getType());
        if (slot == null) return;
        CosmeticSlot cosmeticSlot = InventoryUtils.BukkitCosmeticSlot(slot);
        if (cosmeticSlot == null) return;
        if (!user.hasCosmeticInSlot(cosmeticSlot)) return;
        Bukkit.getScheduler().runTaskLater(HMCCosmeticsPlugin.getInstance(), () -> {
            user.updateCosmetic(cosmeticSlot);
        }, 1);
        HMCCosmeticsPlugin.getInstance().getLogger().info("Event fired, updated cosmetic " + cosmeticSlot);
    }

    @EventHandler
    public void onPlayerShift(PlayerToggleSneakEvent event) {
        CosmeticUser user = CosmeticUsers.getUser(event.getPlayer().getUniqueId());

        if (!event.isSneaking()) return;
        if (user == null) return;
        if (!user.isInWardrobe()) return;

        user.leaveWardrobe();
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        CosmeticUser user = CosmeticUsers.getUser(event.getPlayer().getUniqueId());

        if (user.hasCosmeticInSlot(CosmeticSlot.BACKPACK)) {
            user.hideBackpack();

            user.getBackpackEntity().teleport(event.getTo());

            Bukkit.getScheduler().runTaskLater(HMCCosmeticsPlugin.getInstance(), () -> {
                user.showBackpack();
            }, 2);
        }
    }

    @EventHandler
    public void onPlayerLook(PlayerMoveEvent event) {
        CosmeticUser user = CosmeticUsers.getUser(event.getPlayer().getUniqueId());
        if (user == null) return;
        // Really need to look into optimization of this
        user.updateCosmetic(CosmeticSlot.BACKPACK);
        user.updateCosmetic(CosmeticSlot.BALLOON);
    }

    private void registerInventoryClickListener() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(HMCCosmeticsPlugin.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Client.WINDOW_CLICK) {
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

                CosmeticUser user = CosmeticUsers.getUser(player);
                if (user == null) return;
                CosmeticSlot cosmeticSlot = InventoryUtils.NMSCosmeticSlot(slotClicked);
                if (cosmeticSlot == null) return;
                if (!user.hasCosmeticInSlot(cosmeticSlot)) return;
                Bukkit.getScheduler().runTaskLater(HMCCosmeticsPlugin.getInstance(), () -> {
                    user.updateCosmetic(cosmeticSlot);
                }, 1);
                HMCCosmeticsPlugin.getInstance().getLogger().info("Packet fired, updated cosmetic " + cosmeticSlot);
            }
        });
    }

    private void registerMenuChangeListener() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(HMCCosmeticsPlugin.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Server.WINDOW_ITEMS) {
            @Override
            public void onPacketSending(PacketEvent event) {
                HMCCosmeticsPlugin.getInstance().getLogger().info("Menu Initial ");
                Player player = event.getPlayer();
                if (event.getPlayer() == null) return;
                if (!(event.getPlayer() instanceof Player)) return;

                int windowID = event.getPacket().getIntegers().read(0);
                List<ItemStack> slotData = event.getPacket().getItemListModifier().read(0);
                if (windowID != 0) return;

                CosmeticUser user = CosmeticUsers.getUser(player);
                if (user == null) return;

                for (Cosmetic cosmetic : user.getCosmetic()) {
                    if (!(cosmetic instanceof CosmeticArmorType)) continue;
                    Bukkit.getScheduler().runTaskLater(HMCCosmeticsPlugin.getInstance(), () -> {
                        user.updateCosmetic(cosmetic.getSlot());
                    }, 1);
                    HMCCosmeticsPlugin.getInstance().getLogger().info("Menu Fired, updated cosmetics " + cosmetic);
                }
            }
        });
    }

    private void registerPlayerEquipmentListener() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(HMCCosmeticsPlugin.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Server.ENTITY_EQUIPMENT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                //HMCCosmeticsPlugin.getInstance().getLogger().info("equipment packet is activated");
                Player player = event.getPlayer(); // Player that's sent
                int entityID = event.getPacket().getIntegers().read(0);
                // User
                CosmeticUser user = CosmeticUsers.getUser(entityID);
                if (user == null) {
                    //HMCCosmeticsPlugin.getInstance().getLogger().info("equipment packet is activated - user null");
                    return;
                }

                List<com.comphenix.protocol.wrappers.Pair<EnumWrappers.ItemSlot, ItemStack>> armor = event.getPacket().getSlotStackPairLists().read(0);

                for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                    CosmeticArmorType cosmeticArmor = (CosmeticArmorType) user.getCosmetic(InventoryUtils.BukkitCosmeticSlot(equipmentSlot));
                    if (cosmeticArmor == null) continue;
                    Pair<EnumWrappers.ItemSlot, ItemStack> pair = new Pair<>(InventoryUtils.itemBukkitSlot(cosmeticArmor.getEquipSlot()), cosmeticArmor.getCosmeticItem());
                    armor.add(pair);
                }

                event.getPacket().getSlotStackPairLists().write(0, armor);
                HMCCosmeticsPlugin.getInstance().getLogger().info("Equipment for " + user.getPlayer().getName() + " has been updated for " + player.getName());
            }
        });
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

    final static Map<EquipmentSlot, Set<Material>> ARMOR_ITEMS = Map.of(
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
}
