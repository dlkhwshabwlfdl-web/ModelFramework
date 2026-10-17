package dev.modelframework.item;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;

public class ItemsAdderProvider implements ItemProvider {

    @Override
    public boolean hasItem(Player player, String itemId) {
        if (itemId == null || itemId.isEmpty()) return true;
        CustomStack customStack = CustomStack.getInstance(itemId);
        if (customStack == null) return false;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && customStack.isItem(item)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasItemInHand(Player player, String itemId) {
        if (itemId == null || itemId.isEmpty()) return true;
        CustomStack customStack = CustomStack.getInstance(itemId);
        if (customStack == null) return false;

        ItemStack main = player.getInventory().getItemInMainHand();
        ItemStack off = player.getInventory().getItemInOffHand();

        return customStack.isItem(main) || customStack.isItem(off);
    }

    @Override
    public ItemStack createItem(String itemId) {
        if (itemId == null || itemId.isEmpty()) return null;
        CustomStack customStack = CustomStack.getInstance(itemId);
        if (customStack == null) return null;
        return customStack.getItemStack();
    }

    @Override
    public void consumeItem(Player player, String itemId, int amount) {
        if (itemId == null || itemId.isEmpty()) return;
        CustomStack customStack = CustomStack.getInstance(itemId);
        if (customStack == null) return;

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && customStack.isItem(item)) {
                int toTake = Math.min(amount, item.getAmount());
                item.setAmount(item.getAmount() - toTake);
                amount -= toTake;
                if (amount <= 0) return;
            }
        }
    }

    @Override
    public void consumeItemInHand(Player player, String itemId, int amount) {
        if (itemId == null || itemId.isEmpty()) return;
        CustomStack customStack = CustomStack.getInstance(itemId);
        if (customStack == null) return;

        ItemStack main = player.getInventory().getItemInMainHand();
        if (customStack.isItem(main)) {
            int toTake = Math.min(amount, main.getAmount());
            main.setAmount(main.getAmount() - toTake);
            amount -= toTake;
            if (amount <= 0) return;
        }

        ItemStack off = player.getInventory().getItemInOffHand();
        if (customStack.isItem(off)) {
            int toTake = Math.min(amount, off.getAmount());
            off.setAmount(off.getAmount() - toTake);
        }
    }

    @Override
    public String getName() {
        return "itemsadder";
    }
}
