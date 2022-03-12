package io.github.fisher2911.hmccosmetics.hook.entity;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.UUID;

public class BalloonEntity {

    private final MEGEntity megEntity;
//    private final UUID uuid;
//    private final int entityId;
//    private EntityType entityType;
//    private Vector velocity;
//    private Location location;
//    private boolean alive;

    public BalloonEntity(final UUID uuid, final int entityId, final EntityType entityType, final Vector velocity, final Location location, final boolean alive) {
        this.megEntity = new MEGEntity(uuid, entityId, entityType, velocity, location, alive);
//        this.uuid = uuid;
//        this.entityId = entityId;
//        this.entityType = entityType;
//        this.velocity = velocity;
//        this.location = location;
//        this.alive = alive;
//        this.megEntity = new MEGEntity(this);
    }

    public BalloonEntity(final UUID uuid, final int entityId, final EntityType entityType) {
//        this.uuid = uuid;
//        this.entityId = entityId;
//        this.entityType = entityType;
        this.megEntity = new MEGEntity(uuid, entityId, entityType);
    }

    public UUID getUuid() {
        return this.megEntity.getUuid();
    }

    public int getEntityId() {
        return this.megEntity.getEntityId();
    }

    public EntityType getType() {
        return this.megEntity.getType();
    }

    public Vector getVelocity() {
        return this.megEntity.getVelocity();
    }

    public void setVelocity(final Vector velocity) {
        this.megEntity.setVelocity(velocity);
    }

    public Location getLocation() {
        return this.megEntity.getLocation();
    }

    public void setLocation(final Location location) {
        this.megEntity.setLocation(location);
    }

    public boolean isAlive() {
        return this.megEntity.isAlive();
    }

    public void setAlive(final boolean alive) {
        this.megEntity.setAlive(alive);
    }

    public void updateModel() {
        final ModeledEntity model = ModelEngineAPI.getModeledEntity(this.getUuid());

        if (model == null) return;

        if (model.getEntity() instanceof final MEGEntity e) e.update(this);
    }

    public void spawnModel(final String id) {
        if (ModelEngineAPI.getModeledEntity(this.getUuid()) != null) return;
        final ActiveModel model = ModelEngineAPI.createActiveModel(id);
        ModeledEntity modeledEntity = ModelEngineAPI.api.getModelManager().createModeledEntity(this.megEntity);
        modeledEntity.addActiveModel(model);
    }

    public void addPlayerToModel(final Player player, final String id) {
        final ModeledEntity model = ModelEngineAPI.getModeledEntity(this.getUuid());
        if (model == null) {
            this.spawnModel(id);
            return;
        }

        if (model.getPlayerInRange().contains(player)) return;
        model.addPlayerAsync(player);
    }

    public void removePlayerFromModel(final Player player) {
        final ModeledEntity model = ModelEngineAPI.getModeledEntity(this.getUuid());

        if (model == null) return;

        model.removePlayerAsync(player);
    }

    public void remove() {
        final ModeledEntity entity = ModelEngineAPI.getModeledEntity(this.getUuid());

        if (entity == null) return;

        for (final Player player : entity.getPlayerInRange()) {
            entity.removePlayerAsync(player);
        }

        entity.getEntity().remove();

        ModelEngineAPI.api.getModelManager().removeModeledEntity(this.getUuid());
    }
}
