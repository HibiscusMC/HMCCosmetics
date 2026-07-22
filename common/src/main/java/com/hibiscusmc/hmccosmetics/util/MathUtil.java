package com.hibiscusmc.hmccosmetics.util;

import org.bukkit.Location;

public final class MathUtil {

    private MathUtil() {}

    public static double lerp(double start, double end, double t) {
        return start + (end - start) * Math.clamp(t, 0.0, 1.0);
    }

    public static Location lerpLocation(Location current, Location target, double horizontalT, double verticalT) {
        Location result = target.clone();
        result.setX(lerp(current.getX(), target.getX(), horizontalT));
        result.setY(lerp(current.getY(), target.getY(), verticalT));
        result.setZ(lerp(current.getZ(), target.getZ(), horizontalT));
        return result;
    }

    public static double lerpAngle(double start, double end, double t) {
        double diff = ((end - start + 180) % 360 + 360) % 360 - 180;
        return start + diff * Math.clamp(t, 0.0, 1.0);
    }
}
