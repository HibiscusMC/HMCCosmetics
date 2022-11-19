package com.hibiscusmc.hmccosmetics.cosmetic.types;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.entities.MEGEntity;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.PlayerUtils;
import com.hibiscusmc.hmccosmetics.util.packets.PacketManager;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.generator.model.ModelBlueprint;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.spongepowered.configurate.ConfigurationNode;

public class CosmeticBalloonType extends Cosmetic {

    private String modelName;
    public CosmeticBalloonType(String id, ConfigurationNode config) {
        super(id, config);

        String modelId = config.node("model").getString();

        this.modelName = modelId;
    }

    @Override
    public void update(CosmeticUser user) {
        Player player = Bukkit.getPlayer(user.getUniqueId());

        user.getBalloonEntity().setLocation(player.getLocation().clone());

        user.getBalloonEntity().updateModel();

        //PacketManager.sendTeleportPacket(user.getBalloonEntity().getBalloonID(), actual, false, PlayerUtils.getNearbyPlayers(player));
        //PacketManager.sendLeashPacket(user.getBalloonEntity().getBalloonID(), player.getEntityId(), PlayerUtils.getNearbyPlayers(player));
    }

    public String getModelName() {
        return this.modelName;
    }
}
