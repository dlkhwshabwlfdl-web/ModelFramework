package dev.modelframework.object;

import org.bukkit.Material;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FrameworkObject {

    private final String id;

    private String sourceFile;

    private String displayName;
    private Material displayMaterial;
    private List<String> displayLore;
    private int displaySlot;

    private String modelId;

    private String placementType;
    private Material baseMaterial;

    private String initialState;

    private double offsetX = 0.0;
    private double offsetY = 0.0;
    private double offsetZ = 0.0;
    private double scale = 1.0;

    private final Map<String, ObjectAnimation> animations = new LinkedHashMap<>();

    private float rotationOffset = 0.0f;

    public FrameworkObject(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Material getDisplayMaterial() {
        return displayMaterial;
    }

    public void setDisplayMaterial(Material displayMaterial) {
        this.displayMaterial = displayMaterial;
    }

    public List<String> getDisplayLore() {
        return displayLore;
    }

    public void setDisplayLore(List<String> displayLore) {
        this.displayLore = displayLore;
    }

    public int getDisplaySlot() {
        return displaySlot;
    }

    public void setDisplaySlot(int displaySlot) {
        this.displaySlot = displaySlot;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getPlacementType() {
        return placementType;
    }

    public void setPlacementType(String placementType) {
        this.placementType = placementType;
    }

    public Material getBaseMaterial() {
        return baseMaterial;
    }

    public void setBaseMaterial(Material baseMaterial) {
        this.baseMaterial = baseMaterial;
    }

    public String getInitialState() {
        return initialState;
    }

    public void setInitialState(String initialState) {
        this.initialState = initialState;
    }

    public double getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(double offsetX) {
        this.offsetX = offsetX;
    }

    public double getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(double offsetY) {
        this.offsetY = offsetY;
    }

    public double getOffsetZ() {
        return offsetZ;
    }

    public void setOffsetZ(double offsetZ) {
        this.offsetZ = offsetZ;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public Map<String, ObjectAnimation> getAnimations() {
        return animations;
    }

    public void addAnimation(String name, ObjectAnimation animation) {
        animations.put(name.toLowerCase(), animation);
    }

    public ObjectAnimation getAnimation(String name) {
        return animations.get(name.toLowerCase());
    }
    public float getRotationOffset() { return rotationOffset; }
    public void setRotationOffset(float rotationOffset) { this.rotationOffset = rotationOffset; }
}