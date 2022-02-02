package io.github.fisher2911.hmccosmetics.message;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.transformation.TransformationRegistry;
import net.kyori.adventure.text.minimessage.transformation.TransformationType;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class Adventure {

    public static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder()
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    public static final MiniMessage MINI_MESSAGE = MiniMessage.builder()
            .transformations(TransformationRegistry.
                    builder().
                    add(TransformationType.CLICK_EVENT,
                            TransformationType.DECORATION,
                            TransformationType.COLOR
                    ).build())
            .build();
}
