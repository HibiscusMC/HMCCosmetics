package io.github.fisher2911.hmccosmetics;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import io.github.fisher2911.hmccosmetics.command.CosmeticsCommand;
import io.github.fisher2911.hmccosmetics.concurrent.Threads;
import io.github.fisher2911.hmccosmetics.config.Settings;
import io.github.fisher2911.hmccosmetics.cosmetic.CosmeticManager;
import io.github.fisher2911.hmccosmetics.database.Database;
import io.github.fisher2911.hmccosmetics.database.DatabaseFactory;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.gui.CosmeticsMenu;
import io.github.fisher2911.hmccosmetics.hook.HookManager;
import io.github.fisher2911.hmccosmetics.hook.CitizensHook;
import io.github.fisher2911.hmccosmetics.hook.item.ItemsAdderHook;
import io.github.fisher2911.hmccosmetics.listener.ClickListener;
import io.github.fisher2911.hmccosmetics.listener.CosmeticFixListener;
import io.github.fisher2911.hmccosmetics.listener.JoinListener;
import io.github.fisher2911.hmccosmetics.listener.MoveListener;
import io.github.fisher2911.hmccosmetics.listener.PlayerShiftListener;
import io.github.fisher2911.hmccosmetics.listener.RespawnListener;
import io.github.fisher2911.hmccosmetics.listener.TeleportListener;
import io.github.fisher2911.hmccosmetics.listener.WardrobeClickListener;
import io.github.fisher2911.hmccosmetics.message.MessageHandler;
import io.github.fisher2911.hmccosmetics.message.Messages;
import io.github.fisher2911.hmccosmetics.message.Translation;
import io.github.fisher2911.hmccosmetics.task.TaskManager;
import io.github.fisher2911.hmccosmetics.user.UserManager;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import me.mattstudios.mf.base.CommandManager;
import me.mattstudios.mf.base.CompletionHandler;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class HMCCosmetics extends JavaPlugin {

    public static final Path PLUGIN_FOLDER = Paths.get("plugins", "HMCCosmetics");

    private ProtocolManager protocolManager;
    private TaskManager taskManager;
    private Settings settings;
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

        this.protocolManager = ProtocolLibrary.getProtocolManager();
        this.taskManager = new TaskManager(this);
        this.taskManager.start();
        this.settings = new Settings(this);
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

        HookManager.getInstance().init();

        this.saveTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
                this,
                () -> Threads.getInstance().execute(
                        () -> this.database.saveAll()
                ),
                20 * 60,
                20 * 60
        );
    }

    @Override
    public void onDisable() {
        this.saveTask.cancel();
        this.database.saveAll();
        this.messageHandler.close();
        this.userManager.removeAll();
        Threads.getInstance().onDisable();
        this.database.close();
        this.taskManager.end();
    }

    private void registerListeners() {
        List.of(
                        new JoinListener(this),
                        new ClickListener(this),
                        new TeleportListener(this),
                        new RespawnListener(this),
                        new CosmeticFixListener(this),
                        new PlayerShiftListener(this),
                        new MoveListener(this),
                        new WardrobeClickListener(this)
                ).
                forEach(
                        listener -> this.getServer().getPluginManager()
                                .registerEvents(listener, this)
                );
    }

    private void registerCommands() {
        this.commandManager = new CommandManager(this, true);
        final HookManager hookManager = HookManager.getInstance();
        this.commandManager.getMessageHandler().register(
                "cmd.no.console", player ->
                        this.messageHandler.sendMessage(
                                player,
                                Messages.MUST_BE_PLAYER
                        )
        );
        final CompletionHandler completionHandler = this.commandManager.getCompletionHandler();
        completionHandler.register("#types",
                resolver ->
                        Arrays.stream(ArmorItem.Type.
                                        values()).
                                map(ArmorItem.Type::toString).
                                collect(Collectors.toList())
        );
        completionHandler.register("#ids",
                resolver ->
                        this.cosmeticManager.getAll().stream().map(ArmorItem::getId)
                                .collect(Collectors.toList()));
        completionHandler.register("#npc-args",
                resolver -> List.of(CosmeticsCommand.NPC_REMOVE, CosmeticsCommand.NPC_APPLY));
        completionHandler.register("#npcs", resolver -> {
            final List<String> ids = new ArrayList<>();
            if (!hookManager.isEnabled(CitizensHook.class)) return ids;
            for (final int id : hookManager.getCitizensHook().getAllNPCS()) {
                ids.add(String.valueOf(id));
            }
            return ids;
        });
        this.commandManager.register(new CosmeticsCommand(this));
    }

    public void load() {
        Bukkit.getScheduler().runTaskLaterAsynchronously(this,
                () -> {
                    this.settings.load();
                    this.messageHandler.load();
                    this.cosmeticsMenu.load();
                    Translation.getInstance().load();
                    this.database.load();
                }, 1);
    }

    public void reload() {
        Bukkit.getScheduler().runTaskAsynchronously(this,
                () -> {
                    this.settings.load();
                    this.messageHandler.load();
                    this.cosmeticsMenu.reload();
                    Translation.getInstance().load();
                });
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

    public Settings getSettings() {
        return settings;
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

