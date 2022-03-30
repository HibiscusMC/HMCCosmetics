package io.github.fisher2911.hmccosmetics.config;

import com.github.retrooper.packetevents.protocol.sound.SoundCategory;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class SoundSerializer implements TypeSerializer<SoundData>  {

    public static final SoundSerializer INSTANCE = new SoundSerializer();

    private SoundSerializer() {}

    private static final String SOUND_NAME = "name";
    private static final String SOUND_VOLUME = "volume";
    private static final String SOUND_PITCH = "pitch";
    private static final String SOUND_CATEGORY = "category";

    @Override
    public SoundData deserialize(final Type type, final ConfigurationNode node) throws SerializationException {
        final ConfigurationNode soundNameNode = node.node(SOUND_NAME);
        final ConfigurationNode volumeNode = node.node(SOUND_VOLUME);
        final ConfigurationNode pitchNode = node.node(SOUND_PITCH);
        final ConfigurationNode categoryNode = node.node(SOUND_CATEGORY);

        final SoundData soundData;

        final String soundName = soundNameNode.getString();
        final String category = categoryNode.getString();
        final int volume = volumeNode.getInt();
        final int pitch = pitchNode.getInt();
        if (soundName == null || category == null) {
            soundData = null;
        } else {
            soundData = new SoundData(
                    soundName,
                    SoundCategory.valueOf(category),
                    volume,
                    pitch
            );
        }

        return soundData;
    }

    @Override
    public void serialize(final Type type, @Nullable final SoundData obj, final ConfigurationNode node) throws SerializationException {

    }
}
