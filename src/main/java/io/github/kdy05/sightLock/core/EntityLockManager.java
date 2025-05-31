package io.github.kdy05.sightlock.core;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EntityLockManager {
    
    private final Map<UUID, EntityTracker> activeTrackers = new ConcurrentHashMap<>();
    private final Object plugin;
    
    public EntityLockManager(@NotNull Object plugin) {
        this.plugin = plugin;
    }
    
    @Nullable
    public EntityTracker createLock(@NotNull Player controller, @NotNull LivingEntity target) {
        UUID controllerId = controller.getUniqueId();
        
        if (activeTrackers.containsKey(controllerId)) {
            return null;
        }
        
        EntityTracker tracker = new EntityTracker(controller, target);
        tracker.startTracking(plugin);
        activeTrackers.put(controllerId, tracker);
        
        return tracker;
    }
    
    public boolean removeLock(@NotNull UUID controllerId) {
        EntityTracker tracker = activeTrackers.remove(controllerId);
        if (tracker != null) {
            tracker.stopTracking();
            return true;
        }
        return false;
    }
    
    public boolean removeTargetLock(@NotNull UUID targetId) {
        for (Map.Entry<UUID, EntityTracker> entry : activeTrackers.entrySet()) {
            if (entry.getValue().getTargetId().equals(targetId)) {
                EntityTracker tracker = activeTrackers.remove(entry.getKey());
                tracker.stopTracking();
                return true;
            }
        }
        return false;
    }
    
    @Nullable
    public EntityTracker getLock(@NotNull UUID controllerId) {
        return activeTrackers.get(controllerId);
    }
    
    public boolean hasLock(@NotNull UUID controllerId) {
        return activeTrackers.containsKey(controllerId);
    }
    
    public void removeAllLocks() {
        activeTrackers.values().forEach(EntityTracker::stopTracking);
        activeTrackers.clear();
    }
    
    public int getActiveLockCount() {
        return activeTrackers.size();
    }
    
    public boolean adjustDistance(@NotNull UUID controllerId, int direction) {
        EntityTracker tracker = activeTrackers.get(controllerId);
        if (tracker != null) {
            tracker.adjustDistance(direction);
            return true;
        }
        return false;
    }
}