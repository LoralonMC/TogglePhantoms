package dev.oakheart.togglephantoms.storage;

import dev.oakheart.togglephantoms.TogglePhantoms;

import java.sql.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class MySQLStorage implements Storage {

    private final TogglePhantoms plugin;
    private Connection connection;
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final Map<UUID, Boolean> cache = new ConcurrentHashMap<>();

    public MySQLStorage(TogglePhantoms plugin, String host, int port, String database, String username, String password) {
        this.plugin = plugin;
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        connect();
        createTable();
    }

    private void connect() {
        try {
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true&useSSL=false";
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not connect to MySQL database: " + e.getMessage());
        }
    }

    private synchronized void ensureConnection() {
        try {
            if (connection == null || connection.isClosed() || !connection.isValid(5)) {
                connect();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not validate MySQL connection: " + e.getMessage());
            connect();
        }
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS phantom_toggles (" +
                "uuid VARCHAR(36) PRIMARY KEY," +
                "disabled TINYINT(1) NOT NULL DEFAULT 0" +
                ")";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not create MySQL table: " + e.getMessage());
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
            ensureConnection();
            String sql = "INSERT INTO phantom_toggles (uuid, disabled) VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE disabled = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                stmt.setInt(2, disabled ? 1 : 0);
                stmt.setInt(3, disabled ? 1 : 0);
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Could not update MySQL database: " + e.getMessage());
            }
        });
    }

    @Override
    public CompletableFuture<Void> loadPlayer(UUID uuid) {
        return CompletableFuture.runAsync(() -> {
            ensureConnection();
            String sql = "SELECT disabled FROM phantom_toggles WHERE uuid = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    cache.put(uuid, rs.getInt("disabled") == 1);
                } else {
                    cache.put(uuid, false);
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Could not query MySQL database: " + e.getMessage());
                cache.put(uuid, false);
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
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not close MySQL connection: " + e.getMessage());
        }
    }
}
