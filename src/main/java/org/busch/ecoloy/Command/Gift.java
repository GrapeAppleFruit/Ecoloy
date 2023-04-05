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
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Gift implements CommandExecutor {

    private final JavaPlugin plugin;

    public Gift(JavaPlugin plugin) {
        this.plugin = plugin;
    }



    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "[B]" + " " + ChatColor.RED + "Usage: /gift <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "[B]" + " " + ChatColor.RED + "Player not found.");
            return true;
        }

        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            sender.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "[B]" + " " + ChatColor.RED + "You are not holding anything to gift.");
            return true;
        }

        if (player.equals(target)) {
            sender.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "[B]" + " " + ChatColor.RED + "You cannot gift yourself.");
            return true;
        }

        target.getInventory().addItem(item);
        player.getInventory().removeItem(item);
        sender.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "[B]" + " " + ChatColor.GREEN + "You have gifted " + ChatColor.GOLD + target.getName() + ChatColor.GREEN + " a " + ChatColor.GOLD + item.getType().toString().toLowerCase() + ChatColor.GREEN + ".");
        target.sendMessage(ChatColor.WHITE + "" + ChatColor.BOLD + "[B]" + " " + ChatColor.GREEN + "You have received a " + ChatColor.GOLD + item.getType().toString().toLowerCase() + ChatColor.GREEN + " from " + ChatColor.GOLD + player.getName() + ChatColor.GREEN + ".");

        return true;
    }
}
