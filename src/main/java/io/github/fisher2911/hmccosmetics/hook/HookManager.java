package io.github.fisher2911.hmccosmetics.hook;

import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.hook.item.ItemHook;
import io.github.fisher2911.hmccosmetics.hook.item.ItemHooks;
import io.github.fisher2911.hmccosmetics.hook.item.ItemsAdderHook;
import io.github.fisher2911.hmccosmetics.hook.item.OraxenHook;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HookManager {

    private static final HookManager INSTANCE;

    static {
        INSTANCE = new HookManager(HMCCosmetics.getPlugin(HMCCosmetics.class));
    }

    public static HookManager getInstance() {
        return INSTANCE;
    }

    private final HMCCosmetics plugin;

    private final ItemHooks itemHooks;
    private final PAPIHook papiHook;
    private final Set<Class<? extends Hook>> registeredHooks;
    private final Set<Listener> listeners;

    private HookManager(final HMCCosmetics plugin) {
        this.plugin = plugin;
        this.registeredHooks = new HashSet<>();
        this.listeners = new HashSet<>();
        final PluginManager pluginManager = Bukkit.getPluginManager();
        if (pluginManager.getPlugin("PlaceholderApi") != null) {
            this.registeredHooks.add(PAPIHook.class);
            this.papiHook = new PAPIHook();
        } else {
            this.papiHook = null;
        }

        final Map<String, ItemHook> itemHookMap = new HashMap<>();
        final OraxenHook oraxenHook = new OraxenHook();
        final ItemsAdderHook itemsAdderHook = new ItemsAdderHook();
        if (pluginManager.getPlugin("Oraxen") != null) itemHookMap.put(oraxenHook.getIdentifier(), oraxenHook);
        if (pluginManager.getPlugin("ItemsAdder") != null) {
            itemHookMap.put(itemsAdderHook.getIdentifier(), itemsAdderHook);
            this.listeners.add(itemsAdderHook);
        }

        this.itemHooks = new ItemHooks(itemHookMap);
        itemHookMap.values().forEach(hook -> this.registerHook(hook.getClass()));
    }

    protected void registerHook(final Class<? extends Hook> hook) {
        this.registeredHooks.add(hook);
    }

    public boolean isEnabled(final Class<? extends Hook> hook) {
        return this.registeredHooks.contains(hook);
    }

    public void registerListeners(final HMCCosmetics plugin) {
        for (final Listener listener : this.listeners) {
            plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        }
    }

    @Nullable
    public PAPIHook getPapiHook() {
        return papiHook;
    }

    public ItemHooks getItemHooks() {
        return itemHooks;
    }
}
