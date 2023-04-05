package org.busch.ecoloy.Chunk;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class GiveChunk implements CommandExecutor {
    private final JavaPlugin plugin;
    private Set<UUID> playerUUIDs = new HashSet<>();

    public GiveChunk(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (playerUUIDs.contains(player.getUniqueId())) {
            sender.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "[B]" + " " + ChatColor.RED + "You have already used this command.");
            return true;
        }

        // Check if there are other players in the same chunk
        Chunk playerChunk = player.getLocation().getChunk();
        for (Player otherPlayer : player.getWorld().getPlayers()) {
            if (otherPlayer != player && otherPlayer.getLocation().getChunk() == playerChunk) {
                sender.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "[B]" + " " + ChatColor.RED + "You cannot use this command in a shared chunk.");
                return true;
            }
        }

        playerUUIDs.add(player.getUniqueId());
        Chunk spawnChunk = player.getLocation().getChunk();
        int spawnRadius = 16;
        Chunk chunk = getRandomChunk(player.getWorld(), spawnChunk, spawnRadius);

        // Teleport the player to the center of the chunk and set their bed spawn location
        int highestY = getHighestBlockY(chunk);
        Location spawnLocation = new Location(chunk.getWorld(), chunk.getX() * 16 + 8, highestY, chunk.getZ() * 16 + 8);
        player.teleport(spawnLocation);
        player.setBedSpawnLocation(spawnLocation, true);

        // Save the chunk to a file
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        UUID playerUUID = player.getUniqueId();
        File chunkFile = new File(dataFolder, playerUUID + ".dat");
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(chunkFile))) {
            out.writeInt(chunk.getX());
            out.writeInt(chunk.getZ());
            sender.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "[B]" + " " + ChatColor.GREEN + "Your chunk has been saved.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }
    private Chunk getRandomChunk(World world, Chunk spawnChunk, int spawnRadius) {
        Random random = new Random();
        int x;
        int z;
        int maxTries = 50; // Maximum number of attempts before giving up
        int tryCount = 0;

        do {
            x = spawnChunk.getX() + random.nextInt(spawnRadius * 2 + 1) - spawnRadius;
            z = spawnChunk.getZ() + random.nextInt(spawnRadius * 2 + 1) - spawnRadius;
            tryCount++;
        } while ((spawnChunk.getX() == x && spawnChunk.getZ() == z)
                || world.getChunkAt(x, z).isForceLoaded()
                || isWithinSpawnRadius(world, spawnChunk, x, z, spawnRadius)
                || !world.isChunkLoaded(x, z)
                || world.getHighestBlockYAt(x, z) <= 0
                || tryCount >= maxTries);

        if (tryCount >= maxTries) {
            // Failed to find a suitable chunk after maxTries attempts
            // Fall back to using spawnChunk
            return spawnChunk;
        } else {
            return world.getChunkAt(x, z);
        }
    }

    private boolean isWithinSpawnRadius(World world, Chunk spawnChunk, int x, int z, int spawnRadius) {
        int dx = spawnChunk.getX() - x;
        int dz = spawnChunk.getZ() - z;
        double distance = Math.sqrt(dx * dx + dz * dz);
        return distance <= spawnRadius;
    }

    private boolean isChunkInSpawnRadius(Chunk chunk, int spawnRadius) {
        World world = chunk.getWorld();
        Location spawnLocation = world.getSpawnLocation();
        int chunkX = chunk.getX() * 16 + 8;
        int chunkZ = chunk.getZ() * 16 + 8;
        double distanceSquared = spawnLocation.distanceSquared(new Location(world, chunkX, 0, chunkZ));
        return distanceSquared < spawnRadius * spawnRadius;
    }
    private int getHighestBlockY(Chunk chunk) {
        int highestY = 0;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int y = chunk.getWorld().getHighestBlockYAt(chunk.getBlock(x, 0, z).getLocation());

                if (y > highestY) {
                    highestY = y;
                }
            }
        }

        return highestY;
    }
}