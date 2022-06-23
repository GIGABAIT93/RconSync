package com.vertisanpro.rconsync;

import com.vertisanpro.rconsync.commands.RconCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class RconSync extends JavaPlugin {

    private static RconSync instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        Message.load(getConfig());
        new RconCommand();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static RconSync getInstance() {
        return instance;
    }
}
