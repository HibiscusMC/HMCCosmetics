package com.hibiscusmc.hmccosmetics.entities;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.nms.NMSHandler;
import com.hibiscusmc.hmccosmetics.nms.NMSHandlers;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.nms.entity.fake.BoneRenderer;
import com.ticxo.modelengine.api.nms.entity.fake.FakeEntity;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.UUID;

// This includes the Pufferfish (The Pufferfish that's what the player leashes to) and the model (MEGEntity)
public class BalloonEntity {

    private final int balloonID;
    private final UUID uniqueID;
    private final Entity modelEntity;

    public BalloonEntity(Location location) {
        this.uniqueID = UUID.randomUUID();
        this.balloonID = NMSHandlers.getHandler().getNextEntityId();
        this.modelEntity = NMSHandlers.getHandler().getMEGEntity(location.add(Settings.getBalloonOffset()));
    }

    public void spawnModel(final String id, Color color) {
        HMCCosmeticsPlugin.getInstance().getLogger().info("Attempting Spawning for " + id);
        if (ModelEngineAPI.api.getModelRegistry().getBlueprint(id) == null) {
            HMCCosmeticsPlugin.getInstance().getLogger().warning("Invalid Model Engine Blueprint " + id);
            return;
        }
        ModeledEntity modeledEntity = ModelEngineAPI.getOrCreateModeledEntity(modelEntity);
        ActiveModel model = ModelEngineAPI.createActiveModel(ModelEngineAPI.getBlueprint(id));
        modeledEntity.addModel(model, false);
        if (color != null) {
            modeledEntity.getModels().forEach((d, singleModel) -> {
                singleModel.getRendererHandler().setColor(color);
            });
        }
    }

    public void remove() {
        final ModeledEntity entity = ModelEngineAPI.api.getModeledEntity(modelEntity.getUniqueId());

        if (entity == null) return;

        for (final Player player : entity.getRangeManager().getPlayerInRange()) {
            entity.hideFromPlayer(player);
        }

        //ModelEngineAPI.removeModeledEntity(megEntity.getUniqueId());
        entity.destroy();
        modelEntity.remove();
    }

    public void addPlayerToModel(final Player player, final String id) {
        addPlayerToModel(player, id, null);
    }

    public void addPlayerToModel(final Player player, final String id, Color color) {
        final ModeledEntity model = ModelEngineAPI.api.getModeledEntity(modelEntity.getUniqueId());
        if (model == null) {
            spawnModel(id, color);
            return;
        }
        if (model.getRangeManager().getPlayerInRange().contains(player)) return;
        model.showToPlayer(player);
    }
    public void removePlayerFromModel(final Player player) {
        final ModeledEntity model = ModelEngineAPI.api.getModeledEntity(modelEntity.getUniqueId());

        if (model == null) return;

        model.hideFromPlayer(player);
    }

    public Entity getModelEntity() {
        return this.modelEntity;
    }


    public int getPufferfishBalloonId() {
        return balloonID;
    }
    public UUID getPufferfishBalloonUniqueId() {
        return uniqueID;
    }

    public UUID getModelUnqiueId() {
        return modelEntity.getUniqueId();
    }

    public int getModelId() {
        return modelEntity.getEntityId();
    }

    public Location getLocation() {
        return this.modelEntity.getLocation();
    }

    public void setLocation(Location location) {
        //this.megEntity.teleportTo(location.getX(), location.getY(), location.getZ());
        this.modelEntity.teleport(location);
    }

    public void setVelocity(Vector vector) {
        this.modelEntity.setVelocity(vector);
    }
}
