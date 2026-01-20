package com.github.kdy05.sightLock.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.logging.Level;

public class ConfigManager {
    private static final String TRIGGER_ITEM_KEY = "tool-material";
    private static final String DEFAULT_TRIGGER_ITEM = "NETHERITE_HOE";

    private final JavaPlugin plugin;
    private FileConfiguration config;
    private FileConfiguration languageConfig;
    private File languageFile;
    
    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadMainConfiguration();
        loadLanguageConfiguration();
    }
    
    private void loadMainConfiguration() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
    }
    
    private void loadLanguageConfiguration() {
        languageFile = new File(plugin.getDataFolder(), "lang.yml");
        if (!languageFile.exists()) {
            plugin.saveResource("lang.yml", false);
        }
        languageConfig = YamlConfiguration.loadConfiguration(languageFile);
    }
    
    public void reloadConfigurations() {
        try {
            plugin.reloadConfig();
            config = plugin.getConfig();
            languageConfig = YamlConfiguration.loadConfiguration(languageFile);
        }
        catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to reload configurations", e);
        }
    }

    @NotNull
    public Component getMessage(String path) {
        TagResolver resolver = TagResolver.resolver(
                Placeholder.parsed("prefix", "<gold>[SightLock] <white>")
        );
        String message = languageConfig.getString(path, "Missing message: " + path);
        return MiniMessage.miniMessage().deserialize(message, resolver);
    }

    @NotNull
    public Material getTriggerItem() {
        String itemName = config.getString(TRIGGER_ITEM_KEY, DEFAULT_TRIGGER_ITEM).toUpperCase();
        try {
            return Material.valueOf(itemName);
        }
        catch (IllegalArgumentException e) {
            plugin.getLogger().warning(String.format(
                    "Invalid trigger item '%s' in config, using default: %s", itemName, DEFAULT_TRIGGER_ITEM)
            );
            return Material.valueOf(DEFAULT_TRIGGER_ITEM);
        }
    }

    @SuppressWarnings("unused")
    @NotNull public FileConfiguration getMainConfig() {
        return config;
    }

    @SuppressWarnings("unused")
    @NotNull public FileConfiguration getLanguageConfig() {
        return languageConfig;
    }

}