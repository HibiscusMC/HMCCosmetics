package com.hibiscusmc.hmccosmetics.user.manager;

import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBalloonType;
import com.hibiscusmc.hmccosmetics.hooks.Hooks;
import com.hibiscusmc.hmccosmetics.nms.NMSHandlers;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.packets.PacketManager;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.entity.BaseEntity;
import com.ticxo.modelengine.api.entity.BukkitEntity;
import com.ticxo.modelengine.api.entity.Dummy;
import com.ticxo.modelengine.api.entity.EntityDataTracker;
import com.ticxo.modelengine.api.entity.data.BukkitEntityData;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import lombok.Getter;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class UserBalloonManager {

    private CosmeticUser user;
    @Getter
    private BalloonType balloonType;
    private CosmeticBalloonType cosmeticBalloonType;
    @Getter
    private UserBalloonPufferfish pufferfish;
    private final ArmorStand modelEntity;
    public UserBalloonManager(CosmeticUser user, @NotNull Location location) {
        this.user = user;
        this.pufferfish = new UserBalloonPufferfish(user.getUniqueId(), NMSHandlers.getHandler().getNextEntityId(), UUID.randomUUID());
        this.modelEntity = NMSHandlers.getHandler().getMEGEntity(location.add(Settings.getBalloonOffset()));
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
                    if (cosmeticBalloonType.isDyablePart(d)) {
                        singleModel.setDefaultTint(color);
                        singleModel.getModelRenderer().sendToClient();
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

    public void setVelocity(Vector vector) {
        this.getModelEntity().setVelocity(vector);
    }

    public void sendRemoveLeashPacket(List<Player> viewer) {
        PacketManager.sendLeashPacket(getPufferfishBalloonId(), -1, viewer);
    }

    public void sendRemoveLeashPacket() {
        PacketManager.sendLeashPacket(getPufferfishBalloonId(), -1, getLocation());
    }

    public void sendLeashPacket(int entityId) {
        if (cosmeticBalloonType.isShowLead()) {
            PacketManager.sendLeashPacket(getPufferfishBalloonId(), entityId, getLocation());
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
            return (!user.getHidden());
        } else {
            if (user.isInWardrobe()) return false;
            MessagesUtil.sendDebugMessages("playerCheck - Not Same Player");
            if (viewer != null && viewer.isInWardrobe()) {
                MessagesUtil.sendDebugMessages("playerCheck - Viewer in Wardrobe");
                return false;
            }
        }
        return (!user.getHidden());
    }
}
