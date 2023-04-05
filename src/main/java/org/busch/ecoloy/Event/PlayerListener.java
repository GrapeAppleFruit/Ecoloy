package org.busch.ecoloy.Event;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.busch.ecoloy.Utils.ChunkUtils;
import org.busch.ecoloy.Ecoloy;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

public class PlayerListener implements Listener {
    private final Ecoloy plugin;
    public PlayerListener(Ecoloy plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        Location respawnLocation = getRespawnLocation(playerName);

        if (respawnLocation != null) {
            event.setRespawnLocation(respawnLocation);
            player.teleport(respawnLocation);
        } else {
            // if there is no custom respawn location, respawn at the world spawn
            event.setRespawnLocation(player.getWorld().getSpawnLocation());
            player.teleport(player.getWorld().getSpawnLocation());
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            return;
        }

        Player player = event.getPlayer();
        Chunk savedChunk = getSavedChunk(player);

        if (savedChunk == null) {
            return;
        }

        Chunk targetChunk = event.getTo().getChunk();

        if (!savedChunk.equals(targetChunk)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "[B]" + " " + ChatColor.RED + "You can only use ender pearls within your saved chunk.");
        }
    }

    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event) {
        Entity vehicle = event.getVehicle();

        if (vehicle instanceof Boat) {
            Boat boat = (Boat) vehicle;
            Entity passenger = boat.getPassenger();

            if (!(passenger instanceof Player)) {
                return;
            }

            Player player = (Player) passenger;
            Chunk savedChunk = getSavedChunk(player);

            if (savedChunk == null) {
                return;
            }

            Chunk boatChunk = event.getTo().getChunk();

            if (!savedChunk.equals(boatChunk)) {
                boat.remove();
                player.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "[B]" + " " + ChatColor.RED + "Your boat has been despawned because it went outside of your saved chunk.");
            }
        } else if (vehicle instanceof Minecart && ((Minecart) vehicle).getType() == EntityType.MINECART) {
            Minecart minecart = (Minecart) vehicle;
            Entity passenger = minecart.getPassenger();

            if (!(passenger instanceof Player)) {
                return;
            }

            Player player = (Player) passenger;
            Chunk savedChunk = getSavedChunk(player);

            if (savedChunk == null) {
                return;
            }

            Chunk minecartChunk = event.getTo().getChunk();

            if (!savedChunk.equals(minecartChunk)) {
                minecart.remove();
                player.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "[B]" + " " + ChatColor.RED + "Your minecart has been despawned because it went outside of your saved chunk.");
            }
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Horse) {
            event.setCancelled(true);
        } else if (entity instanceof Pig) {
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        // Check if the player has the eco.bypass permission
        if (player.hasPermission("eco.bypass")) {
            return;
        }

        Chunk savedChunk = getSavedChunk(player);

        if (savedChunk == null) {
            return;
        }

        Chunk blockChunk = event.getBlock().getChunk();

        if (!savedChunk.equals(blockChunk)) {
            UUID sharedChunkOwner = ChunkUtils.getSharedChunkOwner(blockChunk);

            if (sharedChunkOwner != null) {
                if (sharedChunkOwner.equals(player.getUniqueId())) {
                    return;
                }

                event.setCancelled(true);
                player.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "[B]" + " " + ChatColor.RED + "You cannot break blocks in another player's shared chunk.");
            } else {
                event.setCancelled(true);
                player.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "[B]" + " " + ChatColor.RED + "You cannot break blocks in another chunk.");
            }
        }
    }
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        // Check if the player has the eco.bypass permission
        if (player.hasPermission("eco.bypass")) {
            return;
        }

        Chunk savedChunk = getSavedChunk(player);

        if (savedChunk == null) {
            return;
        }

        Chunk blockChunk = event.getBlock().getChunk();

        if (!savedChunk.equals(blockChunk)) {
            UUID sharedChunkOwner = ChunkUtils.getSharedChunkOwner(blockChunk);

            if (sharedChunkOwner != null) {
                if (sharedChunkOwner.equals(player.getUniqueId())) {
                    return;
                }

                event.setCancelled(true);
                player.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "[B]" + " " + ChatColor.RED + "You cannot place blocks in another player's shared chunk.");
            } else {
                event.setCancelled(true);
                player.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "[B]" + " " + ChatColor.RED + "You cannot place blocks in another chunk.");
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Check if the player has the eco.bypass permission
        if (player.hasPermission("eco.bypass")) {
            return;
        }

        Chunk savedChunk = getSavedChunk(player);

        if (savedChunk == null) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();

        if (clickedBlock == null) {
            return;
        }

        Material material = clickedBlock.getType();
        if (isRestricted(material)) {
            Chunk blockChunk = clickedBlock.getChunk();

            if (!savedChunk.equals(blockChunk)) {
                UUID sharedChunkOwner = ChunkUtils.getSharedChunkOwner(blockChunk);

                if (sharedChunkOwner != null) {
                    if (sharedChunkOwner.equals(player.getUniqueId())) {
                        return;
                    }

                    event.setCancelled(true);
                    player.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "[B]" + " " + ChatColor.RED + "You cannot interact with this block in another player's shared chunk.");
                } else {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "[B]" + " " + ChatColor.RED + "You cannot interact with this block in another chunk.");
                }
            }
        }
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

    private Location getRespawnLocation(String playerName) {
        Player player = Bukkit.getPlayerExact(playerName);

        if (player == null) {
            return null;
        }

        Chunk chunk = getSavedChunk(player);

        if (chunk == null) {
            return null;
        }

        int chunkX = chunk.getX() * 16 + 8;
        int chunkZ = chunk.getZ() * 16 + 8;
        int highestY = getHighestBlockY(chunk);

        if (chunkX <= 0 || chunkZ <= 0 || highestY <= 0) {
            // Invalid chunk data, unable to calculate respawn location
            return null;
        }

        Location respawnLocation = new Location(chunk.getWorld(), chunkX, highestY, chunkZ);

        if (!respawnLocation.getWorld().equals(player.getWorld())) {
            // Saved chunk is in a different world than the player's current world
            return null;
        }

        return respawnLocation;
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

    private boolean isRestricted(Material material) {
        return material == Material.CHEST || material == Material.TRAPPED_CHEST || material == Material.FURNACE
                || material == Material.DISPENSER || material == Material.DROPPER || material == Material.HOPPER
                || material == Material.BREWING_STAND || material == Material.SHULKER_BOX || material == Material.ENDER_CHEST
                || material == Material.ANVIL || material == Material.BEACON || material.name().contains("BED")
                || material == Material.BELL || material == Material.BLAST_FURNACE || material == Material.CAKE
                || material == Material.CAMPFIRE || material == Material.CARTOGRAPHY_TABLE || material == Material.COMPOSTER
                || material == Material.ENCHANTING_TABLE || material == Material.GRINDSTONE || material == Material.LECTERN
                || material == Material.LOOM || material == Material.SMITHING_TABLE || material == Material.SMOKER
                || material == Material.STONECUTTER;
    }
}