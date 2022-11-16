package com.hibiscusmc.hmccosmetics.cosmetic.types;

import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.PlayerUtils;
import com.ticxo.modelengine.api.generator.model.ModelBlueprint;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.List;

public class CosmeticBalloonType extends Cosmetic {

    private ModelBlueprint model;
    public CosmeticBalloonType(String id, ConfigurationNode config) {
        super(id, config);
    }

    @Override
    public void update(CosmeticUser user) {
        Player player = Bukkit.getPlayer(user.getUniqueId());
        List<Player> sendTo = PlayerUtils.getNearbyPlayers(player.getLocation());
        Location loc = player.getLocation().clone();

        // TODO: Offsets
        loc.add(Settings.getBalloonOffset());
        user.getBalloonEntity().setLocation(loc);

        //user.getBackpackEntity().getBukkitLivingEntity().setRotation(loc.getYaw(), loc.getPitch());

    }
}
