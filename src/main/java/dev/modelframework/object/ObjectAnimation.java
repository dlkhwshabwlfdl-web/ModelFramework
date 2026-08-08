package dev.modelframework.object;

import dev.modelframework.interaction.TriggerType;

import java.util.ArrayList;
import java.util.List;

public class ObjectAnimation {

    private final String name;
    private String animationId;
    private TriggerType triggerType;

    private boolean itemRequired;
    private String itemProvider;
    private String itemId;
    private boolean itemConsume;

    private String requiredState;
    private String afterState;

    private String permission;

    private int cooldownTime;

    private boolean loop = false;

    private String itemCheck = "INVENTORY"; // HAND or INVENTORY

    private final List<ObjectAction> actions = new ArrayList<>();


    public ObjectAnimation(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public String getAnimationId() { return animationId; }
    public void setAnimationId(String animationId) { this.animationId = animationId; }

    public TriggerType getTriggerType() { return triggerType; }
    public void setTriggerType(TriggerType triggerType) { this.triggerType = triggerType; }

    public boolean isItemRequired() { return itemRequired; }
    public void setItemRequired(boolean itemRequired) { this.itemRequired = itemRequired; }

    public String getItemProvider() { return itemProvider; }
    public void setItemProvider(String itemProvider) { this.itemProvider = itemProvider; }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public boolean isItemConsume() { return itemConsume; }
    public void setItemConsume(boolean itemConsume) { this.itemConsume = itemConsume; }

    public String getRequiredState() { return requiredState; }
    public void setRequiredState(String requiredState) { this.requiredState = requiredState; }

    public String getAfterState() { return afterState; }
    public void setAfterState(String afterState) { this.afterState = afterState; }

    public String getPermission() { return permission; }
    public void setPermission(String permission) { this.permission = permission; }

    public int getCooldownTime() { return cooldownTime; }
    public void setCooldownTime(int cooldownTime) { this.cooldownTime = cooldownTime; }

    public List<ObjectAction> getActions() { return actions; }
    public void addAction(ObjectAction action) { actions.add(action); }

    public boolean isLoop() { return loop; }
    public void setLoop(boolean loop) { this.loop = loop; }

    public String getItemCheck() { return itemCheck; }
    public void setItemCheck(String itemCheck) { this.itemCheck = itemCheck; }
}