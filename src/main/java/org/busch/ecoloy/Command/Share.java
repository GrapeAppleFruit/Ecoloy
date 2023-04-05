package org.busch.ecoloy.Command;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.busch.ecoloy.ChunkData;
import org.busch.ecoloy.Utils.ChunkUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.*;

public class Share implements CommandExecutor, Listener {

    private Map<String, List<String>> sharedChunks = new HashMap<>();

    private final JavaPlugin plugin;

    public Share(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by a player.");
            return true;
        }

        Player player = (Player) sender;

        // Check if player is already in a shared chunk
        if (isPlayerInSharedChunk(player)) {
            sender.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "[B]" + " " + ChatColor.RED + "You are already in a shared chunk.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "[B]" + " " + ChatColor.RED + "Usage: /share <player>");
            return true;
        }

        if (args[0].equalsIgnoreCase(player.getName())) {
            sender.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "[B]" + " " + ChatColor.RED + "You can't share a chunk with yourself.");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            sender.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "[B]" + " " + ChatColor.RED + "Player not found.");
            return true;
        }

        // Check if target player is already in a shared chunk
        if (isPlayerInSharedChunk(target)) {
            sender.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "[B]" + " " + ChatColor.RED + "That player is already in a shared chunk.");
            return true;
        }

        // Send accept/decline GUI to target player
        Inventory gui = Bukkit.createInventory(null, 9, ChatColor.GREEN + "Share Invite");
        ItemStack acceptItem = new ItemStack(Material.GREEN_WOOL, 1);
        ItemMeta acceptMeta = acceptItem.getItemMeta();
        acceptMeta.setDisplayName(ChatColor.GREEN + "Accept");
        acceptItem.setItemMeta(acceptMeta);
        gui.setItem(2, acceptItem);
        ItemStack declineItem = new ItemStack(Material.RED_WOOL, 1);
        ItemMeta declineMeta = declineItem.getItemMeta();
        declineMeta.setDisplayName(ChatColor.RED + "Decline");
        declineItem.setItemMeta(declineMeta);
        gui.setItem(6, declineItem);
        for (int i = 0; i < gui.getSize(); i++) {
            ItemStack item = gui.getItem(i);
            if (item != null) {
                gui.setItem(i, item.clone());
                gui.getItem(i).setAmount(1);
            }
        }
        target.openInventory(gui);

        sender.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "[B]" + " " + ChatColor.GREEN + "You've sent " + target.getName() + " an invite request.");
        target.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "[B]" + " " + ChatColor.GREEN + "You've received an invite request from " + player.getName() + ".");

        return true;
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        String inventoryTitle = event.getView().getTitle();
        if (inventoryTitle.equals(ChatColor.GREEN + "Share Invite")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String inventoryTitle = event.getView().getTitle();
        if (inventoryTitle.equals(ChatColor.GREEN + "Share Invite")) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null) {
                return;
            }
            if (clickedItem.getType() == Material.GREEN_WOOL) {
                // Accept share invite
                Player player = (Player) event.getWhoClicked();
                InventoryView inventoryView = event.getView();
                if (inventoryView != null) {
                    inventoryView.close();
                }
                transferInventory(player);
                Location sharedChunkLocation = getSharedChunkLocation(player.getLocation());
                player.teleport(sharedChunkLocation);
                addPlayersToSharedChunk(player);
                player.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "[B]" + " " + ChatColor.GREEN + "You accepted the share invite do /chunk to teleport.");
            } else if (clickedItem.getType() == Material.RED_WOOL) {
                // Decline share invite
                Player player = (Player) event.getWhoClicked();
                InventoryView inventoryView = event.getView();
                if (inventoryView != null) {
                    inventoryView.close();
                }
                player.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "[B]" + " " + ChatColor.GREEN + "You declined the share invite.");
            }

            // Remove the player from sharedChunks map to prevent inviting the same player again
            Player player = (Player) event.getWhoClicked();
            Chunk playerChunk = player.getLocation().getChunk();
            String chunkKey = getChunkKey(playerChunk);
            if (sharedChunks.containsKey(chunkKey)) {
                List<String> playerList = sharedChunks.get(chunkKey);
                playerList.remove(player.getUniqueId().toString());
                sharedChunks.put(chunkKey, playerList);
            }
        }
    }

    private boolean isPlayerInSharedChunk(Player player) {
        Chunk playerChunk = player.getLocation().getChunk();
        UUID owner = ChunkUtils.getSharedChunkOwner(playerChunk);

        if (owner == null) {
            return false;
        }

        return owner.equals(player.getUniqueId());
    }

    private void transferInventory(Player player) {
        player.getInventory().clear();
    }

    private void addPlayersToSharedChunk(Player player) {
        Chunk sharedChunk = player.getLocation().getChunk();
        String chunkKey = getChunkKey(sharedChunk);
        List<String> playerList = sharedChunks.getOrDefault(chunkKey, new ArrayList<>());
        playerList.add(player.getUniqueId().toString());
        sharedChunks.put(chunkKey, playerList);

        // Save shared chunk to player's chunk file if they accept
        if (playerAcceptsShareInvite(player)) {
            ChunkGenerator.ChunkData sharedChunkData = new ChunkData(sharedChunk);
            String playerChunkFileName = getPlayerChunkFileName(player);
            try {
                sharedChunkData.saveToFile(playerChunkFileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean playerAcceptsShareInvite(Player player) {
        // You need to implement this method based on how you want to check if the player accepts the share invite.
        // For example, you can have a boolean flag in the player object that you set to true when they accept the invite.
        return player.hasAcceptedShareInvite();
    }

    private Location getSharedChunkLocation(Location location) {
        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;
        return new Location(location.getWorld(), chunkX << 4, location.getBlockY(), chunkZ << 4);
    }

    private String getChunkKey(Chunk chunk) {
        return chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
    }
}