package com.hibiscusmc.hmccosmetics.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.api.events.PlayerCosmeticPostEquipEvent;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticArmorType;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBackpackType;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBalloonType;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticEmoteType;
import com.hibiscusmc.hmccosmetics.gui.Menu;
import com.hibiscusmc.hmccosmetics.gui.Menus;
import com.hibiscusmc.hmccosmetics.nms.NMSHandlers;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import com.hibiscusmc.hmccosmetics.user.manager.UserEmoteManager;
import com.hibiscusmc.hmccosmetics.util.InventoryUtils;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.packets.PacketManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spigotmc.event.entity.EntityDismountEvent;
import org.spigotmc.event.entity.EntityMountEvent;

import java.util.*;

public class PlayerGameListener implements Listener {

    public PlayerGameListener() {
        registerInventoryClickListener();
        registerMenuChangeListener();
        registerEntityStatusListener();
        registerPlayerEquipmentListener();
        registerPlayerArmListener();
        registerEntityUseListener();
        registerSlotChangeListener();

        //registerLookMovement();
        //registerMoveListener();
        //registerTeleportMovement();
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerClick(@NotNull InventoryClickEvent event) {
        // || !event.getClickedInventory().getType().equals(InventoryType.PLAYER)
        if (event.getClick().isShiftClick()) return;
        MessagesUtil.sendDebugMessages("inventoryclickevent");
        //if (event.getSlotType() != InventoryType.SlotType.ARMOR) return;
        CosmeticUser user = CosmeticUsers.getUser(event.getWhoClicked().getUniqueId());
        if (user == null) return;
        ItemStack item = event.getCurrentItem();
        if (item == null) return;

        if (Settings.isDestroyLooseCosmetics() && InventoryUtils.isCosmeticItem(event.getCurrentItem())) {
            MessagesUtil.sendDebugMessages("remvoe item");
            event.getWhoClicked().getInventory().removeItem(event.getCurrentItem());
        }

        EquipmentSlot slot = getArmorSlot(item.getType());
        if (slot == null) return;
        CosmeticSlot cosmeticSlot = InventoryUtils.BukkitCosmeticSlot(slot);
        if (cosmeticSlot == null) return;
        if (!user.hasCosmeticInSlot(cosmeticSlot)) return;
        Bukkit.getScheduler().runTaskLater(HMCCosmeticsPlugin.getInstance(), () -> {
            user.updateCosmetic(cosmeticSlot);
        }, 1);
        MessagesUtil.sendDebugMessages("Event fired, updated cosmetic " + cosmeticSlot);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerShift(PlayerToggleSneakEvent event) {
        CosmeticUser user = CosmeticUsers.getUser(event.getPlayer().getUniqueId());

        if (user == null) return;
        if (event.isSneaking()) {
            user.getUserEmoteManager().stopEmote(UserEmoteManager.StopEmoteReason.SNEAK);
        }

        if (!event.isSneaking()) return;
        if (!user.isInWardrobe()) return;

        user.leaveWardrobe();
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        CosmeticUser user = CosmeticUsers.getUser(event.getPlayer().getUniqueId());

        MessagesUtil.sendDebugMessages("Player Teleport Event");
        if (user == null) {
            MessagesUtil.sendDebugMessages("user is null");
            return;
        }

        if (user.isInWardrobe()) {
            user.leaveWardrobe();
        }

        Bukkit.getScheduler().runTaskLater(HMCCosmeticsPlugin.getInstance(), () -> {
            if (user.hasCosmeticInSlot(CosmeticSlot.BACKPACK) && user.getUserBackpackManager() != null) {
                user.respawnBackpack();
            }
            if (user.hasCosmeticInSlot(CosmeticSlot.BALLOON)) {
                user.respawnBalloon();
            }
            user.updateCosmetic();
        }, 1);

        if (event.getCause().equals(PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) || event.getCause().equals(PlayerTeleportEvent.TeleportCause.END_PORTAL)) return;
        if (user.getUserEmoteManager().isPlayingEmote()) {
            user.getUserEmoteManager().stopEmote(UserEmoteManager.StopEmoteReason.TELEPORT);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPortalTeleport(PlayerPortalEvent event) {
        CosmeticUser user = CosmeticUsers.getUser(event.getPlayer().getUniqueId());

        MessagesUtil.sendDebugMessages("Player Teleport Event");
        if (user == null) {
            MessagesUtil.sendDebugMessages("user is null");
            return;
        }

        if (user.hasCosmeticInSlot(CosmeticSlot.BALLOON)) {
            user.despawnBalloon();

            Bukkit.getScheduler().runTaskLater(HMCCosmeticsPlugin.getInstance(), () -> {
                user.spawnBalloon((CosmeticBalloonType) user.getCosmetic(CosmeticSlot.BALLOON));
                user.updateCosmetic();
            }, 4);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (event.getEntity().getEntityId() == event.getDamager().getEntityId()) event.setCancelled(true);
        if (!entity.getPersistentDataContainer().has(new NamespacedKey(HMCCosmeticsPlugin.getInstance(), "cosmeticMob"), PersistentDataType.SHORT))
            return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerDamaged(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        CosmeticUser user = CosmeticUsers.getUser(player);
        if (user == null) return;
        if (user.getUserEmoteManager().isPlayingEmote()) {
            if (Settings.isEmoteInvincible()) {
                event.setCancelled(true);
            }
            if (Settings.isEmoteDamageLeave()) {
                user.getUserEmoteManager().stopEmote(UserEmoteManager.StopEmoteReason.DAMAGE);
            }
        }
        if (user.isInWardrobe()) {
            user.leaveWardrobe();
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerLook(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        CosmeticUser user = CosmeticUsers.getUser(player);
        if (user == null) return;
        if (!Settings.isEmoteMoveCheck() && user.getUserEmoteManager().isPlayingEmote()) {
            event.setCancelled(true);
            return;
        }
        user.updateCosmetic(CosmeticSlot.BACKPACK);
        user.updateCosmetic(CosmeticSlot.BALLOON);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerPoseChange(EntityPoseChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        CosmeticUser user = CosmeticUsers.getUser(player);
        if (user == null) return;
        if (!user.hasCosmeticInSlot(CosmeticSlot.BACKPACK)) return;
        Pose pose = event.getPose();
        if (pose.equals(Pose.STANDING)) {
            // #84, Riptides mess with backpacks
            ItemStack currentItem = player.getInventory().getItemInMainHand();
            if (currentItem.containsEnchantment(Enchantment.RIPTIDE)) return;
            if (!user.isBackpackSpawned()) {
                user.spawnBackpack((CosmeticBackpackType) user.getCosmetic(CosmeticSlot.BACKPACK));
            }
            return;
        }
        if (pose.equals(Pose.SLEEPING) || pose.equals(Pose.SWIMMING) || pose.equals(Pose.FALL_FLYING) || pose.equals(Pose.SPIN_ATTACK)) {
            user.despawnBackpack();
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerArmorDamage(PlayerItemDamageEvent event) {
        // Possibly look into cancelling the event, then handling the damage on our own.
        MessagesUtil.sendDebugMessages("PlayerItemDamageEvent");

        int slot = -1;
        int w = 36;
        for (ItemStack armorItem : event.getPlayer().getInventory().getArmorContents()) {
            if (armorItem == null) continue;
            if (armorItem.isSimilar(event.getItem())) {
                slot = w;
                break;
            }
            w++;
        }

        if (slot == -1) return;

        CosmeticUser user = CosmeticUsers.getUser(event.getPlayer().getUniqueId());
        if (user == null) return;
        CosmeticSlot cosmeticSlot = InventoryUtils.BukkitCosmeticSlot(slot);

        if (!user.hasCosmeticInSlot(cosmeticSlot)) {
            MessagesUtil.sendDebugMessages("No cosmetic in " + cosmeticSlot);
            return;
        }

        Bukkit.getScheduler().runTaskLater(HMCCosmeticsPlugin.getInstance(), () -> {
            MessagesUtil.sendDebugMessages("PlayerItemDamageEvent UpdateCosmetic " + cosmeticSlot);
            user.updateCosmetic(cosmeticSlot);
        }, 2);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerOffhandSwap(PlayerSwapHandItemsEvent event) {
        CosmeticUser user = CosmeticUsers.getUser(event.getPlayer().getUniqueId());
        if (user == null) return;
        // Really need to look into optimization of this
        if (user.hasCosmeticInSlot(CosmeticSlot.EMOTE) && event.getPlayer().isSneaking() && event.getPlayer().hasPermission("hmccosmetics.emote.shiftrun")) {
            CosmeticEmoteType cosmeticEmoteType = (CosmeticEmoteType) user.getCosmetic(CosmeticSlot.EMOTE);
            cosmeticEmoteType.run(user);
            event.setCancelled(true);
            return;
        }
        Bukkit.getScheduler().runTaskLater(HMCCosmeticsPlugin.getInstance(), () -> {
            user.updateCosmetic(CosmeticSlot.OFFHAND);
            List<Player> viewers = PacketManager.getViewers(user.getEntity().getLocation());
            if (viewers.isEmpty()) return;
            viewers.remove(user.getPlayer());
            NMSHandlers.getHandler().equipmentSlotUpdate(user.getEntity().getEntityId(), EquipmentSlot.HAND, event.getPlayer().getInventory().getItemInMainHand(), viewers);
        }, 2);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        CosmeticUser user = CosmeticUsers.getUser(event.getEntity().getUniqueId());
        if (user == null) return;
        if (user.isInWardrobe()) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerPotionEffect(EntityPotionEffectEvent event) {
        if (!event.getModifiedType().equals(PotionEffectType.INVISIBILITY)) return;
        if (!event.getEntityType().equals(EntityType.PLAYER)) return;
        Player player = (Player) event.getEntity();
        CosmeticUser user = CosmeticUsers.getUser(player);
        if (user == null) return;
        if (event.getAction().equals(EntityPotionEffectEvent.Action.ADDED)) {
            user.hideCosmetics(CosmeticUser.HiddenReason.POTION);
            return;
        }
        if (event.getAction().equals(EntityPotionEffectEvent.Action.CLEARED) || event.getAction().equals(EntityPotionEffectEvent.Action.REMOVED)) {
            user.showCosmetics();
            return;
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onMainHandSwitch(PlayerItemHeldEvent event) {
        CosmeticUser user = CosmeticUsers.getUser(event.getPlayer());
        if (user == null) return;

        event.getPlayer().getInventory().setItem(event.getPreviousSlot(), event.getPlayer().getInventory().getItem(event.getPreviousSlot()));
        //NMSHandlers.getHandler().slotUpdate(event.getPlayer(), event.getPreviousSlot());
        Bukkit.getScheduler().runTaskLater(HMCCosmeticsPlugin.getInstance(), () -> {
            user.updateCosmetic(CosmeticSlot.MAINHAND);
        }, 2);

        // #84, Riptides mess with backpacks
        ItemStack currentItem = event.getPlayer().getInventory().getItem(event.getNewSlot());
        if (currentItem == null) return;
        if (!currentItem.hasItemMeta()) return;
        if (user.hasCosmeticInSlot(CosmeticSlot.BACKPACK) && currentItem.containsEnchantment(Enchantment.RIPTIDE)) {
            user.despawnBackpack();
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerDeath(PlayerDeathEvent event) {
        CosmeticUser user = CosmeticUsers.getUser(event.getEntity());
        if (user == null) return;

        if (user.isInWardrobe()) user.leaveWardrobe();

        if (Settings.isUnapplyOnDeath() && !event.getEntity().hasPermission("hmccosmetics.unapplydeath.bypass")) {
            user.removeCosmetics();
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerGamemodeSwitch(PlayerGameModeChangeEvent event) {
        CosmeticUser user = CosmeticUsers.getUser(event.getPlayer());
        if (user == null) return;

        if (Settings.isDisabledGamemodesEnabled()) {
            if (Settings.getDisabledGamemodes().contains(event.getNewGameMode().toString())) {
                user.hideCosmetics(CosmeticUser.HiddenReason.GAMEMODE);
            } else {
                if (user.getHiddenReason() != null && user.getHiddenReason().equals(CosmeticUser.HiddenReason.GAMEMODE)) {
                    user.showCosmetics();
                }
            }
        }

        if (Settings.isDestroyLooseCosmetics()) {
            ItemStack[] equippedArmor = event.getPlayer().getInventory().getArmorContents();
            if (equippedArmor.length == 0) return;
            for (ItemStack armor : equippedArmor) {
                if (InventoryUtils.isCosmeticItem(armor)) armor.setAmount(0);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerCosmeticEquip(PlayerCosmeticPostEquipEvent event) {
        CosmeticUser user = event.getUser();
        if (user.isInWardrobe() && event.getCosmetic().getSlot().equals(CosmeticSlot.BALLOON)) {
            CosmeticBalloonType cosmetic = (CosmeticBalloonType) event.getCosmetic();
            Location npclocation = user.getWardrobeManager().getNpcLocation().clone().add(cosmetic.getBalloonOffset());
            // We know that no other entity besides a regular player will be in the wardrobe
            List<Player> viewer = List.of(user.getPlayer());
            user.getBalloonManager().getPufferfish().spawnPufferfish(npclocation.clone().add(cosmetic.getBalloonOffset()), viewer);
            PacketManager.sendLeashPacket(user.getBalloonManager().getPufferfishBalloonId(), user.getWardrobeManager().getNPC_ID(), viewer);
            PacketManager.sendTeleportPacket(user.getBalloonManager().getPufferfishBalloonId(), npclocation, false, viewer);
            user.getBalloonManager().getModelEntity().teleport(npclocation);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerMounted(EntityMountEvent event) {
		if (event.getEntity() instanceof Player player) {
            CosmeticUser user = CosmeticUsers.getUser(player);
            if (user == null) return;

            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(HMCCosmeticsPlugin.getInstance(), user::respawnBackpack, 1);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerDismounted(EntityDismountEvent event) {
		if (event.getDismounted() instanceof Player player) {
            CosmeticUser user = CosmeticUsers.getUser(player);
            if (user == null) return;

            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(HMCCosmeticsPlugin.getInstance(), user::respawnBackpack, 1);
		}
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
                if (event.getPlayer() == null) return;

                CosmeticUser user = CosmeticUsers.getUser(player);
                if (user == null) return;
                CosmeticSlot cosmeticSlot = InventoryUtils.NMSCosmeticSlot(slotClicked);
                if (cosmeticSlot == null) return;
                if (!user.hasCosmeticInSlot(cosmeticSlot)) return;
                Bukkit.getScheduler().runTaskLater(HMCCosmeticsPlugin.getInstance(), () -> user.updateCosmetic(cosmeticSlot), 1);
                MessagesUtil.sendDebugMessages("Packet fired, updated cosmetic " + cosmeticSlot);
            }
        });
    }

    private void registerMenuChangeListener() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(HMCCosmeticsPlugin.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Server.WINDOW_ITEMS) {
            @Override
            public void onPacketSending(PacketEvent event) {
                MessagesUtil.sendDebugMessages("Menu Initial ");
                Player player = event.getPlayer();
                if (event.getPlayer() == null) return;

                int windowID = event.getPacket().getIntegers().read(0);
                List<ItemStack> slotData = event.getPacket().getItemListModifier().read(0);
                if (windowID != 0) return;

                CosmeticUser user = CosmeticUsers.getUser(player);
                if (user == null) return;

                HashMap<Integer, ItemStack> items = new HashMap<>();

                for (Cosmetic cosmetic : user.getCosmetics()) {
                    if ((cosmetic instanceof CosmeticArmorType cosmeticArmorType)) {
                        items.put(InventoryUtils.getPacketArmorSlot(cosmeticArmorType.getEquipSlot()), user.getUserCosmeticItem(cosmeticArmorType));
                    }
                }

                PacketContainer packet = new PacketContainer(PacketType.Play.Server.WINDOW_ITEMS);
                packet.getIntegers().write(0, 0);
                for (int slot = 0; slot < 46; slot++) {
                    if ((slot >= 5 && slot <= 8) || slot == 45) {
                        if (!items.containsKey(slot)) continue;
                        slotData.set(slot, items.get(slot));
                        MessagesUtil.sendDebugMessages("Set " + slot + " as " + items.get(slot));
                    }
                }
                packet.getItemListModifier().write(0, slotData);
                packet.getItemModifier().write(0, event.getPacket().getItemModifier().read(0));
                event.setPacket(packet);
                MessagesUtil.sendDebugMessages("Menu Fired, updated cosmetics " + " on slotdata " + windowID + " with " + slotData.size());
                /*
                for (Cosmetic cosmetic : user.getCosmetic()) {
                    if ((cosmetic instanceof CosmeticArmorType) || (cosmetic instanceof CosmeticMainhandType)) {
                        Bukkit.getScheduler().runTaskLater(HMCCosmeticsPlugin.getInstance(), () -> {
                            user.updateCosmetic(cosmetic);
                        }, 1);

                    }
                }
                 */
            }
        });
    }

    private void registerSlotChangeListener() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(HMCCosmeticsPlugin.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Server.SET_SLOT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                MessagesUtil.sendDebugMessages("SetSlot Initial ");

                Player player = event.getPlayer();
                if (event.getPlayer() == null) return;

                int windowID = event.getPacket().getIntegers().read(0);
                if (windowID != 0) return;

                CosmeticUser user = CosmeticUsers.getUser(player);
                if (user == null) return;

                int slot = event.getPacket().getIntegers().read(2);
                MessagesUtil.sendDebugMessages("SetSlot Slot " + slot);
                if (slot == 45 && user.hasCosmeticInSlot(CosmeticSlot.OFFHAND) && player.getInventory().getItemInOffHand().getType().isAir()) {
                    event.getPacket().getItemModifier().write(0, user.getUserCosmeticItem(CosmeticSlot.OFFHAND));
                }
            }
        });
    }

    private void registerPlayerEquipmentListener() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(HMCCosmeticsPlugin.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Server.ENTITY_EQUIPMENT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                Player player = event.getPlayer(); // Player that's sent
                int entityID = event.getPacket().getIntegers().read(0);
                // User
                CosmeticUser user = CosmeticUsers.getUser(entityID);
                if (user == null) {
                    return;
                }
                List<com.comphenix.protocol.wrappers.Pair<EnumWrappers.ItemSlot, ItemStack>> armor = event.getPacket().getSlotStackPairLists().read(0);

                for (int i = 0; i < armor.size(); i++) {
                    com.comphenix.protocol.wrappers.Pair<EnumWrappers.ItemSlot, ItemStack> pair = armor.get(i);
                    switch (pair.getFirst()) {
                        case MAINHAND -> {
                            if (user.getPlayer() == event.getPlayer()) continue; // When a player scrolls real fast, it messes up the mainhand. This fixes it
                            armor.set(i, new Pair<>(pair.getFirst(), user.getPlayer().getInventory().getItemInMainHand()));
                        }
                        case OFFHAND -> {
                            if (Settings.isCosmeticForceOffhandCosmeticShow() && user.hasCosmeticInSlot(CosmeticSlot.OFFHAND)) {
                                ItemStack item = user.getUserCosmeticItem(CosmeticSlot.OFFHAND);
                                if (item == null) continue;
                                Pair<EnumWrappers.ItemSlot, ItemStack> offhandPair = new Pair<>(EnumWrappers.ItemSlot.OFFHAND, item);
                                armor.set(i, offhandPair);
                            }
                        }
                        default -> {
                            CosmeticArmorType cosmeticArmor = (CosmeticArmorType) user.getCosmetic(InventoryUtils.getItemSlotToCosmeticSlot(pair.getFirst()));
                            if (cosmeticArmor == null) continue;
                            ItemStack item = user.getUserCosmeticItem(cosmeticArmor);
                            if (item == null) continue;
                            Pair<EnumWrappers.ItemSlot, ItemStack> armorPair = new Pair<>(InventoryUtils.itemBukkitSlot(cosmeticArmor.getEquipSlot()), item);
                            armor.set(i, armorPair);
                        }
                    }
                }

                event.getPacket().getSlotStackPairLists().write(0, armor);
                MessagesUtil.sendDebugMessages("Equipment for " + user.getPlayer().getName() + " has been updated for " + player.getName());
            }
        });
    }

    private void registerEntityStatusListener() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(HMCCosmeticsPlugin.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Server.ENTITY_STATUS) {
            @Override
            public void onPacketSending(PacketEvent event) {
                int entityid = event.getPacket().getIntegers().read(0);
                byte status = event.getPacket().getBytes().read(0);

                MessagesUtil.sendDebugMessages("EntityStatus Initial " + entityid + " - " + status);
                if (status != 55) return;

                CosmeticUser user = CosmeticUsers.getUser(entityid);
                if (user == null) {
                    MessagesUtil.sendDebugMessages("EntityStatus User is null");
                    return;
                }
                if (!user.hasCosmeticInSlot(CosmeticSlot.OFFHAND)) return;
                event.setCancelled(true);
            }
        });
    }

    private void registerPlayerArmListener() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(HMCCosmeticsPlugin.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Client.ARM_ANIMATION) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPlayer() == null) return;
                Player player = event.getPlayer();
                CosmeticUser user = CosmeticUsers.getUser(player);
                if (user == null) return;
                if (user.getUserEmoteManager().isPlayingEmote()) {
                    event.setCancelled(true);
                    return;
                }
                if (!user.isInWardrobe()) return;
                Menu menu = Menus.getDefaultMenu();
                if (menu == null) return;
                menu.openMenu(user);
                event.setCancelled(true);
            }
        });
    }

    private void registerEntityUseListener() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(HMCCosmeticsPlugin.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Client.USE_ENTITY) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPlayer() == null) return;
                CosmeticUser user = CosmeticUsers.getUser(event.getPlayer());
                if (user == null) return;
                if (user.getUserEmoteManager().isPlayingEmote() || user.isInWardrobe()) {
                    event.setCancelled(true);
                }
            }
        });
    }

    private void registerLookMovement() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(HMCCosmeticsPlugin.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Client.LOOK) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                // TODO: Finish
                MessagesUtil.sendDebugMessages("Look Packet ");
                Player player = event.getPlayer();
                if (event.getPlayer() == null) return;
                CosmeticUser user = CosmeticUsers.getUser(player);
                if (user == null) return;
                if (user.isBackpackSpawned()) {
                    user.getUserBackpackManager().getEntityManager().setRotation(Math.round(event.getPacket().getFloat().read(0)));
                }
            }
        });
    }

    private void registerMoveListener() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(HMCCosmeticsPlugin.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Client.POSITION) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                // TODO: Finish
                MessagesUtil.sendDebugMessages("Position Packet ");
                Player player = event.getPlayer();
                if (event.getPlayer() == null) return;
                CosmeticUser user = CosmeticUsers.getUser(player);
                if (user == null) return;
                if (user.isBackpackSpawned()) {
                    // The yaw follows the head, which makes it look weird and do weird things when moving around
                    user.getUserBackpackManager().getEntityManager().teleport(new Location(player.getWorld(), event.getPacket().getDoubles().read(0), event.getPacket().getDoubles().read(1), event.getPacket().getDoubles().read(2), event.getPacket().getFloat().read(0), event.getPacket().getFloat().read(1)));
                }
            }
        });
    }

    private void registerTeleportMovement() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(HMCCosmeticsPlugin.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Client.POSITION_LOOK) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                // TODO: Finish
                MessagesUtil.sendDebugMessages("Teleport Packet ");
                Player player = event.getPlayer();
                if (event.getPlayer() == null) return;
                CosmeticUser user = CosmeticUsers.getUser(player);
                if (user == null) return;
                if (user.isBackpackSpawned()) {
                    Bukkit.getScheduler().runTask(HMCCosmeticsPlugin.getInstance(), () -> user.updateCosmetic(CosmeticSlot.BACKPACK));
                }
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
