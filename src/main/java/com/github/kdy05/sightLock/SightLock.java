package com.github.kdy05.sightLock;

import com.github.kdy05.sightLock.command.SightLockCommand;
import com.github.kdy05.sightLock.config.ConfigManager;
import com.github.kdy05.sightLock.core.EntityLockManager;
import com.github.kdy05.sightLock.core.PlayerToggleService;
import com.github.kdy05.sightLock.listener.PlayerListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class SightLock extends JavaPlugin {
    
    private ConfigManager configManager;
    private PlayerToggleService toggleService;
    private EntityLockManager lockManager;
    private PlayerListener eventListener;
    private SightLockCommand commandExecutor;
    
    @Override
    public void onEnable() {
        try {
            initializeServices();
            registerCommands();
            registerEvents();
            getLogger().info(configManager.getMessage("plugin.enabled"));
        }
        catch (Exception e) {
            getLogger().severe(configManager.getMessage("plugin.enabled-failed") + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    @Override
    public void onDisable() {
        cleanupResources();
        
        if (configManager != null) {
            getLogger().info(configManager.getMessage("plugin.disabled"));
        }
    }
    
    private void initializeServices() {
        configManager = new ConfigManager(this);
        toggleService = new PlayerToggleService();
        lockManager = new EntityLockManager(this);
        
        eventListener = new PlayerListener(lockManager, toggleService, configManager);
        commandExecutor = new SightLockCommand(toggleService, configManager);
    }
    
    private void registerCommands() {
        Objects.requireNonNull(getCommand("sightlock"))
                .setExecutor(commandExecutor);
        
        Objects.requireNonNull(getCommand("sightlock"))
                .setTabCompleter(commandExecutor);
    }
    
    private void registerEvents() {
        getServer().getPluginManager().registerEvents(eventListener, this);
    }
    
    private void cleanupResources() {
        if (lockManager != null) {
            lockManager.removeAllLocks();
        }
        
        if (toggleService != null) {
            toggleService.disableAll();
        }
    }
    
    @NotNull
    public ConfigManager getConfigurationManager() {
        return configManager;
    }
    
    @NotNull
    public PlayerToggleService getToggleService() {
        return toggleService;
    }
    
    @NotNull
    public EntityLockManager getLockManager() {
        return lockManager;
    }
}