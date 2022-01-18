package io.github.fisher2911.hmccosmetics.listener;

import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.database.Database;
import io.github.fisher2911.hmccosmetics.user.User;
import io.github.fisher2911.hmccosmetics.user.UserManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ItemsAdderListener implements Listener {

    private final HMCCosmetics plugin;
    private boolean enabled;
    private boolean loaded;

    public JoinListener(final HMCCosmetics plugin) {
        this.plugin = plugin;
        enabled = HookManager.getInstance().isEnabled(ItemAdderHook.class);
        if (!this.enabled) {
           this.plugin.load();
          this.loaded = true;
          return;
        }    
    }

    @EventHandler
    public void onJoin(final ItemsAdderLoadDataEvent event) {
        this.load();
    }
  
    private void load() {
      if (this.enabled) {
        this.plugin.load();
        this.loaded = true;
    }
}
