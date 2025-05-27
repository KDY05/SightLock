package io.github.kdy05.sightLock;

import io.github.kdy05.sightLock.commands.SightLockCommand;
import io.github.kdy05.sightLock.config.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class SightLock extends JavaPlugin {

    private static JavaPlugin plugin;
    private ConfigManager configManager;

    public static final String PREFIX = ChatColor.GOLD + "[SightLock] " + ChatColor.WHITE;

    @Override
    public void onEnable() {
        plugin = this;
        configManager = new ConfigManager(this);

        Objects.requireNonNull(getServer().getPluginCommand("sightlock"))
                .setExecutor(new SightLockCommand(this));

        getLogger().info(configManager.getMessage("plugin.enabled"));
    }

    @Override
    public void onDisable() {
        plugin = null;
        configManager = null;

        getLogger().info(configManager.getMessage("plugin.disabled"));
    }

    public static JavaPlugin getPlugin() {
        return plugin;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

}
