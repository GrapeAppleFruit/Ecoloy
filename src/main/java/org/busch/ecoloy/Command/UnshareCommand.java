package org.busch.ecoloy.Command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.busch.ecoloy.Utils.ChunkUtils;

import java.util.*;

public class UnshareCommand implements CommandExecutor {

    private final Map<Chunk, Set<UUID>> sharedChunks;
    private int maxChunks = 1;
    private Map<UUID, Set<Chunk>> claimedChunks = new HashMap<>();

    public UnshareCommand(Map<Chunk, Set<UUID>> sharedChunks) {
        this.sharedChunks = sharedChunks;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by a player.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            sender.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "[B]" + " " + ChatColor.RED + "Usage: /unshare <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            sender.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "[B]" + " " + ChatColor.RED + "Player not found.");
            return true;
        }

        Chunk chunk = player.getLocation().getChunk();

        if (!sharedChunks.containsKey(chunk)) {
            sender.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "[B]" + " " + ChatColor.RED + "You are not sharing this chunk with anyone.");
            return true;
        }

        Set<UUID> players = sharedChunks.get(chunk);

        if (!players.contains(target.getUniqueId())) {
            sender.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "[B]" + " " + ChatColor.RED + target.getName() + " is not sharing this chunk with you.");
            return true;
        }

        target.teleport(target.getWorld().getSpawnLocation());
        players.remove(target.getUniqueId());

        target.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "[B]" + " " + ChatColor.GREEN + "You have been removed from a shared chunk.");
        sender.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "[B]" + " " + ChatColor.GREEN + "You are no longer sharing a chunk with " + target.getName() + ".");

        target.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "[B]" + " " + ChatColor.GREEN + "Use /givechunk to get your own chunk.");

        return true;
    }

    public void shareChunk(Player player, Player target) {
        Chunk chunk = player.getLocation().getChunk();

        if (sharedChunks.containsKey(chunk)) {
            Set<UUID> players = sharedChunks.get(chunk);

            if (players.contains(target.getUniqueId())) {
                player.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "[B]" + " " + ChatColor.RED + target.getName() + " is already sharing this chunk with you.");
                return;
            }

            players.add(target.getUniqueId());
        } else {
            Set<UUID> players = new HashSet<>();
            players.add(player.getUniqueId());
            players.add(target.getUniqueId());
            sharedChunks.put(chunk, players);
        }

        target.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "[B]" + " " + ChatColor.GREEN + "You are now sharing a chunk with " + player.getName() + ".");
        player.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "[B]" + " " + ChatColor.GREEN + target.getName() + " is now sharing a chunk with you.");
    }

    public void giveChunk(Player player) {
        UUID playerId = player.getUniqueId();

        if (claimedChunks.containsKey(playerId)) {
            Set<Chunk> claimedChunksSet = claimedChunks.get(playerId);

            if (claimedChunksSet.size() >= maxChunks) {
                player.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "[B]" + " " + ChatColor.RED + "You have reached the maximum number of claimed chunks.");
                return;
            }

            Chunk chunk = player.getLocation().getChunk();

            if (claimedChunksSet.contains(chunk)) {
                player.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "[B]" + " " + ChatColor.RED + "This chunk is already claimed by you.");
                return;
            }

            claimedChunksSet.add(chunk);
            player.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "[B]" + " " + ChatColor.GREEN + "This chunk has been claimed by you.");
        } else {
            Set<Chunk> newSet = new HashSet<>();
            newSet.add(player.getLocation().getChunk());
            claimedChunks.put(playerId, newSet);
            player.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "[B]" + " " + ChatColor.GREEN + "This chunk has been claimed by you.");
        }
    }
}
