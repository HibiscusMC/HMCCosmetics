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
import org.jetbrains.annotations.NotNull;

public class ServerListener implements Listener {

    @EventHandler(priority = EventPriority.LOW)
    public void onHookReload(@NotNull HibiscusHookReload event) {
        final Hook hook = event.getHook();
        final HibiscusHookReload.ReloadType reloadType = event.getReloadType();

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

    @EventHandler(priority = EventPriority.NORMAL)
    public void onAllHooksReady(@NotNull HibiscusHooksAllActiveEvent event) {
        HMCCosmeticsPlugin.setup();
    }
}
