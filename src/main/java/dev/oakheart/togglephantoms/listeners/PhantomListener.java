package dev.oakheart.togglephantoms.listeners;

import com.destroystokyo.paper.event.entity.PhantomPreSpawnEvent;
import dev.oakheart.togglephantoms.TogglePhantoms;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PhantomListener implements Listener {

    private final TogglePhantoms plugin;

    public PhantomListener(TogglePhantoms plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPhantomPreSpawn(PhantomPreSpawnEvent event) {
        if (!(event.getSpawningEntity() instanceof Player player)) {
            return;
        }

        if (plugin.arePhantomsDisabled(player.getUniqueId())) {
            event.setCancelled(true);
            event.setShouldAbortSpawn(true);
        }
    }
}
