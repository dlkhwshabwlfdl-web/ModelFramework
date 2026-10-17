package dev.modelframework.listener;

import dev.modelframework.ModelFramework;
import dev.modelframework.instance.ObjectInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class ViaVersionPlayerListener implements Listener {

    private final ModelFramework plugin;

    public ViaVersionPlayerListener(ModelFramework plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        handlePlayerVisibility(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        handlePlayerVisibility(event.getPlayer());
    }

    private void handlePlayerVisibility(Player player) {
        if (plugin.getModelEngineHook() == null) return;

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (ObjectInstance instance : plugin.getInstanceManager().getInstances().values()) {
                if (instance.getWorldName().equals(player.getWorld().getName())) {
                    if (instance.getModeledEntity() != null) {
                        plugin.getModelEngineHook().updateVisibility(instance.getModeledEntity(), instance.getBaseEntity(), player);
                    }
                }
            }
        }, 20L); // Delay a bit for ModelEngine to load
    }
}
