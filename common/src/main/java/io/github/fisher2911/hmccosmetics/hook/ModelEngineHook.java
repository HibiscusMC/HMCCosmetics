package io.github.fisher2911.hmccosmetics.hook;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import io.github.fisher2911.hmccosmetics.hook.entity.BalloonEntity;
import io.github.fisher2911.hmccosmetics.hook.entity.MEGEntity;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ModelEngineHook implements Hook {

    public ModelEngineHook() {
    }

    private static final String ID = "model-engine";

    public void spawnModel(final String id, final BalloonEntity entity) {
        this.spawnModel(id, new MEGEntity(entity));
    }

    public void spawnModel(final String id, final MEGEntity entity) {
        if (ModelEngineAPI.getModeledEntity(entity.getUuid()) != null) return;
        final ActiveModel model = ModelEngineAPI.createActiveModel(id);
        ModeledEntity modeledEntity = ModelEngineAPI.api.getModelManager().createModeledEntity(entity);
        modeledEntity.addActiveModel(model);
    }

    public void addPlayerToModel(final Player player, final String id, final BalloonEntity entity) {
        final ModeledEntity model = ModelEngineAPI.getModeledEntity(entity.getUuid());
        if (model == null) {
            this.spawnModel(id, entity);
            return;
        }

        if (model.getPlayerInRange().contains(player)) return;
        model.addPlayerAsync(player);
    }

    public void removePlayerFromModel(final Player player, final UUID uuid) {
        final ModeledEntity model = ModelEngineAPI.getModeledEntity(uuid);

        if (model == null) return;

        model.removePlayerAsync(player);
    }

    public void remove(final UUID uuid) {
        final ModeledEntity entity = ModelEngineAPI.getModeledEntity(uuid);

        if (entity == null) return;

        for (final Player player : entity.getPlayerInRange()) {
            entity.removePlayerAsync(player);
        }

        entity.getEntity().remove();

        ModelEngineAPI.api.getModelManager().removeModeledEntity(uuid);
    }

    @Override
    public String getId() {
        return ID;
    }
}
