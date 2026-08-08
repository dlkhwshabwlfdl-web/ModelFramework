package dev.modelframework.hook;

import dev.modelframework.ModelFramework;
import kr.toxicity.model.api.BetterModel;
import kr.toxicity.model.api.animation.AnimationModifier;
import kr.toxicity.model.api.bukkit.platform.BukkitAdapter;
import kr.toxicity.model.api.data.renderer.ModelRenderer;
import kr.toxicity.model.api.tracker.EntityTracker;
import kr.toxicity.model.api.tracker.ModelScaler;
import kr.toxicity.model.api.tracker.Tracker;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.util.Optional;

public class BetterModelHook {

    private final ModelFramework plugin;

    public BetterModelHook(ModelFramework plugin) {
        this.plugin = plugin;
    }

    public boolean initialize() {
        if (!Bukkit.getPluginManager().isPluginEnabled("BetterModel")) {
            plugin.getLogger().severe("BetterModel plugin is not enabled!");
            return false;
        }

        try {
            Class.forName("kr.toxicity.model.api.BetterModel");
            plugin.getLogger().info("BetterModel API found and accessible.");
            return true;
        } catch (ClassNotFoundException e) {
            plugin.getLogger().severe("BetterModel API class not found!");
            return false;
        }
    }

    public Optional<ModelRenderer> renderer(String modelId) {
        if (modelId == null || modelId.isBlank()) return Optional.empty();

        try {
            return BetterModel.model(modelId);
        } catch (Throwable t) {
            plugin.debug("Failed to lookup model '" + modelId + "': " + t.getMessage());
            return Optional.empty();
        }
    }

    public boolean modelExists(String modelId) {
        return renderer(modelId).isPresent();
    }

    public EntityTracker spawnOnEntity(String modelId, Entity entity) {
        Optional<ModelRenderer> optional = renderer(modelId);

        if (optional.isEmpty()) {
            plugin.getLogger().warning("BetterModel model not found: " + modelId);
            return null;
        }

        try {
            EntityTracker tracker = optional.get().getOrCreate(BukkitAdapter.adapt(entity));
            if (tracker != null) {
                plugin.debug("Attached model '" + modelId + "' to entity " + entity.getUniqueId());
            } else {
                plugin.debug("Renderer returned null for model '" + modelId + "'");
            }
            return tracker;
        } catch (Throwable t) {
            plugin.getLogger().warning("Failed to attach model '" + modelId + "': " + t.getMessage());
            if (plugin.isDebug()) t.printStackTrace();
            return null;
        }
    }

    public boolean playAnimation(Tracker tracker, String animationName) {
        if (tracker == null || tracker.isClosed()) return false;
        if (animationName == null || animationName.isBlank()) return false;

        try {
            tracker.animate(animationName);
            plugin.debug("Playing animation '" + animationName + "'");
            return true;
        } catch (Throwable t) {
            plugin.debug("Failed: '" + animationName + "': " + t.getMessage());
            return false;
        }
    }

    public boolean playAnimationOnce(Tracker tracker, String animationName) {
        if (tracker == null || tracker.isClosed()) return false;
        if (animationName == null || animationName.isBlank()) return false;

        try {
            tracker.animate(animationName, AnimationModifier.DEFAULT_WITH_PLAY_ONCE);
            plugin.debug("Playing once '" + animationName + "'");
            return true;
        } catch (Throwable t) {
            plugin.debug("Failed once: '" + animationName + "': " + t.getMessage());
            return false;
        }
    }

    public boolean playAnimation(Tracker tracker, String animationName, Runnable onFinish) {
        if (tracker == null || tracker.isClosed()) return false;
        if (animationName == null || animationName.isBlank()) return false;

        try {
            tracker.animate(animationName, AnimationModifier.DEFAULT_WITH_PLAY_ONCE, onFinish);
            plugin.debug("Playing '" + animationName + "' with callback");
            return true;
        } catch (Throwable t) {
            plugin.debug("Failed callback: '" + animationName + "': " + t.getMessage());
            return false;
        }
    }

    public boolean stopAnimation(Tracker tracker, String animationName) {
        if (tracker == null || tracker.isClosed()) return false;
        if (animationName == null || animationName.isBlank()) return false;

        try {
            tracker.stopAnimation(animationName);
            plugin.debug("Stopped '" + animationName + "'");
            return true;
        } catch (Throwable t) {
            plugin.debug("Failed stop: '" + animationName + "': " + t.getMessage());
            return false;
        }
    }

    public void applyScale(Tracker tracker, double scale) {
        if (tracker == null || tracker.isClosed()) return;

        float finalScale = (float) Math.max(0.001D, scale);

        try {
            tracker.scaler(ModelScaler.value(finalScale));
            tracker.forceUpdate(true);
            plugin.debug("Applied scale " + finalScale);
        } catch (Throwable t) {
            plugin.debug("Failed scale " + finalScale + ": " + t.getMessage());
        }
    }

    public void close(Tracker tracker) {
        if (tracker == null) return;

        try {
            if (!tracker.isClosed()) {
                tracker.close();
                plugin.debug("Tracker closed.");
            }
        } catch (Throwable t) {
            plugin.debug("Failed close: " + t.getMessage());
        }
    }
}