package com.hibiscusmc.hmccosmetics.gui.action.actions;

import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticHolder;
import com.hibiscusmc.hmccosmetics.gui.action.Action;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.HMCCServerUtils;
import com.hibiscusmc.hmccosmetics.util.packets.HMCCPacketManager;
import com.owen1212055.particlehelper.api.particle.types.BlockDataParticle;
import com.owen1212055.particlehelper.api.particle.types.DestinationParticle;
import com.owen1212055.particlehelper.api.particle.types.velocity.VelocityParticle;
import com.owen1212055.particlehelper.api.particle.types.vibration.VibrationParticle;
import com.owen1212055.particlehelper.api.type.ParticleType;
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
    @SuppressWarnings("ConstantConditions")
    public void run(Player viewer, CosmeticHolder cosmeticHolder, String raw) {
        String[] rawString = raw.split(" ");
        ParticleType<?, ?> particleType = Particles.fromKey(NamespacedKey.minecraft(rawString[0].toLowerCase()));
        if (particleType == null) {
            MessagesUtil.sendDebugMessages("The particle " + rawString[0] + " does not exist!");
            return;
        }

        // particleType.multi() should never be null, but particleType can be.
        boolean multi = particleType.multi() != null;

        var particle = multi ? particleType.multi() : particleType.single();
        if (particle instanceof DestinationParticle || particle instanceof BlockDataParticle
                || particle instanceof VibrationParticle || particle instanceof VelocityParticle) {
            MessagesUtil.sendDebugMessages("The particle " + rawString[0] + " is not supported by this action!");
            return;
        }

        particle = HMCCServerUtils.addParticleValues(particle, rawString);
        Location location = viewer.getLocation();
        for (Player player : HMCCPacketManager.getViewers(location)) {
            particle.compile().send(player, location);
        }
    }

    @Override
    public void run(CosmeticUser user, @NotNull String raw) {
        run(user.getPlayer(), user, raw);
    }
}
