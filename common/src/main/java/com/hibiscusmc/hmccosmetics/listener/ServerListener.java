package com.hibiscusmc.hmccosmetics.listener;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.Settings;
import me.lojosho.hibiscuscommons.api.events.HibiscusHookReload;
import me.lojosho.hibiscuscommons.api.events.HibiscusHooksAllActiveEvent;
import me.lojosho.hibiscuscommons.hooks.Hook;
import me.lojosho.hibiscuscommons.hooks.items.HookItemAdder;
import me.lojosho.hibiscuscommons.hooks.items.HookNexo;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ServerListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHookReload(HibiscusHookReload event) {
        Hook hook = event.getHook();
        HibiscusHookReload.ReloadType reloadType = event.getReloadType();
        if (hook instanceof HookItemAdder) {
            if (reloadType == HibiscusHookReload.ReloadType.RELOAD) {
                if (!Settings.isItemsAdderChangeReload()) return;
                HMCCosmeticsPlugin.setup();
            }
        }
        if (hook instanceof HookNexo) {
            if (reloadType == HibiscusHookReload.ReloadType.RELOAD) {
                if (!Settings.isNexoChangeReload()) return;
                HMCCosmeticsPlugin.setup();
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAllHooksReady(HibiscusHooksAllActiveEvent event) {
        HMCCosmeticsPlugin.setup();
    }
}
