package dev.oakheart.togglephantoms.config;

import dev.oakheart.togglephantoms.TogglePhantoms;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class ConfigManager {

    private static final Set<String> VALID_STORAGE_TYPES = Set.of("yaml", "sqlite", "mysql");

    private final TogglePhantoms plugin;
    private final Logger logger;
    private final Path configFile;
    private dev.oakheart.config.ConfigManager config;

    private String storageType;
    private String mysqlHost;
    private int mysqlPort;
    private String mysqlDatabase;
    private String mysqlUsername;
    private String mysqlPassword;
    private String placeholderEnabled;
    private String placeholderDisabled;

    public ConfigManager(TogglePhantoms plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.configFile = plugin.getDataFolder().toPath().resolve("config.yml");
    }

    public void load() {
        if (!configFile.toFile().exists()) {
            plugin.saveResource("config.yml", false);
        }

        try {
            config = dev.oakheart.config.ConfigManager.load(configFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.yml", e);
        }

        mergeDefaults();

        if (!validate(config)) {
            throw new RuntimeException("Configuration validation failed");
        }

        cacheValues();
    }

    public boolean reload() {
        try {
            config.reload();
        } catch (IOException e) {
            logger.warning("Failed to reload config.yml: " + e.getMessage());
            return false;
        }

        if (!validate(config)) {
            logger.warning("Configuration reload failed validation. Keeping previous configuration.");
            return false;
        }

        cacheValues();
        logger.info("Configuration reloaded successfully.");
        return true;
    }

    private void mergeDefaults() {
        try (var stream = plugin.getResource("config.yml")) {
            if (stream != null) {
                var defaults = dev.oakheart.config.ConfigManager.fromStream(stream);
                if (config.mergeDefaults(defaults)) {
                    config.save();
                    logger.info("Config updated with new default values.");
                }
            }
        } catch (IOException e) {
            logger.warning("Could not save config defaults: " + e.getMessage());
        }
    }

    private boolean validate(dev.oakheart.config.ConfigManager configToValidate) {
        List<String> warnings = new ArrayList<>();
        boolean valid = true;

        String type = configToValidate.getString("storage.type", "yaml").toLowerCase();
        if (!VALID_STORAGE_TYPES.contains(type)) {
            warnings.add("Invalid storage type '" + type + "', defaulting to 'yaml'. Valid types: yaml, sqlite, mysql");
        }

        if ("mysql".equals(type)) {
            String host = configToValidate.getString("storage.mysql.host", "");
            if (host.isBlank()) {
                warnings.add("MySQL host is empty — cannot connect");
                valid = false;
            }
            String database = configToValidate.getString("storage.mysql.database", "");
            if (database.isBlank()) {
                warnings.add("MySQL database name is empty — cannot connect");
                valid = false;
            }
        }

        if (!warnings.isEmpty()) {
            logger.warning("=== Configuration Warnings ===");
            warnings.forEach(w -> logger.warning("  - " + w));
            logger.warning("==============================");
        }

        return valid;
    }

    private void cacheValues() {
        storageType = config.getString("storage.type", "yaml").toLowerCase();
        if (!VALID_STORAGE_TYPES.contains(storageType)) {
            storageType = "yaml";
        }

        mysqlHost = config.getString("storage.mysql.host", "localhost");
        mysqlPort = config.getInt("storage.mysql.port", 3306);
        mysqlDatabase = config.getString("storage.mysql.database", "minecraft");
        mysqlUsername = config.getString("storage.mysql.username", "root");
        mysqlPassword = config.getString("storage.mysql.password", "");
        placeholderEnabled = config.getString("placeholder-enabled", "Enabled");
        placeholderDisabled = config.getString("placeholder-disabled", "Disabled");
    }

    public dev.oakheart.config.ConfigManager getConfig() {
        return config;
    }

    public String getStorageType() {
        return storageType;
    }

    public String getMysqlHost() {
        return mysqlHost;
    }

    public int getMysqlPort() {
        return mysqlPort;
    }

    public String getMysqlDatabase() {
        return mysqlDatabase;
    }

    public String getMysqlUsername() {
        return mysqlUsername;
    }

    public String getMysqlPassword() {
        return mysqlPassword;
    }

    public String getPlaceholderEnabled() {
        return placeholderEnabled;
    }

    public String getPlaceholderDisabled() {
        return placeholderDisabled;
    }
}
