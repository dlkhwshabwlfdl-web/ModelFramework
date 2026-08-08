package dev.modelframework.condition;

import dev.modelframework.ModelFramework;
import dev.modelframework.instance.ObjectInstance;
import dev.modelframework.item.ItemProvider;
import dev.modelframework.object.ObjectAnimation;
import dev.modelframework.util.TextUtil;
import org.bukkit.entity.Player;

public class ConditionManager {

    private final ModelFramework plugin;

    public ConditionManager(ModelFramework plugin) {
        this.plugin = plugin;
    }

    public boolean checkAll(Player player, ObjectInstance instance, ObjectAnimation animation) {
        if (animation.getPermission() != null && !animation.getPermission().isEmpty()) {
            if (!player.hasPermission(animation.getPermission())) {
                player.sendMessage(TextUtil.colorize(plugin.getMessage("no-permission")));
                return false;
            }
        }

        return true;
    }

    public boolean checkItem(Player player, ObjectAnimation animation) {
        if (!animation.isItemRequired()) return true;

        String providerName = animation.getItemProvider();
        String itemId = animation.getItemId();
        if (providerName == null || itemId == null) return true;

        ItemProvider provider = plugin.getItemProvider(providerName);
        if (provider == null) {
            plugin.debug("Item provider '" + providerName + "' not found.");
            return false;
        }

        boolean isHand = "HAND".equalsIgnoreCase(animation.getItemCheck());

        if (isHand) {
            return provider.hasItemInHand(player, itemId);
        } else {
            return provider.hasItem(player, itemId);
        }
    }

    public void consumeItem(Player player, ObjectAnimation animation) {
        if (!animation.isItemRequired() || !animation.isItemConsume()) return;

        String providerName = animation.getItemProvider();
        String itemId = animation.getItemId();
        if (providerName == null || itemId == null) return;

        ItemProvider provider = plugin.getItemProvider(providerName);
        if (provider == null) return;

        boolean isHand = "HAND".equalsIgnoreCase(animation.getItemCheck());

        if (isHand) {
            provider.consumeItemInHand(player, itemId, 1);
        } else {
            provider.consumeItem(player, itemId, 1);
        }
    }
}