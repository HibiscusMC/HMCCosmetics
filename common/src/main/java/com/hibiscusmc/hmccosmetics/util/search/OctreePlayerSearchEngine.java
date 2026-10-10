package com.hibiscusmc.hmccosmetics.util.search;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * Kept as a configuration-compatible alias. The former mutable octree was
 * shared by independent region threads and could corrupt itself during moves.
 */
public final class OctreePlayerSearchEngine extends BukkitPlayerSearchEngine {
    public OctreePlayerSearchEngine(@NotNull HMCCosmeticsPlugin plugin) {
        super(plugin);
    }
}
