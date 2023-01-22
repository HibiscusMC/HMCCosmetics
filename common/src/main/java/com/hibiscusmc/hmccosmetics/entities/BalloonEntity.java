package com.hibiscusmc.hmccosmetics.entities;

import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBalloonType;
import com.hibiscusmc.hmccosmetics.nms.NMSHandlers;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.UUID;
import java.util.logging.Level;

public class BalloonEntity {

    private BalloonType balloonType;
    private final int balloonID;
    private final UUID uniqueID;
    private final ArmorStand modelEntity;

    public BalloonEntity(Location location) {
        this.uniqueID = UUID.randomUUID();
        this.balloonID = NMSHandlers.getHandler().getNextEntityId();
        this.modelEntity = NMSHandlers.getHandler().getMEGEntity(location.add(Settings.getBalloonOffset()));
    }

    public void spawnModel(CosmeticBalloonType cosmeticBalloonType, Color color) {
        // redo this
        if (cosmeticBalloonType.getModelName() != null) {
            balloonType = BalloonType.MODELENGINE;
        } else {
            if (cosmeticBalloonType.getItem() != null) {
                balloonType = BalloonType.ITEM;
            } else {
                balloonType = BalloonType.NONE;
            }
        }
        MessagesUtil.sendDebugMessages("balloontype is " + balloonType);

        if (balloonType == BalloonType.MODELENGINE) {
            String id = cosmeticBalloonType.getModelName();
            MessagesUtil.sendDebugMessages("Attempting Spawning for " + id);
            if (ModelEngineAPI.api.getModelRegistry().getBlueprint(id) == null) {
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
                        singleModel.getRendererHandler().setColor(color);
                        singleModel.getRendererHandler().update();
                    }
                });
            }
            return;
        }
        if (balloonType == BalloonType.ITEM) {
            modelEntity.getEquipment().setHelmet(cosmeticBalloonType.getItem());
        }
    }

    public void remove() {
        if (balloonType == BalloonType.MODELENGINE) {
            final ModeledEntity entity = ModelEngineAPI.api.getModeledEntity(modelEntity.getUniqueId());

            if (entity == null) return;

            for (final Player player : entity.getRangeManager().getPlayerInRange()) {
                entity.hideFromPlayer(player);
            }
            entity.destroy();
        }

        modelEntity.remove();
    }

    public void addPlayerToModel(final Player player, final CosmeticBalloonType cosmeticBalloonType) {
        addPlayerToModel(player, cosmeticBalloonType, null);
    }

    public void addPlayerToModel(final Player player, final CosmeticBalloonType cosmeticBalloonType, Color color) {
        if (balloonType == BalloonType.MODELENGINE) {
            final ModeledEntity model = ModelEngineAPI.api.getModeledEntity(modelEntity.getUniqueId());
            if (model == null) {
                spawnModel(cosmeticBalloonType, color);
                MessagesUtil.sendDebugMessages("model is null");
                return;
            }
            //if (model.getRangeManager().getPlayerInRange().contains(player)) return;
            model.showToPlayer(player);
            MessagesUtil.sendDebugMessages("Show to player");
            return;
        }
        if (balloonType == BalloonType.ITEM) {
            modelEntity.getEquipment().setHelmet(cosmeticBalloonType.getItem());
        }
    }
    public void removePlayerFromModel(final Player player) {
        if (balloonType == BalloonType.MODELENGINE) {
            final ModeledEntity model = ModelEngineAPI.api.getModeledEntity(modelEntity.getUniqueId());

            if (model == null) return;

            model.hideFromPlayer(player);
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
        return balloonID;
    }
    public UUID getPufferfishBalloonUniqueId() {
        return uniqueID;
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

    public enum BalloonType {
        MODELENGINE,
        ITEM,
        NONE
    }
}
