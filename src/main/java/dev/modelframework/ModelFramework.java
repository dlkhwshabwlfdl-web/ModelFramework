package dev.modelframework;

import dev.modelframework.action.ActionManager;
import dev.modelframework.animation.AnimationManager;
import dev.modelframework.command.ModelCommand;
import dev.modelframework.condition.ConditionManager;
import dev.modelframework.gui.GUIManager;
import dev.modelframework.hook.BetterModelHook;
import dev.modelframework.instance.InstanceManager;
import dev.modelframework.interaction.InteractionManager;
import dev.modelframework.item.ItemProvider;
import dev.modelframework.item.OraxenProvider;
import dev.modelframework.item.VanillaProvider;
import dev.modelframework.object.ObjectManager;
import dev.modelframework.placement.ItemSecurityListener;
import dev.modelframework.placement.PlacementManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public final class ModelFramework extends JavaPlugin {

    private static ModelFramework instance;

    private BetterModelHook betterModelHook;
    private ObjectManager objectManager;
    private InstanceManager instanceManager;
    private PlacementManager placementManager;
    private GUIManager guiManager;
    private InteractionManager interactionManager;
    private AnimationManager animationManager;
    private ConditionManager conditionManager;
    private ActionManager actionManager;

    private final Map<String, ItemProvider> itemProviders = new HashMap<>();

    private boolean debug = false;
    private boolean oraxenEnabled = false;
    private boolean placeholderApiEnabled = false;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        saveDefaultObjects();

        debug = getConfig().getBoolean("debug", false);

        if (!Bukkit.getPluginManager().isPluginEnabled("BetterModel")) {
            getLogger().severe("BetterModel not found! Disabling ModelFramework...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        Bukkit.getScheduler().runTask(this, this::finishEnable);
    }

    private void finishEnable() {
        betterModelHook = new BetterModelHook(this);
        if (!betterModelHook.initialize()) {
            getLogger().severe("Failed to hook into BetterModel! Disabling...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        oraxenEnabled = Bukkit.getPluginManager().isPluginEnabled("Oraxen");
        placeholderApiEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");

        if (oraxenEnabled) getLogger().info("Oraxen found!");
        if (placeholderApiEnabled) getLogger().info("PlaceholderAPI found!");

        registerItemProviders();

        objectManager = new ObjectManager(this);
        instanceManager = new InstanceManager(this);
        conditionManager = new ConditionManager(this);
        actionManager = new ActionManager(this);
        animationManager = new AnimationManager(this);
        interactionManager = new InteractionManager(this);
        placementManager = new PlacementManager(this);
        guiManager = new GUIManager(this);

        objectManager.loadAll();
        instanceManager.loadAll();

        // Register listeners ONCE
        Bukkit.getPluginManager().registerEvents(interactionManager, this);
        Bukkit.getPluginManager().registerEvents(placementManager, this);
        Bukkit.getPluginManager().registerEvents(guiManager, this);
        Bukkit.getPluginManager().registerEvents(new ItemSecurityListener(this), this);
        Bukkit.getPluginManager().registerEvents(new dev.modelframework.listener.ChunkListener(this), this);

        ModelCommand modelCommand = new ModelCommand(this);
        var cmd = getCommand("model");
        if (cmd != null) {
            cmd.setExecutor(modelCommand);
            cmd.setTabCompleter(modelCommand);
        }

        int autoSave = getConfig().getInt("storage.auto-save", 5);
        if (autoSave > 0) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(this,
                    () -> instanceManager.saveAll(),
                    autoSave * 60L * 20L,
                    autoSave * 60L * 20L
            );
        }

        // Clean old entities then respawn
        Bukkit.getScheduler().runTaskLater(this, () -> {
            instanceManager.cleanOldEntities();
            instanceManager.respawnAll();
            getLogger().info("ModelFramework v" + getDescription().getVersion() + " enabled!");
            getLogger().info("Loaded " + objectManager.getObjects().size() + " objects, "
                    + instanceManager.getInstances().size() + " instances.");
        }, 100L);
    }

    @Override
    public void onDisable() {
        if (instanceManager != null) {
            instanceManager.despawnAll();
            instanceManager.saveAll();
        }
        getLogger().info("ModelFramework disabled.");
    }

    private void saveDefaultObjects() {
        File objectsDir = new File(getDataFolder(), "objects");
        if (!objectsDir.exists()) {
            objectsDir.mkdirs();
            saveResource("objects/examples.yml", false);
        }
    }

    private void registerItemProviders() {
        itemProviders.put("vanilla", new VanillaProvider());
        if (oraxenEnabled) {
            itemProviders.put("oraxen", new OraxenProvider());
        }
    }

    public void reload() {
        reloadConfig();
        debug = getConfig().getBoolean("debug", false);

        objectManager.loadAll();
        instanceManager.saveAll();
        instanceManager.despawnAll();

        Bukkit.getScheduler().runTaskLater(this, () -> {
            instanceManager.cleanOldEntities();
            instanceManager.respawnAll();
            getLogger().info("Reload complete. " + instanceManager.getInstances().size() + " instances respawned.");
        }, 20L);
    }

    public static ModelFramework getInstance() { return instance; }
    public BetterModelHook getBetterModelHook() { return betterModelHook; }
    public ObjectManager getObjectManager() { return objectManager; }
    public InstanceManager getInstanceManager() { return instanceManager; }
    public PlacementManager getPlacementManager() { return placementManager; }
    public GUIManager getGuiManager() { return guiManager; }
    public InteractionManager getInteractionManager() { return interactionManager; }
    public AnimationManager getAnimationManager() { return animationManager; }
    public ConditionManager getConditionManager() { return conditionManager; }
    public ActionManager getActionManager() { return actionManager; }

    public ItemProvider getItemProvider(String name) {
        return itemProviders.get(name.toLowerCase());
    }

    public Map<String, ItemProvider> getItemProviders() { return itemProviders; }
    public boolean isDebug() { return debug; }
    public void setDebug(boolean debug) { this.debug = debug; }
    public boolean isOraxenEnabled() { return oraxenEnabled; }
    public boolean isPlaceholderApiEnabled() { return placeholderApiEnabled; }

    public void debug(String message) {
        if (debug) {
            getLogger().log(Level.INFO, "[DEBUG] " + message);
        }
    }

    public String getMessage(String key) {
        String prefix = getConfig().getString("messages.prefix", "&7[&bModelFW&7] ");
        String msg = getConfig().getString("messages." + key, "&cMissing message: " + key);
        return prefix + msg;
    }
}