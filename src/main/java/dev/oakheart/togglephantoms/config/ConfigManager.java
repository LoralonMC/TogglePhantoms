package dev.oakheart.togglephantoms.config;

import dev.oakheart.togglephantoms.TogglePhantoms;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Manages plugin configuration with validation, caching, and safe reload.
 */
public class ConfigManager {

    private static final Set<String> VALID_STORAGE_TYPES = Set.of("yaml", "sqlite", "mysql");

    private final TogglePhantoms plugin;
    private final Logger logger;
    private final File configFile;
    private FileConfiguration config;

    // Cached config values
    private String storageType;
    private String mysqlHost;
    private int mysqlPort;
    private String mysqlDatabase;
    private String mysqlUsername;
    private String mysqlPassword;

    public ConfigManager(TogglePhantoms plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
    }

    /**
     * Initial load of configuration. Called once during onEnable.
     */
    public void load() {
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        mergeDefaults();

        if (!validate(config)) {
            throw new RuntimeException("Configuration validation failed");
        }

        cacheValues();
    }

    /**
     * Reloads configuration from disk. Validates before applying.
     *
     * @return true if reload was successful
     */
    public boolean reload() {
        FileConfiguration newConfig = YamlConfiguration.loadConfiguration(configFile);

        if (!validate(newConfig)) {
            logger.warning("Configuration reload failed validation. Keeping previous configuration.");
            return false;
        }

        this.config = newConfig;
        cacheValues();
        logger.info("Configuration reloaded successfully.");
        return true;
    }

    /**
     * Merges default config values from the JAR resource into the user's config.
     * Only saves to disk if new keys were added (to avoid reformatting the file
     * on every startup). When no new keys exist, the file stays untouched.
     */
    private void mergeDefaults() {
        try (var stream = plugin.getResource("config.yml")) {
            if (stream != null) {
                YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                        new InputStreamReader(stream, StandardCharsets.UTF_8));
                config.setDefaults(defaults);

                if (hasNewKeys(defaults)) {
                    config.options().copyDefaults(true);
                    config.save(configFile);
                    logger.info("Config updated with new default values.");
                }
            }
        } catch (IOException e) {
            logger.warning("Could not save config defaults: " + e.getMessage());
        }
    }

    /**
     * Checks if the defaults contain any value keys not present in the user's config.
     */
    private boolean hasNewKeys(FileConfiguration defaults) {
        for (String key : defaults.getKeys(true)) {
            if (!defaults.isConfigurationSection(key) && !config.contains(key, true)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates configuration values and logs warnings for issues.
     *
     * @param configToValidate the configuration to validate
     * @return true if configuration is valid
     */
    private boolean validate(FileConfiguration configToValidate) {
        List<String> warnings = new ArrayList<>();
        boolean valid = true;

        // Validate storage type
        String type = configToValidate.getString("storage.type", "yaml").toLowerCase();
        if (!VALID_STORAGE_TYPES.contains(type)) {
            warnings.add("Invalid storage type '" + type + "', defaulting to 'yaml'. Valid types: yaml, sqlite, mysql");
        }

        // Validate MySQL settings if MySQL is selected
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

    /**
     * Caches frequently accessed config values as typed fields.
     */
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
    }

    /**
     * Gets the raw FileConfiguration for direct access.
     */
    public FileConfiguration getConfig() {
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
}
