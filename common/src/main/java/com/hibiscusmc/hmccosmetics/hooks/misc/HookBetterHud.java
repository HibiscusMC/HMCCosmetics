package com.hibiscusmc.hmccosmetics.hooks.misc;

import com.hibiscusmc.hmccosmetics.api.events.PlayerWardrobeEnterEvent;
import com.hibiscusmc.hmccosmetics.api.events.PlayerWardrobeLeaveEvent;
import com.hibiscusmc.hmccosmetics.config.Settings;
import kr.toxicity.hud.api.BetterHud;
import kr.toxicity.hud.api.player.HudPlayer;
import me.lojosho.hibiscuscommons.hooks.Hook;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class HookBetterHud extends Hook {

    public HookBetterHud() {
        super("BetterHUD");
        setActive(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerEnterWardrobe(@NotNull PlayerWardrobeEnterEvent event) {
        if (!Settings.isWardrobeHideHud()) return;
        UUID uuid = event.getUniqueId();
        HudPlayer hudPlayer = BetterHud.getInstance().getPlayerManager().getHudPlayer(uuid);
        if (hudPlayer == null) return;
        hudPlayer.setHudEnabled(false);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerLeaveWardrobe(@NotNull PlayerWardrobeLeaveEvent event) {
        if (!Settings.isWardrobeHideHud()) return;
        UUID uuid = event.getUniqueId();
        HudPlayer hudPlayer = BetterHud.getInstance().getPlayerManager().getHudPlayer(uuid);
        if (hudPlayer == null) return;
        hudPlayer.setHudEnabled(true);
    }
}
