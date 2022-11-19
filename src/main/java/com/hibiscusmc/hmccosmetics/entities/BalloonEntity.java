package com.hibiscusmc.hmccosmetics.entities;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.UUID;

public class BalloonEntity {

    private final int balloonID;
    private final MEGEntity megEntity;

    public BalloonEntity(int balloonID, Location location) {
        this.balloonID = balloonID;
        this.megEntity = new MEGEntity(UUID.randomUUID(), balloonID, new Vector(0, 0, 0), location.add(Settings.getBalloonOffset()), false);
    }

    public void updateModel() {
        final ModeledEntity model = ModelEngineAPI.api.getModeledEntity(megEntity.getUniqueId());

        if (model == null) return;

        if (model.getBase() instanceof final MEGEntity e) {
            HMCCosmeticsPlugin.getInstance().getLogger().info("Updated Model");
            e.update(this);
        }
    }

    public void spawnModel(final String id) {
        HMCCosmeticsPlugin.getInstance().getLogger().info("Attempting Spawning for " + id);
        if (ModelEngineAPI.api.getModelRegistry().getBlueprint(id) == null) {
            HMCCosmeticsPlugin.getInstance().getLogger().warning("Invalid Model Engine Blueprint " + id);
            return;
        }
        HMCCosmeticsPlugin.getInstance().getLogger().warning("Possible Blueprints" + ModelEngineAPI.api.getModelRegistry().getAllBlueprintId());
        ActiveModel model = ModelEngineAPI.api.createActiveModelImpl(ModelEngineAPI.api.getModelRegistry().getBlueprint(id));
        ModeledEntity modeledEntity = ModelEngineAPI.api.createModeledEntityImpl(megEntity);
        modeledEntity.setRenderRadius(32);
        modeledEntity.addModel(model, false);
    }

    public void remove() {
        final ModeledEntity entity = ModelEngineAPI.api.getModeledEntity(megEntity.getUniqueId());

        if (entity == null) return;

        for (final Player player : entity.getRangeManager().getPlayerInRange()) {
            entity.hideFromPlayer(player);
        }

        ModelEngineAPI.removeModeledEntity(megEntity.getUniqueId());
        entity.destroy();
    }

    public void addPlayerToModel(final Player player, final String id) {
        final ModeledEntity model = ModelEngineAPI.api.getModeledEntity(megEntity.getUniqueId());
        if (model == null) {
            this.spawnModel(id);
            return;
        }
        //if (megEntity.getRangeManager().getPlayerInRange().contains(player)) return;
        model.showToPlayer(player);
    }

    public void removePlayerFromModel(final Player player) {
        final ModeledEntity model = ModelEngineAPI.api.getModeledEntity(megEntity.getUniqueId());

        if (model == null) return;

        model.hideFromPlayer(player);
    }


    public int getBalloonID() {
        return balloonID;
    }

    public UUID getUniqueID() {
        return megEntity.getUniqueId();
    }

    public Location getLocation() {
        return this.megEntity.getLocation();
    }

    public boolean isAlive() {
        return this.megEntity.isAlive();
    }

    public void setLocation(Location location) {
        this.megEntity.setLocation(location);
    }

    public void setVelocity(Vector vector) {
        this.megEntity.setVelocity(vector);
    }

    public void setAlive(boolean alive) {
        this.megEntity.setAlive(alive);
    }
}
