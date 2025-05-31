package io.github.kdy05.sightlock;

import io.github.kdy05.sightlock.command.SightLockCommandExecutor;
import io.github.kdy05.sightlock.config.ConfigurationManager;
import io.github.kdy05.sightlock.core.EntityLockManager;
import io.github.kdy05.sightlock.core.PlayerToggleService;
import io.github.kdy05.sightlock.listener.PlayerInteractionListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class SightLock extends JavaPlugin {
    
    private ConfigurationManager configManager;
    private PlayerToggleService toggleService;
    private EntityLockManager lockManager;
    private PlayerInteractionListener eventListener;
    private SightLockCommandExecutor commandExecutor;
    
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
        configManager = new ConfigurationManager(this);
        toggleService = new PlayerToggleService();
        lockManager = new EntityLockManager(this);
        
        eventListener = new PlayerInteractionListener(lockManager, toggleService, configManager);
        commandExecutor = new SightLockCommandExecutor(toggleService, configManager);
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
    public ConfigurationManager getConfigurationManager() {
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