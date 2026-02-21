package dev.oakheart.togglephantoms.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MessageManager {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Map<String, String> texts = new HashMap<>();
    private final Map<String, String> displays = new HashMap<>();

    private String placeholderEnabled;
    private String placeholderDisabled;

    public void load(FileConfiguration config) {
        texts.clear();
        displays.clear();

        for (String key : new String[]{
                "phantoms-enabled", "phantoms-disabled",
                "status-enabled", "status-disabled",
                "admin-phantoms-enabled", "admin-phantoms-disabled",
                "admin-status-enabled", "admin-status-disabled",
                "admin-notify-enabled", "admin-notify-disabled",
                "player-not-found",
                "reload-success", "reload-failed"
        }) {
            texts.put(key, config.getString("messages." + key + ".text", ""));
            displays.put(key, config.getString("messages." + key + ".display", "chat"));
        }

        placeholderEnabled = config.getString("placeholder-enabled", "Enabled");
        placeholderDisabled = config.getString("placeholder-disabled", "Disabled");
    }

    // --- Send helpers ---

    public void send(CommandSender sender, String key, TagResolver... resolvers) {
        parse(key, resolvers).ifPresent(component -> {
            String display = displays.getOrDefault(key, "chat");
            if ("action_bar".equals(display) && sender instanceof Player player) {
                player.sendActionBar(component);
            } else {
                sender.sendMessage(component);
            }
        });
    }

    // --- Named convenience methods ---

    public void sendPhantomsEnabled(CommandSender sender) {
        send(sender, "phantoms-enabled");
    }

    public void sendPhantomsDisabled(CommandSender sender) {
        send(sender, "phantoms-disabled");
    }

    public void sendStatusEnabled(CommandSender sender) {
        send(sender, "status-enabled");
    }

    public void sendStatusDisabled(CommandSender sender) {
        send(sender, "status-disabled");
    }

    public void sendAdminPhantomsEnabled(CommandSender sender, String playerName) {
        send(sender, "admin-phantoms-enabled", Placeholder.unparsed("player", playerName));
    }

    public void sendAdminPhantomsDisabled(CommandSender sender, String playerName) {
        send(sender, "admin-phantoms-disabled", Placeholder.unparsed("player", playerName));
    }

    public void sendAdminStatusEnabled(CommandSender sender, String playerName) {
        send(sender, "admin-status-enabled", Placeholder.unparsed("player", playerName));
    }

    public void sendAdminStatusDisabled(CommandSender sender, String playerName) {
        send(sender, "admin-status-disabled", Placeholder.unparsed("player", playerName));
    }

    public void sendAdminNotifyEnabled(CommandSender sender) {
        send(sender, "admin-notify-enabled");
    }

    public void sendAdminNotifyDisabled(CommandSender sender) {
        send(sender, "admin-notify-disabled");
    }

    public void sendPlayerNotFound(CommandSender sender, String playerName) {
        send(sender, "player-not-found", Placeholder.unparsed("player", playerName));
    }

    public void sendReloadSuccess(CommandSender sender) {
        send(sender, "reload-success");
    }

    public void sendReloadFailed(CommandSender sender) {
        send(sender, "reload-failed");
    }

    public String placeholderEnabled() {
        return placeholderEnabled;
    }

    public String placeholderDisabled() {
        return placeholderDisabled;
    }

    private Optional<Component> parse(String key, TagResolver... resolvers) {
        String text = texts.get(key);
        if (text == null || text.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(miniMessage.deserialize(text, resolvers));
    }
}
