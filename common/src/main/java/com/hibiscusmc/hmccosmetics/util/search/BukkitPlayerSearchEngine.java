package com.hibiscusmc.hmccosmetics.util.search;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.util.Octree;
import com.hibiscusmc.hmccosmetics.util.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class BukkitPlayerSearchEngine extends PlayerSearchEngine {

    public BukkitPlayerSearchEngine(@NotNull HMCCosmeticsPlugin plugin) {
        super(plugin);
    }

    @Override
    public List<Player> getPlayersInRange(Location location, double range) {
        if (SchedulerUtil.isFolia()) {
            // 在Folia环境中，我们需要在主线程中获取附近的玩家
            try {
                CompletableFuture<List<Player>> future = new CompletableFuture<>();
                SchedulerUtil.runTask(getInstance(), () -> {
                    try {
                        future.complete(location.getNearbyPlayers(range).stream().toList());
                    } catch (Exception e) {
                        future.complete(Collections.emptyList());
                    }
                });
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                return Collections.emptyList();
            }
        } else {
            // 在非Folia环境中，可以直接获取附近的玩家
            return location.getNearbyPlayers(range).stream().toList();
        }
    }
}