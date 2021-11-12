package io.github.fisher2911.hmccosmetics;

import com.comphenix.protocol.ProtocolLib;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import io.github.fisher2911.hmccosmetics.command.CosmeticsCommand;
import io.github.fisher2911.hmccosmetics.gui.CosmeticsMenu;
import io.github.fisher2911.hmccosmetics.listener.ClickListener;
import io.github.fisher2911.hmccosmetics.listener.JoinListener;
import io.github.fisher2911.hmccosmetics.listener.TeleportListener;
import io.github.fisher2911.hmccosmetics.message.MessageHandler;
import io.github.fisher2911.hmccosmetics.message.Messages;
import io.github.fisher2911.hmccosmetics.user.UserManager;
import me.mattstudios.mf.base.CommandManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class HMCCosmetics extends JavaPlugin {

    private ProtocolManager protocolManager;
    private UserManager userManager;
    private MessageHandler messageHandler;
    private CosmeticsMenu cosmeticsMenu;
    private CommandManager commandManager;

    @Override
    public void onEnable() {
        protocolManager = ProtocolLibrary.getProtocolManager();
        this.messageHandler = new MessageHandler(this);
        this.userManager = new UserManager(this);
        this.cosmeticsMenu = new CosmeticsMenu(this);
        this.messageHandler.load();
        this.cosmeticsMenu.load();
        this.registerCommands();
        this.registerListeners();

        this.userManager.startTeleportTask();
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
                        new TeleportListener(this)).
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
        this.commandManager.register(new CosmeticsCommand(this));
    }

    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public CosmeticsMenu getCosmeticsMenu() {
        return cosmeticsMenu;
    }

    public ProtocolManager getProtocolManager() {
        return protocolManager;
    }
}

