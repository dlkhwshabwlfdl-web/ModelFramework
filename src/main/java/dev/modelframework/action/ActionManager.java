package dev.modelframework.action;

import dev.modelframework.ModelFramework;
import dev.modelframework.instance.ObjectInstance;
import dev.modelframework.object.ObjectAction;
import dev.modelframework.object.ObjectAnimation;
import dev.modelframework.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class ActionManager {

    private final ModelFramework plugin;

    public ActionManager(ModelFramework plugin) {
        this.plugin = plugin;
    }

    public void executeAll(Player player, ObjectInstance instance, ObjectAnimation animation) {
        for (ObjectAction action : animation.getActions()) {
            execute(player, instance, action);
        }
    }

    public void execute(Player player, ObjectInstance instance, ObjectAction action) {
        try {
            switch (action.getType()) {
                case SOUND -> executeSound(player, instance, action);
                case MESSAGE -> executeMessage(player, action);
                case COMMAND -> executeCommand(player, action);
            }
        } catch (Exception e) {
            plugin.debug("Error executing action " + action.getType() + ": " + e.getMessage());
        }
    }

    private void executeSound(Player player, ObjectInstance instance, ObjectAction action) {
        String soundStr = action.getSound();
        if (soundStr == null || soundStr.isEmpty()) return;

        Location loc = instance.getLocation();
        if (loc == null) loc = player.getLocation();

        // Try Registry lookup (safe for 1.21.3+)
        Sound resolved = resolveSound(soundStr);
        if (resolved != null) {
            player.playSound(loc, resolved, action.getVolume(), action.getPitch());
        } else {
            // Fallback: play as namespaced string
            player.playSound(loc, soundStr.toLowerCase(), action.getVolume(), action.getPitch());
        }
    }

    private Sound resolveSound(String soundStr) {
        // Try dot format: block.chest.open
        try {
            NamespacedKey key = NamespacedKey.minecraft(soundStr.toLowerCase());
            Sound sound = Registry.SOUNDS.get(key);
            if (sound != null) return sound;
        } catch (Exception ignored) {}

        // Try underscore->dot: BLOCK_CHEST_OPEN -> block.chest.open
        try {
            String dotFormat = soundStr.toLowerCase().replace("_", ".");
            NamespacedKey key = NamespacedKey.minecraft(dotFormat);
            Sound sound = Registry.SOUNDS.get(key);
            if (sound != null) return sound;
        } catch (Exception ignored) {}

        return null;
    }

    private void executeMessage(Player player, ObjectAction action) {
        if (action.getMessage() == null) return;
        String msg = TextUtil.replacePlaceholders(player, action.getMessage());
        player.sendMessage(TextUtil.colorize(msg));
    }

    private void executeCommand(Player player, ObjectAction action) {
        if (action.getCommand() == null) return;
        String command = TextUtil.replacePlaceholders(player, action.getCommand());

        Bukkit.getScheduler().runTask(plugin, () -> {
            if ("PLAYER".equalsIgnoreCase(action.getExecutor())) {
                player.performCommand(command);
            } else {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            }
        });
    }
}