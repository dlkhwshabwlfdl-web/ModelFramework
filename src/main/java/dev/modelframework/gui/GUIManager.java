package dev.modelframework.gui;

import dev.modelframework.ModelFramework;
import dev.modelframework.object.FrameworkObject;
import dev.modelframework.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dev.modelframework.placement.PlacementManager.OBJECT_KEY;

public class GUIManager implements Listener {

    private final ModelFramework plugin;
    private final NamespacedKey categoryKey;
    private final NamespacedKey backKey;

    public GUIManager(ModelFramework plugin) {
        this.plugin = plugin;
        this.categoryKey = new NamespacedKey(plugin, "mf_gui_category");
        this.backKey = new NamespacedKey(plugin, "mf_gui_back");
    }

    public void openGUI(Player player) {
        String title = plugin.getConfig().getString("gui.title", "&8Model Categories");
        int size = getGuiSize();

        Inventory gui = Bukkit.createInventory(new RootHolder(), size, TextUtil.colorize(title));

        int slot = 0;
        for (Map.Entry<String, List<FrameworkObject>> entry : plugin.getObjectManager().getObjectsByFile().entrySet()) {
            String category = entry.getKey();
            List<FrameworkObject> objects = entry.getValue();
            if (objects.isEmpty()) continue;
            if (slot >= size) break;

            gui.setItem(slot++, createCategoryItem(category, objects));
        }

        player.openInventory(gui);
    }

    public void openCategoryGUI(Player player, String category) {
        String title = plugin.getConfig().getString("gui.category-title", "&8Objects: %category%");
        title = title.replace("%category%", prettify(category));

        int size = getGuiSize();
        Inventory gui = Bukkit.createInventory(new CategoryHolder(category), size, TextUtil.colorize(title));

        List<FrameworkObject> objects = plugin.getObjectManager().getObjectsInFile(category);
        for (FrameworkObject obj : objects) {
            int slot = obj.getDisplaySlot();
            if (slot < 0 || slot >= size) continue;

            gui.setItem(slot, createObjectDisplayItem(obj));
        }

        // Back button
        if (size > 0) {
            gui.setItem(size - 1, createBackItem());
        }

        player.openInventory(gui);
    }

    private int getGuiSize() {
        int size = plugin.getConfig().getInt("gui.size", 54);
        size = Math.min(54, Math.max(9, size));
        if (size % 9 != 0) size = 54;
        return size;
    }

    private ItemStack createCategoryItem(String category, List<FrameworkObject> objects) {
        FrameworkObject first = objects.getFirst();

        ItemStack item = new ItemStack(first.getDisplayMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.displayName(TextUtil.colorize("&e" + prettify(category)));

        List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
        lore.add(TextUtil.colorize("&7Click to open this category"));
        lore.add(TextUtil.colorize("&7Objects: &f" + objects.size()));
        lore.add(TextUtil.colorize("&8File: " + category + ".yml"));
        meta.lore(lore);

        meta.getPersistentDataContainer().set(categoryKey, PersistentDataType.STRING, category);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createObjectDisplayItem(FrameworkObject obj) {
        ItemStack item = new ItemStack(obj.getDisplayMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.displayName(TextUtil.colorize(obj.getDisplayName()));

        List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
        if (obj.getDisplayLore() != null) {
            for (String line : obj.getDisplayLore()) {
                lore.add(TextUtil.colorize(line));
            }
        }
        lore.add(TextUtil.colorize(""));
        lore.add(TextUtil.colorize("&eClick to receive"));
        lore.add(TextUtil.colorize("&8ID: " + obj.getId()));
        meta.lore(lore);

        meta.getPersistentDataContainer().set(OBJECT_KEY, PersistentDataType.STRING, obj.getId());
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createBackItem() {
        ItemStack item = new ItemStack(org.bukkit.Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.displayName(TextUtil.colorize("&cBack"));
        meta.lore(List.of(TextUtil.colorize("&7Return to categories")));
        meta.getPersistentDataContainer().set(backKey, PersistentDataType.BYTE, (byte) 1);

        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (top == null) return;

        InventoryHolder holder = top.getHolder();
        boolean root = holder instanceof RootHolder;
        boolean category = holder instanceof CategoryHolder;

        if (!root && !category) return;

        // Prevent moving items in/out of custom GUI
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() == null) return;
        if (event.getClickedInventory() != top) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        if (root) {
            String selectedCategory = pdc.get(categoryKey, PersistentDataType.STRING);
            if (selectedCategory == null) return;

            Bukkit.getScheduler().runTask(plugin, () -> openCategoryGUI(player, selectedCategory));
            return;
        }

        if (category) {
            Byte back = pdc.get(backKey, PersistentDataType.BYTE);
            if (back != null && back == (byte) 1) {
                Bukkit.getScheduler().runTask(plugin, () -> openGUI(player));
                return;
            }

            String objectId = pdc.get(OBJECT_KEY, PersistentDataType.STRING);
            if (objectId == null) return;

            FrameworkObject obj = plugin.getObjectManager().getObject(objectId);
            if (obj == null) return;

            ItemStack objectItem = plugin.getPlacementManager().createObjectItem(obj);

            Map<Integer, ItemStack> leftover = player.getInventory().addItem(objectItem);
            if (!leftover.isEmpty()) {
                leftover.values().forEach(stack ->
                        player.getWorld().dropItemNaturally(player.getLocation(), stack)
                );
            }

            // GUI intentionally stays open
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (top == null) return;

        InventoryHolder holder = top.getHolder();
        if (holder instanceof RootHolder || holder instanceof CategoryHolder) {
            event.setCancelled(true);
        }
    }

    private String prettify(String raw) {
        if (raw == null || raw.isBlank()) return "Unknown";

        String[] split = raw.replace("-", "_").split("_");
        StringBuilder builder = new StringBuilder();

        for (String part : split) {
            if (part.isBlank()) continue;
            if (!builder.isEmpty()) builder.append(" ");
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1).toLowerCase());
            }
        }

        return builder.toString();
    }

    private static final class RootHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private record CategoryHolder(String category) implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}