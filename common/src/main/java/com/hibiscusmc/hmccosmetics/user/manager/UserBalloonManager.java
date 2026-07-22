package com.hibiscusmc.hmccosmetics.user.manager;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBalloonType;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import com.hibiscusmc.hmccosmetics.util.HMCCServerUtils;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.packets.HMCCPacketManager;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.entity.data.BukkitEntityData;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import lombok.Getter;
import lombok.Setter;
import me.lojosho.hibiscuscommons.hooks.Hooks;
import me.lojosho.hibiscuscommons.nms.NMSHandlers;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class UserBalloonManager {

    /** Pose changes smaller than this are not worth a metadata broadcast. Degrees. */
    private static final double TILT_EPSILON = 0.05;

    private final CosmeticUser user;
    @Getter
    private BalloonType balloonType;
    private CosmeticBalloonType cosmeticBalloonType;
    @Getter
    private UserBalloonPufferfish pufferfish;
    private final ArmorStand modelEntity;

    // Smoothing state, updated by the balloon smoothing ticker. Degrees.
    @Getter
    private double tiltPitch = 0;
    @Getter
    private double tiltRoll = 0;
    // Last pose actually broadcast. Tracked apart from the logical tilt above so the tilt lerp keeps
    // converging normally while the metadata packet only goes out on a visible change.
    private double sentTiltPitch = 0;
    private double sentTiltRoll = 0;
    // Un-bobbed follow-lerp position. The entity's rendered location is this plus the idle bob offset;
    // lerping from the rendered location instead would feed the bob back into the follow-lerp.
    @Getter
    @Setter
    private Location smoothedBase;
    // Last position actually broadcast by the smoothing task. Lets the tail of a lerp - where the
    // per-tick step shrinks below visibility - stop teleporting the entity before it fully settles.
    @Getter
    @Setter
    private Location lastRendered;

    /**
     * Applies the movement and idle tilt.
     * <p>
     * Only ITEM balloons - a real armor stand wearing the cosmetic as a helmet - are actually posed.
     * ModelEngine renders its bones from its own transform data and never reads the stand's head pose
     * ({@code BukkitEntityData} exposes only yRot/yHeadRot/xHeadRot/yBodyRot), so posing a ME balloon is
     * an entity metadata broadcast to every tracker for no visual change at all.
     */
    public void setTilt(double pitch, double roll) {
        this.tiltPitch = pitch;
        this.tiltRoll = roll;
        if (balloonType != BalloonType.ITEM) return;
        if (Math.abs(pitch - sentTiltPitch) < TILT_EPSILON && Math.abs(roll - sentTiltRoll) < TILT_EPSILON) return;

        this.sentTiltPitch = pitch;
        this.sentTiltRoll = roll;
        modelEntity.setHeadPose(new EulerAngle(Math.toRadians(pitch), 0, Math.toRadians(roll)));
    }

    /**
     * Places the balloon at {@code location} with no smoothing carry-over: the entity, the follow-lerp
     * base and the tilt all snap to it. Use whenever the balloon has to appear somewhere instantly -
     * spawn, world change, long-distance teleport - rather than lerping across the gap.
     */
    public void snapTo(@NotNull Location location) {
        setLocation(location);
        this.smoothedBase = location.clone();
        this.lastRendered = location.clone();
        setTilt(0, 0);
    }

    public UserBalloonManager(CosmeticUser user, @NotNull Location location) {
        this.user = user;
        this.smoothedBase = location.clone();
        this.pufferfish = new UserBalloonPufferfish(user.getUniqueId(), NMSHandlers.getHandler().getUtilHandler().getNextEntityId(location.getWorld()), UUID.randomUUID());
        this.modelEntity = location.getWorld().spawn(location, ArmorStand.class, (e) -> {
            e.setInvisible(true);
            e.setGravity(false);
            e.setSilent(true);
            e.setInvulnerable(true);
            e.setSmall(true);
            e.setMarker(true);
            e.setPersistent(false);
            e.setAI(false);
            e.getPersistentDataContainer().set(HMCCServerUtils.getCosmemeticMobKey(), PersistentDataType.BOOLEAN, true);
        });
    }

    public void spawnModel(@NotNull CosmeticBalloonType cosmeticBalloonType, Color color) {
        // redo this
        if (cosmeticBalloonType.getModelName() != null && Hooks.isActiveHook("ModelEngine")) {
            balloonType = BalloonType.MODELENGINE;
        } else {
            if (cosmeticBalloonType.getItem() != null) {
                balloonType = BalloonType.ITEM;
            } else {
                balloonType = BalloonType.NONE;
            }
        }
        this.cosmeticBalloonType = cosmeticBalloonType;
        MessagesUtil.sendDebugMessages("balloontype is " + balloonType);

        if (balloonType == BalloonType.MODELENGINE) {
            String id = cosmeticBalloonType.getModelName();
            MessagesUtil.sendDebugMessages("Attempting Spawning for " + id);
            if (ModelEngineAPI.getBlueprint(id) == null) {
                MessagesUtil.sendDebugMessages("Invalid Model Engine Blueprint " + id, Level.SEVERE);
                return;
            }
            ModeledEntity modeledEntity = ModelEngineAPI.getOrCreateModeledEntity(modelEntity);
            ActiveModel model = ModelEngineAPI.createActiveModel(ModelEngineAPI.getBlueprint(id));
            model.setCanHurt(false);
            modeledEntity.addModel(model, false);

            if (color != null) {
                modeledEntity.getModels().forEach((d, singleModel) -> {
                    if (cosmeticBalloonType.isDyeablePart(d)) {
                        singleModel.setDefaultTint(color);
                        singleModel.getModelRenderer().sendToClient(ModelEngineAPI.getNMSHandler().createParsers());
                    }
                });
            }

            BukkitEntityData data = (BukkitEntityData) modeledEntity.getBase().getData();
            data.setBlockedCullIgnoreRadius((double) Settings.getViewDistance());
            data.getTracked().setPlayerPredicate(this::playerCheck);
            return;
        }
        if (balloonType == BalloonType.ITEM) {
            modelEntity.getEquipment().setHelmet(cosmeticBalloonType.getItem());
        }
    }

    public void remove() {
        // This code is like a brick road, always bumpy.
        // Basically, the balloon viewers ignore people in wardrobe, which well, if your the user in the wardrobe, ain't including you.
        // This manually passes in the viewers for it to destroy, which includes the person in the wardrobe
        if (user.getPlayer() != null && user.isInWardrobe()) pufferfish.destroyPufferfish(List.of(user.getPlayer()));
        else pufferfish.destroyPufferfish();

        if (balloonType == BalloonType.MODELENGINE) {
            final ModeledEntity entity = ModelEngineAPI.getModeledEntity(modelEntity);
            if (entity == null) {
                MessagesUtil.sendDebugMessages("Balloon Removal Failed - Model Entity is Null");
                return;
            }

            entity.destroy();
            MessagesUtil.sendDebugMessages("Balloon Model Engine Removal");
        }

        modelEntity.remove();
        cosmeticBalloonType = null;
        MessagesUtil.sendDebugMessages("Balloon Entity Removed");
    }

    public void addPlayerToModel(final CosmeticUser user, final CosmeticBalloonType cosmeticBalloonType) {
        addPlayerToModel(user, cosmeticBalloonType, null);
    }

    public void addPlayerToModel(final CosmeticUser user, final CosmeticBalloonType cosmeticBalloonType, Color color) {
        if (balloonType == BalloonType.MODELENGINE) {
            final ModeledEntity model = ModelEngineAPI.getModeledEntity(modelEntity);
            if (model == null) {
                spawnModel(cosmeticBalloonType, color);
                MessagesUtil.sendDebugMessages("model is null");
                return;
            }

            MessagesUtil.sendDebugMessages("Show to player");
            return;
        }
        if (balloonType == BalloonType.ITEM) {
            modelEntity.getEquipment().setHelmet(user.getUserCosmeticItem(cosmeticBalloonType));
        }
    }
    public void removePlayerFromModel(final Player viewer) {
        if (balloonType == BalloonType.MODELENGINE) {
            final ModeledEntity model = ModelEngineAPI.getModeledEntity(modelEntity);
            if (model == null) return;

            MessagesUtil.sendDebugMessages("Hidden from player");
            return;
        }
        if (balloonType == BalloonType.ITEM) {
            modelEntity.getEquipment().clear();
            return;
        }
    }

    public Entity getModelEntity() {
        return this.modelEntity;
    }


    public int getPufferfishBalloonId() {
        return pufferfish.getPufferFishEntityId();
    }

    public UUID getPufferfishBalloonUniqueId() {
        return pufferfish.getUuid();
    }

    public UUID getModelUnqiueId() {
        return getModelEntity().getUniqueId();
    }

    public int getModelId() {
        return getModelEntity().getEntityId();
    }

    public Location getLocation() {
        return this.getModelEntity().getLocation();
    }

    public void setLocation(Location location) {
        this.getModelEntity().teleport(location);
    }

    public Vector getVelocity() {
        return getModelEntity().getVelocity();
    }

    public void setVelocity(Vector vector) {
        this.getModelEntity().setVelocity(vector);
    }

    public void sendRemoveLeashPacket(List<Player> viewer) {
        HMCCPacketManager.sendLeashPacket(getPufferfishBalloonId(), -1, viewer);
    }

    public void sendRemoveLeashPacket() {
        HMCCPacketManager.sendLeashPacket(getPufferfishBalloonId(), -1, getLocation());
    }

    public void sendLeashPacket(int entityId) {
        if (cosmeticBalloonType.isShowLead()) {
            HMCCPacketManager.sendLeashPacket(getPufferfishBalloonId(), entityId, getLocation());
        }
    }

    public enum BalloonType {
        MODELENGINE,
        ITEM,
        NONE
    }

    private boolean playerCheck(final Player player) {
        MessagesUtil.sendDebugMessages("playerCheck");
        CosmeticUser viewer = CosmeticUsers.getUser(player.getUniqueId());

        if (user.getPlayer() == player) {
            return (!user.isHidden());
        } else {
            if (user.isInWardrobe()) return false;
            MessagesUtil.sendDebugMessages("playerCheck - Not Same Player");
            if (viewer != null && viewer.isInWardrobe()) {
                MessagesUtil.sendDebugMessages("playerCheck - Viewer in Wardrobe");
                return false;
            }
        }
        return (!user.isHidden());
    }
}
