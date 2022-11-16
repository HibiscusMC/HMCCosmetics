package com.hibiscusmc.hmccosmetics.cosmetic.types;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.generator.model.ModelBlueprint;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.spongepowered.configurate.ConfigurationNode;

public class CosmeticBalloonType extends Cosmetic {

    private ModelBlueprint model;
    public CosmeticBalloonType(String id, ConfigurationNode config) {
        super(id, config);

        String modelId = config.node("model").getString();

        HMCCosmeticsPlugin.getInstance().getLogger().info("Attempting Spawning");
        if (ModelEngineAPI.api.getModelRegistry().getBlueprint(modelId) == null) {
            HMCCosmeticsPlugin.getInstance().getLogger().warning("Invalid Model Engine Blueprint " + id);
            HMCCosmeticsPlugin.getInstance().getLogger().warning("Possible Blueprints" + ModelEngineAPI.api.getModelRegistry().getAllBlueprintId());
            return;
        }
        this.model = ModelEngineAPI.api.getModelRegistry().getBlueprint(modelId);
    }

    @Override
    public void update(CosmeticUser user) {
        Player player = Bukkit.getPlayer(user.getUniqueId());
        Location loc = player.getLocation().clone();

        loc.add(Settings.getBalloonOffset());
        user.getBalloonEntity().setLocation(loc);

        //user.getBackpackEntity().getBukkitLivingEntity().setRotation(loc.getYaw(), loc.getPitch());

    }

    public ModelBlueprint getModel() {
        return this.model;
    }
}
