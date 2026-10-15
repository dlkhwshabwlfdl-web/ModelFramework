package dev.modelframework.listener;

import dev.modelframework.ModelFramework;
import dev.modelframework.instance.ObjectInstance;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ChunkListener implements Listener {

    private final ModelFramework plugin;

    public ChunkListener(ModelFramework plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();
        String worldName = chunk.getWorld().getName();
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();

        plugin.getInstanceManager().respawnInstancesInChunk(worldName, chunkX, chunkZ);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onChunkUnload(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();
        String worldName = chunk.getWorld().getName();
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();

        plugin.getInstanceManager().despawnInstancesInChunk(worldName, chunkX, chunkZ);
    }
}
