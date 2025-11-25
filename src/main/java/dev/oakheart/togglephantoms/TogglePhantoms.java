package dev.oakheart.togglephantoms;

import dev.oakheart.togglephantoms.commands.TogglePhantomsCommand;
import dev.oakheart.togglephantoms.listeners.PhantomListener;
import dev.oakheart.togglephantoms.listeners.PlayerListener;
import dev.oakheart.togglephantoms.storage.Storage;
import dev.oakheart.togglephantoms.storage.MySQLStorage;
import dev.oakheart.togglephantoms.storage.SQLiteStorage;
import dev.oakheart.togglephantoms.storage.YamlStorage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public final class TogglePhantoms extends JavaPlugin {

    private static TogglePhantoms instance;
    private Storage storage;
    private Messages messages;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config
        saveDefaultConfig();

        // Load messages
        messages = new Messages();
        messages.load(getConfig());

        // Initialize storage based on config
        initStorage();

        // Register listeners
        getServer().getPluginManager().registerEvents(new PhantomListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        // Load online players into cache (in case of reload)
        Bukkit.getOnlinePlayers().forEach(player -> storage.loadPlayer(player.getUniqueId()));

        // Register commands
        new TogglePhantomsCommand(this).register();

        // Register PlaceholderAPI expansion if available
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PhantomsPlaceholderExpansion(this).register();
            getLogger().info("PlaceholderAPI found! Registered placeholders.");
        }

        getLogger().info("TogglePhantoms has been enabled!");
    }

    @Override
    public void onDisable() {
        if (storage != null) {
            storage.close();
        }
        getLogger().info("TogglePhantoms has been disabled!");
    }

    private void initStorage() {
        String storageType = getConfig().getString("storage.type", "yaml").toLowerCase();

        switch (storageType) {
            case "mysql":
                String host = getConfig().getString("storage.mysql.host", "localhost");
                int port = getConfig().getInt("storage.mysql.port", 3306);
                String database = getConfig().getString("storage.mysql.database", "minecraft");
                String username = getConfig().getString("storage.mysql.username", "root");
                String password = getConfig().getString("storage.mysql.password", "");
                storage = new MySQLStorage(this, host, port, database, username, password);
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

    public void reloadPlugin() {
        reloadConfig();
        messages.load(getConfig());
        if (storage != null) {
            storage.close();
        }
        initStorage();
    }

    public static TogglePhantoms getInstance() {
        return instance;
    }

    public Storage getStorage() {
        return storage;
    }

    public Messages getMessages() {
        return messages;
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
