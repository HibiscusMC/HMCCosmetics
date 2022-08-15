package io.github.fisher2911.hmccosmetics.config;

import com.comphenix.protocol.wrappers.EnumWrappers;
import net.minecraft.server.v1_16_R3.MinecraftKey;
import org.bukkit.entity.Player;

public class SoundData {

    private final String name;
    private final EnumWrappers.SoundCategory soundCategory;
    private final float volume;
    private final float pitch;

    public SoundData(final String name, final EnumWrappers.SoundCategory soundCategory, final float volume, final float pitch) {
        this.name = name;
        this.soundCategory = soundCategory;
        this.volume = volume;
        this.pitch = pitch;
    }

    public String getName() {
        return name;
    }

    public float getVolume() {
        return volume;
    }

    public float getPitch() {
        return pitch;
    }

    public EnumWrappers.SoundCategory getSoundCategory() {
        return soundCategory;
    }

    public void play(final Player player) {
        // todo - once packetevents updates
//        final PacketContainer soundPacket = PacketManager.getSoundPacket(
//                player,
//                player.getLocation(),
//                this.getKey(this.name),
//                this.volume,
//                this.pitch,
//                this.soundCategory
//        );
//
//        PacketManager.sendPacket(player, soundPacket);
    }

    private MinecraftKey getKey(final String string) {
        if (!string.contains(":")) {
            return new MinecraftKey(string);
        }

        final String[] parts = string.split(":");

        return new MinecraftKey(parts[0], parts[1]);
    }
}
