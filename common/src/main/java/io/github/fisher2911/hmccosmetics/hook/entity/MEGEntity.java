package io.github.fisher2911.hmccosmetics.hook.entity;

import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.base.BaseEntity;
import com.ticxo.modelengine.api.model.base.EntityData;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MEGEntity implements BaseEntity<MEGEntity> {

    private final UUID uuid;
    private final int entityId;
    private Vector velocity = new Vector(0, 0, 0);
    private Location location;
    private boolean alive;

    public MEGEntity(final BalloonEntity entity) {
        this.uuid = entity.getUuid();
        this.entityId = entity.getEntityId();
        this.velocity = entity.getVelocity();
        this.location = entity.getLocation();
        this.alive = entity.isAlive();
    }

    protected MEGEntity(final UUID uuid, final int entityId, final Vector velocity, final Location location, final boolean alive) {
        this.uuid = uuid;
        this.entityId = entityId;
        this.velocity = velocity;
        this.location = location;
        this.alive = alive;
    }

    protected MEGEntity(final UUID uuid, final int entityId) {
        this.uuid = uuid;
        this.entityId = entityId;
        this.alive = true;
    }

    public void update(final BalloonEntity entity) {
        this.velocity = entity.getVelocity();
        this.location = entity.getLocation();
        this.alive = entity.isAlive();
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setVelocity(final Vector velocity) {
        this.velocity = velocity;
    }

    public void setLocation(final Location location) {
        this.location = location;
    }

    public void setAlive(final boolean alive) {
        this.alive = alive;
    }

    public boolean isAlive() {
        return alive;
    }

    public EntityData getEntityData() {
        return entityData;
    }

    @Override
    public MEGEntity getBase() {
        return this;
    }

    @Override
    public Location getLocation() {
        return this.location;
    }

    @Override
    public Vector getVelocity() {
        return velocity;
    }

    @Override
    public boolean isOnGround() {
        return false;
    }

    @Override
    public World getWorld() {
        return this.location.getWorld();
    }

    @Override
    public List<Entity> getNearbyEntities(final double v, final double v1, final double v2) {
        return Collections.emptyList();
    }

    @Override
    public int getEntityId() {
        return -1;
    }

    @Override
    public void remove() {
        this.alive = false;
    }

    @Override
    public boolean isCustomNameVisible() {
        return false;
    }

    @Override
    public boolean isDead() {
        return !this.alive;
    }

    @Override
    public UUID getUniqueId() {
        return this.uuid;
    }

    @Override
    public EntityType getType() {
        return EntityType.PUFFERFISH;
    }

    @Override
    public boolean isInvulnerable() {
        return true;
    }

    @Override
    public boolean hasGravity() {
        return false;
    }

    @Override
    public void setGravity(final boolean flag) {

    }

    @Override
    public double getHealth() {
        return 1;
    }

    @Override
    public double getMaxHealth() {
        return 1;
    }

    @Override
    public String getCustomName() {
        return null;
    }

    @Override
    public void setCustomName(final String s) {

    }

    @Override
    public double getMovementSpeed() {
        return 0;
    }

    @Override
    public ItemStack getItemInMainHand() {
        return null;
    }

    @Override
    public ItemStack getItemInOffHand() {
        return null;
    }

    @Override
    public boolean isLivingEntity() {
        return false;
    }

    @Override
    public void addPotionEffect(final PotionEffect potion) {

    }

    @Override
    public void removePotionEffect(final PotionEffectType potion) {

    }

    @Override
    public void setEntitySize(final float width, final float height, final float eye) {

    }

    @Override
    public void sendDespawnPacket(final ModeledEntity modeledEntity) {

    }

    @Override
    public void sendSpawnPacket(final ModeledEntity modeledEntity) {

    }

    @Override
    public double getLastX() {
        return this.location.getX();
    }

    @Override
    public double getLastY() {
        return this.location.getY();
    }

    @Override
    public double getLastZ() {
        return this.location.getZ();
    }

    @Override
    public double getWantedX() {
        return this.location.getX();
    }

    @Override
    public double getWantedY() {
        return this.location.getY();
    }

    @Override
    public double getWantedZ() {
        return this.location.getZ();
    }

    @Override
    public void saveModelList(final Map<String, ActiveModel> models) {

    }

    @Override
    public void saveModelInfo(final ModeledEntity model) {

    }

    @Override
    public List<String> getModelList() {
        return Collections.emptyList();
    }

    final EntityData entityData = new EntityData();

    @Override
    public EntityData loadModelInfo() {
        return this.entityData;
    }
}
