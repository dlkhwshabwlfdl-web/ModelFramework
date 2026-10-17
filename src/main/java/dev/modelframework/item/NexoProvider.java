package dev.modelframework.item;

import com.nexomc.nexo.api.NexoItems;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;

public class NexoProvider implements ItemProvider {

    @Override
    public boolean hasItem(Player player, String itemId) {
        if (itemId == null || itemId.isEmpty()) return true;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null) {
                String id = NexoItems.idFromItem(item);
                if (itemId.equals(id)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean hasItemInHand(Player player, String itemId) {
        if (itemId == null || itemId.isEmpty()) return true;

        ItemStack main = player.getInventory().getItemInMainHand();
        String mainId = NexoItems.idFromItem(main);
        if (itemId.equals(mainId)) return true;

        ItemStack off = player.getInventory().getItemInOffHand();
        String offId = NexoItems.idFromItem(off);
        return itemId.equals(offId);
    }

    @Override
    public ItemStack createItem(String itemId) {
        if (itemId == null || itemId.isEmpty()) return null;
        var builder = com.nexomc.nexo.api.NexoItems.itemFromId(itemId);
        return builder != null ? builder.build() : null;
    }

    @Override
    public void consumeItem(Player player, String itemId, int amount) {
        if (itemId == null || itemId.isEmpty()) return;

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null) {
                String id = NexoItems.idFromItem(item);
                if (itemId.equals(id)) {
                    int toTake = Math.min(amount, item.getAmount());
                    item.setAmount(item.getAmount() - toTake);
                    amount -= toTake;
                    if (amount <= 0) return;
                }
            }
        }
    }

    @Override
    public void consumeItemInHand(Player player, String itemId, int amount) {
        if (itemId == null || itemId.isEmpty()) return;

        ItemStack main = player.getInventory().getItemInMainHand();
        String mainId = NexoItems.idFromItem(main);
        if (itemId.equals(mainId)) {
            int toTake = Math.min(amount, main.getAmount());
            main.setAmount(main.getAmount() - toTake);
            amount -= toTake;
            if (amount <= 0) return;
        }

        ItemStack off = player.getInventory().getItemInOffHand();
        String offId = NexoItems.idFromItem(off);
        if (itemId.equals(offId)) {
            int toTake = Math.min(amount, off.getAmount());
            off.setAmount(off.getAmount() - toTake);
        }
    }

    @Override
    public String getName() {
        return "nexo";
    }
}
