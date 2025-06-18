package com.hibiscusmc.hmccosmetics.hooks.worldguard;

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
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) return;

        CosmeticUser user = CosmeticUsers.getUser(player);
        if (user == null) return;
        Location location = player.getLocation();
        ApplicableRegionSet set = getRegions(location);
        if (user.isHidden() && set.getRegions().isEmpty()) {
            user.showCosmetics(CosmeticUser.HiddenReason.WORLDGUARD);
        }

        Set<String> wardrobeNames = WardrobeSettings.getWardrobeNames();
        for (ProtectedRegion protectedRegion : set.getRegions()) {
            Map<Flag<?>, Object> flags = protectedRegion.getFlags();
            if (flags.containsKey(WGHook.getCosmeticEnableFlag())) {
                if (flags.get(WGHook.getCosmeticEnableFlag()).toString().equalsIgnoreCase("ALLOW")) {
                    user.showCosmetics(CosmeticUser.HiddenReason.WORLDGUARD);
                } else {
                    user.hideCosmetics(CosmeticUser.HiddenReason.WORLDGUARD);
                }
            }
            if (flags.containsKey(WGHook.getCosmeticWardrobeFlag())) {
                String wardrobeName = flags.getOrDefault(WGHook.getCosmeticWardrobeFlag(), "").toString();
                if (wardrobeName.isEmpty() || !wardrobeNames.contains(wardrobeName)) return;
                Wardrobe wardrobe = WardrobeSettings.getWardrobe(wardrobeName);
                if (wardrobe == null) return;
                user.enterWardrobe(wardrobe, true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        CosmeticUser user = CosmeticUsers.getUser(event.getPlayer());
        if (user == null) return;
        Location location = event.getTo();
        ApplicableRegionSet set = getRegions(location);
        if (user.isHidden()) {
            if (set.getRegions().isEmpty()) {
                user.showCosmetics(CosmeticUser.HiddenReason.WORLDGUARD);
            }
        }
        for (ProtectedRegion protectedRegion : set.getRegions()) {
            if (protectedRegion.getFlags().containsKey(WGHook.getCosmeticEnableFlag())) {
                if (protectedRegion.getFlags().get(WGHook.getCosmeticEnableFlag()).toString().equalsIgnoreCase("ALLOW")) {
                    user.showCosmetics(CosmeticUser.HiddenReason.WORLDGUARD);
                    return;
                }
                user.hideCosmetics(CosmeticUser.HiddenReason.WORLDGUARD);
                return;
            }
            if (protectedRegion.getFlags().containsKey(WGHook.getCosmeticWardrobeFlag())) {
                if (!WardrobeSettings.getWardrobeNames().contains(protectedRegion.getFlags().get(WGHook.getCosmeticWardrobeFlag()).toString())) return;
                Wardrobe wardrobe = WardrobeSettings.getWardrobe(protectedRegion.getFlags().get(WGHook.getCosmeticWardrobeFlag()).toString());
                user.enterWardrobe(wardrobe, true);
            }
        }
    }

    private ApplicableRegionSet getRegions(Location location) {
        com.sk89q.worldedit.util.Location loc = BukkitAdapter.adapt(location);
        RegionContainer region = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = region.createQuery();
        return query.getApplicableRegions(loc);
    }
}
