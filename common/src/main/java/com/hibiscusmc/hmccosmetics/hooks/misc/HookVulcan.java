package com.hibiscusmc.hmccosmetics.hooks.misc;

import com.hibiscusmc.hmccosmetics.api.events.PlayerWardrobeEnterEvent;
import com.hibiscusmc.hmccosmetics.api.events.PlayerWardrobeLeaveEvent;
import com.hibiscusmc.hmccosmetics.config.Settings;
import me.frep.vulcan.api.event.VulcanFlagEvent;
import me.lojosho.hibiscuscommons.hooks.Hook;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

public class HookVulcan extends Hook {

    private final Set<UUID> EXEMPT = ConcurrentHashMap.newKeySet();

    public HookVulcan() {
        super("Vulcan");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerEnterWardrobe(PlayerWardrobeEnterEvent event) {
        if (!Settings.isVulcanIgnoreViolationInWardrobe()) return;

        EXEMPT.add(event.getUser().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerLeaveWardrobe(PlayerWardrobeLeaveEvent event) {
        EXEMPT.remove(event.getUser().getUniqueId());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onCheck(VulcanFlagEvent event) {
        if (!Settings.isVulcanIgnoreViolationInWardrobe()) return;

        if (!EXEMPT.contains(event.getPlayer().getUniqueId())) return;
        event.setCancelled(true);
    }
}
