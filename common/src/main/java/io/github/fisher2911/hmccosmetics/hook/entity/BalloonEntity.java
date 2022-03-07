package io.github.fisher2911.hmccosmetics.hook.entity;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import java.util.UUID;

public class BalloonEntity {

    private final UUID uuid;
    private final int entityId;
    private EntityType entityType;
    private Vector velocity;
    private Location location;
    private boolean alive;

    public BalloonEntity(final UUID uuid, final int entityId, final EntityType entityType, final Vector velocity, final Location location, final boolean alive) {
        this.uuid = uuid;
        this.entityId = entityId;
        this.entityType = entityType;
        this.velocity = velocity;
        this.location = location;
        this.alive = alive;
    }

    public BalloonEntity(final UUID uuid, final int entityId, final EntityType entityType) {
        this.uuid = uuid;
        this.entityId = entityId;
        this.entityType = entityType;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getEntityId() {
        return entityId;
    }

    public EntityType getType() {
        return entityType;
    }

    public void setEntityType(final EntityType entityType) {
        this.entityType = entityType;
    }

    public Vector getVelocity() {
        return velocity;
    }

    public void setVelocity(final Vector velocity) {
        this.velocity = velocity;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(final Location location) {
        this.location = location;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(final boolean alive) {
        this.alive = alive;
    }
}
