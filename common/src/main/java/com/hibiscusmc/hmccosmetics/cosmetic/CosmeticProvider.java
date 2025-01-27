package com.hibiscusmc.hmccosmetics.cosmetic;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.cosmetic.types.*;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import lombok.extern.slf4j.Slf4j;
import me.lojosho.shaded.configurate.ConfigurationNode;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiFunction;

/**
 * Allow custom implementations of a {@link Cosmetic}.
 */
@Slf4j
public abstract class CosmeticProvider {
    protected static final Map<CosmeticSlot, BiFunction<String, ConfigurationNode, Cosmetic>> MAPPINGS = Map.ofEntries(
        Map.entry(CosmeticSlot.HELMET, CosmeticArmorType::new),
        Map.entry(CosmeticSlot.CHESTPLATE, CosmeticArmorType::new),
        Map.entry(CosmeticSlot.LEGGINGS, CosmeticArmorType::new),
        Map.entry(CosmeticSlot.BOOTS, CosmeticArmorType::new),
        Map.entry(CosmeticSlot.OFFHAND, CosmeticArmorType::new),

        Map.entry(CosmeticSlot.MAINHAND, CosmeticMainhandType::new),

        Map.entry(CosmeticSlot.BACKPACK, CosmeticBackpackType::new),

        Map.entry(CosmeticSlot.BALLOON, CosmeticBalloonType::new),

        Map.entry(CosmeticSlot.EMOTE, CosmeticEmoteType::new)
    );

    private static final String EXCEPTION_MESSAGE = "Unknown slot %s provided for mapping, if you registered your own CosmeticSlot please ensure that you've also registered a custom CosmeticProvider! Or if you have already registered a custom CosmeticProvider ensure it is registered in your plugins `onLoad` method instead of `onEnable`!";

    /**
     * Construct the {@link Cosmetic}.
     * @param id the cosmetic id
     * @param config the configuration node of the cosmetic
     * @param slot the occupying slot of the cosmetic
     * @return the {@link Cosmetic}
     * @throws IllegalArgumentException if the provided cosmetic could not be mapped
     */
    public @NotNull Cosmetic createCosmetic(String id, ConfigurationNode config, CosmeticSlot slot) throws IllegalArgumentException {
        final var mapper = MAPPINGS.get(slot);
        if(mapper == null) {
            throw new IllegalArgumentException(
                EXCEPTION_MESSAGE.formatted(slot)
            );
        }

        return mapper.apply(id, config);
    }

    /**
     * Represents the plugin that is providing this {@link CosmeticProvider}
     * @return the plugin
     */
    public abstract Plugin getProviderPlugin();

    /**
     * Default Implementation.
     */
    static class Default extends CosmeticProvider {
        public static final CosmeticProvider INSTANCE = new Default();

        @Override
        public Plugin getProviderPlugin() {
            return HMCCosmeticsPlugin.getInstance();
        }
    }
}