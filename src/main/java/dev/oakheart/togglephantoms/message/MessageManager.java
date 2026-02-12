package dev.oakheart.togglephantoms.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Optional;

/**
 * Manages all player-facing messages with MiniMessage formatting.
 * Uses TagResolver API with Placeholder.unparsed() for dynamic content.
 */
public class MessageManager {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    private String phantomsEnabled;
    private String phantomsDisabled;
    private String statusEnabled;
    private String statusDisabled;
    private String adminPhantomsEnabled;
    private String adminPhantomsDisabled;
    private String adminStatusEnabled;
    private String adminStatusDisabled;
    private String adminNotifyEnabled;
    private String adminNotifyDisabled;
    private String noPermission;
    private String playerOnly;
    private String playerNotFound;
    private String reloadSuccess;
    private String reloadFailed;

    private String placeholderEnabled;
    private String placeholderDisabled;

    /**
     * Loads all messages from the configuration.
     *
     * @param config The plugin configuration
     */
    public void load(FileConfiguration config) {
        phantomsEnabled = config.getString("messages.phantoms-enabled", "<green>Phantom spawning has been <green>enabled</green> for you.");
        phantomsDisabled = config.getString("messages.phantoms-disabled", "<green>Phantom spawning has been <red>disabled</red> for you.");
        statusEnabled = config.getString("messages.status-enabled", "<gray>Phantom spawning is currently <green>enabled</green> for you.");
        statusDisabled = config.getString("messages.status-disabled", "<gray>Phantom spawning is currently <red>disabled</red> for you.");
        adminPhantomsEnabled = config.getString("messages.admin-phantoms-enabled", "<green>Phantom spawning has been <green>enabled</green> for <player>.");
        adminPhantomsDisabled = config.getString("messages.admin-phantoms-disabled", "<green>Phantom spawning has been <red>disabled</red> for <player>.");
        adminStatusEnabled = config.getString("messages.admin-status-enabled", "<gray>Phantom spawning is <green>enabled</green> for <player>.");
        adminStatusDisabled = config.getString("messages.admin-status-disabled", "<gray>Phantom spawning is <red>disabled</red> for <player>.");
        adminNotifyEnabled = config.getString("messages.admin-notify-enabled", "<yellow>An admin has <green>enabled</green> phantom spawning for you.");
        adminNotifyDisabled = config.getString("messages.admin-notify-disabled", "<yellow>An admin has <red>disabled</red> phantom spawning for you.");
        noPermission = config.getString("messages.no-permission", "<red>You don't have permission to use this command.");
        playerOnly = config.getString("messages.player-only", "<red>This command can only be used by players.");
        playerNotFound = config.getString("messages.player-not-found", "<red>Player '<player>' has never played on this server.");
        reloadSuccess = config.getString("messages.reload-success", "<green>TogglePhantoms configuration reloaded.");
        reloadFailed = config.getString("messages.reload-failed", "<red>Configuration reload failed. Check console for details.");

        placeholderEnabled = config.getString("messages.placeholder-enabled", "Enabled");
        placeholderDisabled = config.getString("messages.placeholder-disabled", "Disabled");
    }

    // --- Message getters ---

    public Optional<Component> phantomsEnabled() {
        return parse(phantomsEnabled);
    }

    public Optional<Component> phantomsDisabled() {
        return parse(phantomsDisabled);
    }

    public Optional<Component> statusEnabled() {
        return parse(statusEnabled);
    }

    public Optional<Component> statusDisabled() {
        return parse(statusDisabled);
    }

    public Optional<Component> adminPhantomsEnabled(String playerName) {
        return parse(adminPhantomsEnabled, Placeholder.unparsed("player", playerName));
    }

    public Optional<Component> adminPhantomsDisabled(String playerName) {
        return parse(adminPhantomsDisabled, Placeholder.unparsed("player", playerName));
    }

    public Optional<Component> adminStatusEnabled(String playerName) {
        return parse(adminStatusEnabled, Placeholder.unparsed("player", playerName));
    }

    public Optional<Component> adminStatusDisabled(String playerName) {
        return parse(adminStatusDisabled, Placeholder.unparsed("player", playerName));
    }

    public Optional<Component> adminNotifyEnabled() {
        return parse(adminNotifyEnabled);
    }

    public Optional<Component> adminNotifyDisabled() {
        return parse(adminNotifyDisabled);
    }

    public Optional<Component> noPermission() {
        return parse(noPermission);
    }

    public Optional<Component> playerOnly() {
        return parse(playerOnly);
    }

    public Optional<Component> playerNotFound(String playerName) {
        return parse(playerNotFound, Placeholder.unparsed("player", playerName));
    }

    public Optional<Component> reloadSuccess() {
        return parse(reloadSuccess);
    }

    public Optional<Component> reloadFailed() {
        return parse(reloadFailed);
    }

    public String placeholderEnabled() {
        return placeholderEnabled;
    }

    public String placeholderDisabled() {
        return placeholderDisabled;
    }

    // --- Parsing utilities ---

    private Optional<Component> parse(String message, TagResolver... resolvers) {
        if (message == null || message.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(miniMessage.deserialize(message, resolvers));
    }
}
