package org.busch.ecoloy.Command;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class StarterKitCommand implements CommandExecutor {

    private Set<UUID> usedStarterKits = new HashSet<>();

    private final ItemStack[] starterKitItems = new ItemStack[] {
            new ItemStack(Material.DIAMOND_PICKAXE),
            new ItemStack(Material.DIAMOND_SHOVEL),
            new ItemStack(Material.DIAMOND_AXE),
            new ItemStack(Material.BREAD, 16),
            new ItemStack(Material.COOKED_BEEF, 16),
            new ItemStack(Material.TORCH, 64)
    };

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be executed by a player!");
            return true;
        }

        Player player = (Player) sender;

        if (usedStarterKits.contains(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You have already used the starter kit command!");
            return true;
        }

        for (ItemStack item : starterKitItems) {
            player.getInventory().addItem(item);
        }

        player.sendMessage(ChatColor.GREEN + "You have been given a starter kit!");
        usedStarterKits.add(player.getUniqueId());

        return true;
    }

}
