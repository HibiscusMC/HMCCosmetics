package com.hibiscusmc.hmccosmetics.config.serializer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class LocationSerializer implements TypeSerializer<Location> {

    public static final LocationSerializer INSTANCE = new LocationSerializer();

    private static final String WORLD = "world";
    private static final String X = "x";
    private static final String Y = "y";
    private static final String Z = "z";
    private static final String PITCH = "pitch";
    private static final String YAW = "yaw";


    private LocationSerializer() {}

    @Override
    @Nullable
    public Location deserialize(final Type type, final ConfigurationNode source) throws SerializationException {
        final World world = Bukkit.getWorld(source.node(WORLD).getString());
        if (world == null) return null;
        return new Location(
                world,
                source.node(X).getDouble(),
                source.node(Y).getDouble(),
                source.node(Z).getDouble(),
                source.node(YAW).getFloat(),
                source.node(PITCH).getFloat()
        );
    }

    @Override
    public void serialize(final Type type, @Nullable final Location loc, final ConfigurationNode source) throws SerializationException {
        // Empty
    }
}
