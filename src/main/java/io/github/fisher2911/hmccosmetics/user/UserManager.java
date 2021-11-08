package io.github.fisher2911.hmccosmetics.user;

import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.inventory.PlayerArmor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class UserManager {

    private final HMCCosmetics plugin;

    private final Map<UUID, User> userMap = new HashMap<>();

    private BukkitTask teleportTask;

    public UserManager(final HMCCosmetics plugin) {
        this.plugin = plugin;
    }

    public void add(final Player player) {
        final UUID uuid = player.getUniqueId();
        this.userMap.put(uuid, new User(uuid, PlayerArmor.empty()));
    }

    public Optional<User> get(final UUID uuid) {
        return Optional.ofNullable(this.userMap.get(uuid));
    }

    public void remove(final UUID uuid) {
        this.get(uuid).ifPresent(User::detach);
        this.userMap.remove(uuid);
    }

    public void startTeleportTask() {
        this.teleportTask = Bukkit.getScheduler().runTaskTimer(
                this.plugin,
                () -> this.userMap.values().forEach(
                        User::updateArmorStand
                ),
                1,
                1
        );
    }

    public void removeAll() {
        for (final var user : this.userMap.values()) {
            user.detach();
        }

        this.userMap.clear();
    }

    public void cancelTeleportTask() {
        this.teleportTask.cancel();
    }
}
