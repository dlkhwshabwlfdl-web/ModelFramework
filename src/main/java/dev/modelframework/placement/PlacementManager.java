package dev.modelframework.placement;

import dev.modelframework.ModelFramework;
import dev.modelframework.instance.ObjectInstance;
import dev.modelframework.object.FrameworkObject;
import dev.modelframework.util.TextUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class PlacementManager implements Listener {

    private final ModelFramework plugin;

    public static final NamespacedKey OBJECT_KEY =
            new NamespacedKey("modelframework", "model_framework_object");

    public PlacementManager(ModelFramework plugin) {
        this.plugin = plugin;
    }

    public ItemStack createObjectItem(FrameworkObject obj) {
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
        lore.add(TextUtil.colorize("&8ID: " + obj.getId()));
        meta.lore(lore);

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(OBJECT_KEY, PersistentDataType.STRING, obj.getId());

        item.setItemMeta(meta);
        return item;
    }

    public String getObjectIdFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        return meta.getPersistentDataContainer().get(OBJECT_KEY, PersistentDataType.STRING);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();

        String objectId = getObjectIdFromItem(item);
        if (objectId == null) return;

        // Only OP can place framework objects
        if (!player.isOp()) {
            event.setCancelled(true);
            // Remove the item from non-OP
            player.getInventory().setItemInMainHand(null);
            plugin.debug("Non-OP player " + player.getName() + " tried to place framework object");
            return;
        }

        FrameworkObject obj = plugin.getObjectManager().getObject(objectId);
        if (obj == null) {
            event.setCancelled(true);
            return;
        }

        Block block = event.getBlockPlaced();
        Location blockLocation = block.getLocation();

        if (plugin.getInstanceManager().getInstanceAt(blockLocation) != null) {
            event.setCancelled(true);
            return;
        }

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            Block placedBlock = blockLocation.getBlock();

            Location placeLoc = blockLocation.clone().add(0.5, 0, 0.5);
            placeLoc.setYaw(player.getLocation().getYaw());

            ObjectInstance instance = plugin.getInstanceManager().createInstance(
                    objectId, placeLoc
            );

            if (instance == null) {
                placedBlock.setType(org.bukkit.Material.AIR, false);
                player.getInventory().addItem(createObjectItem(obj));
                player.sendMessage(TextUtil.colorize("&cFailed to place object."));
                return;
            }

            String msg = plugin.getMessage("placed").replace("%object%", obj.getDisplayName());
            player.sendMessage(TextUtil.colorize(msg));
        });
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        ObjectInstance instance = plugin.getInstanceManager().getInstanceAt(block.getLocation());
        if (instance == null) return;

        Player player = event.getPlayer();

        if (!player.hasPermission("modelframework.admin")) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);

        FrameworkObject obj = plugin.getObjectManager().getObject(instance.getObjectId());
        plugin.getInstanceManager().removeInstance(instance.getId());
        plugin.getInstanceManager().saveAll();

        if (obj != null) {
            player.getInventory().addItem(createObjectItem(obj));
        }

        player.sendMessage(TextUtil.colorize(plugin.getMessage("removed")));
    }
}