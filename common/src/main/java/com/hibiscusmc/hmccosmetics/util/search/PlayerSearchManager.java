package com.hibiscusmc.hmccosmetics.util.search;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.util.SchedulerUtil;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PlayerSearchManager {

    @Getter
    private final HMCCosmeticsPlugin plugin;
    @Getter
    private final PlayerSearchEngine engine;

    public PlayerSearchManager(@NotNull SearchEngine engine, @NotNull HMCCosmeticsPlugin plugin) {
        this.plugin = plugin;

        // 在Folia环境中，优先使用Octree搜索引擎，因为它不需要在主线程中执行操作
        if (SchedulerUtil.isFolia()) {
            this.engine = new OctreePlayerSearchEngine(plugin);
        } else {
            // 在非Folia环境中，根据配置选择搜索引擎
            switch (engine) {
                case OCTREE -> this.engine = new OctreePlayerSearchEngine(plugin);
                default -> this.engine = new BukkitPlayerSearchEngine(plugin);
            }
        }
    }

    public @NotNull List<Player> getPlayersInRange(@NotNull Location location, double range) {
        return engine.getPlayersInRange(location, range);
    }

    public enum SearchEngine {
        BUKKIT,
        OCTREE
    }
}