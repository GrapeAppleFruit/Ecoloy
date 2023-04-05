package org.busch.ecoloy.Utils;

import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.busch.ecoloy.Ecoloy;

import java.util.UUID;

public class ChunkUtils {

    private static final Plugin plugin = Ecoloy.getInstance();
    private static final NamespacedKey OWNER_KEY = new NamespacedKey(plugin, "chunk-owner");

    public static UUID getSharedChunkOwner(Chunk chunk) {
        if (chunk == null) {
            return null;
        }

        String owner = chunk.getPersistentDataContainer().get(OWNER_KEY, PersistentDataType.STRING);
        if (owner == null) {
            return null;
        }

        return UUID.fromString(owner);
    }

    public static void setSharedChunkOwner(Chunk chunk, UUID owner) {
        if (chunk == null || owner == null) {
            return;
        }

        chunk.getPersistentDataContainer().set(OWNER_KEY, PersistentDataType.STRING, owner.toString());
        chunk.setForceLoaded(true);
    }
}
