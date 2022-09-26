package io.github.fisher2911.hmccosmetics.hook.entity;

import com.ticxo.modelengine.api.entity.BaseEntity;
import com.ticxo.modelengine.api.generator.Hitbox;
import com.ticxo.modelengine.api.model.IModel;
import com.ticxo.modelengine.api.nms.entity.wrapper.BodyRotationController;
import com.ticxo.modelengine.api.nms.entity.wrapper.LookController;
import com.ticxo.modelengine.api.nms.entity.wrapper.MoveController;
import com.ticxo.modelengine.api.nms.entity.wrapper.RangeManager;
import com.ticxo.modelengine.api.nms.world.IDamageSource;
import com.ticxo.modelengine.api.utils.data.EntityData;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;

public class MEGEntity implements BaseEntity {

    private final UUID uuid;
    private final int entityId;
    private Vector velocity = new Vector(0, 0, 0);
    private Location location;
    private boolean alive;

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
        this.alive = false;
    }

    public void update(final BalloonEntity entity) {
        this.velocity = entity.getLocation().toVector();
        this.location = entity.getLocation();
        this.alive = entity.isAlive();
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

    final EntityData entityData = new EntityData();

    @Override
    public Object getOriginal() {
        return null;
    }

    @Override
    public MoveController wrapMoveControl() {
        return null;
    }

    @Override
    public LookController wrapLookControl() {
        return null;
    }

    @Override
    public BodyRotationController wrapBodyRotationControl() {
        return null;
    }

    @Override
    public void wrapNavigation() {

    }

    @Override
    public RangeManager wrapRangeManager(IModel model) {
        return null;
    }

    @Override
    public RangeManager getRangeManager() {
        return null;
    }

    @Override
    public boolean onHurt(IDamageSource damageSource, float damage) {
        return false;
    }

    @Override
    public void onInteract(Player player, EquipmentSlot hand) {

    }

    @Override
    public void setHitbox(Hitbox hitbox) {

    }

    @Override
    public Hitbox getHitbox() {
        return null;
    }

    @Override
    public void setStepHeight(double height) {

    }

    @Override
    public Double getStepHeight() {
        return null;
    }

    @Override
    public void setCollidableToLiving(LivingEntity living, boolean isRemove) {

    }

    @Override
    public void broadcastSpawnPacket() {

    }

    @Override
    public void broadcastDespawnPacket() {

    }

    @Override
    public int getEntityId() {
        return this.entityId;
    }

    @Override
    public UUID getUniqueId() {
        return this.uuid;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public World getWorld() {
        return null;
    }

    @Override
    public boolean isDead() {
        return alive;
    }

    @Override
    public boolean isGlowing() {
        return false;
    }

    @Override
    public boolean isOnGround() {
        return false;
    }

    @Override
    public boolean isMoving() {
        return false;
    }

    @Override
    public void setYHeadRot(float rot) {

    }

    @Override
    public float getYHeadRot() {
        return 0;
    }

    @Override
    public float getXHeadRot() {
        return 0;
    }

    @Override
    public void setYBodyRot(float rot) {

    }

    @Override
    public float getYBodyRot() {
        return 0;
    }

    @Override
    public List<Entity> getPassengers() {
        return null;
    }

    public Vector getVelocity() {
        return velocity;
    }
}
