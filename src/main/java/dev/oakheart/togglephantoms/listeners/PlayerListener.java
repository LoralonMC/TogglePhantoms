package dev.oakheart.togglephantoms.listeners;

import dev.oakheart.togglephantoms.TogglePhantoms;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final TogglePhantoms plugin;

    public PlayerListener(TogglePhantoms plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getStorage().loadPlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getStorage().unloadPlayer(event.getPlayer().getUniqueId());
    }
}
