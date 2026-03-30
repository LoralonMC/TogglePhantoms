package dev.oakheart.togglephantoms.placeholder;

import dev.oakheart.togglephantoms.TogglePhantoms;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PhantomsPlaceholderExpansion extends PlaceholderExpansion {

    private final TogglePhantoms plugin;

    public PhantomsPlaceholderExpansion(TogglePhantoms plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "togglephantoms";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getPluginMeta().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return null;
        }

        boolean phantomsDisabled = plugin.arePhantomsDisabled(player.getUniqueId());

        return switch (params.toLowerCase()) {
            case "enabled" -> String.valueOf(!phantomsDisabled);
            case "status" -> phantomsDisabled
                    ? plugin.getConfigManager().getPlaceholderDisabled()
                    : plugin.getConfigManager().getPlaceholderEnabled();
            default -> null;
        };
    }
}
