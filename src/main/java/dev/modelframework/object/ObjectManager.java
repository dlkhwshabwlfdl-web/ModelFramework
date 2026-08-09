package dev.modelframework.object;

import dev.modelframework.ModelFramework;
import dev.modelframework.interaction.TriggerType;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class ObjectManager {

    private final ModelFramework plugin;

    private final Map<String, FrameworkObject> objects = new LinkedHashMap<>();
    private final Map<String, List<FrameworkObject>> objectsByFile = new LinkedHashMap<>();

    public ObjectManager(ModelFramework plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        objects.clear();
        objectsByFile.clear();

        File objectsDir = new File(plugin.getDataFolder(), "objects");
        if (!objectsDir.exists()) {
            objectsDir.mkdirs();
            return;
        }

        File[] files = objectsDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        Arrays.sort(files, Comparator.comparing(File::getName));

        for (File file : files) {
            loadFile(file);
        }

        plugin.getLogger().info("Loaded " + objects.size() + " object definitions from "
                + objectsByFile.size() + " object files.");
    }

    private void loadFile(File file) {
        String fileKey = file.getName().replace(".yml", "");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        List<FrameworkObject> list = objectsByFile.computeIfAbsent(fileKey, k -> new ArrayList<>());

        List<String> keys = new ArrayList<>(config.getKeys(false));
        Collections.sort(keys);

        for (String key : keys) {
            try {
                ConfigurationSection section = config.getConfigurationSection(key);
                if (section == null) continue;

                FrameworkObject obj = parseObject(key, section);
                if (obj != null) {
                    obj.setSourceFile(fileKey);

                    objects.put(key.toLowerCase(), obj);
                    list.add(obj);

                    plugin.debug("Loaded object: " + key + " from " + file.getName());
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error loading object '" + key
                        + "' from " + file.getName() + ": " + e.getMessage());
            }
        }

        list.sort(Comparator.comparingInt(FrameworkObject::getDisplaySlot));
    }

    private FrameworkObject parseObject(String id, ConfigurationSection section) {
        FrameworkObject obj = new FrameworkObject(id);

        // Display
        ConfigurationSection display = section.getConfigurationSection("display");
        if (display != null) {
            obj.setDisplayName(display.getString("name", "&f" + id));
            try {
                obj.setDisplayMaterial(Material.valueOf(
                        display.getString("material", "STONE").toUpperCase()));
            } catch (IllegalArgumentException e) {
                obj.setDisplayMaterial(Material.STONE);
            }
            obj.setDisplayLore(display.getStringList("lore"));
            obj.setDisplaySlot(display.getInt("slot", 0));
        }

        // Model
        ConfigurationSection model = section.getConfigurationSection("model");
        if (model != null) {
            obj.setModelId(model.getString("id", id));
            obj.setScale(model.getDouble("scale", 1.0));
            obj.setOffsetX(model.getDouble("offset-x", 0.0));
            obj.setOffsetY(model.getDouble("offset-y", 0.0));
            obj.setOffsetZ(model.getDouble("offset-z", 0.0));
            obj.setRotationOffset((float) model.getDouble("rotation-offset", 0.0));
        } else {
            obj.setModelId(id);
        }

        // Placement
        ConfigurationSection placement = section.getConfigurationSection("placement");
        if (placement != null) {
            obj.setPlacementType(placement.getString("type", "BLOCK"));
            try {
                obj.setBaseMaterial(Material.valueOf(
                        placement.getString("base-material", "BARRIER").toUpperCase()));
            } catch (IllegalArgumentException e) {
                obj.setBaseMaterial(Material.BARRIER);
            }
        } else {
            obj.setPlacementType("BLOCK");
            obj.setBaseMaterial(Material.BARRIER);
        }

        // State
        ConfigurationSection state = section.getConfigurationSection("state");
        obj.setInitialState(state != null ? state.getString("initial", "DEFAULT") : "DEFAULT");

        // Animations
        ConfigurationSection animations = section.getConfigurationSection("animations");
        if (animations != null) {
            List<String> animKeys = new ArrayList<>(animations.getKeys(false));
            for (String animKey : animKeys) {
                ConfigurationSection animSection = animations.getConfigurationSection(animKey);
                if (animSection == null) continue;

                obj.addAnimation(animKey, parseAnimation(animKey, animSection));
            }
        }

        return obj;
    }

    private ObjectAnimation parseAnimation(String name, ConfigurationSection section) {
        ObjectAnimation anim = new ObjectAnimation(name);

        anim.setAnimationId(section.getString("animation", name));
        anim.setLoop(section.getBoolean("loop", false));

        // Trigger
        ConfigurationSection trigger = section.getConfigurationSection("trigger");
        if (trigger != null) {
            try {
                anim.setTriggerType(TriggerType.valueOf(
                        trigger.getString("type", "NONE").toUpperCase()));
            } catch (IllegalArgumentException e) {
                anim.setTriggerType(TriggerType.NONE);
            }
        } else {
            anim.setTriggerType(TriggerType.NONE);
        }

        // Item
        ConfigurationSection item = section.getConfigurationSection("item");
        if (item != null) {
            boolean required = item.getBoolean("required", true);
            anim.setItemRequired(required);
            if (required) {
                anim.setItemProvider(item.getString("provider", "vanilla"));
                anim.setItemId(item.getString("id", ""));
                anim.setItemConsume(item.getBoolean("consume", false));
                anim.setItemCheck(item.getString("check", "INVENTORY").toUpperCase());
            }
        } else {
            anim.setItemRequired(false);
        }

        // State
        ConfigurationSection stateSection = section.getConfigurationSection("state");
        if (stateSection != null) {
            anim.setRequiredState(stateSection.getString("required", null));
            anim.setAfterState(stateSection.getString("after", null));
        }

        // Conditions
        ConfigurationSection conditions = section.getConfigurationSection("conditions");
        if (conditions != null) {
            anim.setPermission(conditions.getString("permission", null));
        }

        // Cooldown
        ConfigurationSection cooldown = section.getConfigurationSection("cooldown");
        if (cooldown != null) {
            anim.setCooldownTime(cooldown.getInt("time",
                    plugin.getConfig().getInt("defaults.cooldown", 3)));
        }

        // Actions
        List<Map<?, ?>> actionsList = section.getMapList("actions");
        for (Map<?, ?> actionMap : actionsList) {
            ObjectAction action = parseAction(actionMap);
            if (action != null) {
                anim.addAction(action);
            }
        }

        return anim;
    }

    private ObjectAction parseAction(Map<?, ?> map) {
        String typeStr = String.valueOf(map.get("type"));
        try {
            ObjectAction.ActionType type = ObjectAction.ActionType.valueOf(typeStr.toUpperCase());
            ObjectAction action = new ObjectAction(type);

            switch (type) {
                case SOUND -> {
                    action.setSound(String.valueOf(map.get("sound")));
                    if (map.containsKey("volume")) {
                        action.setVolume(Float.parseFloat(String.valueOf(map.get("volume"))));
                    }
                    if (map.containsKey("pitch")) {
                        action.setPitch(Float.parseFloat(String.valueOf(map.get("pitch"))));
                    }
                }
                case MESSAGE -> action.setMessage(String.valueOf(map.get("message")));
                case COMMAND -> {
                    action.setCommand(String.valueOf(map.get("command")));
                    if (map.containsKey("executor")) {
                        action.setExecutor(String.valueOf(map.get("executor")));
                    }
                }
            }

            return action;
        } catch (Exception e) {
            plugin.debug("Failed to parse action: " + typeStr);
            return null;
        }
    }

    public FrameworkObject getObject(String id) {
        return objects.get(id.toLowerCase());
    }

    public Map<String, FrameworkObject> getObjects() {
        return Collections.unmodifiableMap(objects);
    }

    public Collection<FrameworkObject> getAllObjects() {
        return objects.values();
    }

    public Map<String, List<FrameworkObject>> getObjectsByFile() {
        Map<String, List<FrameworkObject>> copy = new LinkedHashMap<>();
        for (Map.Entry<String, List<FrameworkObject>> entry : objectsByFile.entrySet()) {
            copy.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
        }
        return Collections.unmodifiableMap(copy);
    }

    public List<FrameworkObject> getObjectsInFile(String fileKey) {
        return objectsByFile.getOrDefault(fileKey, Collections.emptyList());
    }
}