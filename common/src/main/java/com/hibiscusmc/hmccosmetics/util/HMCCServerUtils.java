package com.hibiscusmc.hmccosmetics.util;

import com.owen1212055.particlehelper.api.particle.MultiParticle;
import com.owen1212055.particlehelper.api.particle.Particle;
import com.owen1212055.particlehelper.api.particle.types.*;
import com.owen1212055.particlehelper.api.particle.types.dust.transition.TransitionDustParticle;
import com.owen1212055.particlehelper.api.particle.types.note.MultiNoteParticle;
import me.lojosho.hibiscuscommons.nms.NMSHandlers;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.Arrays;

public class HMCCServerUtils {

    private static String COLOR_CHAR = "&";

    /**
     * Converts a bukkit gamemode into an integer for use in packets
     * @param gamemode Bukkit gamemode to convert.
     * @return int of the gamemode
     */
    public static int convertGamemode(final GameMode gamemode) {
        return switch (gamemode) {
            case SURVIVAL -> 0;
            case CREATIVE -> 1;
            case ADVENTURE -> 2;
            case SPECTATOR -> 3;
        };
    }

    public static org.bukkit.entity.Entity getEntity(int entityId) {
        return NMSHandlers.getHandler().getUtilHandler().getEntity(entityId);
    }

    /**
     * This takes in a string like #FFFFFF to convert it into a Bukkit color
     * @param colorStr
     * @return
     */
    public static Color hex2Rgb(String colorStr) {
        if (colorStr.startsWith("#")) return Color.fromRGB(Integer.valueOf(colorStr.substring(1), 16));
        if (colorStr.startsWith("0x")) return Color.fromRGB(Integer.valueOf(colorStr.substring(2), 16));
        if (colorStr.contains(",")) {
            String[] colorString = colorStr.replace(" ", "").split(",");
            for (String color : colorString) if (Integer.valueOf(color) == null) return Color.WHITE;
            Color.fromRGB(Integer.valueOf(colorString[0]), Integer.valueOf(colorString[1]), Integer.valueOf(colorString[2]));
        }

        return Color.WHITE;
    }

    /**
     * This takes in a string like 55,49,181 to convert it into a Bukkit Color
     * @param colorStr
     * @return
     */
    public static Color rgbToRgb(String colorStr) {
        if (colorStr.contains(",")) {
            String[] colors = colorStr.split(",", 3);
            if (colors.length == 3) {
                return Color.fromRGB(Integer.parseInt(colors[0]), Integer.parseInt(colors[1]), Integer.parseInt(colors[2]));
            }
        }

        return Color.WHITE;
    }

    // particle amount offsetxyz
    // Ex. HEART 10 0.1 0.1 0.1
    public static Particle addParticleValues(Particle particle, String[] split) {
        var counter = 1;
        if (particle instanceof MultiParticle multiParticle) {
            multiParticle.setCount(getBigInteger(split[counter]).intValue());
            counter++;
            multiParticle.setXOffset(getBigInteger(split[counter]).floatValue());
            counter++;
            multiParticle.setYOffset(getBigInteger(split[counter]).floatValue());
            counter++;
            multiParticle.setZOffset(getBigInteger(split[counter]).floatValue());
            counter++;
            if (multiParticle instanceof MultiNoteParticle multiNoteParticle) {
                multiNoteParticle.setColorMultplier(getBigInteger(split[counter]).intValue());
                counter++;
            }
        }
        if (particle instanceof ColorableParticle colorableParticle && colorFromString(split[counter]) != null) {
            colorableParticle.setColor(colorFromString(split[counter]));
            counter++;
        }
        if (particle instanceof TransitionDustParticle transitionDustParticle && colorFromString(split[counter]) != null) {
            transitionDustParticle.setFadeColor(colorFromString(split[counter]));
            counter++;
        }
        if (particle instanceof MaterialParticle materialParticle && Material.getMaterial(split[counter]) != null) {
            materialParticle.setMaterial(Material.getMaterial(split[counter]));
            counter++;
        }
        if (particle instanceof SpeedModifiableParticle speedModifiableParticle) {
            speedModifiableParticle.setSpeed(getBigInteger(split[counter]).floatValue());
            counter++;
        }
        if (particle instanceof DelayableParticle delayableParticle) {
            delayableParticle.setDelay(getBigInteger(split[counter]).intValue());
            counter++;
        }
        if (particle instanceof SizeableParticle sizeableParticle) {
            sizeableParticle.setSize(getBigInteger(split[counter]).floatValue());
            counter++;
        }
        if (particle instanceof RollableParticle rollableParticle) {
            rollableParticle.setRoll(getBigInteger(split[counter]).floatValue());
        }
        return particle;
    }

    private static BigInteger getBigInteger(String string) {
        try {
            return new BigInteger(string);
        } catch (Exception e) {
            return BigInteger.valueOf(1);
        }
    }

    /**
     * Parse a color from a string.
     * Formats: #RRGGBB; R,G,B
     *
     * @param color The string
     * @return The color, if the string can't be parsed, null is returned
     */
    public static Color colorFromString(@Nullable String color) {
        if (color == null) {
            return null;
        }
        try {
            var decodedColor = java.awt.Color.decode(color.startsWith("#") ? color : "#" + color);
            return Color.fromRGB(decodedColor.getRed(), decodedColor.getGreen(), decodedColor.getBlue());
        } catch (NumberFormatException invalidHex) {
            try {
                var rgbValues = Arrays.stream(color.split(",")).map(Integer::parseInt).toArray(Integer[]::new);
                return Color.fromRGB(rgbValues[0], rgbValues[1], rgbValues[2]);
            } catch (Exception invalidRgb) {
                return null;
            }
        }
    }

    public static int getNextYaw(final int current, final int rotationSpeed) {
        int nextYaw = current + rotationSpeed;
        if (nextYaw > 179) {
            nextYaw = (current + rotationSpeed) - 358;
            return nextYaw;
        }
        return nextYaw;
    }
}
