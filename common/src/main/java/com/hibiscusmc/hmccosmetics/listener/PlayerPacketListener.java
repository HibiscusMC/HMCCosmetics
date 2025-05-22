package com.hibiscusmc.hmccosmetics.listener;

import com.hibiscusmc.hmccosmetics.nms.CosmeticChannelHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import net.minecraft.network.Connection;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerPacketListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Channel channel = ((CraftPlayer) event.getPlayer()).getHandle().connection.connection.channel;
        ChannelPipeline pipeline = channel.pipeline();

        CosmeticChannelHandler channelHandler = new CosmeticChannelHandler(player);
        for (String key : pipeline.toMap().keySet()) {
            if (!(pipeline.get(key) instanceof Connection)) continue;
            pipeline.addBefore(key, "hmccosmetics_channel_handler", channelHandler);
        }
    }
}
