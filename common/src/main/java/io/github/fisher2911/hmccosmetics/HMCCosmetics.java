package io.github.fisher2911.hmccosmetics;

import io.github.fisher2911.hmccosmetics.command.CosmeticsCommand;
import io.github.fisher2911.hmccosmetics.concurrent.Threads;
import io.github.fisher2911.hmccosmetics.config.Settings;
import io.github.fisher2911.hmccosmetics.config.TokenLoader;
import io.github.fisher2911.hmccosmetics.cosmetic.CosmeticManager;
import io.github.fisher2911.hmccosmetics.database.Database;
import io.github.fisher2911.hmccosmetics.database.DatabaseFactory;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.gui.CosmeticsMenu;
import io.github.fisher2911.hmccosmetics.gui.Token;
import io.github.fisher2911.hmccosmetics.hook.CitizensHook;
import io.github.fisher2911.hmccosmetics.hook.HookManager;
import io.github.fisher2911.hmccosmetics.hook.item.ItemsAdderHook;
import io.github.fisher2911.hmccosmetics.listener.ClickListener;
import io.github.fisher2911.hmccosmetics.listener.CosmeticFixListener;
import io.github.fisher2911.hmccosmetics.listener.JoinListener;
import io.github.fisher2911.hmccosmetics.listener.PlayerShiftListener;
import io.github.fisher2911.hmccosmetics.listener.RespawnListener;
import io.github.fisher2911.hmccosmetics.listener.TeleportListener;
import io.github.fisher2911.hmccosmetics.listener.WardrobeListener;
import io.github.fisher2911.hmccosmetics.message.MessageHandler;
import io.github.fisher2911.hmccosmetics.message.Messages;
import io.github.fisher2911.hmccosmetics.message.Translation;
import io.github.fisher2911.hmccosmetics.task.TaskManager;
import io.github.fisher2911.hmccosmetics.user.UserManager;
import me.mattstudios.mf.base.CommandManager;
import me.mattstudios.mf.base.CompletionHandler;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class HMCCosmetics extends JavaPlugin {

    public static final Path PLUGIN_FOLDER = Paths.get("plugins", "HMCCosmetics");

    private TaskManager taskManager;
    private Settings settings;
    private UserManager userManager;
    private CosmeticManager cosmeticManager;
    private MessageHandler messageHandler;
    private CosmeticsMenu cosmeticsMenu;
    private TokenLoader tokenLoader;
    private CommandManager commandManager;
    private Database database;

    private BukkitTask saveTask;

    /* // commented because PacketEvents is no longer shaded
    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }
*/
    @Override
    public void onEnable() {
        final int pluginId = 13873;
        final Metrics metrics = new Metrics(this, pluginId);
        this.taskManager = new TaskManager(this);
        this.taskManager.start();
        this.settings = new Settings(this);
        this.messageHandler = new MessageHandler(this);
        this.userManager = new UserManager(this);
        this.cosmeticManager = new CosmeticManager(new HashMap<>(), new HashMap<>(), new HashMap<>());
        this.cosmeticsMenu = new CosmeticsMenu(this);
        this.tokenLoader = new TokenLoader(this);

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
        //PacketEvents.getAPI().terminate();
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
                        new WardrobeListener(this)
                ).
                forEach(
                        listener -> this.getServer().getPluginManager()
                                .registerEvents(listener, this)
                );
    }

    private void registerCommands() {
        this.commandManager = new CommandManager(this, true);
        final HookManager hookManager = HookManager.getInstance();
        final me.mattstudios.mf.base.MessageHandler messageHandler = this.commandManager.getMessageHandler();
        messageHandler.register("cmd.no.console", player ->
                        this.messageHandler.sendMessage(
                                player,
                                Messages.MUST_BE_PLAYER
                        )
        );
        messageHandler.register("cmd.no.permission", player ->
                        this.messageHandler.sendMessage(
                                player,
                                Messages.NO_PERMISSION
                        )
        );
        messageHandler.register("cmd.no.exists", player ->
                this.messageHandler.sendMessage(
                        player,
                        Messages.HELP_COMMAND
                ));
        messageHandler.register("cmd.wrong.usage", player ->
                this.messageHandler.sendMessage(
                        player,
                        Messages.HELP_COMMAND
                ));
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
                        this.cosmeticManager.getAllArmorItems().stream().map(ArmorItem::getId)
                                .collect(Collectors.toList()));
        completionHandler.register("#tokens",
                resolver ->
                        this.cosmeticManager.getAllTokens().stream().map(Token::getId)
                                .collect(Collectors.toList()));
        completionHandler.register("#menus",
                resolver -> new ArrayList<>(this.cosmeticsMenu.getMenus())
        );
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
                    this.tokenLoader.load();
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
                    this.tokenLoader.load();
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

    public Database getDatabase() {
        return database;
    }

}

