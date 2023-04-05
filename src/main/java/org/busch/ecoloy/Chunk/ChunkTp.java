package org.busch.ecoloy.Chunk;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ChunkTp implements CommandExecutor {
    private final JavaPlugin plugin;

    public ChunkTp(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        // Get the player's saved chunk
        Player player = (Player) sender;
        Chunk chunk = getSavedChunk(player);

        if (chunk == null) {
            sender.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "[B]" + " " + ChatColor.RED + "You don't have a saved chunk.");
            return true;
        }

        // Calculate the teleport destination
        int chunkX = chunk.getX() * 16 + 8;
        int chunkZ = chunk.getZ() * 16 + 8;
        int highestY = getHighestBlockY(chunk);
        Location destination = new Location(chunk.getWorld(), chunkX, highestY, chunkZ);

        // Teleport the player
        player.teleport(destination);
        sender.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "[B]" + " " + ChatColor.GREEN + "Teleported to your saved chunk.");

        return true;
    }

    private Chunk getSavedChunk(Player player) {
        File chunkFile = new File(plugin.getDataFolder(), "chunk.dat");

        if (!chunkFile.exists()) {
            return null;
        }

        int x = 0;
        int z = 0;

        try (DataInputStream in = new DataInputStream(new FileInputStream(chunkFile))) {
            x = in.readInt();
            z = in.readInt();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return player.getWorld().getChunkAt(x, z);
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
