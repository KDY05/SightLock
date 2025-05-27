package io.github.kdy05.sightLock.events;

import io.github.kdy05.sightLock.SightLockTask;
import io.github.kdy05.sightLock.SightLock;
import io.github.kdy05.sightLock.SightLockToggle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SightLockListener implements Listener {

    private final SightLock plugin;
    private final Map<UUID, SightLockTask> activeLocks = new HashMap<>();
    private final Map<UUID, Long> lastClickTime = new HashMap<>();

    public SightLockListener(SightLock plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Entity clicked = event.getRightClicked();
        if (clicked instanceof LivingEntity target) {
            toggleSightLock(event.getPlayer(), target);
        }
    }

    // 허공을 포함하여 무엇을 우클릭하든 고정을 해제할 수 있음
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        toggleSightLock(event.getPlayer(), null); // 고정 없이 해제만 가능
    }

    @EventHandler
    public void onPlayerSwapItem(PlayerSwapHandItemsEvent event) {
        Player controller = event.getPlayer();
        UUID id = controller.getUniqueId();

        // 기본 조건 검사
        if (!SightLockToggle.isEnabled(id)) return;
        ItemStack item = controller.getInventory().getItemInMainHand();
        if (item.getType() != plugin.getConfigManager().getTriggerItem()) return;
        if (!activeLocks.containsKey(id)) return;

        int direction = controller.isSneaking() ? -1 : 1;
        SightLockTask task = activeLocks.get(id);
        task.adjustDistance(direction);

        event.setCancelled(true);
    }

    // 사망 시 자동 해제
    @EventHandler
    public void onPlayerDeath(EntityDeathEvent event) {
        UUID id = event.getEntity().getUniqueId();
        deactiveLock(id);
    }

    // 퇴장 시 자동 해제
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        deactiveLock(id);
    }

    private void toggleSightLock(Player controller, @Nullable LivingEntity target) {
        UUID id = controller.getUniqueId();

        if (!SightLockToggle.isEnabled(id)) return;
        ItemStack item = controller.getInventory().getItemInMainHand();
        if (item.getType() != plugin.getConfigManager().getTriggerItem()) return;
        if (isDuplicateClick(id)) return;

        if (activeLocks.containsKey(id)) {
            activeLocks.get(id).cancel();
            activeLocks.remove(id);
            controller.sendMessage(plugin.getConfigManager().getMessage("sightlock.unlocked"));
        } else if (target != null) {
            SightLockTask lockTask = new SightLockTask(controller, target);
            lockTask.start();
            activeLocks.put(id, lockTask);
            controller.sendMessage(plugin.getConfigManager().getMessage("sightlock.locked"));
        }
    }

    private boolean isDuplicateClick(UUID playerId) {
        long now = System.currentTimeMillis();
        if (lastClickTime.containsKey(playerId) && now - lastClickTime.get(playerId) < 300) return true;
        lastClickTime.put(playerId, now);
        return false;
    }

    private void deactiveLock(UUID id) {
        // 컨트롤러가 관련된 경우
        if (activeLocks.containsKey(id)) {
            activeLocks.get(id).cancel();
            activeLocks.remove(id);
        }

        // 타겟이 관련된 경우
        for (Map.Entry<UUID, SightLockTask> entry : activeLocks.entrySet()) {
            SightLockTask task = entry.getValue();
            if (task.getTargetId().equals(id)) {
                task.cancel();
                activeLocks.remove(entry.getKey()); // controllerId 기준 제거
                // 메시지 보내기
                Player controller = plugin.getServer().getPlayer(entry.getKey());
                if (controller != null && controller.isOnline()) {
                    controller.sendMessage(plugin.getConfigManager().getMessage("sightlock.unlocked"));
                }
                break; // 1:1 대응이므로 바로 중단
            }
        }
    }

}
