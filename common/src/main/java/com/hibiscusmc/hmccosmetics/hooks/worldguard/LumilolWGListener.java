package com.hibiscusmc.hmccosmetics.hooks.worldguard;

import com.hibiscusmc.hmccosmetics.config.section.Wardrobe;
import com.hibiscusmc.hmccosmetics.config.WardrobeSettings;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.SchedulerUtil;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.earthme.luminol.api.entity.EntityTeleportAsyncEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.Set;

/**
 * LumilolWGListener 处理Folia环境下WorldGuard相关的事件
 * 用于在Folia环境下替代标准的WorldGuard事件监听器
 */
public class LumilolWGListener implements Listener {

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityTeleportAsync(EntityTeleportAsyncEvent event) {
        // 仅在Folia环境下处理此事件
        if (!SchedulerUtil.isFolia()) {
            return;
        }
        
        // 只处理玩家传送
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        
        CosmeticUser user = CosmeticUsers.getUser(player.getUniqueId());

        MessagesUtil.sendDebugMessages("LumilolWGListener: Entity Teleport Async Event");
        if (user == null) {
            MessagesUtil.sendDebugMessages("LumilolWGListener: user is null");
            return;
        }

        // 检查是否是衣柜触发的传送，如果是则跳过WorldGuard处理
        if (event.getTeleportCause().equals(org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.PLUGIN) && user.isInWardrobe()) {
            MessagesUtil.sendDebugMessages("LumilolWGListener: Wardrobe teleport detected, skipping WorldGuard processing");
            return;
        }

        // 处理WorldGuard相关逻辑
        if (com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin.getInstance().getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            Location location = event.getDestination();
            ApplicableRegionSet set = getRegions(location);
            
            // 处理隐藏/显示逻辑
            if (user.isHidden()) {
                if (set.getRegions().isEmpty()) {
                    user.showCosmetics(CosmeticUser.HiddenReason.WORLDGUARD);
                }
            }
            
            // 处理区域标志
            for (ProtectedRegion protectedRegion : set.getRegions()) {
                Map<Flag<?>, Object> flags = protectedRegion.getFlags();
                
                // 处理时装启用标志
                if (flags.containsKey(WGHook.getCosmeticEnableFlag())) {
                    if (flags.get(WGHook.getCosmeticEnableFlag()).toString().equalsIgnoreCase("ALLOW")) {
                        user.showCosmetics(CosmeticUser.HiddenReason.WORLDGUARD);
                        return;
                    }
                    user.hideCosmetics(CosmeticUser.HiddenReason.WORLDGUARD);
                    return;
                }
                
                // 处理衣柜标志
                if (flags.containsKey(WGHook.getCosmeticWardrobeFlag())) {
                    Set<String> wardrobeNames = WardrobeSettings.getWardrobeNames();
                    String wardrobeName = flags.get(WGHook.getCosmeticWardrobeFlag()).toString();
                    
                    if (!wardrobeNames.contains(wardrobeName)) {
                        MessagesUtil.sendDebugMessages("LumilolWGListener: Wardrobe name not found: " + wardrobeName);
                        return;
                    }
                    
                    Wardrobe wardrobe = WardrobeSettings.getWardrobe(wardrobeName);
                    if (wardrobe == null) {
                        MessagesUtil.sendDebugMessages("LumilolWGListener: Wardrobe is null for name: " + wardrobeName);
                        return;
                    }
                    
                    user.enterWardrobe(wardrobe, true);
                    MessagesUtil.sendDebugMessages("LumilolWGListener: Player entered wardrobe via WorldGuard region");
                }
            }
        }

        // 跳过特定传送类型
        if (event.getTeleportCause().equals(org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) || 
            event.getTeleportCause().equals(org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.END_PORTAL)) {
            return;
        }
    }
    
    /**
     * 获取指定位置的区域集合
     * @param location 位置
     * @return 区域集合
     */
    private ApplicableRegionSet getRegions(Location location) {
        com.sk89q.worldedit.util.Location loc = BukkitAdapter.adapt(location);
        RegionContainer region = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = region.createQuery();
        return query.getApplicableRegions(loc);
    }
}