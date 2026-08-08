package dev.modelframework.item;

import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.items.ItemBuilder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Locale;

public class OraxenProvider implements ItemProvider {

    @Override
    public String getName() {
        return "oraxen";
    }

    @Override
    public boolean hasItem(Player player, String itemId) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (matches(item, itemId)) return true;
        }
        return false;
    }

    @Override
    public boolean hasItemInHand(Player player, String itemId) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (matches(mainHand, itemId)) return true;

        ItemStack offHand = player.getInventory().getItemInOffHand();
        return matches(offHand, itemId);
    }

    @Override
    public void consumeItem(Player player, String itemId, int amount) {
        PlayerInventory inv = player.getInventory();
        int remaining = amount;

        for (int i = 0; i < inv.getSize() && remaining > 0; i++) {
            ItemStack item = inv.getItem(i);
            if (!matches(item, itemId)) continue;

            if (item.getAmount() <= remaining) {
                remaining -= item.getAmount();
                inv.setItem(i, null);
            } else {
                item.setAmount(item.getAmount() - remaining);
                remaining = 0;
            }
        }
    }

    @Override
    public void consumeItemInHand(Player player, String itemId, int amount) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (matches(mainHand, itemId)) {
            if (mainHand.getAmount() <= amount) {
                player.getInventory().setItemInMainHand(null);
            } else {
                mainHand.setAmount(mainHand.getAmount() - amount);
            }
            return;
        }

        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (matches(offHand, itemId)) {
            if (offHand.getAmount() <= amount) {
                player.getInventory().setItemInOffHand(null);
            } else {
                offHand.setAmount(offHand.getAmount() - amount);
            }
        }
    }

    @Override
    public ItemStack createItem(String itemId) {
        ItemBuilder builder = OraxenItems.getItemById(itemId);
        return builder != null ? builder.build() : null;
    }

    private boolean matches(ItemStack item, String wanted) {
        if (item == null || wanted == null || wanted.isBlank()) return false;

        String oraxenId = OraxenItems.getIdByItem(item);
        if (oraxenId == null) return false;

        if (oraxenId.equals(wanted)) return true;

        return normalize(oraxenId).equals(normalize(wanted));
    }

    private String normalize(String input) {
        return input == null ? "" : input
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "");
    }
}