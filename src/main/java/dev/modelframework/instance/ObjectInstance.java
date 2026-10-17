package dev.modelframework.instance;

import kr.toxicity.model.api.tracker.Tracker;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.UUID;

public class ObjectInstance {

    private final int id;
    private final String objectId;

    private final String worldName;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;

    private String state;

    // Runtime only
    private transient Tracker tracker;
    private transient com.ticxo.modelengine.api.model.ModeledEntity modeledEntity;
    private transient UUID baseEntityUuid;
    private transient Entity cachedBaseEntity;

    public ObjectInstance(int id, String objectId, Location location, String initialState) {
        this.id = id;
        this.objectId = objectId;
        this.worldName = location.getWorld().getName();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
        this.state = initialState;
    }

    public ObjectInstance(int id, String objectId, String worldName,
                          double x, double y, double z,
                          float yaw, float pitch, String state) {
        this.id = id;
        this.objectId = objectId;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.state = state;
    }

    public Location getLocation() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        return new Location(world, x, y, z, yaw, pitch);
    }

    public String getLocationKey() {
        return worldName + ":"
                + (int) Math.floor(x) + ":"
                + (int) Math.floor(y) + ":"
                + (int) Math.floor(z);
    }

    public int getId() {
        return id;
    }

    public String getObjectId() {
        return objectId;
    }

    public String getWorldName() {
        return worldName;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Tracker getTracker() {
        return tracker;
    }

    public void setTracker(Tracker tracker) {
        this.tracker = tracker;
    }

    public com.ticxo.modelengine.api.model.ModeledEntity getModeledEntity() {
        return modeledEntity;
    }

    public void setModeledEntity(com.ticxo.modelengine.api.model.ModeledEntity modeledEntity) {
        this.modeledEntity = modeledEntity;
    }

    public UUID getBaseEntityUuid() {
        return baseEntityUuid;
    }

    public Entity getBaseEntity() {
        if (cachedBaseEntity != null && cachedBaseEntity.isValid()) {
            return cachedBaseEntity;
        }

        if (baseEntityUuid != null) {
            Entity found = Bukkit.getEntity(baseEntityUuid);
            if (found != null && found.isValid()) {
                cachedBaseEntity = found;
                return found;
            }
        }

        cachedBaseEntity = null;
        return null;
    }

    public void setBaseEntity(Entity entity) {
        if (entity == null) {
            this.baseEntityUuid = null;
            this.cachedBaseEntity = null;
            return;
        }

        this.baseEntityUuid = entity.getUniqueId();
        this.cachedBaseEntity = entity;
    }
}