package dev.oakheart.togglephantoms.storage;

import dev.oakheart.togglephantoms.TogglePhantoms;

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

public class MySQLStorage implements Storage {

    private final TogglePhantoms plugin;
    private final String jdbcUrl;
    private final String username;
    private final String password;
    private Connection connection;
    private PreparedStatement selectStmt;
    private PreparedStatement upsertStmt;
    private final Map<UUID, Boolean> cache = new ConcurrentHashMap<>();

    public MySQLStorage(TogglePhantoms plugin, String host, int port, String database,
                        String username, String password) {
        this.plugin = plugin;
        this.jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false";
        this.username = username;
        this.password = password;

        connect();
        createTable();
        prepareStatements();
    }

    private void connect() {
        try {
            connection = DriverManager.getConnection(jdbcUrl, username, password);
        } catch (SQLException e) {
            throw new RuntimeException("Could not connect to MySQL database", e);
        }
    }

    private void ensureConnection() throws SQLException {
        if (connection == null || connection.isClosed() || !connection.isValid(5)) {
            connection = DriverManager.getConnection(jdbcUrl, username, password);
            prepareStatements();
        }
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS phantom_toggles ("
                + "uuid VARCHAR(36) PRIMARY KEY,"
                + "disabled TINYINT(1) NOT NULL DEFAULT 0"
                + ")";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Could not create MySQL table", e);
        }
    }

    private void prepareStatements() {
        try {
            selectStmt = connection.prepareStatement(
                    "SELECT disabled FROM phantom_toggles WHERE uuid = ?");
            upsertStmt = connection.prepareStatement(
                    "INSERT INTO phantom_toggles (uuid, disabled) VALUES (?, ?) "
                            + "ON DUPLICATE KEY UPDATE disabled = VALUES(disabled)");
        } catch (SQLException e) {
            throw new RuntimeException("Could not prepare MySQL statements", e);
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
                    plugin.getLogger().log(Level.SEVERE, "Could not update MySQL database", e);
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
                    plugin.getLogger().log(Level.SEVERE, "Could not query MySQL database", e);
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
            plugin.getLogger().log(Level.SEVERE, "Could not close MySQL connection", e);
        }
    }
}
