package dev.oakheart.togglephantoms;

import dev.oakheart.togglephantoms.commands.TogglePhantomsCommand;
import dev.oakheart.togglephantoms.config.ConfigManager;
import dev.oakheart.togglephantoms.listeners.PhantomListener;
import dev.oakheart.togglephantoms.listeners.PlayerListener;
import dev.oakheart.message.MessageManager;
import dev.oakheart.togglephantoms.placeholder.PhantomsPlaceholderExpansion;
import dev.oakheart.togglephantoms.storage.MySQLStorage;
import dev.oakheart.togglephantoms.storage.SQLiteStorage;
import dev.oakheart.togglephantoms.storage.Storage;
import dev.oakheart.togglephantoms.storage.YamlStorage;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.logging.Level;

public final class TogglePhantoms extends JavaPlugin {

    private ConfigManager configManager;
    private MessageManager messageManager;
    private Storage storage;

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

        messageManager = new MessageManager(this, getLogger());
        messageManager.load();

        storage = createStorage(configManager.getStorageType());
    }

    private Storage createStorage(String type) {
        return switch (type) {
            case "mysql" -> {
                getLogger().info("Using MySQL storage.");
                yield new MySQLStorage(this,
                        configManager.getMysqlHost(),
                        configManager.getMysqlPort(),
                        configManager.getMysqlDatabase(),
                        configManager.getMysqlUsername(),
                        configManager.getMysqlPassword());
            }
            case "sqlite" -> {
                getLogger().info("Using SQLite storage.");
                yield new SQLiteStorage(this);
            }
            default -> {
                getLogger().info("Using YAML storage.");
                yield new YamlStorage(this);
            }
        };
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PhantomListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        // Load online players into cache (in case of server reload)
        Bukkit.getOnlinePlayers().forEach(player -> storage.loadPlayer(player.getUniqueId()));
    }

    private void registerCommands() {
        new TogglePhantomsCommand(this).register();
    }

    private void initializeMetrics() {
        new Metrics(this, 29484);
    }

    private void registerPlaceholders() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PhantomsPlaceholderExpansion(this).register();
            getLogger().info("PlaceholderAPI found! Registered placeholders.");
        }
    }

    public boolean reloadPlugin() {
        String oldStorageType = configManager.getStorageType();

        if (!configManager.reload()) {
            return false;
        }

        messageManager.reload();

        String newStorageType = configManager.getStorageType();
        if (!oldStorageType.equals(newStorageType)) {
            if (storage != null) {
                storage.close();
            }
            storage = createStorage(newStorageType);
        }

        // Refresh cache for online players
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
