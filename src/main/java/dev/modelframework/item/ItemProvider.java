package dev.modelframework.item;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface ItemProvider {

    String getName();

    boolean hasItem(Player player, String itemId);

    boolean hasItemInHand(Player player, String itemId);

    void consumeItem(Player player, String itemId, int amount);

    void consumeItemInHand(Player player, String itemId, int amount);

    ItemStack createItem(String itemId);
}