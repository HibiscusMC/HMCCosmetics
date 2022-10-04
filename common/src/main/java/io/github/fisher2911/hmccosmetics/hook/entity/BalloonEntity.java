package io.github.fisher2911.hmccosmetics.hook.entity;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BalloonEntity {

    private final int balloonID;
    private final MEGEntity megEntity;

    public BalloonEntity(int balloonID, Location location) {
        this.balloonID = balloonID;
        this.megEntity = new MEGEntity(UUID.randomUUID(), balloonID, new Vector(0, 0, 0), location, false);
    }

    public void updateModel() {
        final ModeledEntity model = ModelEngineAPI.api.getModeledEntity(megEntity.getUniqueId());

        if (model == null) return;

        if (model.getBase() instanceof final MEGEntity e) e.update(this);
    }

    public void spawnModel(final String id) {
        HMCCosmetics.getPlugin(HMCCosmetics.class).getLogger().info("Attempting Spawning");
        if (ModelEngineAPI.api.getModelRegistry().getBlueprint(id) == null) {
            HMCCosmetics.getPlugin(HMCCosmetics.class).getLogger().warning("Invalid Model Engine Blueprint " + id);
            HMCCosmetics.getPlugin(HMCCosmetics.class).getLogger().warning("Possible Blueprints" + ModelEngineAPI.api.getModelRegistry().getAllBlueprintId());
            return;
        }
        final ActiveModel model = ModelEngineAPI.api.createActiveModelImpl(ModelEngineAPI.getBlueprint(id));
        ModeledEntity modeledEntity = ModelEngineAPI.api.createModeledEntityImpl(megEntity);
        modeledEntity.addModel(model, false);
        HMCCosmetics.getPlugin(HMCCosmetics.class).getLogger().info("Spawned Model");
    }

    public void remove() {
        final ModeledEntity entity = ModelEngineAPI.api.getModeledEntity(megEntity.getUniqueId());

        if (entity == null) return;

        for (final Player player : entity.getRangeManager().getPlayerInRange()) {
            entity.hideFromPlayer(player);
        }

        //ModelEngineAPI.removeModeledEntity(megEntity.getUniqueId());
        entity.destroy();
    }

    public void addPlayerToModel(final Player player, final String id) {
        final ModeledEntity model = ModelEngineAPI.api.getModeledEntity(megEntity.getUniqueId());
        if (model == null) {
            this.spawnModel(id);
            return;
        }
        if (megEntity.getRangeManager().getPlayerInRange().contains(player)) return;
        model.showToPlayer(player);
        HMCCosmetics.getPlugin(HMCCosmetics.class).getLogger().info("Added " + player.getName() + " to " + id);
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
