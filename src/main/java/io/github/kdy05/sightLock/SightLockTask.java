package io.github.kdy05.sightLock;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SightLockTask {

    private final Player controller;
    private final LivingEntity target;
    private double distance;
    private BukkitTask task;

    public SightLockTask(Player controller, LivingEntity target) {
        this.controller = controller;
        this.target = target;
        this.distance = controller.getLocation().distance(target.getLocation());
    }

    public void adjustDistance(int direction) {
        distance += direction; // +1 or -1
        distance = Math.max(1.0, Math.min(30, distance)); // 범위 제한
    }

    public void start() {
        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!controller.isOnline() || !target.isValid()) {
                    cancel();
                    return;
                }
                target.teleport(getTargetLoc());
            }
        }.runTaskTimer(SightLock.getPlugin(), 0L, 2L);
    }

    @NotNull
    private Location getTargetLoc() {
        Location eye = controller.getEyeLocation();
        Vector direction = eye.getDirection();
        
        Location targetLoc = eye.clone().add(direction.multiply(distance));
        targetLoc.subtract(0, target.getHeight() / 2.0, 0);

        Vector toController = controller.getLocation().toVector().subtract(targetLoc.toVector());
        if (toController.lengthSquared() > 0) {
            toController.normalize();
            float yaw = (float) Math.toDegrees(Math.atan2(-toController.getX(), toController.getZ()));
            targetLoc.setYaw(yaw);

            Location targetEye = targetLoc.clone().add(0, target.getEyeHeight(), 0);
            Vector toCtrl = controller.getEyeLocation().toVector().subtract(targetEye.toVector());
            if (toCtrl.lengthSquared() > 0) {
                toCtrl.normalize();
                double dy = toCtrl.getY();
                double dxz = Math.sqrt(toCtrl.getX() * toCtrl.getX() + toCtrl.getZ() * toCtrl.getZ());
                float pitch = (float) Math.toDegrees(Math.atan2(-dy, dxz));
                targetLoc.setPitch(pitch);
            }
        }

        return targetLoc;
    }

    public void cancel() {
        if (task != null) {
            task.cancel();
        }
    }

    public UUID getTargetId() {
        return target.getUniqueId();
    }
}
