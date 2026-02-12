package dev.oakheart.togglephantoms;

import dev.oakheart.togglephantoms.commands.TogglePhantomsCommand;
import dev.oakheart.togglephantoms.config.ConfigManager;
import dev.oakheart.togglephantoms.listeners.PhantomListener;
import dev.oakheart.togglephantoms.listeners.PlayerListener;
import dev.oakheart.togglephantoms.message.MessageManager;
import dev.oakheart.togglephantoms.storage.Storage;
import dev.oakheart.togglephantoms.storage.MySQLStorage;
import dev.oakheart.togglephantoms.storage.SQLiteStorage;
import dev.oakheart.togglephantoms.storage.YamlStorage;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.logging.Level;

public final class TogglePhantoms extends JavaPlugin {

    private ConfigManager configManager;
    private Storage storage;
    private MessageManager messageManager;

    @Override
    public void onEnable() {
        try {
            initializeComponents();
            registerListeners();
            registerCommands();
            initializeMetrics();
            registerPlaceholders();

            getLogger().info("TogglePhantoms has been enabled!");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to enable TogglePhantoms", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (storage != null) {
            storage.close();
        }
        getLogger().info("TogglePhantoms has been disabled!");
    }

    private void initializeComponents() {
        configManager = new ConfigManager(this);
        configManager.load();

        messageManager = new MessageManager();
        messageManager.load(configManager.getConfig());

        initStorage();
    }

    private void initStorage() {
        switch (configManager.getStorageType()) {
            case "mysql":
                storage = new MySQLStorage(this,
                        configManager.getMysqlHost(),
                        configManager.getMysqlPort(),
                        configManager.getMysqlDatabase(),
                        configManager.getMysqlUsername(),
                        configManager.getMysqlPassword());
                getLogger().info("Using MySQL storage.");
                break;
            case "sqlite":
                storage = new SQLiteStorage(this);
                getLogger().info("Using SQLite storage.");
                break;
            case "yaml":
            default:
                storage = new YamlStorage(this);
                getLogger().info("Using YAML storage.");
                break;
        }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PhantomListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        // Load online players into cache (in case of reload)
        Bukkit.getOnlinePlayers().forEach(player -> storage.loadPlayer(player.getUniqueId()));
    }

    private void registerCommands() {
        new TogglePhantomsCommand(this).register();
    }

    private void initializeMetrics() {
        int pluginId = 29484;
        new Metrics(this, pluginId);
    }

    private void registerPlaceholders() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PhantomsPlaceholderExpansion(this).register();
            getLogger().info("PlaceholderAPI found! Registered placeholders.");
        }
    }

    public boolean reloadPlugin() {
        if (!configManager.reload()) {
            return false;
        }
        messageManager.load(configManager.getConfig());
        if (storage != null) {
            storage.close();
        }
        initStorage();
        Bukkit.getOnlinePlayers().forEach(player -> storage.loadPlayer(player.getUniqueId()));
        return true;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public Storage getStorage() {
        return storage;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public boolean arePhantomsDisabled(UUID uuid) {
        return storage.arePhantomsDisabled(uuid);
    }

    public boolean togglePhantoms(UUID uuid) {
        boolean currentlyDisabled = storage.arePhantomsDisabled(uuid);
        storage.setPhantomsDisabled(uuid, !currentlyDisabled);
        return !currentlyDisabled;
    }

    public void setPhantomsDisabled(UUID uuid, boolean disabled) {
        storage.setPhantomsDisabled(uuid, disabled);
    }
}
