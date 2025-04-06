package com.hibiscusmc.hmccosmetics.listener;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.api.events.PlayerLoadEvent;
import com.hibiscusmc.hmccosmetics.api.events.PlayerPreLoadEvent;
import com.hibiscusmc.hmccosmetics.api.events.PlayerUnloadEvent;
import com.hibiscusmc.hmccosmetics.config.DatabaseSettings;
import com.hibiscusmc.hmccosmetics.database.Database;
import com.hibiscusmc.hmccosmetics.gui.Menus;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import com.hibiscusmc.hmccosmetics.user.manager.UserEmoteManager;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PlayerConnectionListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        if (event.getPlayer().isOp() || event.getPlayer().hasPermission("hmccosmetics.notifyupdate")) {
            if (!HMCCosmeticsPlugin.getInstance().getLatestVersion().equalsIgnoreCase(HMCCosmeticsPlugin.getInstance().getDescription().getVersion()) && HMCCosmeticsPlugin.getInstance().getLatestVersion().isEmpty())
                MessagesUtil.sendMessageNoKey(
                        event.getPlayer(),
                        "<br>" +
                                "<GRAY>There is a new version of <light_purple><Bold>HMCCosmetics<reset><gray> available!<br>" +
                                "<GRAY>Current version: <red>" + HMCCosmeticsPlugin.getInstance().getDescription().getVersion() + " <GRAY>| Latest version: <light_purple>" + HMCCosmeticsPlugin.getInstance().getLatestVersion() + "<br>" +
                                "<GRAY>Download it on <gold><click:OPEN_URL:'https://www.spigotmc.org/resources/100107/'>Spigot<reset> <gray>or <gold><click:OPEN_URL:'https://polymart.org/resource/1879'>Polymart<reset><gray>!" +
                                "<br>"
                );
        }

        // This literally makes me want to end it all but I can't do that so I'll just cry instead
        Runnable run = () -> {
            if (!event.getPlayer().isOnline()) return; // If a player is no longer online, don't run this.
            UUID uuid = event.getPlayer().getUniqueId();

            PlayerPreLoadEvent preLoadEvent = new PlayerPreLoadEvent(uuid);
            Bukkit.getPluginManager().callEvent(preLoadEvent);
            if (preLoadEvent.isCancelled()) return;

            Database.get(uuid).thenAccept(userData -> {
                if (userData == null) {
                    return;
                }

                Bukkit.getScheduler().runTask(HMCCosmeticsPlugin.getInstance(), () -> {
                    CosmeticUser cosmeticUser = CosmeticUsers.getProvider()
                        .createCosmeticUser(uuid)
                        .initialize(userData);
                    cosmeticUser.startTicking();

                    CosmeticUsers.addUser(cosmeticUser);
                    MessagesUtil.sendDebugMessages("Run User Join for " + uuid);

                    PlayerLoadEvent playerLoadEvent = new PlayerLoadEvent(cosmeticUser);
                    Bukkit.getPluginManager().callEvent(playerLoadEvent);

                    // And finally, launch an update for the cosmetics they have.
                    Bukkit.getScheduler().runTaskLater(HMCCosmeticsPlugin.getInstance(), () -> {
                        if (cosmeticUser.getPlayer() == null) return;
                        cosmeticUser.updateCosmetic();
                    }, 4);
                });
            }).exceptionally(ex -> {
                MessagesUtil.sendDebugMessages("Unable to load Cosmetic User " + uuid + ". Exception: " + ex.getMessage());
                return null;
            });
        };

        if (DatabaseSettings.isEnabledDelay()) {
            MessagesUtil.sendDebugMessages("Delay Enabled with " + DatabaseSettings.getDelayLength() + " ticks");
            Bukkit.getScheduler().runTaskLater(HMCCosmeticsPlugin.getInstance(), run, DatabaseSettings.getDelayLength());
        } else {
            run.run();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        CosmeticUser user = CosmeticUsers.getUser(event.getPlayer());
        if (user == null) return; // Player never initialized, don't do anything

        PlayerUnloadEvent playerUnloadEvent = new PlayerUnloadEvent(user);
        Bukkit.getPluginManager().callEvent(playerUnloadEvent);

        if (user.isInWardrobe()) {
            user.leaveWardrobe(true);
            user.getPlayer().setInvisible(false);
        }
        if (user.getUserEmoteManager().isPlayingEmote()) {
            user.getUserEmoteManager().stopEmote(UserEmoteManager.StopEmoteReason.CONNECTION);
            event.getPlayer().setInvisible(false);
        }
        Menus.removeCooldown(event.getPlayer().getUniqueId()); // Removes any menu cooldowns a player might have
        Database.save(user);
        user.destroy();
        CosmeticUsers.removeUser(user.getUniqueId());
    }
}