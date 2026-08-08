package dev.modelframework.util;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class TextUtil {

    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.legacyAmpersand();

    private TextUtil() {
    }

    public static Component colorize(String text) {
        if (text == null) return Component.empty();
        return LEGACY.deserialize(text);
    }

    public static String replacePlaceholders(Player player, String text) {
        if (text == null) return "";
        text = text.replace("%player%", player.getName());

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            text = PlaceholderAPI.setPlaceholders(player, text);
        }

        return text;
    }

    public static Component colorize(Player player, String text) {
        return colorize(replacePlaceholders(player, text));
    }
}