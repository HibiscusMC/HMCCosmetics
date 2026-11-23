package com.hibiscusmc.hmccosmetics.listener;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.api.events.PlayerCosmeticPostEquipEvent;
import com.hibiscusmc.hmccosmetics.api.events.PlayerCosmeticRemoveEvent;
import com.hibiscusmc.hmccosmetics.database.Database;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Saves a user one tick after a cosmetic was equipped, changed or removed, so another server sharing the storage sees
 * the change without waiting for the player to quit. Several changes within a tick make one save.
 * Only registered for storages that servers share (see {@link HMCCosmeticsPlugin}).
 */
public class CosmeticSaveListener implements Listener {

    private final Set<UUID> pending = new HashSet<>();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEquip(PlayerCosmeticPostEquipEvent event) {
        scheduleSave(event.getUser());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRemove(PlayerCosmeticRemoveEvent event) {
        scheduleSave(event.getUser());
    }

    private void scheduleSave(CosmeticUser user) {
        // A user that is not registered is still loading (the load must not be written back) or is quitting (quit saves).
        if (CosmeticUsers.getUser(user.getUniqueId()) != user || !pending.add(user.getUniqueId())) return;
        Bukkit.getScheduler().runTask(HMCCosmeticsPlugin.getInstance(), () -> {
            pending.remove(user.getUniqueId());
            if (CosmeticUsers.getUser(user.getUniqueId()) == user) Database.save(user);
        });
    }
}
