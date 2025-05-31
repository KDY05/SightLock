package io.github.kdy05.sightlock.config;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.logging.Level;

public class ConfigurationManager {
    
    private static final String CONFIG_FILE = "config.yml";
    private static final String LANG_FILE = "lang.yml";
    private static final String DEFAULT_TRIGGER_ITEM = "NETHERITE_HOE";
    private static final String TRIGGER_ITEM_KEY = "tool-material";
    
    private final JavaPlugin plugin;
    private FileConfiguration config;
    private FileConfiguration languageConfig;
    private File languageFile;
    
    public ConfigurationManager(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        initializeConfigurations();
    }
    
    private void initializeConfigurations() {
        loadMainConfiguration();
        loadLanguageConfiguration();
    }
    
    private void loadMainConfiguration() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
    }
    
    private void loadLanguageConfiguration() {
        languageFile = new File(plugin.getDataFolder(), LANG_FILE);
        
        if (!languageFile.exists()) {
            plugin.saveResource(LANG_FILE, false);
        }
        
        languageConfig = YamlConfiguration.loadConfiguration(languageFile);
    }
    
    public void reloadConfigurations() {
        try {
            plugin.reloadConfig();
            config = plugin.getConfig();
            languageConfig = YamlConfiguration.loadConfiguration(languageFile);
            
            plugin.getLogger().info("Configurations reloaded successfully");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to reload configurations", e);
        }
    }
    
    @NotNull
    public FileConfiguration getMainConfig() {
        return config;
    }
    
    @NotNull
    public FileConfiguration getLanguageConfig() {
        return languageConfig;
    }
    
    @NotNull
    public String getMessage(@NotNull String path) {
        String message = languageConfig.getString(path, "Missing message: " + path);
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    @NotNull
    public Material getTriggerItem() {
        String itemName = config.getString(TRIGGER_ITEM_KEY, DEFAULT_TRIGGER_ITEM).toUpperCase();
        
        try {
            return Material.valueOf(itemName);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning(
                String.format("Invalid trigger item '%s' in config, using default: %s", 
                              itemName, DEFAULT_TRIGGER_ITEM)
            );
            return Material.valueOf(DEFAULT_TRIGGER_ITEM);
        }
    }
    
    public boolean isValidConfiguration() {
        return config != null && languageConfig != null;
    }
}