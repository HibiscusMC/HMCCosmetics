package com.hibiscusmc.hmccosmetics.api;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticProvider;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics;
import com.hibiscusmc.hmccosmetics.gui.Menu;
import com.hibiscusmc.hmccosmetics.gui.Menus;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUserProvider;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import me.lojosho.hibiscuscommons.nms.NMSHandlers;
import me.lojosho.shaded.configurate.ConfigurationNode;
import org.bukkit.Color;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * The main API class for HMCCosmetics. This class provides methods to interact with the plugin.
 */
public final class HMCCosmeticsAPI {
    private HMCCosmeticsAPI() {
        throw new UnsupportedOperationException("This class cannot be instantiated.");
    }

    /**
     * Retrieves a {@link Cosmetic} associated with the specified id.
     * <p>
     * This method attempts to fetch a {@link Cosmetic} using the given id. If no {@link Cosmetic} exists
     * with the specified id, it will return {@code null}.
     * </p>
     * @param id the id of the {@link Cosmetic} to retrieve
     * @return the {@link Cosmetic} if it exists, or {@code null} if no cosmetic is associated with the given id
     */
    public static @Nullable Cosmetic getCosmetic(@NotNull String id) {
        return Cosmetics.getCosmetic(id);
    }

    /**
     * Retrieves the {@link CosmeticUser} associated with the specified player's {@link UUID}.
     * <p>
     * This method attempts to fetch a {@link CosmeticUser} for an online player. If the player is offline,
     * or if no {@link CosmeticUser} is currently associated with them, it will return {@code null}.
     * </p>
     * Note that a player may be online but not have a {@link CosmeticUser} attached due to:
     * <ul>
     *   <li>A delay specified in the configuration</li>
     *   <li>A recent server reload (e.g., via the {@code /reload} command)</li>
     * </ul>
     * Always perform a {@code null} check before using the returned object to ensure safe operation.
     *
     * @param uuid the {@link UUID} of the player whose {@link CosmeticUser} is being retrieved
     * @return the {@link CosmeticUser} if it exists, or {@code null} if the player is offline or unassociated
     */
    public static @Nullable CosmeticUser getUser(@NotNull UUID uuid) {
        return CosmeticUsers.getUser(uuid);
    }

    /**
     * Retrieves a {@link Menu} associated with the specified id, or {@code null} if no menu exists with the given id.
     *
     * @param id the id of the menu to retrieve
     * @return the {@link Menu} if it exists, or {@code null} if no menu is associated with the given id
     */
    public static @Nullable Menu getMenu(@NotNull String id) {
        return Menus.getMenu(id);
    }

    /**
     * Equips a {@link Cosmetic} to a player. To retrieve the necessary {@code CosmeticUser} and {@code Cosmetic}, use the {@link #getUser}
     * and {@link #getCosmetic} methods respectively.
     *
     * @param user the {@link CosmeticUser} to equip the cosmetic to
     * @param cosmetic the {@link Cosmetic} to equip
     */
    public static void equipCosmetic(@NotNull CosmeticUser user, @NotNull Cosmetic cosmetic) {
        equipCosmetic(user, cosmetic, null);
    }

    /**
     * Equips a {@link Cosmetic} to a player with an optional color customization. To retrieve the necessary
     * {@code CosmeticUser} and {@code Cosmetic}, use the {@link #getUser} and {@link #getCosmetic} methods
     * respectively.
     *
     * @param user the {@link CosmeticUser} to equip the cosmetic to
     * @param cosmetic the {@link Cosmetic} to equip
     * @param color the color to apply to the cosmetic, or {@code null} if the cosmetic does not support color
     *              customization
     */
    public static void equipCosmetic(@NotNull CosmeticUser user, @NotNull Cosmetic cosmetic, @Nullable Color color) {
        user.addPlayerCosmetic(cosmetic, color);
    }

    /**
     * Removes a cosmetic from a specified slot for the given user.
     *
     * @param user the {@link CosmeticUser} from whom the cosmetic will be removed
     * @param slot the {@link CosmeticSlot} from which the cosmetic will be removed
     */
    public static void unequipCosmetic(@NotNull CosmeticUser user, @NotNull CosmeticSlot slot) {
        user.removeCosmeticSlot(slot);
    }

    /**
     * Retrieves a list of all cosmetics currently registered with HMCC.
     *
     * @return an {@code immutable} list containing all registered {@link Cosmetic} object
     */
    public static @NotNull List<Cosmetic> getAllCosmetics() {
        return List.copyOf(Cosmetics.values());
    }

    /**
     * Retrieves a list of all cosmetic users currently registered with HMCC.
     *
     * @return an immutable list containing all registered {@link CosmeticUser} objects
     */
    public static @NotNull List<CosmeticUser> getAllCosmeticUsers() {
        return List.copyOf(CosmeticUsers.values());
    }

    /**
     * Retrieves a map of all cosmetic slots currently registered with HMCC.
     *
     * @return an immutable {@link Map} containing all registered cosmetic slots
     */
    public static @NotNull Map<String, CosmeticSlot> getAllCosmeticSlots() {
        return Map.copyOf(CosmeticSlot.values());
    }

    /**
     * Registers a new cosmetic slot with the specified id. If a slot with the same id already exists,
     * the existing slot will be returned.
     *
     * <p>
     *     The provided id will automatically be converted to uppercase when registering the slot.
     * </p>
     *
     * @param id the id for the cosmetic slot
     * @return the {@link CosmeticSlot} associated with the given id
     * @apiNote this should be done in your {@link JavaPlugin#onLoad()} or it may error.
     */
    public static @NotNull CosmeticSlot registerCosmeticSlot(@NotNull String id) {
        return CosmeticSlot.register(id);
    }

    /**
     * Registers a new cosmetic user provider to use for constructing {@link CosmeticUser} instances.
     *
     * @param provider the provider to register
     * @throws IllegalArgumentException if another plugin has already registered a provider
     * @apiNote this should be done in your {@link JavaPlugin#onLoad()} or it may error.
     */
    public static void registerCosmeticUserProvider(@NotNull CosmeticUserProvider provider) {
        CosmeticUsers.registerProvider(provider);
    }

    /**
     * Retrieves the current {@link CosmeticUserProvider} that is in use.
     *
     * @return the current {@link CosmeticUserProvider}
     */
    public static @NotNull CosmeticUserProvider getCosmeticUserProvider() {
        return CosmeticUsers.getProvider();
    }

    /**
     * Registers a new cosmetic user provider to use for constructing {@link Cosmetic} instances.
     *
     * @param provider the provider to register
     * @throws IllegalArgumentException if another plugin has already registered a provider
     * @apiNote this should be done in your {@link JavaPlugin#onLoad()} or it may error.
     */
    public static void registerCosmeticProvider(@NotNull CosmeticProvider provider) {
        Cosmetics.registerProvider(provider);
    }

    /**
     * Retrieves the current {@link CosmeticProvider} that is in use.
     *
     * @return the current {@link CosmeticProvider}
     */
    public static @NotNull CosmeticProvider getCosmeticProvider() {
        return Cosmetics.getProvider();
    }

    /**
     * Retrieves the NMS version of the server as recognized by HMCCosmetics.
     *
     * <p>This value will be {@code null} until the HMCC setup has been completed. Ensure setup is finished
     * before attempting to access this version.</p>
     *
     * @return the NMS version of the server in string format, or {@code null} if setup is not complete.
     */
    public static @Nullable String getNMSVersion() {
        return NMSHandlers.getVersion();
    }

    /**
     * Retrieves the version of HMCCosmetics currently in use.
     *
     * @return the HMCCosmetics version in string format
     */
    public static @NotNull String getHMCCVersion() {
        return HMCCosmeticsPlugin.getInstance().getDescription().getVersion();
    }
}
