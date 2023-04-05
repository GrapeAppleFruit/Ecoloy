package org.busch.ecoloy;

import java.util.List;

public class ChunkData {
    private String chunkKey;
    private List<String> players;

    public ChunkData(String chunkKey, List<String> players) {
        this.chunkKey = chunkKey;
        this.players = players;
    }

    public String getChunkKey() {
        return chunkKey;
    }

    public List<String> getPlayers() {
        return players;
    }

    public void setPlayers(List<String> players) {
        this.players = players;
    }
}
