package org.busch.ecoloy;

import org.bukkit.plugin.java.JavaPlugin;
import org.busch.ecoloy.Chunk.ChunkListener;
import org.busch.ecoloy.Chunk.ChunkTp;
import org.busch.ecoloy.Chunk.GiveChunk;
import org.busch.ecoloy.Command.Gift;
import org.busch.ecoloy.Command.SetSpawn;
import org.busch.ecoloy.Command.Share;
import org.busch.ecoloy.Command.StarterKitCommand;
import org.busch.ecoloy.Event.PlayerListener;

public class Ecoloy extends JavaPlugin {

    private static Ecoloy instance = null;

    @Override
    public void onEnable() {
        getServer().dispatchCommand(getServer().getConsoleSender(), "chunky world world");
        getServer().dispatchCommand(getServer().getConsoleSender(), "chunky start");
        // Register the /givechunk command
        getCommand("givechunk").setExecutor(new GiveChunk(this));
        getCommand("chunk").setExecutor(new ChunkTp(this));
        getCommand("gift").setExecutor(new Gift(this));
        getCommand("share").setExecutor(new Share(this));
        getCommand("setspawn").setExecutor(new SetSpawn());
        getCommand("spawn").setExecutor(new SetSpawn());
        getCommand("kit").setExecutor(new StarterKitCommand());
        getServer().getPluginManager().registerEvents(new ChunkListener(this), this);
        getServer().getPluginManager().registerEvents(new Share(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    public static Ecoloy getInstance() {
        return JavaPlugin.getPlugin(Ecoloy.class);
    }
}