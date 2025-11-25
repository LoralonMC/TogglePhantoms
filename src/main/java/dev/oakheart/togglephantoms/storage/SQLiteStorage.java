package dev.oakheart.togglephantoms.storage;

import dev.oakheart.togglephantoms.TogglePhantoms;

import java.io.File;
import java.sql.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class SQLiteStorage implements Storage {

    private final TogglePhantoms plugin;
    private Connection connection;
    private final Map<UUID, Boolean> cache = new ConcurrentHashMap<>();
    private final ReentrantLock dbLock = new ReentrantLock();

    public SQLiteStorage(TogglePhantoms plugin) {
        this.plugin = plugin;
        connect();
        createTable();
    }

    private void connect() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            String url = "jdbc:sqlite:" + new File(dataFolder, "playerdata.db").getAbsolutePath();
            connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not connect to SQLite database: " + e.getMessage());
        }
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS phantom_toggles (" +
                "uuid TEXT PRIMARY KEY," +
                "disabled INTEGER NOT NULL DEFAULT 0" +
                ")";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not create SQLite table: " + e.getMessage());
        }
    }

    @Override
    public boolean arePhantomsDisabled(UUID uuid) {
        return cache.getOrDefault(uuid, false);
    }

    @Override
    public CompletableFuture<Void> setPhantomsDisabled(UUID uuid, boolean disabled) {
        cache.put(uuid, disabled);
        return CompletableFuture.runAsync(() -> {
            dbLock.lock();
            try {
                String sql = "INSERT OR REPLACE INTO phantom_toggles (uuid, disabled) VALUES (?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, uuid.toString());
                    stmt.setInt(2, disabled ? 1 : 0);
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Could not update SQLite database: " + e.getMessage());
            } finally {
                dbLock.unlock();
            }
        });
    }

    @Override
    public CompletableFuture<Void> loadPlayer(UUID uuid) {
        return CompletableFuture.runAsync(() -> {
            dbLock.lock();
            try {
                String sql = "SELECT disabled FROM phantom_toggles WHERE uuid = ?";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, uuid.toString());
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        cache.put(uuid, rs.getInt("disabled") == 1);
                    } else {
                        cache.put(uuid, false);
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Could not query SQLite database: " + e.getMessage());
                cache.put(uuid, false);
            } finally {
                dbLock.unlock();
            }
        });
    }

    @Override
    public void unloadPlayer(UUID uuid) {
        cache.remove(uuid);
    }

    @Override
    public void close() {
        cache.clear();
        dbLock.lock();
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not close SQLite connection: " + e.getMessage());
        } finally {
            dbLock.unlock();
        }
    }
}
