package io.github.fisher2911.hmccosmetics;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

import io.github.fisher2911.hmccosmetics.command.CosmeticsCommand;
import io.github.fisher2911.hmccosmetics.concurrent.Threads;
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
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.sql.SQLException;
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

    private BukkitTask saveTask;

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

        try {
            this.database = DatabaseFactory.create(this);
        } catch (final SQLException exception) {
            exception.printStackTrace();
        }

        this.registerCommands();
        this.registerListeners();

        if (!HookManager.getInstance().isEnabled(ItemsAdderHook.class)) {
            this.load();
        }

        HookManager.getInstance().registerListeners(this);

        this.saveTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
                this,
                () -> {
                    for (final Player player : Bukkit.getOnlinePlayers()) {
                        this.userManager.get(player.getUniqueId()).ifPresent(
                                this.database::saveUser
                        );
                    }
                },
                20,
                20 * 60
        );
    }

    @Override
    public void onDisable() {
        this.saveTask.cancel();
        this.database.saveAll();
        this.messageHandler.close();
        this.userManager.cancelTeleportTask();
        this.userManager.removeAll();
        Threads.getInstance().onDisable();
        this.database.close();
    }

    private void registerListeners() {
        List.of(
                        new JoinListener(this),
                        new ClickListener(this),
                        new TeleportListener(this),
                        new RespawnListener(this),
                        new CosmeticFixListener(this)
                ).
                forEach(
                        listener -> this.getServer().getPluginManager().registerEvents(listener, this)
                );
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
        Bukkit.getScheduler().runTaskAsynchronously(this,
                () -> {
                    this.messageHandler.load();
                    this.cosmeticsMenu.load();
                    this.database.load();
                });
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

