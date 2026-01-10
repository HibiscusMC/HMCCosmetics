package com.hibiscusmc.hmccosmetics.listener;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.api.events.PlayerCosmeticPostEquipEvent;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.config.WardrobeSettings;
import com.hibiscusmc.hmccosmetics.config.section.Wardrobe;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBackpackType;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBalloonType;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import com.hibiscusmc.hmccosmetics.user.manager.UserWardrobeManager;
import com.hibiscusmc.hmccosmetics.util.HMCCInventoryUtils;
import com.hibiscusmc.hmccosmetics.util.HMCCServerUtils;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.SchedulerUtil;
import com.hibiscusmc.hmccosmetics.util.packets.HMCCPacketManager;
import me.lojosho.hibiscuscommons.api.events.*;
import me.lojosho.hibiscuscommons.nms.NMSHandlers;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PlayerGameListener implements Listener {
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

        if (Settings.isDestroyLooseCosmetics() && HMCCInventoryUtils.isCosmeticItem(event.getCurrentItem())) {
            MessagesUtil.sendDebugMessages("remvoe item");
            event.getWhoClicked().getInventory().removeItem(event.getCurrentItem());
        }

        EquipmentSlot slot = getArmorSlot(item.getType());
        if (slot == null) return;
        CosmeticSlot cosmeticSlot = HMCCInventoryUtils.BukkitCosmeticSlot(slot);
        if (cosmeticSlot == null) return;
        if (!user.hasCosmeticInSlot(cosmeticSlot)) return;
        SchedulerUtil.runTaskLater(HMCCosmeticsPlugin.getInstance(), () -> {
            user.updateCosmetic(cosmeticSlot);
        }, 1);
        MessagesUtil.sendDebugMessages("Event fired, updated cosmetic " + cosmeticSlot);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerShift(PlayerToggleSneakEvent event) {
        CosmeticUser user = CosmeticUsers.getUser(event.getPlayer().getUniqueId());

        if (user == null) return;
        if (!event.isSneaking()) return;
        if (!user.isInWardrobe()) return;

        user.leaveWardrobe(false);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        CosmeticUser user = CosmeticUsers.getUser(event.getPlayer().getUniqueId());

        MessagesUtil.sendDebugMessages("Player Teleport Event");
        if (user == null) {
            MessagesUtil.sendDebugMessages("user is null");
            return;
        }

        if (user.isInWardrobe()) {
            user.leaveWardrobe(false);
        }

        SchedulerUtil.runTaskLater(HMCCosmeticsPlugin.getInstance(), () -> {
            if (user.getEntity() == null || user.isInWardrobe()) return; // fixes disconnecting when in wardrobe (the entity stuff)

            if (Settings.getDisabledWorlds().contains(user.getEntity().getLocation().getWorld().getName())) {
                user.hideCosmetics(CosmeticUser.HiddenReason.WORLD);
            } else {
                user.showCosmetics(CosmeticUser.HiddenReason.WORLD);
            }

            user.respawnBackpack();
            user.respawnBalloon();
            user.updateCosmetic();
        }, 4);

        if (event.getCause().equals(PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) || event.getCause().equals(PlayerTeleportEvent.TeleportCause.END_PORTAL)) return;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPortalTeleport(PlayerPortalEvent event) {
        CosmeticUser user = CosmeticUsers.getUser(event.getPlayer().getUniqueId());

        MessagesUtil.sendDebugMessages("Player Teleport Event");
        if (user == null) {
            MessagesUtil.sendDebugMessages("user is null");
            return;
        }

        if (Settings.getDisabledWorlds().contains(user.getEntity().getLocation().getWorld().getName())) {
            user.hideCosmetics(CosmeticUser.HiddenReason.WORLD);
        } else {
            user.showCosmetics(CosmeticUser.HiddenReason.WORLD);
        }

        if (user.hasCosmeticInSlot(CosmeticSlot.BALLOON)) {
            user.despawnBalloon();

            SchedulerUtil.runTaskLater(HMCCosmeticsPlugin.getInstance(), () -> {
                user.spawnBalloon((CosmeticBalloonType) user.getCosmetic(CosmeticSlot.BALLOON));
                user.updateCosmetic();
            }, 4);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (!entity.getPersistentDataContainer().has(HMCCServerUtils.getCosmemeticMobKey(), PersistentDataType.BOOLEAN)) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerDamaged(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        CosmeticUser user = CosmeticUsers.getUser(player);
        if (user == null) return;
        if (user.isInWardrobe()) {
            if (WardrobeSettings.isPreventDamage()) {
                event.setCancelled(true);
                return;
            }
            if (WardrobeSettings.isDamagedKicked()) user.leaveWardrobe(false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerPoseChange(EntityPoseChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        CosmeticUser user = CosmeticUsers.getUser(player);
        if (user == null || user.isInWardrobe()) return;
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
        CosmeticSlot cosmeticSlot = HMCCInventoryUtils.BukkitCosmeticSlot(slot);

        if (!user.hasCosmeticInSlot(cosmeticSlot)) {
            MessagesUtil.sendDebugMessages("No cosmetic in " + cosmeticSlot);
            return;
        }

        SchedulerUtil.runTaskLater(HMCCosmeticsPlugin.getInstance(), () -> {
            MessagesUtil.sendDebugMessages("PlayerItemDamageEvent UpdateCosmetic " + cosmeticSlot);
            user.updateCosmetic(cosmeticSlot);
        }, 2);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerOffhandSwap(PlayerSwapHandItemsEvent event) {
        CosmeticUser user = CosmeticUsers.getUser(event.getPlayer().getUniqueId());
        if (user == null) return;
        // Really need to look into optimization of this
        SchedulerUtil.runTaskLater(HMCCosmeticsPlugin.getInstance(), () -> {
            if (user.getEntity() == null) return; // Player has likely logged off
            user.updateCosmetic(CosmeticSlot.OFFHAND);
            List<Player> viewers = HMCCPacketManager.getViewers(user.getEntity().getLocation());
            if (viewers.isEmpty()) return;
            viewers.remove(user.getPlayer());
            HMCCPacketManager.equipmentSlotUpdate(user.getEntity().getEntityId(), EquipmentSlot.HAND, event.getPlayer().getInventory().getItemInMainHand(), viewers);
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
            user.showCosmetics(CosmeticUser.HiddenReason.POTION);
            return;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMainHandSwitch(PlayerItemHeldEvent event) {
        CosmeticUser user = CosmeticUsers.getUser(event.getPlayer());
        if (user == null) return;

        // 检查玩家是否在衣柜中
        if (user.getWardrobeManager() != null && user.getWardrobeManager().isActive()) {
            // 取消切换物品栏槽位事件
            event.setCancelled(true);
            
            // 获取当前和之前的槽位
            int previousSlot = event.getPreviousSlot();
            int newSlot = event.getNewSlot();
            
            // 计算旋转角度（向右切换为正，向左切换为负）
            int rotationAmount = 0;
            if (newSlot > previousSlot) {
                // 向右切换（考虑循环）
                if (previousSlot == 0 && newSlot == 8) {
                    // 从0到8是向左切换
                    rotationAmount = -15;
                } else {
                    // 正常向右切换
                    rotationAmount = 15;
                }
            } else if (newSlot < previousSlot) {
                // 向左切换（考虑循环）
                if (previousSlot == 8 && newSlot == 0) {
                    // 从8到0是向右切换
                    rotationAmount = 15;
                } else {
                    // 正常向左切换
                    rotationAmount = -15;
                }
            }
            
            // 如果有旋转量，则旋转NPC
            if (rotationAmount != 0) {
                rotateWardrobeNPC(user, rotationAmount);
            }
            return;
        }

        // 原有的主手切换处理逻辑
        user.updateCosmetic(CosmeticSlot.MAINHAND);
        if (user.hasCosmeticInSlot(CosmeticSlot.BACKPACK)) {
            Cosmetic cosmetic = user.getCosmetic(CosmeticSlot.BACKPACK);
            if (cosmetic.getId().equalsIgnoreCase("trident")) {
                user.removeCosmeticSlot(CosmeticSlot.BACKPACK);
            }
        }
    }
    
    /**
     * 旋转衣柜NPC
     * @param user 化妆品用户
     * @param rotationAmount 旋转角度
     */
    private void rotateWardrobeNPC(CosmeticUser user, int rotationAmount) {
        if (user.getWardrobeManager() == null || !user.getWardrobeManager().isActive()) return;
        
        Player player = user.getPlayer();
        if (player == null) return;
        
        // 获取NPC当前位置
        Location npcLocation = user.getWardrobeManager().getNpcLocation();
        int currentYaw = (int) npcLocation.getYaw();
        
        // 计算新的角度
        int newYaw = (currentYaw + rotationAmount) % 360;
        if (newYaw < 0) newYaw += 360;
        
        // 更新位置
        npcLocation.setYaw(newYaw);
        
        // 获取观看者
        List<Player> viewer = Collections.singletonList(player);
        
        // 发送旋转数据包
        HMCCPacketManager.sendRotateHeadPacket(user.getWardrobeManager().getNPC_ID(), npcLocation, viewer);
        NMSHandlers.getHandler().getPacketBuilder()
            .buildEntityRotatePacket(user.getWardrobeManager().getNPC_ID(), newYaw, 0, false)
            .sendPacket(viewer);
        
        // 播放音效
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_BAMBOO_BREAK, org.bukkit.SoundCategory.UI, 0.5f, 1.0f);
        
        // 如果有背包，也需要旋转
        if (user.hasCosmeticInSlot(CosmeticSlot.BACKPACK) && user.getUserBackpackManager() != null) {
            user.getUserBackpackManager().getEntityManager().setRotation(newYaw);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerDeath(PlayerDeathEvent event) {
        CosmeticUser user = CosmeticUsers.getUser(event.getEntity());
        if (user == null) return;

        if (user.isInWardrobe()) user.leaveWardrobe(false);

        if (Settings.isUnapplyOnDeath() && !event.getEntity().hasPermission("hmccosmetics.unapplydeath.bypass")) {
            user.removeCosmetics();
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerGamemodeSwitch(PlayerGameModeChangeEvent event) {
        CosmeticUser user = CosmeticUsers.getUser(event.getPlayer());
        if (user == null) return;
        if (user.isInWardrobe()) user.leaveWardrobe(true);

        if (Settings.isDisabledGamemodesEnabled()) {
            if (Settings.getDisabledGamemodes().contains(event.getNewGameMode().toString())) {
                user.hideCosmetics(CosmeticUser.HiddenReason.GAMEMODE);
            } else {
                user.showCosmetics(CosmeticUser.HiddenReason.GAMEMODE);
            }
        }

        if (Settings.isDestroyLooseCosmetics()) {
            ItemStack[] equippedArmor = event.getPlayer().getInventory().getArmorContents();
            if (equippedArmor.length == 0) return;
            for (ItemStack armor : equippedArmor) {
                if (HMCCInventoryUtils.isCosmeticItem(armor)) armor.setAmount(0);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerCosmeticEquip(PlayerCosmeticPostEquipEvent event) {
        CosmeticUser user = event.getUser();
        if (user.isInWardrobe() && event.getCosmetic().getSlot().equals(CosmeticSlot.BALLOON)) {
            if (user.getBalloonManager() == null) {
                MessagesUtil.sendDebugMessages("Balloon Manager is null? " + user.getEntity().getName());
                return;
            }
            CosmeticBalloonType cosmetic = (CosmeticBalloonType) event.getCosmetic();
            Location npclocation = user.getWardrobeManager().getNpcLocation().clone().add(cosmetic.getBalloonOffset());
            // We know that no other entity besides a regular player will be in the wardrobe
            List<Player> viewer = List.of(user.getPlayer());
            user.getBalloonManager().getPufferfish().spawnPufferfish(npclocation.clone().add(cosmetic.getBalloonOffset()), viewer);
            HMCCPacketManager.sendLeashPacket(user.getBalloonManager().getPufferfishBalloonId(), user.getWardrobeManager().getNPC_ID(), viewer);
            HMCCPacketManager.sendTeleportPacket(user.getBalloonManager().getPufferfishBalloonId(), npclocation, false, viewer);
            // 在Folia环境中使用异步传送
            user.getBalloonManager().getModelEntity().teleportAsync(npclocation);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerMounted(EntityMountEvent event) {
		if (event.getEntity() instanceof Player player) {
            CosmeticUser user = CosmeticUsers.getUser(player);
            if (user == null) return;

            // 使用实体特定的调度器，确保在Folia中正确执行
            SchedulerUtil.runTaskLater(HMCCosmeticsPlugin.getInstance(), player, user::respawnBackpack, 1);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerDismounted(EntityDismountEvent event) {
		if (event.getDismounted() instanceof Player player) {
            CosmeticUser user = CosmeticUsers.getUser(player);
            if (user == null) return;

            // 使用实体特定的调度器，确保在Folia中正确执行
            SchedulerUtil.runTaskLater(HMCCosmeticsPlugin.getInstance(), player, user::respawnBackpack, 1);
		}
	}

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player)) return;
        
        Player player = event.getPlayer();
        CosmeticUser user = CosmeticUsers.getUser(player);
        
        // 检查实体是否为衣柜NPC
        if (user.getWardrobeManager() != null && 
            event.getRightClicked().getEntityId() == user.getWardrobeManager().getNPC_ID()) {
            // 如果玩家不在衣柜中，则打开衣柜
            if (!user.isInWardrobe()) {
                // 获取默认衣柜
                Wardrobe wardrobe = WardrobeSettings.getWardrobe("default");
                if (wardrobe != null) {
                    user.enterWardrobe(wardrobe, false);
                    event.setCancelled(true);
                }
            }
            return;
        }
        
        // 原有的HMCCosmetics实体交互处理
        if (HMCCServerUtils.getEntity(event.getRightClicked().getEntityId()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerVanish(HibiscusPlayerVanishEvent event) {
        CosmeticUser user = CosmeticUsers.getUser(event.getPlayer());
        if (user == null) return;
        user.hideCosmetics(CosmeticUser.HiddenReason.PLUGIN);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerUnVanish(HibiscusPlayerUnVanishEvent event) {
        CosmeticUser user = CosmeticUsers.getUser(event.getPlayer());
        if (user == null) return;
        if (!user.isHidden()) return;
        user.showCosmetics(CosmeticUser.HiddenReason.PLUGIN);
    }

    // These emote mostly handles emotes from other plugins, such as ItemsAdder
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerPlayEmote(HibiscusPlayerEmotePlayEvent event) {
        CosmeticUser user = CosmeticUsers.getUser(event.getPlayer());
        if (user == null) return;
        user.hideCosmetics(CosmeticUser.HiddenReason.EMOTE);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerEndEmote(HibiscusPlayerEmoteEndEvent event) {
        CosmeticUser user = CosmeticUsers.getUser(event.getPlayer());
        if (user == null) return;
        user.showCosmetics(CosmeticUser.HiddenReason.EMOTE);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerHibiscusPose(HibiscusPlayerPoseEvent event) {
        if (event.isGettingUp()) return;
        CosmeticUser user = CosmeticUsers.getUser(event.getPlayer());
        if (user == null) return;
        user.hideCosmetics(CosmeticUser.HiddenReason.PLUGIN);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerHibiscusGetUpPose(HibiscusPlayerPoseEvent event) {
        if (!event.isGettingUp()) return;
        CosmeticUser user = CosmeticUsers.getUser(event.getPlayer());
        if (user == null) return;
        user.showCosmetics(CosmeticUser.HiddenReason.PLUGIN);
    }

    @Nullable
    private EquipmentSlot getArmorSlot(final Material material) {
        for (final EquipmentSlot slot : EquipmentSlot.values()) {
            final Set<Material> armorItems = ARMOR_ITEMS.get(slot);
            if (armorItems == null) continue;
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