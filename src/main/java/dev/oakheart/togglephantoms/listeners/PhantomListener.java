package dev.oakheart.togglephantoms.listeners;

import dev.oakheart.togglephantoms.TogglePhantoms;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class PhantomListener implements Listener {

    private final TogglePhantoms plugin;

    public PhantomListener(TogglePhantoms plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPhantomSpawn(CreatureSpawnEvent event) {
        // Only handle phantom spawns
        if (event.getEntityType() != EntityType.PHANTOM) {
            return;
        }

        // Only block natural spawns (from insomnia)
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL) {
            return;
        }

        // Find the nearest player - phantoms spawn because of a specific player's insomnia
        Player targetPlayer = findNearestPlayer(event.getLocation(), 64);

        if (targetPlayer != null && plugin.arePhantomsDisabled(targetPlayer.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    private Player findNearestPlayer(org.bukkit.Location location, double maxDistance) {
        Player nearest = null;
        double nearestDistanceSq = maxDistance * maxDistance;

        for (Player player : location.getWorld().getPlayers()) {
            double distanceSq = player.getLocation().distanceSquared(location);
            if (distanceSq < nearestDistanceSq) {
                nearestDistanceSq = distanceSq;
                nearest = player;
            }
        }

        return nearest;
    }
}
