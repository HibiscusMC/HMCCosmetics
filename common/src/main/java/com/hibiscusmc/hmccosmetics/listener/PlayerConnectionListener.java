package com.hibiscusmc.hmccosmetics.listener;

import com.hibiscusmc.hmccosmetics.database.Database;
import com.hibiscusmc.hmccosmetics.entities.InvisibleArmorstand;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        CosmeticUser user = Database.get(event.getPlayer().getUniqueId());
        CosmeticUsers.addUser(user);
        user.updateCosmetic();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        CosmeticUser user = CosmeticUsers.getUser(event.getPlayer());
        if (user == null) { // Remove any passengers if a user failed to initialize. Bugs can cause this to happen
            if (!event.getPlayer().getPassengers().isEmpty()) {
                for (Entity entity : event.getPlayer().getPassengers()) {
                    if (entity instanceof InvisibleArmorstand) {
                        ((InvisibleArmorstand) entity).setHealth(0);
                        entity.remove();
                    }
                }
            }
        }
        if (user.isInWardrobe()) user.leaveWardrobe();
        Database.save(user);
        user.despawnBackpack();
        user.despawnBalloon();
        CosmeticUsers.removeUser(user.getUniqueId());
    }
}