package io.github.fisher2911.hmccosmetics.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.user.UserManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class WardrobeListener implements Listener {

    private final HMCCosmetics plugin;
    private final UserManager userManager;

    public WardrobeListener(final HMCCosmetics plugin) {
        this.plugin = plugin;
        this.userManager = this.plugin.getUserManager();

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(HMCCosmetics.getPlugin(HMCCosmetics.class), ListenerPriority.NORMAL, PacketType.Play.Client.ARM_ANIMATION) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (!(event.getPlayer() instanceof Player)) return;
                Player player = event.getPlayer();
                WardrobeListener.this.userManager.get(player.getUniqueId()).ifPresent(user -> {
                            if (!user.getWardrobe().isActive()) return;
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                WardrobeListener.this.plugin.getCosmeticsMenu().openDefault(player);
                            });
                        }
                );
            }
        });

        // TODO: REDO this
        /*
        PacketEvents.getAPI().getEventManager().registerListener(
                new PacketListenerAbstract() {
                    @Override
                    public void onPacketReceive(PacketReceiveEvent event) {
                        if (event.getPacketType() != PacketType.Play.Client.ANIMATION) return;
                        if (!(event.getPlayer() instanceof final Player player)) return;
                        WardrobeListener.this.userManager.get(player.getUniqueId()).ifPresent(user -> {
                                    if (!user.getWardrobe().isActive()) return;
                                    Bukkit.getScheduler().runTask(plugin, () -> {
                                        WardrobeListener.this.plugin.getCosmeticsMenu().openDefault(player);
                                    });
                                }
                        );
                    }
                });
                
         */
    }

    @EventHandler
    public void onDamage(final EntityDamageEvent event) {
        if (!(event.getEntity() instanceof final Player player)) return;
        this.userManager.get(player.getUniqueId()).ifPresent(user -> {
            if (user.isWardrobeActive()) event.setCancelled(true);
        });
    }
}
