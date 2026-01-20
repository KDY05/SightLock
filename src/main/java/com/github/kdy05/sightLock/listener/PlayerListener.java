package com.github.kdy05.sightLock.listener;

import com.github.kdy05.sightLock.config.ConfigManager;
import com.github.kdy05.sightLock.core.EntityLockManager;
import com.github.kdy05.sightLock.core.EntityTracker;
import com.github.kdy05.sightLock.core.PlayerToggleService;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerListener implements Listener {
    
    private static final long CLICK_COOLDOWN_MS = 300L;
    private static final long CLEANUP_INTERVAL_MS = 60000L;
    private static final int MAX_CLICK_HISTORY_SIZE = 1000;
    
    private final EntityLockManager lockManager;
    private final PlayerToggleService toggleService;
    private final ConfigManager configManager;
    private final Map<UUID, Long> lastClickTimes = new ConcurrentHashMap<>();
    
    public PlayerListener(@NotNull EntityLockManager lockManager,
                          @NotNull PlayerToggleService toggleService,
                          @NotNull ConfigManager configManager) {
        this.lockManager = lockManager;
        this.toggleService = toggleService;
        this.configManager = configManager;
    }
    
    @EventHandler
    public void onPlayerInteractEntity(@NotNull PlayerInteractEntityEvent event) {
        Entity clickedEntity = event.getRightClicked();
        
        if (clickedEntity instanceof LivingEntity target) {
            handleSightLockToggle(event.getPlayer(), target);
        }
    }
    
    @EventHandler
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        handleSightLockToggle(event.getPlayer(), null);
    }
    
    @EventHandler
    public void onPlayerSwapItem(@NotNull PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        if (!isValidDistanceAdjustment(player, playerId)) {
            return;
        }
        
        int direction = player.isSneaking() ? -1 : 1;
        
        if (lockManager.adjustDistance(playerId, direction)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onEntityDeath(@NotNull EntityDeathEvent event) {
        UUID entityId = event.getEntity().getUniqueId();
        
        lockManager.removeTargetLock(entityId);
        
        if (event.getEntity() instanceof Player) {
            lockManager.removeLock(entityId);
            toggleService.disable(entityId);
        }
    }
    
    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        lockManager.removeLock(playerId);
        toggleService.disable(playerId);
        cleanupPlayerData(playerId);
    }
    
    private void handleSightLockToggle(@NotNull Player player, @Nullable LivingEntity target) {
        UUID playerId = player.getUniqueId();
        
        if (!isValidToggleRequest(player, playerId)) {
            return;
        }
        
        if (lockManager.hasLock(playerId)) {
            unlockEntity(player, playerId);
        } else if (target != null) {
            lockEntity(player, target);
        }
    }
    
    private boolean isValidToggleRequest(@NotNull Player player, @NotNull UUID playerId) {
        if (!toggleService.isEnabled(playerId)) {
            return false;
        }
        
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (heldItem.getType() != configManager.getTriggerItem()) {
            return false;
        }
        
        return !isDuplicateClick(playerId);
    }
    
    private boolean isValidDistanceAdjustment(@NotNull Player player, @NotNull UUID playerId) {
        if (!toggleService.isEnabled(playerId)) {
            return false;
        }
        
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (heldItem.getType() != configManager.getTriggerItem()) {
            return false;
        }
        
        return lockManager.hasLock(playerId);
    }
    
    private void lockEntity(@NotNull Player player, @NotNull LivingEntity target) {
        EntityTracker tracker = lockManager.createLock(player, target);
        
        if (tracker != null) {
            player.sendMessage(configManager.getMessage("sightlock.locked"));
        }
    }
    
    private void unlockEntity(@NotNull Player player, @NotNull UUID playerId) {
        if (lockManager.removeLock(playerId)) {
            player.sendMessage(configManager.getMessage("sightlock.unlocked"));
        }
    }
    
    private boolean isDuplicateClick(@NotNull UUID playerId) {
        long currentTime = System.currentTimeMillis();
        Long lastClickTime = lastClickTimes.get(playerId);
        
        if (lastClickTime != null && currentTime - lastClickTime < CLICK_COOLDOWN_MS) {
            return true;
        }
        
        lastClickTimes.put(playerId, currentTime);
        cleanupClickHistory(currentTime);
        
        return false;
    }
    
    private void cleanupClickHistory(long currentTime) {
        if (lastClickTimes.size() > MAX_CLICK_HISTORY_SIZE) {
            lastClickTimes.entrySet().removeIf(entry -> 
                currentTime - entry.getValue() > CLEANUP_INTERVAL_MS);
        }
    }
    
    private void cleanupPlayerData(@NotNull UUID playerId) {
        lastClickTimes.remove(playerId);
    }
}