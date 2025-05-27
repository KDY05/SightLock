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
    private final double initialDistance;
    private BukkitTask task;

    public SightLockTask(Player controller, LivingEntity target) {
        this.controller = controller;
        this.target = target;
        this.initialDistance = controller.getLocation().distance(target.getLocation());
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
        }.runTaskTimer(SightLock.getPlugin(), 0L, 1L);
    }

    @NotNull
    private Location getTargetLoc() {
        // x, y, z 계산
        Location eye = controller.getEyeLocation();
        Vector offset = eye.getDirection().normalize().multiply(initialDistance);
        Location targetLoc = eye.add(offset);
        targetLoc.subtract(0, target.getHeight() / 2.0, 0); // 중심 보정

        // 시선 방향 계산
        Vector toController = controller.getLocation().toVector().subtract(targetLoc.toVector()).normalize();
        float yaw = (float) Math.toDegrees(Math.atan2(-toController.getX(), toController.getZ()));
        targetLoc.setYaw(yaw);
        targetLoc.setPitch(0);

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
