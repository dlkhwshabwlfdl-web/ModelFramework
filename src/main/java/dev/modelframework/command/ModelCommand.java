package dev.modelframework.command;

import dev.modelframework.ModelFramework;
import dev.modelframework.instance.ObjectInstance;
import dev.modelframework.object.FrameworkObject;
import dev.modelframework.util.TextUtil;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ModelCommand implements CommandExecutor, TabCompleter {

    private final ModelFramework plugin;

    public ModelCommand(ModelFramework plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("modelframework.admin")) {
            sender.sendMessage(TextUtil.colorize(plugin.getMessage("no-permission")));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> handleReload(sender);
            case "gui" -> handleGUI(sender);
            case "give" -> handleGive(sender, args);
            case "debug" -> handleDebug(sender);
            case "inspect" -> handleInspect(sender);
            case "remove" -> handleRemove(sender);
            case "list" -> handleList(sender);
            default -> sendHelp(sender);
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(TextUtil.colorize("&7&m--------&r &bModelFramework &7&m--------"));
        sender.sendMessage(TextUtil.colorize("&b/model reload &7- Reload configuration"));
        sender.sendMessage(TextUtil.colorize("&b/model gui &7- Open object selector"));
        sender.sendMessage(TextUtil.colorize("&b/model give <object> &7- Get object item"));
        sender.sendMessage(TextUtil.colorize("&b/model debug &7- Toggle debug mode"));
        sender.sendMessage(TextUtil.colorize("&b/model inspect &7- Inspect target block"));
        sender.sendMessage(TextUtil.colorize("&b/model remove &7- Remove target instance"));
        sender.sendMessage(TextUtil.colorize("&b/model list &7- List all objects"));
        sender.sendMessage(TextUtil.colorize("&7&m------------------------------"));
    }

    private void handleReload(CommandSender sender) {
        long start = System.currentTimeMillis();
        plugin.reload();
        long time = System.currentTimeMillis() - start;

        String msg = plugin.getMessage("reload").replace("%time%", String.valueOf(time));
        sender.sendMessage(TextUtil.colorize(msg));
    }

    private void handleGUI(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(TextUtil.colorize(plugin.getMessage("no-console")));
            return;
        }
        plugin.getGuiManager().openGUI(player);
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(TextUtil.colorize(plugin.getMessage("no-console")));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(TextUtil.colorize("&cUsage: /model give <object>"));
            return;
        }

        FrameworkObject obj = plugin.getObjectManager().getObject(args[1]);
        if (obj == null) {
            String msg = plugin.getMessage("object-not-found").replace("%object%", args[1]);
            sender.sendMessage(TextUtil.colorize(msg));
            return;
        }

        ItemStack item = plugin.getPlacementManager().createObjectItem(obj);
        player.getInventory().addItem(item);

        String msg = plugin.getMessage("item-given").replace("%object%", obj.getDisplayName());
        player.sendMessage(TextUtil.colorize(msg));
    }

    private void handleDebug(CommandSender sender) {
        boolean newState = !plugin.isDebug();
        plugin.setDebug(newState);

        String key = newState ? "debug-enabled" : "debug-disabled";
        sender.sendMessage(TextUtil.colorize(plugin.getMessage(key)));
    }

    private void handleInspect(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(TextUtil.colorize(plugin.getMessage("no-console")));
            return;
        }

        Block target = player.getTargetBlockExact(5);
        if (target == null) {
            sender.sendMessage(TextUtil.colorize(plugin.getMessage("inspect-none")));
            return;
        }

        ObjectInstance instance = plugin.getInstanceManager().getInstanceAt(target.getLocation());
        if (instance == null) {
            sender.sendMessage(TextUtil.colorize(plugin.getMessage("inspect-none")));
            return;
        }

        FrameworkObject obj = plugin.getObjectManager().getObject(instance.getObjectId());

        sender.sendMessage(TextUtil.colorize(plugin.getMessage("inspect-header")));
        sender.sendMessage(TextUtil.colorize("&7Instance ID: &b" + instance.getId()));
        sender.sendMessage(TextUtil.colorize("&7Object ID: &b" + instance.getObjectId()));
        sender.sendMessage(TextUtil.colorize("&7Model ID: &b"
                + (obj != null ? obj.getModelId() : "N/A")));
        sender.sendMessage(TextUtil.colorize("&7State: &e" + instance.getState()));
        sender.sendMessage(TextUtil.colorize("&7Location: &f" + instance.getWorldName()
                + " " + (int) instance.getX()
                + ", " + (int) instance.getY()
                + ", " + (int) instance.getZ()));
        sender.sendMessage(TextUtil.colorize("&7Tracker: &f"
                + (instance.getTracker() != null ? "Active" : "None")));

        if (obj != null) {
            sender.sendMessage(TextUtil.colorize("&7Animations: &f"
                    + String.join(", ", obj.getAnimations().keySet())));
        }
    }

    private void handleRemove(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(TextUtil.colorize(plugin.getMessage("no-console")));
            return;
        }

        Block target = player.getTargetBlockExact(5);
        if (target == null) {
            sender.sendMessage(TextUtil.colorize(plugin.getMessage("inspect-none")));
            return;
        }

        ObjectInstance instance = plugin.getInstanceManager().getInstanceAt(target.getLocation());
        if (instance == null) {
            sender.sendMessage(TextUtil.colorize(plugin.getMessage("inspect-none")));
            return;
        }

        plugin.getInstanceManager().removeInstance(instance.getId());
        plugin.getInstanceManager().saveAll();

        sender.sendMessage(TextUtil.colorize(plugin.getMessage("removed")));
    }

    private void handleList(CommandSender sender) {
        var objects = plugin.getObjectManager().getObjects();
        if (objects.isEmpty()) {
            sender.sendMessage(TextUtil.colorize("&7No objects defined."));
            return;
        }

        sender.sendMessage(TextUtil.colorize("&7&m--------&r &bRegistered Objects &7&m--------"));
        for (var entry : objects.entrySet()) {
            FrameworkObject obj = entry.getValue();
            sender.sendMessage(TextUtil.colorize("&b" + entry.getKey()
                    + " &7- Model: &f" + obj.getModelId()
                    + " &7- Animations: &f" + obj.getAnimations().size()));
        }
        sender.sendMessage(TextUtil.colorize("&7Total: &b" + objects.size() + " objects"));

        var instances = plugin.getInstanceManager().getInstances();
        sender.sendMessage(TextUtil.colorize("&7Active instances: &b" + instances.size()));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(List.of("reload", "gui", "give", "debug", "inspect", "remove", "list"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            completions.addAll(plugin.getObjectManager().getObjects().keySet());
        }

        String input = args[args.length - 1].toLowerCase();
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(input))
                .collect(Collectors.toList());
    }
}