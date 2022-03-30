package io.github.fisher2911.hmccosmetics.config;

import io.github.fisher2911.hmccosmetics.gui.CosmeticGui;
import io.github.fisher2911.hmccosmetics.gui.TokenGui;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class TokenGuiSerializer implements TypeSerializer<TokenGui> {

    public static final TokenGuiSerializer INSTANCE = new TokenGuiSerializer();
    private static final String TOKEN_SLOT = "token-slot";
    private static final String COSMETIC_SLOT = "cosmetic-slot";

    private TokenGuiSerializer() {}

    @Override
    public TokenGui deserialize(final Type type, final ConfigurationNode source) throws SerializationException {
        final CosmeticGui cosmeticGui = GuiSerializer.INSTANCE.deserialize(CosmeticGui.class, source);
        final ConfigurationNode tokenSlotNode = source.node(TOKEN_SLOT);
        final ConfigurationNode cosmeticSlotNode = source.node(COSMETIC_SLOT);

        return new TokenGui(
                cosmeticGui,
                tokenSlotNode.getInt(),
                cosmeticSlotNode.getInt()
        );
    }

    @Override
    public void serialize(final Type type, @Nullable final TokenGui obj, final ConfigurationNode node) throws SerializationException {

    }
}
