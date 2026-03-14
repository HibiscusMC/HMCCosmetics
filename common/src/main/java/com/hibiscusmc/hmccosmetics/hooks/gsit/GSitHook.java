package com.hibiscusmc.hmccosmetics.hooks.gsit;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import me.lojosho.hibiscuscommons.hooks.Hook;
import org.bukkit.Bukkit;

/**
 * GSitHook 集成GSit插件
 * 提供与GSit插件的兼容性，确保背包时装不会被其他玩家坐到头上
 */
public class GSitHook extends Hook {

    public GSitHook() {
        super("GSit");
        setActive(true);
    }

    public void onHook() {
        // 注册GSit事件监听器
        Bukkit.getPluginManager().registerEvents(new GSitListener(), HMCCosmeticsPlugin.getInstance());
        MessagesUtil.sendDebugMessages("GSit hook has been enabled!");
    }

    public void onUnhook() {
        // 清理资源
        MessagesUtil.sendDebugMessages("GSit hook has been disabled!");
    }
}