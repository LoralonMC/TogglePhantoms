package dev.oakheart.togglephantoms.storage;

import dev.oakheart.togglephantoms.TogglePhantoms;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class SQLiteStorage implements Storage {

    private final TogglePhantoms plugin;
    private final String dbUrl;
    private Connection connection;
    private PreparedStatement selectStmt;
    private PreparedStatement upsertStmt;
    private final Map<UUID, Boolean> cache = new ConcurrentHashMap<>();

    public SQLiteStorage(TogglePhantoms plugin) {
        this.plugin = plugin;

        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        this.dbUrl = "jdbc:sqlite:" + new File(dataFolder, "playerdata.db").getAbsolutePath();

        connect();
        createTable();
        prepareStatements();
    }

    private void connect() {
        try {
            connection = DriverManager.getConnection(dbUrl);
        } catch (SQLException e) {
            throw new RuntimeException("Could not connect to SQLite database", e);
        }
    }

    private void ensureConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(dbUrl);
            prepareStatements();
        }
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS phantom_toggles ("
                + "uuid TEXT PRIMARY KEY,"
                + "disabled INTEGER NOT NULL DEFAULT 0"
                + ")";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Could not create SQLite table", e);
        }
    }

    private void prepareStatements() {
        try {
            selectStmt = connection.prepareStatement(
                    "SELECT disabled FROM phantom_toggles WHERE uuid = ?");
            upsertStmt = connection.prepareStatement(
                    "INSERT OR REPLACE INTO phantom_toggles (uuid, disabled) VALUES (?, ?)");
        } catch (SQLException e) {
            throw new RuntimeException("Could not prepare SQLite statements", e);
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
            synchronized (this) {
                try {
                    ensureConnection();
                    upsertStmt.setString(1, uuid.toString());
                    upsertStmt.setInt(2, disabled ? 1 : 0);
                    upsertStmt.executeUpdate();
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.SEVERE, "Could not update SQLite database", e);
                }
            }
        });
    }

    @Override
    public CompletableFuture<Void> loadPlayer(UUID uuid) {
        return CompletableFuture.runAsync(() -> {
            synchronized (this) {
                try {
                    ensureConnection();
                    selectStmt.setString(1, uuid.toString());
                    ResultSet rs = selectStmt.executeQuery();
                    cache.put(uuid, rs.next() && rs.getInt("disabled") == 1);
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.SEVERE, "Could not query SQLite database", e);
                    cache.put(uuid, false);
                }
            }
        });
    }

    @Override
    public void unloadPlayer(UUID uuid) {
        cache.remove(uuid);
    }

    @Override
    public synchronized void close() {
        cache.clear();
        try {
            if (selectStmt != null) selectStmt.close();
            if (upsertStmt != null) upsertStmt.close();
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not close SQLite connection", e);
        }
    }
}
