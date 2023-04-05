package org.busch.ecoloy.Chunk;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ChunkListener implements Listener {
    private final JavaPlugin plugin;

    public ChunkListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();

        if (from.getWorld() != to.getWorld()) {
            // If the player teleported to a different world, do nothing
            return;
        }

        if (from.getChunk() == to.getChunk()) {
            // If the player is still in the same chunk, do nothing
            return;
        }

        if (player.hasPermission("eco.bypass")) {
            // If the player has the 'eco.bypass' permission, allow them to leave the chunk
            return;
        }

        Chunk toChunk = to.getChunk();

        if (isNearChunkBorder(toChunk, player.getLocation())) {
            event.setCancelled(true);
            setPlayerToChunkEdge(toChunk, player);
            player.sendMessage(ChatColor.RED + "You cannot leave your chunk!");
        }
    }

    private boolean isNearChunkBorder(Chunk chunk, Location location) {
        int x = location.getBlockX();
        int z = location.getBlockZ();

        if (x < chunk.getX() * 16 + 2 || x > chunk.getX() * 16 + 14) {
            return true;
        }

        if (z < chunk.getZ() * 16 + 2 || z > chunk.getZ() * 16 + 14) {
            return true;
        }

        return false;
    }

    private void setPlayerToChunkEdge(Chunk chunk, Player player) {
        int x = player.getLocation().getBlockX();
        int z = player.getLocation().getBlockZ();

        if (x < chunk.getX() * 16 + 2) {
            player.teleport(new Location(player.getWorld(), chunk.getX() * 16 + 2, player.getLocation().getY(), player.getLocation().getZ()));
        } else if (x > chunk.getX() * 16 + 14) {
            player.teleport(new Location(player.getWorld(), chunk.getX() * 16 + 14, player.getLocation().getY(), player.getLocation().getZ()));
        }

        if (z < chunk.getZ() * 16 + 2) {
            player.teleport(new Location(player.getWorld(), player.getLocation().getX(), player.getLocation().getY(), chunk.getZ() * 16 + 2));
        } else if (z > chunk.getZ() * 16 + 14) {
            player.teleport(new Location(player.getWorld(), player.getLocation().getX(), player.getLocation().getY(), chunk.getZ() * 16 + 14));
        }
    }
}
