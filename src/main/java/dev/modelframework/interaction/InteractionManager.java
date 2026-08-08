package dev.modelframework.interaction;

import dev.modelframework.ModelFramework;
import dev.modelframework.instance.ObjectInstance;
import dev.modelframework.object.FrameworkObject;
import dev.modelframework.object.ObjectAnimation;
import dev.modelframework.util.TextUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class InteractionManager implements Listener {

    private final ModelFramework plugin;
    private final Map<String, Long> cooldowns = new HashMap<>();

    public InteractionManager(ModelFramework plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getClickedBlock() == null) return;

        Player player = event.getPlayer();

        ObjectInstance instance = plugin.getInstanceManager()
                .getInstanceAt(event.getClickedBlock().getLocation());
        if (instance == null) return;

        event.setCancelled(true);

        TriggerType trigger = determineTrigger(event.getAction().name(), player.isSneaking());
        handleInteraction(player, instance, trigger);
    }

    public void handleInteraction(Player player, ObjectInstance instance, TriggerType trigger) {
        FrameworkObject obj = plugin.getObjectManager().getObject(instance.getObjectId());
        if (obj == null) return;

        if (!player.hasPermission("modelframework.interact")) return;

        plugin.debug("Interaction: player=" + player.getName()
                + " object=" + instance.getObjectId()
                + " trigger=" + trigger
                + " state=" + instance.getState());

        // Sort: animations with item requirement first
        List<ObjectAnimation> sorted = new ArrayList<>(obj.getAnimations().values());
        sorted.sort((a, b) -> {
            if (a.isItemRequired() && !b.isItemRequired()) return -1;
            if (!a.isItemRequired() && b.isItemRequired()) return 1;
            return 0;
        });

        for (ObjectAnimation anim : sorted) {
            if (anim.getTriggerType() != trigger) continue;

            // State check
            if (anim.getRequiredState() != null) {
                if (!anim.getRequiredState().equalsIgnoreCase(instance.getState())) {
                    continue;
                }
            }

            // Conditions (permission etc)
            if (!plugin.getConditionManager().checkAll(player, instance, anim)) return;

            // Cooldown
            if (!checkCooldown(player, instance, anim)) {
                long remaining = getCooldownRemaining(player, instance);
                String msg = plugin.getMessage("cooldown")
                        .replace("%time%", String.valueOf(remaining));
                player.sendMessage(TextUtil.colorize(msg));
                return;
            }

            // Item check
            if (anim.isItemRequired()) {
                if (!plugin.getConditionManager().checkItem(player, anim)) {
                    // Item required but player doesn't have it → skip to next animation
                    continue;
                }
                if (anim.isItemConsume()) {
                    plugin.getConditionManager().consumeItem(player, anim);
                }
            }

            // Play animation
            plugin.getAnimationManager().play(instance, anim);

            // Update state
            if (anim.getAfterState() != null) {
                instance.setState(anim.getAfterState());
            }

            // Set cooldown
            if (anim.getCooldownTime() > 0) {
                setCooldown(player, instance, anim.getCooldownTime());
            }

            // Execute actions
            plugin.getActionManager().executeAll(player, instance, anim);

            plugin.debug("Triggered animation '" + anim.getAnimationId()
                    + "' new state='" + instance.getState() + "'");

            break;
        }
    }

    private boolean checkCooldown(Player player, ObjectInstance instance, ObjectAnimation anim) {
        if (anim.getCooldownTime() <= 0) return true;
        String key = player.getUniqueId() + ":" + instance.getId();
        Long expiry = cooldowns.get(key);
        return expiry == null || System.currentTimeMillis() >= expiry;
    }

    private void setCooldown(Player player, ObjectInstance instance, int seconds) {
        String key = player.getUniqueId() + ":" + instance.getId();
        cooldowns.put(key, System.currentTimeMillis() + (seconds * 1000L));
    }

    private long getCooldownRemaining(Player player, ObjectInstance instance) {
        String key = player.getUniqueId() + ":" + instance.getId();
        Long expiry = cooldowns.get(key);
        if (expiry == null) return 0;
        return Math.max(0, (expiry - System.currentTimeMillis()) / 1000);
    }

    private TriggerType determineTrigger(String action, boolean sneaking) {
        if (action.contains("RIGHT")) {
            return sneaking ? TriggerType.SHIFT_RIGHT_CLICK : TriggerType.RIGHT_CLICK;
        } else if (action.contains("LEFT")) {
            return sneaking ? TriggerType.SHIFT_LEFT_CLICK : TriggerType.LEFT_CLICK;
        }
        return TriggerType.NONE;
    }
}