package com.hibiscusmc.hmccosmetics.hooks.worldguard;

import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class WGListener implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        CosmeticUser user = CosmeticUsers.getUser(event.getPlayer());
        if (user == null) return;
        Location location = event.getPlayer().getLocation();
        com.sk89q.worldedit.util.Location loc = BukkitAdapter.adapt(location);
        RegionContainer region = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = region.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(loc);
        // TODO: Add more cosmetics
        if (set.getRegions().size() == 0) {
            user.showCosmetics();
        }
        for (ProtectedRegion protectedRegion : set.getRegions()) {
            if (protectedRegion.getFlags().containsKey(WGHook.getCosmeticEnableFlag())) {
                user.hideCosmetics(CosmeticUser.HiddenReason.WORLDGUARD);
                return;
            }
            if (protectedRegion.getFlags().containsKey(WGHook.getCosmeticWardrobeFlag())) {
                user.enterWardrobe();
            }
        }
    }
}
