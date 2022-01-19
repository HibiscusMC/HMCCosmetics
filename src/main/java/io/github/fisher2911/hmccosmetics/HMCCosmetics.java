package io.github.fisher2911.hmccosmetics;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

import io.github.fisher2911.hmccosmetics.command.CosmeticsCommand;
import io.github.fisher2911.hmccosmetics.cosmetic.CosmeticManager;
import io.github.fisher2911.hmccosmetics.database.Database;
import io.github.fisher2911.hmccosmetics.database.DatabaseFactory;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.gui.CosmeticsMenu;
import io.github.fisher2911.hmccosmetics.hook.HookManager;
import io.github.fisher2911.hmccosmetics.hook.item.ItemsAdderHook;
import io.github.fisher2911.hmccosmetics.listener.*;
import io.github.fisher2911.hmccosmetics.message.MessageHandler;
import io.github.fisher2911.hmccosmetics.message.Messages;
import io.github.fisher2911.hmccosmetics.user.UserManager;
import me.mattstudios.mf.base.CommandManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class HMCCosmetics extends JavaPlugin {

    private ProtocolManager protocolManager;
    private UserManager userManager;
    private CosmeticManager cosmeticManager;
    private MessageHandler messageHandler;
    private CosmeticsMenu cosmeticsMenu;
    private CommandManager commandManager;
    private Database database;

    @Override
    public void onEnable() {
        final int pluginId = 13873;
        final Metrics metrics = new Metrics(this, pluginId);

        protocolManager = ProtocolLibrary.getProtocolManager();
        this.messageHandler = new MessageHandler(this);
        this.userManager = new UserManager(this);
        this.cosmeticManager = new CosmeticManager(new HashMap<>());
        this.cosmeticsMenu = new CosmeticsMenu(this);

        this.userManager.startTeleportTask();

        this.database = DatabaseFactory.create(this);

        this.registerCommands();
        this.registerListeners();

        if (!HookManager.getInstance().isEnabled(ItemsAdderHook.class)) {
            this.load();
        }

        HookManager.getInstance().registerListeners(this);
    }

    @Override
    public void onDisable() {
        this.messageHandler.close();
        this.userManager.cancelTeleportTask();
        this.userManager.removeAll();
        this.database.close();
    }

    private void registerListeners() {
        final List<Listener> listeners = List.of(
                        new JoinListener(this),
                        new ClickListener(this),
                        new TeleportListener(this),
                        new RespawnListener(this),
                        new HatRemoveFixListener(this)
                );
        for (final Listener : listener) {
            this.getLogger().info("Loading listener: " + listener.class);
            this.getServer().getPluginManager().registerEvents(listener, this);
        }
    }

    private void registerCommands() {
        this.commandManager = new CommandManager(this, true);
        this.commandManager.getMessageHandler().register(
                "cmd.no.console", player ->
                        this.messageHandler.sendMessage(
                                player,
                                Messages.MUST_BE_PLAYER
                        )

        );
        this.commandManager.getCompletionHandler().register("#types",
                resolver ->
                        Arrays.stream(ArmorItem.Type.
                                        values()).
                                map(ArmorItem.Type::toString).
                                collect(Collectors.toList())
        );
        this.commandManager.getCompletionHandler().register("#ids",
                resolver ->
                        this.cosmeticManager.getAll().stream().map(ArmorItem::getId).collect(Collectors.toList()));
        this.commandManager.register(new CosmeticsCommand(this));
    }

    public void load() {
        this.messageHandler.load();
        this.cosmeticsMenu.load();
        this.database.load();
    }

    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public CosmeticManager getCosmeticManager() {
        return cosmeticManager;
    }

    public CosmeticsMenu getCosmeticsMenu() {
        return cosmeticsMenu;
    }

    public ProtocolManager getProtocolManager() {
        return protocolManager;
    }

    public Database getDatabase() {
        return database;
    }
}

