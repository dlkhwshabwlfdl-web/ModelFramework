package dev.modelframework.animation;

import dev.modelframework.ModelFramework;
import dev.modelframework.instance.ObjectInstance;
import dev.modelframework.object.ObjectAnimation;

public class AnimationManager {

    private final ModelFramework plugin;

    public AnimationManager(ModelFramework plugin) {
        this.plugin = plugin;
    }

    public boolean play(ObjectInstance instance, ObjectAnimation animation) {
        if (instance == null || animation == null) return false;

        String animId = animation.getAnimationId();
        if (animId == null || animId.isBlank()) return false;

        boolean result = false;

        if (instance.getModeledEntity() != null) {
            String modelId = plugin.getObjectManager().getObject(instance.getObjectId()).getModelId();
            plugin.getModelEngineHook().playAnimation(instance.getModeledEntity(), modelId, animId);
            result = true;
        }

        if (instance.getTracker() != null) {
            if (animation.isLoop()) {
                result = plugin.getBetterModelHook().playAnimation(instance.getTracker(), animId) || result;
            } else {
                result = plugin.getBetterModelHook().playAnimationOnce(instance.getTracker(), animId) || result;
            }
        }

        if (instance.getModeledEntity() == null && instance.getTracker() == null) {
            plugin.debug("No tracker/model engine for instance #" + instance.getId());
        }

        plugin.debug("Animation '" + animId + "' loop=" + animation.isLoop()
                + " on instance #" + instance.getId() + " result=" + result);
        return result;
    }

    public boolean play(ObjectInstance instance, ObjectAnimation animation, Runnable onFinish) {
        if (instance == null || animation == null) return false;

        String animId = animation.getAnimationId();
        if (animId == null || animId.isBlank()) return false;

        boolean played = false;
        if (instance.getModeledEntity() != null) {
            String modelId = plugin.getObjectManager().getObject(instance.getObjectId()).getModelId();
            plugin.getModelEngineHook().playAnimation(instance.getModeledEntity(), modelId, animId);
            played = true;
        }

        if (instance.getTracker() != null) {
            // Give callback to better model if available, else handle it via MEG fallback
            played = plugin.getBetterModelHook().playAnimation(instance.getTracker(), animId, onFinish) || played;
        } else if (played && onFinish != null) {
            org.bukkit.Bukkit.getScheduler().runTask(plugin, onFinish);
        }
        return played;
    }

    public boolean playByName(ObjectInstance instance, String animationName) {
        if (instance == null || animationName == null || animationName.isBlank()) return false;

        boolean played = false;
        if (instance.getModeledEntity() != null) {
            String modelId = plugin.getObjectManager().getObject(instance.getObjectId()).getModelId();
            plugin.getModelEngineHook().playAnimation(instance.getModeledEntity(), modelId, animationName);
            played = true;
        }

        if (instance.getTracker() != null) {
            played = plugin.getBetterModelHook().playAnimationOnce(instance.getTracker(), animationName) || played;
        }
        return played;
    }

    public boolean stopByName(ObjectInstance instance, String animationName) {
        if (instance == null || animationName == null || animationName.isBlank()) return false;

        boolean stopped = false;
        if (instance.getModeledEntity() != null) {
            // ModelEngine does not support stopping specific animations natively without casting to ActiveModel easily, ignoring for now
            stopped = true;
        }

        if (instance.getTracker() != null) {
            stopped = plugin.getBetterModelHook().stopAnimation(instance.getTracker(), animationName) || stopped;
        }
        return stopped;
    }
}