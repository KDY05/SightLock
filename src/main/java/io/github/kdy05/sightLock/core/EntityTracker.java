package io.github.kdy05.sightlock.core;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class EntityTracker {
    
    private static final double MIN_DISTANCE = 1.0;
    private static final double MAX_DISTANCE = 30.0;
    private static final long TICK_INTERVAL = 1L;
    
    private final Player controller;
    private final LivingEntity target;
    private double distance;
    private BukkitTask task;
    
    public EntityTracker(@NotNull Player controller, @NotNull LivingEntity target) {
        this.controller = controller;
        this.target = target;
        this.distance = calculateInitialDistance();
    }
    
    private double calculateInitialDistance() {
        return Math.max(MIN_DISTANCE, 
               Math.min(MAX_DISTANCE, 
                       controller.getLocation().distance(target.getLocation())));
    }
    
    public void adjustDistance(int direction) {
        distance += direction;
        distance = Math.max(MIN_DISTANCE, Math.min(MAX_DISTANCE, distance));
    }
    
    public void startTracking(@NotNull Object plugin) {
        if (task != null) {
            return;
        }
        
        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isValidState()) {
                    stopTracking();
                    return;
                }
                updateTargetPosition();
            }
        }.runTaskTimer((org.bukkit.plugin.Plugin) plugin, 0L, TICK_INTERVAL);
    }
    
    public void stopTracking() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
            task = null;
        }
    }
    
    private boolean isValidState() {
        return controller.isOnline() && target.isValid();
    }
    
    private void updateTargetPosition() {
        Location targetLocation = calculateTargetLocation();
        target.teleport(targetLocation);
    }
    
    @NotNull
    private Location calculateTargetLocation() {
        Location eyeLocation = controller.getEyeLocation();
        Vector direction = eyeLocation.getDirection();
        
        Location targetLocation = eyeLocation.clone().add(direction.multiply(distance));
        targetLocation.subtract(0, target.getHeight() / 2.0, 0);
        
        applyRotation(targetLocation);
        
        return targetLocation;
    }
    
    private void applyRotation(@NotNull Location targetLocation) {
        Vector toController = controller.getLocation().toVector().subtract(targetLocation.toVector());
        
        if (toController.lengthSquared() > 0) {
            toController.normalize();
            float yaw = (float) Math.toDegrees(Math.atan2(-toController.getX(), toController.getZ()));
            targetLocation.setYaw(yaw);
            
            applyPitch(targetLocation);
        }
    }
    
    private void applyPitch(@NotNull Location targetLocation) {
        Location targetEyeLocation = targetLocation.clone().add(0, target.getEyeHeight(), 0);
        Vector toController = controller.getEyeLocation().toVector().subtract(targetEyeLocation.toVector());
        
        if (toController.lengthSquared() > 0) {
            toController.normalize();
            double dy = toController.getY();
            double dxz = Math.sqrt(toController.getX() * toController.getX() + toController.getZ() * toController.getZ());
            float pitch = (float) Math.toDegrees(Math.atan2(-dy, dxz));
            targetLocation.setPitch(pitch);
        }
    }
    
    public UUID getTargetId() {
        return target.getUniqueId();
    }
    
    public UUID getControllerId() {
        return controller.getUniqueId();
    }
    
    public double getDistance() {
        return distance;
    }
    
    public boolean isActive() {
        return task != null && !task.isCancelled();
    }
}