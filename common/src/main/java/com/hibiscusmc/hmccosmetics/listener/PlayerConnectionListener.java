package com.hibiscusmc.hmccosmetics.listener;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.DatabaseSettings;
import com.hibiscusmc.hmccosmetics.database.Database;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event.getPlayer().isOp() || event.getPlayer().hasPermission("hmccosmetics.notifyupdate")) {
            if (!HMCCosmeticsPlugin.getLatestVersion().equalsIgnoreCase(HMCCosmeticsPlugin.getInstance().getDescription().getVersion()) && HMCCosmeticsPlugin.getLatestVersion() != null)
                MessagesUtil.sendMessageNoKey(
                        event.getPlayer(),
                        "<br>" +
                                "<GRAY>There is a new version of <light_purple><Bold>HMCCosmetics<reset><gray> available!<br>" +
                                "<GRAY>Current version: <red>" + HMCCosmeticsPlugin.getInstance().getDescription().getVersion() + " <GRAY>| Latest version: <light_purple>" + HMCCosmeticsPlugin.getLatestVersion() + "<br>" +
                                "<GRAY>Download it on <gold><click:OPEN_URL:'https://www.spigotmc.org/resources/100107/'>Spigot<reset> <gray>or <gold><click:OPEN_URL:'https://polymart.org/resource/1879'>Polymart<reset><gray>!" +
                                "<br>"
                );
        }

        Runnable run = () -> {
            CosmeticUser user = Database.get(event.getPlayer().getUniqueId());
            CosmeticUsers.addUser(user);
            MessagesUtil.sendDebugMessages("Run User Join");
            Bukkit.getScheduler().runTaskLater(HMCCosmeticsPlugin.getInstance(), () -> user.updateCosmetic(), 4);
        };

        if (DatabaseSettings.isEnabledDelay()) {
            MessagesUtil.sendDebugMessages("Delay Enabled with " + DatabaseSettings.getDelayLength() + " ticks");
            Bukkit.getScheduler().runTaskLater(HMCCosmeticsPlugin.getInstance(), run, DatabaseSettings.getDelayLength());
        } else {
            run.run();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        CosmeticUser user = CosmeticUsers.getUser(event.getPlayer());
        if (user == null) { // Remove any passengers if a user failed to initialize. Bugs can cause this to happen
            if (!event.getPlayer().getPassengers().isEmpty()) {
                for (Entity entity : event.getPlayer().getPassengers()) {
                    if (entity.getType() == EntityType.ARMOR_STAND) {
                        entity.remove();
                    }
                }
            }
        }
        if (user.isInWardrobe()) user.leaveWardrobe();
        if (user.getUserEmoteManager().isPlayingEmote()) {
            user.getUserEmoteManager().stopEmote();
            event.getPlayer().setInvisible(false);
        }
        Database.save(user);
        user.destroy();
        CosmeticUsers.removeUser(user.getUniqueId());
    }
}