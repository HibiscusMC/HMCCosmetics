package io.github.fisher2911.hmccosmetics.hook.listener;

import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ItemsAdderListener implements Listener {

    private final HMCCosmetics plugin;
    private boolean loaded;

    public ItemsAdderListener(final HMCCosmetics plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onItemAdderLoad(final ItemsAdderLoadDataEvent event) {
        if (this.loaded) return;
        this.plugin.load();
        this.loaded = true;
    }
}
