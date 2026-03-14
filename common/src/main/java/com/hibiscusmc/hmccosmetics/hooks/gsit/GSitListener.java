package com.hibiscusmc.hmccosmetics.hooks.gsit;

import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * GSitListener 处理GSit插件相关事件
 * 监听玩家坐到其他玩家头上的事件，检查目标玩家是否有背包
 */
public class GSitListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPrePlayerPlayerSit(dev.geco.gsit.api.event.PrePlayerPlayerSitEvent event) {
        Player player = event.getPlayer();
        Player target = event.getTarget();
        
        // 检查玩家是否有绕过权限
        if (player.hasPermission("hmccosmetics.gsit.bypass")) {
            // 有权限的玩家可以坐到任何玩家头上，包括有背包的玩家
            MessagesUtil.sendDebugMessages("GSit: Player " + player.getName() + 
                " has bypass permission and can sit on " + target.getName());
            return;
        }
        
        // 获取目标玩家的CosmeticUser
        CosmeticUser targetUser = CosmeticUsers.getUser(target);
        if (targetUser == null) return;
        
        // 检查目标玩家是否有背包
        if (targetUser.hasCosmeticInSlot(CosmeticSlot.BACKPACK)) {
            // 取消事件，禁止坐到有背包的玩家头上
            event.setCancelled(true);
            
            // 发送消息给尝试坐的玩家
            MessagesUtil.sendMessage(player, "gsit.backpack-blocked");
            
            // 发送调试消息
            MessagesUtil.sendDebugMessages("GSit: Player " + player.getName() + 
                " attempted to sit on " + target.getName() + 
                " who has a backpack equipped. Event cancelled.");
        }
    }
}