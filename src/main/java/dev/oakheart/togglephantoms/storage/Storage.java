package dev.oakheart.togglephantoms.storage;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Storage {

    boolean arePhantomsDisabled(UUID uuid);

    CompletableFuture<Void> setPhantomsDisabled(UUID uuid, boolean disabled);

    CompletableFuture<Void> loadPlayer(UUID uuid);

    void unloadPlayer(UUID uuid);

    void close();
}
