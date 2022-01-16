package io.github.fisher2911.hmccosmetics;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

import io.github.fisher2911.hmccosmetics.command.CosmeticsCommand;
import io.github.fisher2911.hmccosmetics.cosmetic.CosmeticManager;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.gui.CosmeticsMenu;
import io.github.fisher2911.hmccosmetics.listener.ClickListener;
import io.github.fisher2911.hmccosmetics.listener.HatRemoveFixListener;
import io.github.fisher2911.hmccosmetics.listener.JoinListener;
import io.github.fisher2911.hmccosmetics.listener.RespawnListener;
import io.github.fisher2911.hmccosmetics.listener.TeleportListener;
import io.github.fisher2911.hmccosmetics.message.MessageHandler;
import io.github.fisher2911.hmccosmetics.message.Messages;
import io.github.fisher2911.hmccosmetics.user.UserManager;
import me.mattstudios.mf.base.CommandManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
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
    private boolean papiEnabled;

    @Override
    public void onEnable() {
        final int pluginId = 13873;
        final Metrics metrics = new Metrics(this, pluginId);

        protocolManager = ProtocolLibrary.getProtocolManager();
        this.messageHandler = new MessageHandler(this);
        this.userManager = new UserManager(this);
        this.cosmeticManager = new CosmeticManager(new HashMap<>());
        this.cosmeticsMenu = new CosmeticsMenu(this);
        this.messageHandler.load();
        this.cosmeticsMenu.load();
        this.registerCommands();
        this.registerListeners();

        this.userManager.startTeleportTask();

        this.papiEnabled = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    @Override
    public void onDisable() {
        this.messageHandler.close();
        this.userManager.cancelTeleportTask();
        this.userManager.removeAll();
    }

    private void registerListeners() {
        List.of(new JoinListener(this),
                        new ClickListener(this),
                        new TeleportListener(this),
                        new RespawnListener(this),
                        new HatRemoveFixListener(this)).
                forEach(listener ->
                        this.getServer().getPluginManager().registerEvents(listener, this)
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

    public boolean isPapiEnabled() {
        return papiEnabled;
    }
}

