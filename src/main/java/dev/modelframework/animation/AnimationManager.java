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
        if (instance.getTracker() == null) {
            plugin.debug("No tracker for instance #" + instance.getId());
            return false;
        }

        String animId = animation.getAnimationId();
        if (animId == null || animId.isBlank()) return false;

        boolean result;
        if (animation.isLoop()) {
            result = plugin.getBetterModelHook().playAnimation(instance.getTracker(), animId);
        } else {
            result = plugin.getBetterModelHook().playAnimationOnce(instance.getTracker(), animId);
        }

        plugin.debug("Animation '" + animId + "' loop=" + animation.isLoop()
                + " on instance #" + instance.getId() + " result=" + result);
        return result;
    }

    public boolean play(ObjectInstance instance, ObjectAnimation animation, Runnable onFinish) {
        if (instance == null || animation == null) return false;
        if (instance.getTracker() == null) return false;

        String animId = animation.getAnimationId();
        if (animId == null || animId.isBlank()) return false;

        return plugin.getBetterModelHook().playAnimation(instance.getTracker(), animId, onFinish);
    }

    public boolean playByName(ObjectInstance instance, String animationName) {
        if (instance == null || animationName == null || animationName.isBlank()) return false;
        if (instance.getTracker() == null) return false;

        return plugin.getBetterModelHook().playAnimationOnce(instance.getTracker(), animationName);
    }

    public boolean stopByName(ObjectInstance instance, String animationName) {
        if (instance == null || animationName == null || animationName.isBlank()) return false;
        if (instance.getTracker() == null) return false;

        return plugin.getBetterModelHook().stopAnimation(instance.getTracker(), animationName);
    }
}