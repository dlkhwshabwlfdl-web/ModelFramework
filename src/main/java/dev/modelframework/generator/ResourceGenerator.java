package dev.modelframework.generator;

import dev.modelframework.ModelFramework;
import dev.modelframework.object.FrameworkObject;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ResourceGenerator {

    private final ModelFramework plugin;

    public ResourceGenerator(ModelFramework plugin) {
        this.plugin = plugin;
    }

    public void generateForOraxen() {
        File oraxenFolder = new File(plugin.getDataFolder().getParentFile(), "Oraxen/items/modelframework");
        if (!oraxenFolder.exists()) {
            oraxenFolder.mkdirs();
        }

        File file = new File(oraxenFolder, "generated_objects.yml");
        YamlConfiguration config = new YamlConfiguration();

        for (FrameworkObject obj : plugin.getObjectManager().getAllObjects()) {
            String id = obj.getId();
            config.set(id + ".displayname", obj.getDisplayName());
            config.set(id + ".material", obj.getDisplayMaterial().name());
            if (obj.getDisplayLore() != null && !obj.getDisplayLore().isEmpty()) {
                config.set(id + ".lore", obj.getDisplayLore());
            }
            config.set(id + ".Pack.custom_model_data", obj.getDisplaySlot()); // Mock logic
        }

        try {
            config.save(file);
            plugin.getLogger().info("Oraxen items generated successfully at " + file.getPath());
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to generate Oraxen items: " + e.getMessage());
        }
    }

    public void generateForItemsAdder() {
        File itemsAdderFolder = new File(plugin.getDataFolder().getParentFile(), "ItemsAdder/data/items_packs/modelframework");
        if (!itemsAdderFolder.exists()) {
            itemsAdderFolder.mkdirs();
        }

        File file = new File(itemsAdderFolder, "generated_objects.yml");
        YamlConfiguration config = new YamlConfiguration();

        for (FrameworkObject obj : plugin.getObjectManager().getAllObjects()) {
            String id = obj.getId();
            String path = "info.namespace.modelframework.items." + id;
            config.set(path + ".display_name", obj.getDisplayName());
            config.set(path + ".resource.material", obj.getDisplayMaterial().name());
            if (obj.getDisplayLore() != null && !obj.getDisplayLore().isEmpty()) {
                config.set(path + ".lore", obj.getDisplayLore());
            }
        }

        try {
            config.save(file);
            plugin.getLogger().info("ItemsAdder items generated successfully at " + file.getPath());
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to generate ItemsAdder items: " + e.getMessage());
        }
    }

    public void generateForNexo() {
        File nexoFolder = new File(plugin.getDataFolder().getParentFile(), "Nexo/items/modelframework");
        if (!nexoFolder.exists()) {
            nexoFolder.mkdirs();
        }

        File file = new File(nexoFolder, "generated_objects.yml");
        YamlConfiguration config = new YamlConfiguration();

        for (FrameworkObject obj : plugin.getObjectManager().getAllObjects()) {
            String id = obj.getId();
            config.set(id + ".display_name", obj.getDisplayName());
            config.set(id + ".material", obj.getDisplayMaterial().name());
            if (obj.getDisplayLore() != null && !obj.getDisplayLore().isEmpty()) {
                config.set(id + ".lore", obj.getDisplayLore());
            }
        }

        try {
            config.save(file);
            plugin.getLogger().info("Nexo items generated successfully at " + file.getPath());
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to generate Nexo items: " + e.getMessage());
        }
    }

    public void generateAll() {
        if (plugin.isOraxenEnabled()) generateForOraxen();
        if (plugin.isNexoEnabled()) generateForNexo();
        if (plugin.isItemsAdderEnabled()) generateForItemsAdder();
    }
}
