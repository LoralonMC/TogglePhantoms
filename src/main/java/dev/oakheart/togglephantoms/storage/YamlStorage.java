package dev.oakheart.togglephantoms.storage;

import dev.oakheart.togglephantoms.TogglePhantoms;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class YamlStorage implements Storage {

    private final TogglePhantoms plugin;
    private final File dataFile;
    private YamlConfiguration dataConfig;
    private final Set<UUID> disabledPlayers = ConcurrentHashMap.newKeySet();

    public YamlStorage(TogglePhantoms plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "playerdata.yml");

        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create playerdata.yml", e);
            }
        }

        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        loadAllData();
    }

    private void loadAllData() {
        disabledPlayers.clear();
        if (dataConfig.contains("disabled-players")) {
            for (String uuidString : dataConfig.getStringList("disabled-players")) {
                try {
                    disabledPlayers.add(UUID.fromString(uuidString));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in playerdata.yml: " + uuidString);
                }
            }
        }
    }

    private void saveDataAsync() {
        // Snapshot the current set for the async write
        var snapshot = disabledPlayers.stream().map(UUID::toString).toList();
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            synchronized (this) {
                dataConfig.set("disabled-players", snapshot);
                try {
                    dataConfig.save(dataFile);
                } catch (IOException e) {
                    plugin.getLogger().log(Level.SEVERE, "Could not save playerdata.yml", e);
                }
            }
        });
    }

    @Override
    public boolean arePhantomsDisabled(UUID uuid) {
        return disabledPlayers.contains(uuid);
    }

    @Override
    public CompletableFuture<Void> setPhantomsDisabled(UUID uuid, boolean disabled) {
        if (disabled) {
            disabledPlayers.add(uuid);
        } else {
            disabledPlayers.remove(uuid);
        }
        saveDataAsync();
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> loadPlayer(UUID uuid) {
        // YAML loads all data at startup, nothing to do per-player
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void unloadPlayer(UUID uuid) {
        // YAML keeps all data in memory, nothing to unload
    }

    @Override
    public void close() {
        // Synchronous save on shutdown to ensure data is persisted
        synchronized (this) {
            dataConfig.set("disabled-players", disabledPlayers.stream()
                    .map(UUID::toString)
                    .toList());
            try {
                dataConfig.save(dataFile);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not save playerdata.yml on shutdown", e);
            }
        }
    }
}
