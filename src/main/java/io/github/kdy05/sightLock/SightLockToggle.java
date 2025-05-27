package io.github.kdy05.sightLock;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SightLockToggle {

    private static final Set<UUID> toggledPlayers = new HashSet<>();

    public static void toggle(UUID playerId) {
        if (toggledPlayers.contains(playerId)) {
            toggledPlayers.remove(playerId);
        } else {
            toggledPlayers.add(playerId);
        }
    }

    public static boolean isEnabled(UUID playerId) {
        return toggledPlayers.contains(playerId);
    }

    public static void disable(UUID playerId) {
        toggledPlayers.remove(playerId);
    }

    public static void clearAll() {
        toggledPlayers.clear();
    }

}
