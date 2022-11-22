package com.hibiscusmc.hmccosmetics.entities;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import net.minecraft.world.entity.Entity;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.UUID;

// This includes the Pufferfish (The Pufferfish that's what the player leashes to) and the model (MEGEntity)
public class BalloonEntity {

    private final int balloonID;
    private final UUID uniqueID;
    private final MEGEntity modelEntity;

    public BalloonEntity(Location location) {
        this.uniqueID = UUID.randomUUID();
        this.balloonID = Entity.nextEntityId();
        this.modelEntity = new MEGEntity(location.add(Settings.getBalloonOffset()));
    }

    public void spawnModel(final String id) {
        HMCCosmeticsPlugin.getInstance().getLogger().info("Attempting Spawning for " + id);
        if (ModelEngineAPI.api.getModelRegistry().getBlueprint(id) == null) {
            HMCCosmeticsPlugin.getInstance().getLogger().warning("Invalid Model Engine Blueprint " + id);
            return;
        }
        ModeledEntity modeledEntity = ModelEngineAPI.getOrCreateModeledEntity(modelEntity.getBukkitEntity());
        ActiveModel model = ModelEngineAPI.createActiveModel(ModelEngineAPI.getBlueprint(id));
        modeledEntity.addModel(model, false);
    }

    public void remove() {
        final ModeledEntity entity = ModelEngineAPI.api.getModeledEntity(modelEntity.getUUID());

        if (entity == null) return;

        for (final Player player : entity.getRangeManager().getPlayerInRange()) {
            entity.hideFromPlayer(player);
        }

        //ModelEngineAPI.removeModeledEntity(megEntity.getUniqueId());
        entity.destroy();
    }

    public void addPlayerToModel(final Player player, final String id) {
        final ModeledEntity model = ModelEngineAPI.api.getModeledEntity(modelEntity.getUUID());
        if (model == null) {
            spawnModel(id);
            return;
        }
        if (model.getRangeManager().getPlayerInRange().contains(player)) return;
        model.showToPlayer(player);
    }
    public void removePlayerFromModel(final Player player) {
        final ModeledEntity model = ModelEngineAPI.api.getModeledEntity(modelEntity.getUUID());

        if (model == null) return;

        model.hideFromPlayer(player);
    }

    public MEGEntity getModelEntity() {
        return this.modelEntity;
    }


    public int getPufferfishBalloonId() {
        return balloonID;
    }
    public UUID getPufferfishBalloonUniqueId() {
        return uniqueID;
    }

    public UUID getModelUnqiueId() {
        return modelEntity.getUUID();
    }

    public int getModelId() {
        return modelEntity.getId();
    }

    public Location getLocation() {
        return this.modelEntity.getBukkitEntity().getLocation();
    }

    public boolean isAlive() {
        return this.modelEntity.isAlive();
    }

    public void setLocation(Location location) {
        //this.megEntity.teleportTo(location.getX(), location.getY(), location.getZ());
        this.modelEntity.getBukkitEntity().teleport(location);
    }

    public void setVelocity(Vector vector) {
        this.modelEntity.getBukkitEntity().setVelocity(vector);
    }
}
