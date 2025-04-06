package com.hibiscusmc.hmccosmetics.gui.action.actions;

import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticHolder;
import com.hibiscusmc.hmccosmetics.gui.action.Action;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class ActionSound extends Action {
    // [SOUND] minecraft:test 1 1

    public ActionSound() {
        super("sound");
    }

    @Override
    public void run(Player viewer, CosmeticHolder cosmeticHolder, String raw) {
        String[] processedString = raw.split(" ");

        String soundName = processedString[0];
        float volume = 1;
        float pitch = 1;

        if (processedString.length > 2) {
            volume = Float.parseFloat(processedString[1]);
            pitch = Float.parseFloat(processedString[2]);
        }

        MessagesUtil.sendDebugMessages("Attempting to play " + soundName, Level.INFO);

        viewer.playSound(viewer.getLocation(), soundName, volume, pitch);
    }

    @Override
    public void run(@NotNull CosmeticUser user, @NotNull String raw) {
        run(user.getPlayer(), user, raw);
    }
}
