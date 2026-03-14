package com.hibiscusmc.hmccosmetics.listener;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.config.WardrobeSettings;
import com.hibiscusmc.hmccosmetics.config.section.Wardrobe;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBalloonType;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBackpackType;
import com.hibiscusmc.hmccosmetics.hooks.worldguard.WGHook;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.SchedulerUtil;
import com.hibiscusmc.hmccosmetics.util.search.PlayerSearchEngine;
import me.earthme.luminol.api.entity.player.PostPlayerRespawnEvent;
import me.earthme.luminol.api.entity.EntityTeleportAsyncEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.Set;

/**
 * LumilolPlayerGameListener 处理Lumilol API特有的事件
 * 用于在Folia环境下替代标准的Paper事件
 */
public class LumilolPlayerGameListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPostPlayerRespawn(PostPlayerRespawnEvent event) {
        // 仅在Folia环境下处理此事件
        if (!SchedulerUtil.isFolia()) {
            return;
        }
        
        CosmeticUser user = CosmeticUsers.getUser(event.getPlayer());
        if (user == null) return;
        if (user.isInWardrobe()) return;
        if (user.hasCosmeticInSlot(CosmeticSlot.BACKPACK)) {
            // 设置刷新状态标记，用于重生后需要刷新时装的情况
            user.setNeedsRefresh(true);
        }
    }

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

        MessagesUtil.sendDebugMessages("Entity Teleport Async Event");
        if (user == null) {
            MessagesUtil.sendDebugMessages("user is null");
            return;
        }

        // 检查是否是衣柜触发的传送，如果是则跳过时装刷新
        // 衣柜传送使用PlayerTeleportEvent.TeleportCause.PLUGIN，并且玩家在衣柜中
        if (event.getTeleportCause().equals(org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.PLUGIN) && user.isInWardrobe()) {
            MessagesUtil.sendDebugMessages("Wardrobe teleport detected, skipping cosmetic refresh");
            return;
        }

        if (user.isInWardrobe()) {
            user.leaveWardrobe(true);
        }

        // 设置刷新状态标记，表示玩家传送后需要刷新时装
        user.setNeedsRefresh(true);

        // 更新玩家搜索引擎中的位置
        PlayerSearchEngine engine = HMCCosmeticsPlugin.getInstance().getPlayerSearchManager().getEngine();
        if (engine != null && engine instanceof com.hibiscusmc.hmccosmetics.util.search.OctreePlayerSearchEngine) {
            ((com.hibiscusmc.hmccosmetics.util.search.OctreePlayerSearchEngine) engine).updatePlayerPosition(player);
        }

        // WorldGuard相关逻辑已迁移到LumilolWGListener类中处理

        if (event.getTeleportCause().equals(org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) || 
            event.getTeleportCause().equals(org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.END_PORTAL)) {
            return;
        }
    }
    

}