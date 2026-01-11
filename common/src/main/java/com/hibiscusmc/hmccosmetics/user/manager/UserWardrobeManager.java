package com.hibiscusmc.hmccosmetics.user.manager;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.WardrobeSettings;
import com.hibiscusmc.hmccosmetics.config.section.Wardrobe;
import com.hibiscusmc.hmccosmetics.config.section.WardrobeLocation;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBalloonType;
import com.hibiscusmc.hmccosmetics.gui.Menu;
import com.hibiscusmc.hmccosmetics.gui.Menus;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.HMCCInventoryUtils;
import com.hibiscusmc.hmccosmetics.util.HMCCServerUtils;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.SchedulerUtil;
import com.hibiscusmc.hmccosmetics.util.packets.HMCCPacketManager;
import lombok.Getter;
import lombok.Setter;
import me.lojosho.hibiscuscommons.nms.NMSHandlers;
import me.lojosho.hibiscuscommons.nms.NMSPacketBuilder;
import me.lojosho.hibiscuscommons.nms.NMSPacketSender;
import me.lojosho.hibiscuscommons.packets.wrapper.PacketWrapper;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.scheduler.BukkitTask;

public class UserWardrobeManager {

    @Getter
    private final int NPC_ID;
    @Getter
    private final int ARMORSTAND_ID;
    @Getter
    private final UUID WARDROBE_UUID;
    @Getter
    private String npcName;
    @Getter
    private GameMode originalGamemode;
    @Getter
    private final CosmeticUser user;
    @Getter
    private final Wardrobe wardrobe;
    @Getter
    private final WardrobeLocation wardrobeLocation;
    @Getter
    private final Location viewingLocation;
    @Getter
    private final Location npcLocation;
    @Getter
    private Location exitLocation;
    @Getter
    private BossBar bossBar;
    @Getter
    private boolean active;
    @Setter
    @Getter
    private WardrobeStatus wardrobeStatus;
    @Getter
    @Setter
    private Menu lastOpenMenu;
    @Getter
    private int originalMainHandSlot; // 记录玩家原始主手槽位位置
    @Getter
    private double originalScale; // 记录玩家原始体型

    private NMSPacketBuilder packetBuilder = NMSHandlers.getHandler().getPacketBuilder();
    private NMSPacketSender packetSender = NMSHandlers.getHandler().getPacketSender();
    private BukkitTask foliaTask;

    public UserWardrobeManager(CosmeticUser user, Wardrobe wardrobe) {
        NPC_ID = me.lojosho.hibiscuscommons.util.ServerUtils.getNextEntityId();
        ARMORSTAND_ID = me.lojosho.hibiscuscommons.util.ServerUtils.getNextEntityId();
        WARDROBE_UUID = UUID.randomUUID();
        this.user = user;

        this.wardrobe = wardrobe;
        this.wardrobeLocation = wardrobe.getLocation();

        this.exitLocation = wardrobeLocation.getLeaveLocation();
        this.viewingLocation = wardrobeLocation.getViewerLocation();
        this.npcLocation = wardrobeLocation.getNpcLocation();

        String defaultMenu = wardrobe.getDefaultMenu();
        if (defaultMenu != null && Menus.hasMenu(defaultMenu)) this.lastOpenMenu = Menus.getMenu(defaultMenu);
        else this.lastOpenMenu = Menus.getDefaultMenu();

        wardrobeStatus = WardrobeStatus.SETUP;
    }

    public void start() {
        setWardrobeStatus(WardrobeStatus.STARTING);
        Player player = user.getPlayer();

        this.originalGamemode = player.getGameMode();
        // 记录玩家原始主手槽位位置
        this.originalMainHandSlot = player.getInventory().getHeldItemSlot();
        
        // 保存玩家原始体型，但不立即修改
        AttributeInstance scaleAttribute = player.getAttribute(Attribute.SCALE);
        if (scaleAttribute != null) {
            this.originalScale = scaleAttribute.getValue();
            // 体型设置将在动画播放后进行
        } else {
            this.originalScale = 1.0; // 默认值
        }
        
        if (WardrobeSettings.isReturnLastLocation() || wardrobeLocation.getLeaveLocation() == null) {
            this.exitLocation = player.getLocation().clone();
        }

        user.hidePlayer();
        if (!Bukkit.getServer().getAllowFlight()) player.setAllowFlight(true);
        List<Player> viewer = Collections.singletonList(player);
        List<Player> outsideViewers = HMCCPacketManager.getViewers(viewingLocation);
        outsideViewers.remove(player);

        MessagesUtil.sendMessage(player, "opened-wardrobe");

        Runnable run = () -> {
            if (!player.isOnline()) {
                end();
                return;
            }

            List<PacketWrapper> viewerPackets = new ArrayList<>();

            // Armorstand
            viewerPackets.add(packetBuilder.buildEntitySpawnPacket(ARMORSTAND_ID, UUID.randomUUID(), EntityType.ARMOR_STAND, viewingLocation));
            viewerPackets.add(packetBuilder.buildEntityMetadataPacket(ARMORSTAND_ID, HMCCPacketManager.getInvisibleArmorStandData()));
            viewerPackets.add(packetBuilder.buildEntityTeleportPacket(ARMORSTAND_ID, viewingLocation.getX(), viewingLocation.getY(), viewingLocation.getZ(), viewingLocation.getYaw(), viewingLocation.getPitch(), false));
            viewerPackets.add(packetBuilder.buildEntityRotateHeadPacket(ARMORSTAND_ID, viewingLocation));

            // Player
            player.teleportAsync(viewingLocation, PlayerTeleportEvent.TeleportCause.PLUGIN).thenAccept(success -> {
                if (!success) {
                    MessagesUtil.sendDebugMessages("Failed to teleport player to wardrobe location");
                    end();
                    return;
                }
                player.setInvisible(true);
                // 设置玩家主手槽位为第4格（第五格）
                player.getInventory().setHeldItemSlot(4);
                viewerPackets.add(packetBuilder.buildPlayerGamemodeChangePacket(GameMode.SPECTATOR));
                viewerPackets.add(packetBuilder.buildEntityCameraPacket(ARMORSTAND_ID));
                
                // Continue with the rest of the setup
                // NPC
                npcName = "WardrobeNPC-" + NPC_ID;
                while (npcName.length() > 16) {
                    npcName = npcName.substring(16);
                }
                viewerPackets.add(packetBuilder.buildPlayerInfoAddPacket(player, NPC_ID, WARDROBE_UUID, npcName));
                viewerPackets.add(packetBuilder.buildEntitySpawnPacket(NPC_ID, WARDROBE_UUID, EntityType.PLAYER, npcLocation));
                viewerPackets.add(packetBuilder.buildEntityMetadataPacket(NPC_ID, HMCCPacketManager.getPlayerOverlayMetaData()));
                viewerPackets.add(packetBuilder.buildPlayerScoreboardRemovePacket(player, npcName));
                viewerPackets.add(packetBuilder.buildPlayerScoreboardCreatePacket(player, npcName));
                viewerPackets.add(packetBuilder.buildPlayerScoreboardAddPlayersPacket(player, npcName));
                // NPC体型将在动画播放后设置

                // Location
                viewerPackets.add(packetBuilder.buildEntityRotateHeadPacket(NPC_ID, npcLocation));
                viewerPackets.add(packetBuilder.buildEntityRotatePacket(NPC_ID, npcLocation, true));

                // Misc
                if (user.hasCosmeticInSlot(CosmeticSlot.BACKPACK)) {
                    // Maybe null as backpack maybe despawned before entering
                    if (user.getUserBackpackManager() == null) user.respawnBackpack();
                    if (user.isBackpackSpawned()) {
                        // 在Folia环境中使用异步传送
                        user.getUserBackpackManager().getEntityManager().teleportAsync(npcLocation.clone().add(0, 2, 0));

                        viewerPackets.add(packetBuilder.buildEntityEquipmentSlotUpdatePacket(user.getUserBackpackManager().getFirstArmorStandId(), Map.of(EquipmentSlot.HEAD, user.getUserCosmeticItem(user.getCosmetic(CosmeticSlot.BACKPACK)))));
                        viewerPackets.add(packetBuilder.buildEntityMountPacket(NPC_ID, new int[]{user.getUserBackpackManager().getFirstArmorStandId()}));
                        // 背包体型将在动画播放后设置
                    }
                }

                packetSender.sendBundle(viewerPackets, viewer);

                if (user.hasCosmeticInSlot(CosmeticSlot.BALLOON)) {
                    if (user.getBalloonManager() == null) user.respawnBalloon();
                    if (user.isBalloonSpawned()) {
                        CosmeticBalloonType cosmetic = (CosmeticBalloonType) user.getCosmetic(CosmeticSlot.BALLOON);
                        user.getBalloonManager().sendRemoveLeashPacket(viewer);
                        user.getBalloonManager().sendLeashPacket(NPC_ID);
                        //PacketManager.sendLeashPacket(VIEWER.getBalloonEntity().getModelId(), NPC_ID, viewer);

                        Location balloonLocation = npcLocation.clone().add(cosmetic.getBalloonOffset());
                        HMCCPacketManager.sendTeleportPacket(user.getBalloonManager().getPufferfishBalloonId(), balloonLocation, false, viewer);
                        // 在Folia环境中使用异步传送
                        user.getBalloonManager().getModelEntity().teleportAsync(balloonLocation);
                        user.getBalloonManager().setLocation(balloonLocation);
                        // 气球体型将在动画播放后设置
                    }
                }

                if (WardrobeSettings.isEnabledBossbar()) {
                    float progress = WardrobeSettings.getBossbarProgress();
                    Component message = MessagesUtil.processStringNoKey(player, WardrobeSettings.getBossbarMessage());

                    bossBar = BossBar.bossBar(message, progress, WardrobeSettings.getBossbarColor(), WardrobeSettings.getBossbarOverlay());
                    //Audience target = BukkitAudiences.create(HMCCosmeticsPlugin.getInstance()).player(player);

                    player.showBossBar(bossBar);
                }

                if (WardrobeSettings.isEnterOpenMenu()) {
                    Menu menu = Menus.getDefaultMenu();
                    if (menu != null) menu.openMenu(user);
                }

                this.active = true;
                update();
                setWardrobeStatus(WardrobeStatus.RUNNING);
                
                // 在动画播放后设置所有体型
                setAllScalesAfterAnimation();
            });
        };


        if (WardrobeSettings.isEnabledTransition()) {
            MessagesUtil.sendTitle(
                    user.getPlayer(),
                    WardrobeSettings.getTransitionText(),
                    WardrobeSettings.getTransitionFadeIn(),
                    WardrobeSettings.getTransitionStay(),
                    WardrobeSettings.getTransitionFadeOut()
            );
            // 在Folia环境中使用实体调度器，非Folia环境使用全局调度器
            if (SchedulerUtil.isFolia() && user.getPlayer() != null) {
                SchedulerUtil.runTaskLater(HMCCosmeticsPlugin.getInstance(), user.getPlayer(), run, WardrobeSettings.getTransitionDelay());
            } else {
                SchedulerUtil.runTaskLater(HMCCosmeticsPlugin.getInstance(), run, WardrobeSettings.getTransitionDelay());
            }
        } else {
            run.run();
        }

    }

    /**
     * 在动画播放后设置所有体型
     */
    private void setAllScalesAfterAnimation() {
        Player player = user.getPlayer();
        if (player == null) return;
        
        List<Player> viewer = Collections.singletonList(player);
        
        // 设置玩家体型为1
        AttributeInstance scaleAttribute = player.getAttribute(Attribute.SCALE);
        if (scaleAttribute != null) {
            scaleAttribute.setBaseValue(1.0);
        }
        
        // 设置NPC体型为1
        HMCCPacketManager.sendEntityScalePacket(NPC_ID, 1.0, viewer);
        
        // 设置背包体型为1
        if (user.hasCosmeticInSlot(CosmeticSlot.BACKPACK) && user.isBackpackSpawned()) {
            HMCCPacketManager.sendEntityScalePacket(user.getUserBackpackManager().getFirstArmorStandId(), 1.0, viewer);
        }
        
        // 设置气球体型为1
        if (user.hasCosmeticInSlot(CosmeticSlot.BALLOON) && user.isBalloonSpawned()) {
            // 在Folia环境中使用实体调度器处理实体操作
            if (SchedulerUtil.isFolia() && user.getBalloonManager().getModelEntity() != null) {
                SchedulerUtil.runTask(HMCCosmeticsPlugin.getInstance(), user.getBalloonManager().getModelEntity(), () -> {
                    HMCCPacketManager.sendEntityScalePacket(user.getBalloonManager().getModelId(), 1.0, viewer);
                });
            } else {
                HMCCPacketManager.sendEntityScalePacket(user.getBalloonManager().getModelId(), 1.0, viewer);
            }
        }
    }

    public void end() {
        setWardrobeStatus(WardrobeStatus.STOPPING);
        Player player = user.getPlayer();

        List<Player> viewer = Collections.singletonList(player);
        List<Player> outsideViewers = HMCCPacketManager.getViewers(viewingLocation);
        outsideViewers.remove(player);

        if (player == null) return;
        if (!Bukkit.getServer().getAllowFlight()) player.setAllowFlight(false);
        MessagesUtil.sendMessage(player, "closed-wardrobe");

        // 在Folia环境中取消任务
        if (SchedulerUtil.isFolia() && foliaTask != null) {
            foliaTask.cancel();
            foliaTask = null;
        }

        Runnable run = () -> {
            this.active = false;

            // For Wardrobe Temp Cosmetics
            for (Cosmetic cosmetic : user.getCosmetics()) {
                MessagesUtil.sendDebugMessages("Checking... " + cosmetic.getId());
                if (!user.canEquipCosmetic(cosmetic)) {
                    MessagesUtil.sendDebugMessages("Unable to keep " + cosmetic.getId());
                    user.removeCosmeticSlot(cosmetic.getSlot());
                }
            }

            // NPC
            if (user.isBalloonSpawned()) user.getBalloonManager().sendRemoveLeashPacket();
            HMCCPacketManager.sendEntityDestroyPacket(NPC_ID, viewer); // Success
            HMCCPacketManager.sendRemovePlayerPacket(player, WARDROBE_UUID, viewer); // Success

            // Player
            packetBuilder.buildEntityCameraPacket(player.getEntityId()).sendPacket(viewer);
            user.getPlayer().setInvisible(false);

            // Armorstand
            HMCCPacketManager.sendEntityDestroyPacket(ARMORSTAND_ID, viewer); // Sucess

            //PacketManager.sendEntityDestroyPacket(player.getEntityId(), viewer); // Success
            if (WardrobeSettings.isForceExitGamemode()) {
                MessagesUtil.sendDebugMessages("Force Exit Gamemode " + WardrobeSettings.getExitGamemode());
                player.setGameMode(WardrobeSettings.getExitGamemode());
                packetBuilder.buildPlayerGamemodeChangePacket(WardrobeSettings.getExitGamemode()).sendPacket(viewer);
            } else {
                MessagesUtil.sendDebugMessages("Original Gamemode " + this.originalGamemode);
                player.setGameMode(this.originalGamemode);
                packetBuilder.buildPlayerGamemodeChangePacket(this.originalGamemode).sendPacket(viewer);
            }
            user.showPlayer();

            if (user.hasCosmeticInSlot(CosmeticSlot.BACKPACK)) {
                user.respawnBackpack();
                //PacketManager.ridingMountPacket(player.getEntityId(), VIEWER.getBackpackEntity().getEntityId(), viewer);
            }

            if (user.hasCosmeticInSlot(CosmeticSlot.BALLOON)) {
                //user.respawnBalloon();
                //PacketManager.sendLeashPacket(VIEWER.getBalloonEntity().getPufferfishBalloonId(), player.getEntityId(), viewer);
            }

            player.teleportAsync(Objects.requireNonNullElseGet(exitLocation, () -> player.getWorld().getSpawnLocation()), PlayerTeleportEvent.TeleportCause.PLUGIN).thenAccept(success -> {
                if (!success) {
                    MessagesUtil.sendDebugMessages("Failed to teleport player out of wardrobe");
                    return;
                }
                
                // 恢复玩家原始主手槽位位置
                player.getInventory().setHeldItemSlot(this.originalMainHandSlot);
                
                // 恢复玩家原始体型
                AttributeInstance scaleAttribute = player.getAttribute(Attribute.SCALE);
                if (scaleAttribute != null) {
                    scaleAttribute.setBaseValue(this.originalScale);
                }
                
                HashMap<EquipmentSlot, ItemStack> items = new HashMap<>();
                for (EquipmentSlot slot : HMCCInventoryUtils.getPlayerArmorSlots()) {
                    ItemStack item = player.getInventory().getItem(slot);
                    items.put(slot, item);
                }
                /*
                if (WardrobeSettings.isEquipPumpkin()) {
                    items.put(EquipmentSlot.HEAD, player.getInventory().getHelmet());
                }
                 */
                packetBuilder.buildEntityEquipmentSlotUpdatePacket(player.getEntityId(), items).sendPacket(viewer);

                if (WardrobeSettings.isEnabledBossbar()) {
                    //Audience target = BukkitAudiences.create(HMCCosmeticsPlugin.getInstance()).player(player);
                    player.hideBossBar(bossBar);
                }

                user.updateCosmetic();
            });
        };
        run.run();
    }

    public void update() {
        final AtomicInteger data = new AtomicInteger();

        // 在Folia环境中使用实体调度器，确保在正确的线程中访问实体状态
        if (SchedulerUtil.isFolia() && user.getPlayer() != null) {
            // 存储任务引用以便后续取消
            this.foliaTask = SchedulerUtil.runTaskTimer(HMCCosmeticsPlugin.getInstance(), user.getPlayer(), () -> {
                Player player = user.getPlayer();
                if (!active || player == null) {
                    MessagesUtil.sendDebugMessages("WardrobeEnd[user=" + user.getUniqueId() + ",reason=Active is false]");
                    // 在Folia环境中取消任务
                    if (foliaTask != null) {
                        foliaTask.cancel();
                        foliaTask = null;
                    }
                    return;
                }
                MessagesUtil.sendDebugMessages("WardrobeUpdate[user=" + user.getUniqueId() + ",status=" + getWardrobeStatus() + "]");
                List<Player> viewer = Collections.singletonList(player);
                List<Player> outsideViewers = HMCCPacketManager.getViewers(viewingLocation);
                outsideViewers.remove(player);

                // 注释掉自动旋转功能，改为通过物品栏槽位控制
                // Location location = npcLocation;
                // int yaw = data.get();
                // location.setYaw(yaw);
                // 
                // HMCCPacketManager.sendRotateHeadPacket(NPC_ID, location, viewer);
                // user.hidePlayer();
                // int rotationSpeed = WardrobeSettings.getRotationSpeed();
                // int newYaw = HMCCServerUtils.getNextYaw(yaw - 30, rotationSpeed);
                // location.setYaw(newYaw);
                // packetBuilder.buildEntityRotatePacket(NPC_ID, newYaw, 0, false).sendPacket(viewer);
                // int nextyaw = HMCCServerUtils.getNextYaw(yaw, rotationSpeed);
                // data.set(nextyaw);

                for (CosmeticSlot slot : CosmeticSlot.values().values()) {
                    HMCCPacketManager.equipmentSlotUpdate(NPC_ID, user, slot, viewer);
                }

                if (user.hasCosmeticInSlot(CosmeticSlot.BACKPACK) && user.getUserBackpackManager() != null) {
                    // 在Folia环境中使用异步传送
                    // user.getUserBackpackManager().getEntityManager().teleportAsync(location);
                    // packetBuilder.buildEntityMountPacket(NPC_ID, new int[]{user.getUserBackpackManager().getFirstArmorStandId()}).sendPacket(viewer);
                    // user.getUserBackpackManager().getEntityManager().setRotation(nextyaw);
                    HMCCPacketManager.sendEntityDestroyPacket(user.getUserBackpackManager().getFirstArmorStandId(), outsideViewers);
                }

                if (user.hasCosmeticInSlot(CosmeticSlot.BALLOON) && user.isBalloonSpawned()) {
                    // The two lines below broke, solved by listening to PlayerCosmeticPostEquipEvent
                    //PacketManager.sendTeleportPacket(user.getBalloonManager().getPufferfishBalloonId(), npcLocation.add(Settings.getBalloonOffset()), false, viewer);
                    //user.getBalloonManager().getModelEntity().teleport(npcLocation.add(Settings.getBalloonOffset()));
                    user.getBalloonManager().sendRemoveLeashPacket(outsideViewers);
                    if (user.getBalloonManager().getBalloonType() != UserBalloonManager.BalloonType.MODELENGINE) {
                        // 在Folia环境中，使用实体调度器获取实体ID，避免线程安全问题
                        if (SchedulerUtil.isFolia() && user.getBalloonManager().getModelEntity() != null) {
                            SchedulerUtil.runTask(HMCCosmeticsPlugin.getInstance(), user.getBalloonManager().getModelEntity(), () -> {
                                HMCCPacketManager.sendEntityDestroyPacket(user.getBalloonManager().getModelId(), outsideViewers);
                            });
                        } else {
                            HMCCPacketManager.sendEntityDestroyPacket(user.getBalloonManager().getModelId(), outsideViewers);
                        }
                    }
                    user.getBalloonManager().sendLeashPacket(NPC_ID);
                }

                if (WardrobeSettings.isEquipPumpkin()) {
                    HMCCPacketManager.equipmentSlotUpdate(user.getPlayer().getEntityId(), EquipmentSlot.HEAD, new ItemStack(Material.CARVED_PUMPKIN), viewer);
                } else {
                    HMCCPacketManager.equipmentSlotUpdate(user.getPlayer(), true, viewer); // Optifine dumbassery
                }
            }, 0, 2);
        } else {
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    Player player = user.getPlayer();
                    if (!active || player == null) {
                        MessagesUtil.sendDebugMessages("WardrobeEnd[user=" + user.getUniqueId() + ",reason=Active is false]");
                        this.cancel();
                        return;
                    }
                    MessagesUtil.sendDebugMessages("WardrobeUpdate[user=" + user.getUniqueId() + ",status=" + getWardrobeStatus() + "]");
                    List<Player> viewer = Collections.singletonList(player);
                    List<Player> outsideViewers = HMCCPacketManager.getViewers(viewingLocation);
                    outsideViewers.remove(player);

                    Location location = npcLocation;
                    int yaw = data.get();
                    location.setYaw(yaw);

                    HMCCPacketManager.sendRotateHeadPacket(NPC_ID, location, viewer);
                    user.hidePlayer();
                    int rotationSpeed = WardrobeSettings.getRotationSpeed();
                    int newYaw = HMCCServerUtils.getNextYaw(yaw - 30, rotationSpeed);
                    location.setYaw(newYaw);
                    packetBuilder.buildEntityRotatePacket(NPC_ID, newYaw, 0, false).sendPacket(viewer);
                    int nextyaw = HMCCServerUtils.getNextYaw(yaw, rotationSpeed);
                    data.set(nextyaw);

                    for (CosmeticSlot slot : CosmeticSlot.values().values()) {
                        HMCCPacketManager.equipmentSlotUpdate(NPC_ID, user, slot, viewer);
                    }

                    if (user.hasCosmeticInSlot(CosmeticSlot.BACKPACK) && user.getUserBackpackManager() != null) {
                        // 在Folia环境中使用异步传送
                        user.getUserBackpackManager().getEntityManager().teleportAsync(location);
                        packetBuilder.buildEntityMountPacket(NPC_ID, new int[]{user.getUserBackpackManager().getFirstArmorStandId()}).sendPacket(viewer);
                        user.getUserBackpackManager().getEntityManager().setRotation(nextyaw);
                        HMCCPacketManager.sendEntityDestroyPacket(user.getUserBackpackManager().getFirstArmorStandId(), outsideViewers);
                    }

                    if (user.hasCosmeticInSlot(CosmeticSlot.BALLOON) && user.isBalloonSpawned()) {
                        // The two lines below broke, solved by listening to PlayerCosmeticPostEquipEvent
                        //PacketManager.sendTeleportPacket(user.getBalloonManager().getPufferfishBalloonId(), npcLocation.add(Settings.getBalloonOffset()), false, viewer);
                        //user.getBalloonManager().getModelEntity().teleport(npcLocation.add(Settings.getBalloonOffset()));
                        user.getBalloonManager().sendRemoveLeashPacket(outsideViewers);
                        if (user.getBalloonManager().getBalloonType() != UserBalloonManager.BalloonType.MODELENGINE) {
                            HMCCPacketManager.sendEntityDestroyPacket(user.getBalloonManager().getModelId(), outsideViewers);
                        }
                        user.getBalloonManager().sendLeashPacket(NPC_ID);
                    }

                    if (WardrobeSettings.isEquipPumpkin()) {
                        HMCCPacketManager.equipmentSlotUpdate(user.getPlayer().getEntityId(), EquipmentSlot.HEAD, new ItemStack(Material.CARVED_PUMPKIN), viewer);
                    } else {
                        HMCCPacketManager.equipmentSlotUpdate(user.getPlayer(), true, viewer); // Optifine dumbassery
                    }
                }
            };
            runnable.runTaskTimer(HMCCosmeticsPlugin.getInstance(), 0, 2);
        }
    }

    public enum WardrobeStatus {
        SETUP,
        STARTING,
        RUNNING,
        STOPPING,
    }

}