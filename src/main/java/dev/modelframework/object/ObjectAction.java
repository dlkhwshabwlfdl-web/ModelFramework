package dev.modelframework.object;

public class ObjectAction {

    public enum ActionType {
        SOUND,
        MESSAGE,
        COMMAND
    }

    private final ActionType type;

    private String sound;
    private float volume = 1.0f;
    private float pitch = 1.0f;

    private String message;

    private String command;
    private String executor = "CONSOLE";

    public ObjectAction(ActionType type) {
        this.type = type;
    }

    public ActionType getType() { return type; }

    public String getSound() { return sound; }
    public void setSound(String sound) { this.sound = sound; }

    public float getVolume() { return volume; }
    public void setVolume(float volume) { this.volume = volume; }

    public float getPitch() { return pitch; }
    public void setPitch(float pitch) { this.pitch = pitch; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }

    public String getExecutor() { return executor; }
    public void setExecutor(String executor) { this.executor = executor; }
}