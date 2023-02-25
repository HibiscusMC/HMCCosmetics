package com.hibiscusmc.hmccosmetics.gui.action.actions;

import com.hibiscusmc.hmccosmetics.gui.action.Action;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.ServerUtils;
import com.hibiscusmc.hmccosmetics.util.packets.PacketManager;
import com.owen1212055.particlehelper.api.particle.types.BlockDataParticle;
import com.owen1212055.particlehelper.api.particle.types.DestinationParticle;
import com.owen1212055.particlehelper.api.particle.types.velocity.VelocityParticle;
import com.owen1212055.particlehelper.api.particle.types.vibration.VibrationParticle;
import com.owen1212055.particlehelper.api.type.Particles;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ActionParticle extends Action {

    public ActionParticle() {
        super("particle");
    }

    @Override
    public void run(CosmeticUser user, @NotNull String raw) {
        String[] rawString = raw.split(" ");
        var particleType = Particles.fromKey(NamespacedKey.minecraft(rawString[0].toLowerCase()));
        if (particleType == null) {
            MessagesUtil.sendDebugMessages("The particle " + rawString[0] + " does not exist!");
            return;
        }
        boolean multi = false;
        if (particleType.multi() != null) {
            multi = true; // Should work?
        }

        var particle = multi ? particleType.multi() : particleType.single();
        if (particle instanceof DestinationParticle || particle instanceof BlockDataParticle
                || particle instanceof VibrationParticle || particle instanceof VelocityParticle) {
            MessagesUtil.sendDebugMessages("The particle " + rawString[0] + " is not supported by this action!");
            return;
        }
        particle = ServerUtils.addParticleValues(particle, rawString);
        Location location = user.getPlayer().getLocation();
        for (Player player : PacketManager.getViewers(location)) {
            particle.compile().send(player, location);
        }
    }
}
