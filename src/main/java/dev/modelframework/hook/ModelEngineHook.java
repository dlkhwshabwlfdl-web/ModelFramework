package dev.modelframework.hook;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import dev.modelframework.ModelFramework;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class ModelEngineHook {

    private final ModelFramework plugin;

    public ModelEngineHook(ModelFramework plugin) {
        this.plugin = plugin;
    }

    public ModeledEntity spawnOnEntity(String modelId, Entity entity) {
        ActiveModel activeModel = ModelEngineAPI.createActiveModel(modelId);
        if (activeModel == null) return null;

        ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(entity);
        modeledEntity.addModel(activeModel, true);
        modeledEntity.setBaseEntityVisible(false);
        modeledEntity.setGlobalVisibility(false);

        for (Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
            updateVisibility(modeledEntity, entity, p);
        }

        return modeledEntity;
    }

    public void updateVisibility(ModeledEntity modeledEntity, Entity baseEntity, Player player) {
        if (player == null) return;
        boolean oldClient = false;
        if (plugin.getViaVersionHook() != null) {
            oldClient = plugin.getViaVersionHook().isOldClient(player);
        }

        if (oldClient) {
            if (baseEntity != null && baseEntity.isValid()) {
                player.hideEntity(plugin, baseEntity);
            }
            if (modeledEntity != null) {
                modeledEntity.addPlayer(player);
            }
        } else {
            if (baseEntity != null && baseEntity.isValid()) {
                player.showEntity(plugin, baseEntity);
            }
            if (modeledEntity != null) {
                modeledEntity.removePlayer(player);
            }
        }
    }

    public void playAnimation(ModeledEntity entity, String modelId, String animationName) {
        if (entity == null) return;
        ActiveModel model = entity.getModel(modelId).orElse(null);
        if (model != null) {
            model.getAnimationHandler().playAnimation(animationName, 1, 1, 1, true);
        }
    }

    public void applyScale(ModeledEntity entity, String modelId, double scale) {
        if (entity == null) return;
        ActiveModel model = entity.getModel(modelId).orElse(null);
        if (model != null) {
            model.setScale(scale);
        }
    }

    public void close(ModeledEntity entity) {
        if (entity == null) return;
        entity.destroy();
    }
}
