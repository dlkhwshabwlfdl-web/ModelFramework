package dev.modelframework.item;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class VanillaProvider implements ItemProvider {

    @Override
    public String getName() {
        return "vanilla";
    }

    @Override
    public boolean hasItem(Player player, String itemId) {
        try {
            Material material = Material.valueOf(itemId.toUpperCase());
            return player.getInventory().contains(material);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public boolean hasItemInHand(Player player, String itemId) {
        try {
            Material material = Material.valueOf(itemId.toUpperCase());
            ItemStack mainHand = player.getInventory().getItemInMainHand();
            if (mainHand.getType() == material) return true;

            ItemStack offHand = player.getInventory().getItemInOffHand();
            return offHand.getType() == material;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public ItemStack createItem(String itemId) {
        try {
            Material material = Material.valueOf(itemId.toUpperCase());
            return new ItemStack(material);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public void consumeItem(Player player, String itemId, int amount) {
        try {
            Material material = Material.valueOf(itemId.toUpperCase());
            PlayerInventory inv = player.getInventory();
            int remaining = amount;

            for (int i = 0; i < inv.getSize() && remaining > 0; i++) {
                ItemStack item = inv.getItem(i);
                if (item == null || item.getType() != material) continue;

                if (item.getAmount() <= remaining) {
                    remaining -= item.getAmount();
                    inv.setItem(i, null);
                } else {
                    item.setAmount(item.getAmount() - remaining);
                    remaining = 0;
                }
            }
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Override
    public void consumeItemInHand(Player player, String itemId, int amount) {
        try {
            Material material = Material.valueOf(itemId.toUpperCase());

            ItemStack mainHand = player.getInventory().getItemInMainHand();
            if (mainHand.getType() == material) {
                if (mainHand.getAmount() <= amount) {
                    player.getInventory().setItemInMainHand(null);
                } else {
                    mainHand.setAmount(mainHand.getAmount() - amount);
                }
                return;
            }

            ItemStack offHand = player.getInventory().getItemInOffHand();
            if (offHand.getType() == material) {
                if (offHand.getAmount() <= amount) {
                    player.getInventory().setItemInOffHand(null);
                } else {
                    offHand.setAmount(offHand.getAmount() - amount);
                }
            }
        } catch (IllegalArgumentException ignored) {
        }
    }

}