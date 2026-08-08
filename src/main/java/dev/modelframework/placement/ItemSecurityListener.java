package dev.modelframework.placement;

import dev.modelframework.ModelFramework;
import dev.modelframework.util.TextUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import static dev.modelframework.placement.PlacementManager.OBJECT_KEY;

import org.bukkit.persistence.PersistentDataType;

public class ItemSecurityListener implements Listener {

    private final ModelFramework plugin;

    public ItemSecurityListener(ModelFramework plugin) {
        this.plugin = plugin;
    }

    /**
     * When non-OP player joins, remove framework items
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.isOp()) return;

        // Delay 1 tick to ensure inventory is loaded
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;
                removeFrameworkItems(player);
            }
        }.runTaskLater(plugin, 5L);
    }

    /**
     * Prevent non-OP from picking up framework items
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.isOp()) return;

        ItemStack item = event.getItem().getItemStack();
        if (isFrameworkItem(item)) {
            event.setCancelled(true);
            event.getItem().remove();
            plugin.debug("Removed dropped framework item near " + player.getName());
        }
    }

    /**
     * Prevent non-OP from dropping framework items (remove instead)
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (player.isOp()) return;

        ItemStack item = event.getItemDrop().getItemStack();
        if (isFrameworkItem(item)) {
            event.getItemDrop().remove();
            plugin.debug("Removed framework item dropped by " + player.getName());
        }
    }

    /**
     * Periodic check: if non-OP holds framework item, remove it
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.isOp()) return;

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (isFrameworkItem(mainHand)) {
            player.getInventory().setItemInMainHand(null);
            plugin.debug("Removed framework item from " + player.getName() + " main hand");
        }

        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (isFrameworkItem(offHand)) {
            player.getInventory().setItemInOffHand(null);
            plugin.debug("Removed framework item from " + player.getName() + " off hand");
        }
    }

    /**
     * Prevent non-OP from moving framework items in inventory
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (player.isOp()) return;

        ItemStack current = event.getCurrentItem();
        if (isFrameworkItem(current)) {
            event.setCancelled(true);
            event.setCurrentItem(null);
            plugin.debug("Removed framework item from " + player.getName() + " inventory click");
        }

        ItemStack cursor = event.getCursor();
        if (isFrameworkItem(cursor)) {
            event.setCancelled(true);
            player.setItemOnCursor(null);
            plugin.debug("Removed framework item from " + player.getName() + " cursor");
        }
    }

    /**
     * Remove all framework items from non-OP player inventory
     */
    private void removeFrameworkItems(Player player) {
        if (player.isOp()) return;

        PlayerInventory inv = player.getInventory();
        boolean removed = false;

        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (isFrameworkItem(item)) {
                inv.setItem(i, null);
                removed = true;
            }
        }

        if (removed) {
            plugin.debug("Cleaned framework items from " + player.getName() + "'s inventory");
        }
    }

    /**
     * Check if item is a framework object item
     */
    private boolean isFrameworkItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        if (item.getItemMeta() == null) return false;

        return item.getItemMeta().getPersistentDataContainer()
                .has(OBJECT_KEY, PersistentDataType.STRING);
    }
}