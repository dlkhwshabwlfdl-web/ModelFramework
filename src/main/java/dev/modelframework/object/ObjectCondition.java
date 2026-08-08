package dev.modelframework.object;

public class ObjectCondition {

    public enum ConditionType {
        PERMISSION,
        STATE,
        ITEM,
        COOLDOWN
    }

    private final ConditionType type;
    private String value;

    public ObjectCondition(ConditionType type, String value) {
        this.type = type;
        this.value = value;
    }

    public ConditionType getType() { return type; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}