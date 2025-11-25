package dev.oakheart.togglephantoms.storage;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Storage {

    /**
     * Check if phantoms are disabled for a player (cached).
     * @param uuid The player's UUID
     * @return true if phantoms are disabled, false otherwise
     */
    boolean arePhantomsDisabled(UUID uuid);

    /**
     * Set whether phantoms are disabled for a player.
     * @param uuid The player's UUID
     * @param disabled true to disable phantoms, false to enable
     * @return CompletableFuture that completes when the operation is done
     */
    CompletableFuture<Void> setPhantomsDisabled(UUID uuid, boolean disabled);

    /**
     * Load a player's data into cache.
     * @param uuid The player's UUID
     * @return CompletableFuture that completes when loaded
     */
    CompletableFuture<Void> loadPlayer(UUID uuid);

    /**
     * Remove a player from cache.
     * @param uuid The player's UUID
     */
    void unloadPlayer(UUID uuid);

    /**
     * Close the storage connection and save any pending data.
     */
    void close();
}
