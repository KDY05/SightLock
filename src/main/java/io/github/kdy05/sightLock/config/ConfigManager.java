package io.github.kdy05.sightLock.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;
    private FileConfiguration langConfig;
    private File langFile;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfigs();
    }

    private void loadConfigs() {
        // Save default config
        plugin.saveDefaultConfig();
        config = plugin.getConfig();

        // Set up language file
        langFile = new File(plugin.getDataFolder(), "lang.yml");
        if (!langFile.exists()) {
            plugin.saveResource("lang.yml", false);
        }
        langConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    /**
     * reloads all the configs.
     */
    public void reloadConfigs() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        langConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getLangConfig() {
        return langConfig;
    }

    /**
     * gets message from lang.yml.
     * @param path yaml path
     * @return message with color codes translated
     */
    public String getMessage(String path) {
        String message = langConfig.getString(path, "Missing message: " + path);
        return ChatColor.translateAlternateColorCodes('&', message);
    }

}
