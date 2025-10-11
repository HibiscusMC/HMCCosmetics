package com.hibiscusmc.hmccosmetics.hooks.worldguard;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.Wardrobe;
import com.hibiscusmc.hmccosmetics.config.WardrobeSettings;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

/**
 * Contains {@link com.sk89q.worldguard.WorldGuard WorldGuard} related event listeners
 */
public class WGListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMove(@NotNull PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        final Location from = event.getFrom();
        final Location to = event.getTo();
        if (to == null) return;
        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) return;

        CosmeticUser user = CosmeticUsers.getUser(player);
        if (user == null) return;

        // This event is already running on the player's region thread on Folia.
        ApplicableRegionSet set = getRegions(player.getLocation());
        if (user.isHidden() && set.getRegions().isEmpty()) {
            user.showCosmetics(CosmeticUser.HiddenReason.WORLDGUARD);
        }

        Set<String> wardrobeNames = WardrobeSettings.getWardrobeNames();
        for (ProtectedRegion protectedRegion : set.getRegions()) {
            Map<Flag<?>, Object> flags = protectedRegion.getFlags();

            if (flags.containsKey(WGHook.getCosmeticEnableFlag())) {
                if (String.valueOf(flags.get(WGHook.getCosmeticEnableFlag())).equalsIgnoreCase("ALLOW")) {
                    user.showCosmetics(CosmeticUser.HiddenReason.WORLDGUARD);
                } else {
                    user.hideCosmetics(CosmeticUser.HiddenReason.WORLDGUARD);
                }
            }

            if (flags.containsKey(WGHook.getCosmeticWardrobeFlag())) {
                String wardrobeName = String.valueOf(flags.getOrDefault(WGHook.getCosmeticWardrobeFlag(), ""));
                if (wardrobeName.isEmpty() || !wardrobeNames.contains(wardrobeName)) return;
                Wardrobe wardrobe = WardrobeSettings.getWardrobe(wardrobeName);
                if (wardrobe == null) return;
                user.enterWardrobe(wardrobe, true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        final Player player = event.getPlayer();
        final CosmeticUser user = CosmeticUsers.getUser(player);
        if (user == null) return;

        final Location to = event.getTo();
        if (to == null) return;

        // Run shortly after the teleport on the player's entity thread (Folia-safe).
        HMCCosmeticsPlugin.getInstance().scheduler().runForLater(player, 1L, () -> {
            ApplicableRegionSet set = getRegions(to);

            if (user.isHidden() && set.getRegions().isEmpty()) {
                user.showCosmetics(CosmeticUser.HiddenReason.WORLDGUARD);
            }

            for (ProtectedRegion protectedRegion : set.getRegions()) {
                Map<Flag<?>, Object> flags = protectedRegion.getFlags();

                if (flags.containsKey(WGHook.getCosmeticEnableFlag())) {
                    if (String.valueOf(flags.get(WGHook.getCosmeticEnableFlag())).equalsIgnoreCase("ALLOW")) {
                        user.showCosmetics(CosmeticUser.HiddenReason.WORLDGUARD);
                    } else {
                        user.hideCosmetics(CosmeticUser.HiddenReason.WORLDGUARD);
                    }
                    return;
                }

                if (flags.containsKey(WGHook.getCosmeticWardrobeFlag())) {
                    String wardrobeName = String.valueOf(flags.get(WGHook.getCosmeticWardrobeFlag()));
                    if (!WardrobeSettings.getWardrobeNames().contains(wardrobeName)) return;
                    Wardrobe wardrobe = WardrobeSettings.getWardrobe(wardrobeName);
                    if (wardrobe != null) {
                        user.enterWardrobe(wardrobe, true);
                    }
                }
            }
        });
    }

    private ApplicableRegionSet getRegions(Location location) {
        com.sk89q.worldedit.util.Location loc = BukkitAdapter.adapt(location);
        RegionContainer region = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = region.createQuery();
        return query.getApplicableRegions(loc);
    }
}
