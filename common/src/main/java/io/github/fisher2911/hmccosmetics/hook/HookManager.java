package io.github.fisher2911.hmccosmetics.hook;

import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.hook.item.ItemHook;
import io.github.fisher2911.hmccosmetics.hook.item.ItemHooks;
import io.github.fisher2911.hmccosmetics.hook.item.ItemsAdderHook;
import io.github.fisher2911.hmccosmetics.hook.item.OraxenHook;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.Nullable;

public class HookManager {

    private static final HookManager INSTANCE;

    static {
        INSTANCE = new HookManager(HMCCosmetics.getPlugin(HMCCosmetics.class));
    }

    private final HMCCosmetics plugin;
    private final ItemHooks itemHooks;
    private final PAPIHook papiHook;
    private final CitizensHook citizensHook;
    private final ModelEngineHook modelEngineHook;
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
        final CitizensHook citizensHook = new CitizensHook(this.plugin);
        if (pluginManager.getPlugin("Oraxen") != null) {
            itemHookMap.put(oraxenHook.getIdentifier(), oraxenHook);
        }
        if (pluginManager.getPlugin("ItemsAdder") != null) {
            itemHookMap.put(itemsAdderHook.getIdentifier(), itemsAdderHook);
            this.listeners.add(itemsAdderHook);
        }
        if (pluginManager.getPlugin("Citizens") != null) {
            this.registerHook(citizensHook.getClass());
            this.listeners.add(citizensHook);
            this.citizensHook = citizensHook;
            this.plugin.getLogger().info("Successfully Hooked into Citizens!");
        } else {
            this.citizensHook = null;
        }
        if (pluginManager.getPlugin("ModelEngine") != null) {
            this.plugin.getLogger().info("ModelEngine has been detected, but is disabled in this build");
            this.modelEngineHook = null;
            // As of the new 3.0 betas, Model Engine integration is now outdated. Readd these lines once updated.
            //final ModelEngineHook modelEngineHook = new ModelEngineHook();
            //this.registerHook(modelEngineHook.getClass());
            //this.modelEngineHook = modelEngineHook;
        } else {
            this.modelEngineHook = null;
        }

        this.itemHooks = new ItemHooks(itemHookMap);
        itemHookMap.values().forEach(hook -> this.registerHook(hook.getClass()));
    }

    public static HookManager getInstance() {
        return INSTANCE;
    }

    protected void registerHook(final Class<? extends Hook> hook) {
        this.registeredHooks.add(hook);
    }

    public boolean isEnabled(final Class<? extends Hook> hook) {
        return this.registeredHooks.contains(hook);
    }

    public void init() {
        if (this.isEnabled(PAPIHook.class)) {
            new PAPIExpansion(this.plugin).register();
        }
        this.registerListeners(this.plugin);
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

    @Nullable
    public CitizensHook getCitizensHook() {
        return this.citizensHook;
    }

    @Nullable
    public ModelEngineHook getModelEngineHook() {
        return modelEngineHook;
    }

    public ItemHooks getItemHooks() {
        return itemHooks;
    }

}
