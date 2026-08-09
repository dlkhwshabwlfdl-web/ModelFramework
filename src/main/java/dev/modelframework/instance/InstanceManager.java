package dev.modelframework.instance;

import dev.modelframework.ModelFramework;
import dev.modelframework.object.FrameworkObject;
import kr.toxicity.model.api.tracker.EntityTracker;
import kr.toxicity.model.api.tracker.Tracker;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InstanceManager {

    private final ModelFramework plugin;

    private final Map<Integer, ObjectInstance> instances = new ConcurrentHashMap<>();
    private final Map<String, ObjectInstance> locationIndex = new ConcurrentHashMap<>();

    private int nextId = 1;

    public InstanceManager(ModelFramework plugin) {
        this.plugin = plugin;
    }

    public ObjectInstance createInstance(String objectId, Location location) {
        FrameworkObject obj = plugin.getObjectManager().getObject(objectId);
        if (obj == null) {
            plugin.debug("Cannot create instance: object '" + objectId + "' not found.");
            return null;
        }

        Location blockCenter = location.getBlock().getLocation().add(0.5, 0.0, 0.5);
        float snappedYaw = snapYaw(location.getYaw());
        float finalYaw = snappedYaw + obj.getRotationOffset();
        blockCenter.setYaw(finalYaw);
        blockCenter.setPitch(0);

        Location displayLoc = blockCenter.clone().add(
                obj.getOffsetX(),
                obj.getOffsetY(),
                obj.getOffsetZ()
        );

        if (displayLoc.getWorld() == null) {
            plugin.debug("Cannot create instance: world is null.");
            return null;
        }

        displayLoc.setYaw(finalYaw);

        cleanEntitiesAt(displayLoc);

        ItemDisplay display = displayLoc.getWorld().spawn(displayLoc, ItemDisplay.class, spawned -> {
            spawned.setGravity(false);
            spawned.setPersistent(false);
            spawned.setInvulnerable(true);
            spawned.setSilent(true);
            spawned.setRotation(finalYaw, 0);
        });

        EntityTracker tracker = plugin.getBetterModelHook().spawnOnEntity(obj.getModelId(), display);
        if (tracker == null) {
            display.remove();
            plugin.getLogger().warning("Failed to create BetterModel tracker for '"
                    + objectId + "' model='" + obj.getModelId() + "'");
            return null;
        }

        ObjectInstance instance = new ObjectInstance(
                nextId++, objectId, blockCenter, obj.getInitialState()
        );

        instance.setBaseEntity(display);
        instance.setTracker(tracker);

        if (obj.getBaseMaterial() != null && obj.getBaseMaterial() != Material.AIR) {
            blockCenter.getBlock().setType(obj.getBaseMaterial(), false);
        }

        instances.put(instance.getId(), instance);
        locationIndex.put(instance.getLocationKey(), instance);

        if (obj.getScale() != 1.0D) {
            final double finalScale = obj.getScale();
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!tracker.isClosed()) {
                    plugin.getBetterModelHook().applyScale(tracker, finalScale);
                }
            }, 10L);
        }

        plugin.debug("Created instance #" + instance.getId()
                + " object='" + objectId + "' yaw=" + finalYaw
                + " (snap=" + snappedYaw + " offset=" + obj.getRotationOffset() + ")"
                + " at " + instance.getLocationKey());

        saveAll();
        return instance;
    }

    public void removeInstance(int id) {
        ObjectInstance instance = instances.remove(id);
        if (instance == null) return;

        locationIndex.remove(instance.getLocationKey());

        Tracker tracker = instance.getTracker();
        if (tracker != null) {
            plugin.getBetterModelHook().close(tracker);
            instance.setTracker(null);
        }

        Entity baseEntity = instance.getBaseEntity();
        if (baseEntity != null && baseEntity.isValid()) {
            baseEntity.remove();
            instance.setBaseEntity(null);
        }

        Location loc = instance.getLocation();
        if (loc != null && loc.getWorld() != null) {
            loc.getBlock().setType(Material.AIR, false);
            // Clean any leftover displays
            cleanEntitiesAt(loc);
        }

        plugin.debug("Removed instance #" + id);
        saveAll();
    }

    public ObjectInstance getInstanceAt(Location location) {
        if (location == null || location.getWorld() == null) return null;
        return locationIndex.get(locationKey(location));
    }

    public ObjectInstance getInstance(int id) {
        return instances.get(id);
    }

    public Map<Integer, ObjectInstance> getInstances() {
        return Collections.unmodifiableMap(instances);
    }

    /**
     * Clean old/orphaned ItemDisplay entities near instance locations
     * Called before respawn to prevent ghost models
     */
    public void cleanOldEntities() {
        int cleaned = 0;

        for (ObjectInstance instance : instances.values()) {
            Location loc = instance.getLocation();
            if (loc == null || loc.getWorld() == null) continue;

            FrameworkObject obj = plugin.getObjectManager().getObject(instance.getObjectId());
            if (obj == null) continue;

            Location displayLoc = loc.clone().add(
                    obj.getOffsetX(),
                    obj.getOffsetY(),
                    obj.getOffsetZ()
            );

            cleaned += cleanEntitiesAt(displayLoc);
        }

        if (cleaned > 0) {
            plugin.debug("Cleaned " + cleaned + " old ItemDisplay entities.");
        }
    }

    /**
     * Remove all ItemDisplay entities within 1 block of location
     */
    private int cleanEntitiesAt(Location location) {
        if (location == null || location.getWorld() == null) return 0;

        int removed = 0;
        for (Entity entity : location.getWorld().getNearbyEntities(location, 1.5, 1.5, 1.5)) {
            if (entity instanceof ItemDisplay) {
                entity.remove();
                removed++;
            }
        }
        return removed;
    }

    public void respawnAll() {
        int success = 0;
        int fail = 0;

        List<ScaleEntry> pendingScale = new ArrayList<>();

        for (ObjectInstance instance : instances.values()) {
            FrameworkObject obj = plugin.getObjectManager().getObject(instance.getObjectId());
            if (obj == null) {
                plugin.getLogger().warning("Object '" + instance.getObjectId()
                        + "' not found for instance #" + instance.getId());
                fail++;
                continue;
            }

            Location loc = instance.getLocation();
            if (loc == null || loc.getWorld() == null) {
                plugin.debug("World not loaded for instance #" + instance.getId());
                fail++;
                continue;
            }

            Location displayLoc = loc.clone().add(
                    obj.getOffsetX(),
                    obj.getOffsetY(),
                    obj.getOffsetZ()
            );

            float savedYaw = instance.getYaw();
            displayLoc.setYaw(savedYaw);

            ItemDisplay display = displayLoc.getWorld().spawn(displayLoc, ItemDisplay.class, spawned -> {
                spawned.setGravity(false);
                spawned.setPersistent(false);
                spawned.setInvulnerable(true);
                spawned.setSilent(true);
                spawned.setRotation(savedYaw, 0);
            });

            EntityTracker tracker = plugin.getBetterModelHook().spawnOnEntity(obj.getModelId(), display);
            if (tracker == null) {
                display.remove();
                plugin.getLogger().warning("Failed to respawn instance #"
                        + instance.getId() + " model='" + obj.getModelId() + "'");
                fail++;
                continue;
            }

            instance.setBaseEntity(display);
            instance.setTracker(tracker);

            if (obj.getBaseMaterial() != null && obj.getBaseMaterial() != Material.AIR) {
                loc.getBlock().setType(obj.getBaseMaterial(), false);
            }

            locationIndex.put(instance.getLocationKey(), instance);

            if (obj.getScale() != 1.0D) {
                pendingScale.add(new ScaleEntry(tracker, obj.getScale()));
            }

            success++;
        }

        // Apply scales after BetterModel fully renders
        if (!pendingScale.isEmpty()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (ScaleEntry entry : pendingScale) {
                    if (!entry.tracker.isClosed()) {
                        plugin.getBetterModelHook().applyScale(entry.tracker, entry.scale);
                    }
                }
                plugin.debug("Applied scale to " + pendingScale.size() + " instances.");
            }, 20L);
        }

        plugin.getLogger().info("Respawned " + success + " instances"
                + (fail > 0 ? " (" + fail + " failed)" : "") + ".");
    }

    private record ScaleEntry(EntityTracker tracker, double scale) {}

    public void despawnAll() {
        int closed = 0;

        for (ObjectInstance instance : instances.values()) {
            Tracker tracker = instance.getTracker();
            if (tracker != null) {
                plugin.getBetterModelHook().close(tracker);
                instance.setTracker(null);
            }

            Entity entity = instance.getBaseEntity();
            if (entity != null && entity.isValid()) {
                entity.remove();
                instance.setBaseEntity(null);
            }

            closed++;
        }

        plugin.debug("Despawned " + closed + " instances.");
    }

    public void loadAll() {
        instances.clear();
        locationIndex.clear();
        nextId = 1;

        File file = getDataFile();
        if (!file.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("instances");
        if (section == null) return;

        int loaded = 0;

        for (String key : section.getKeys(false)) {
            try {
                ConfigurationSection entry = section.getConfigurationSection(key);
                if (entry == null) continue;

                int id = Integer.parseInt(key);
                String objectId = entry.getString("object");
                if (objectId == null || objectId.isBlank()) continue;

                ObjectInstance instance = new ObjectInstance(
                        id, objectId,
                        entry.getString("world", "world"),
                        entry.getDouble("x", 0),
                        entry.getDouble("y", 0),
                        entry.getDouble("z", 0),
                        (float) entry.getDouble("yaw", 0),
                        (float) entry.getDouble("pitch", 0),
                        entry.getString("state", "DEFAULT")
                );

                instances.put(id, instance);
                locationIndex.put(instance.getLocationKey(), instance);

                if (id >= nextId) nextId = id + 1;
                loaded++;
            } catch (Exception e) {
                plugin.getLogger().warning("Error loading instance '" + key + "': " + e.getMessage());
            }
        }

        plugin.getLogger().info("Loaded " + loaded + " instances. Next ID: " + nextId);
    }

    public void saveAll() {
        File file = getDataFile();
        YamlConfiguration config = new YamlConfiguration();

        ConfigurationSection section = config.createSection("instances");

        for (ObjectInstance instance : instances.values()) {
            ConfigurationSection entry = section.createSection(String.valueOf(instance.getId()));
            entry.set("object", instance.getObjectId());
            entry.set("world", instance.getWorldName());
            entry.set("x", instance.getX());
            entry.set("y", instance.getY());
            entry.set("z", instance.getZ());
            entry.set("yaw", instance.getYaw());
            entry.set("pitch", instance.getPitch());
            entry.set("state", instance.getState());
        }

        try {
            file.getParentFile().mkdirs();
            config.save(file);
            plugin.debug("Saved " + instances.size() + " instances.");
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save instances: " + e.getMessage());
        }
    }

    private File getDataFile() {
        return new File(plugin.getDataFolder(), "data/instances.yml");
    }

    private String locationKey(Location location) {
        if (location.getWorld() == null) return "null:0:0:0";
        return location.getWorld().getName() + ":"
                + location.getBlockX() + ":"
                + location.getBlockY() + ":"
                + location.getBlockZ();
    }

    private float snapYaw(float yaw) {
        yaw = yaw % 360;
        if (yaw < 0) yaw += 360;

        if (yaw >= 315 || yaw < 45) return 0;
        if (yaw >= 45 && yaw < 135) return 90;
        if (yaw >= 135 && yaw < 225) return 180;
        return 270;
    }
}