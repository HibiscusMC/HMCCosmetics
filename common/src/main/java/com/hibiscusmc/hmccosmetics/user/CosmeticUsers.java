package com.hibiscusmc.hmccosmetics.user;

import com.hibiscusmc.hmccosmetics.util.HMCCServerUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CosmeticUsers {

    private static final ConcurrentHashMap<UUID, CosmeticUser> COSMETIC_USERS = new ConcurrentHashMap<>();

    // Users who currently have a balloon spawned. Balloons are opt-in and usually a minority, so the
    // per-tick BalloonSmoothingTask iterates this instead of walking every user. Kept in sync by
    // spawnBalloon/despawnBalloon, the only two places CosmeticUser assigns/clears its balloon manager.
    private static final Set<CosmeticUser> BALLOON_USERS = ConcurrentHashMap.newKeySet();

    private static CosmeticUserProvider PROVIDER = CosmeticUserProvider.Default.INSTANCE;

    /**
     * Adds a user to the Hashmap of stored CosmeticUsers. This will not override an entry if it already exists. If you need to override, delete then add.
     * @param user The user to add to the HashMap.
     */
    public static void addUser(@NotNull CosmeticUser user) {
        if (COSMETIC_USERS.containsKey(user.getUniqueId())) return; // do not add if already exists
        COSMETIC_USERS.put(user.getUniqueId(), user);
    }

    /**
     * Removes a CosmeticUser from the Hashmap
     * @param uuid The UUID to remove.
     */
    public static void removeUser(UUID uuid) {
        COSMETIC_USERS.remove(uuid);
    }

    /**
     * Removes a CosmeticUser from the hashmap by their CosmeticUser method
     * @param user The user to be removed (will get their UUID from this class)
     */
    public static void removeUser(@NotNull CosmeticUser user) {
        COSMETIC_USERS.remove(user.getUniqueId());
    }

    /**
     * This method allows you to get a CosmeticUser from their UUID. If you are using not internally, HIGHLY recommend to use the API implementation of this.
     * @param uuid The UUID of the user that you wish to lookup.
     * @return Returns the user if there is a valid user, returns null if not.
     */
    @Nullable
    public static CosmeticUser getUser(UUID uuid) {
        return COSMETIC_USERS.get(uuid);
    }

    /**
     * This method allows you to get a CosmeticUser from just using the player class. This just allows you to have a bit less boilerplate.
     * @param player The player to lookup (will take their UUID from the class)
     * @return Returns the user if there is a valid user, returns null if not.
     */
    @Nullable
    public static CosmeticUser getUser(@NotNull Player player) {
        return COSMETIC_USERS.get(player.getUniqueId());
    }

    /**
     * This method gets the CosmeticUser from an entity id (said entity must be a player). This is not ideal, as it requires the plugin to go through all entities, but it's a possibility.
     * @param entityId The entity ID in an integer.
     * @return The cosmetic user if there is an entity id associated with that.
     */
    @Nullable
    public static CosmeticUser getUser(int entityId) {
        for (World world : Bukkit.getWorlds()) {
            if (HMCCServerUtils.getEntity(entityId, world) instanceof Player player)
                return COSMETIC_USERS.get(player.getUniqueId());
        }
        return null;
    }

    /**
     * Register a custom {@link CosmeticUserProvider} to provide your own user implementation to
     * be used and queried.
     * @param provider the provider to register
     * @throws IllegalArgumentException if the provider is already registered by another plugin
     */
    public static void registerProvider(final CosmeticUserProvider provider) {
        if(PROVIDER != CosmeticUserProvider.Default.INSTANCE) {
            throw new IllegalArgumentException("CosmeticUserProvider already registered by %s, this conflicts with %s attempting to register their own.".formatted(
                PROVIDER.getProviderPlugin().getName(),
                provider.getProviderPlugin().getName()
            ));
        }

        PROVIDER = provider;
    }

    /**
     * Fetch the current {@link CosmeticUserProvider} being used.
     * @return the current {@link CosmeticUserProvider} being used
     */
    public static CosmeticUserProvider getProvider() {
        return PROVIDER;
    }

    /**
     * Gets all the values for CosmeticUsers to allow you to iterate over everyone.
     * @return CosmeticUsers in a set. This will never be null, but might be empty.
     */
    @NotNull
    public static Set<CosmeticUser> values() {
        // fix this later; this is a temporary fix. It was originally a set, now it's a collection
        return Set.copyOf(COSMETIC_USERS.values());
    }

    /**
     * A live, read-only view over the users. Unlike {@link #values()} this does not copy, so it is safe to
     * call from per-tick code. The backing map is a {@link ConcurrentHashMap}, so the returned view is safe
     * to iterate while users join and quit; it reflects those changes rather than snapshotting them.
     * @return the CosmeticUsers currently registered. Never null, might be empty.
     */
    @NotNull
    public static Collection<CosmeticUser> view() {
        return Collections.unmodifiableCollection(COSMETIC_USERS.values());
    }

    /**
     * Registers a user as having a spawned balloon. Called from {@link CosmeticUser#spawnBalloon}.
     */
    public static void addBalloonUser(@NotNull CosmeticUser user) {
        BALLOON_USERS.add(user);
    }

    /**
     * Unregisters a user's balloon. Called from {@link CosmeticUser#despawnBalloon}, which every
     * teardown path (unequip, hide, quit/destroy, respawn) funnels through.
     */
    public static void removeBalloonUser(@NotNull CosmeticUser user) {
        BALLOON_USERS.remove(user);
    }

    /**
     * A live, read-only view over the users who currently have a balloon. Safe to iterate from
     * per-tick code; backed by a {@link ConcurrentHashMap} key set, so it reflects joins/quits.
     * @return the CosmeticUsers with a spawned balloon. Never null, might be empty.
     */
    @NotNull
    public static Collection<CosmeticUser> balloonView() {
        return Collections.unmodifiableCollection(BALLOON_USERS);
    }
}
