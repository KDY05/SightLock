package com.github.kdy05.sightLock.core;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerToggleService {
    
    private final Set<UUID> enabledPlayers = ConcurrentHashMap.newKeySet();
    
    public void toggle(UUID playerId) {
        if (enabledPlayers.contains(playerId)) {
            enabledPlayers.remove(playerId);
        } else {
            enabledPlayers.add(playerId);
        }
    }
    
    public boolean isEnabled(UUID playerId) {
        return enabledPlayers.contains(playerId);
    }
    
    public void disable(UUID playerId) {
        enabledPlayers.remove(playerId);
    }
    
    public void disableAll() {
        enabledPlayers.clear();
    }
    
}