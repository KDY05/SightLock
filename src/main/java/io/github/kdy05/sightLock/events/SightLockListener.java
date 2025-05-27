package io.github.kdy05.sightLock.events;

import io.github.kdy05.sightLock.SightLockTask;
import io.github.kdy05.sightLock.SightLock;
import io.github.kdy05.sightLock.SightLockToggle;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

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
        Player controller = event.getPlayer();

        // LivingEntity 대상으로 실행
        Entity clicked = event.getRightClicked();
        if (!(clicked instanceof LivingEntity target)) return;

        // 실행 조건
        ItemStack item = controller.getInventory().getItemInMainHand();
        if (!SightLockToggle.isEnabled(controller.getUniqueId())) return;
        if (item.getType() != Material.NETHERITE_HOE) return;

        // 이벤트 연속 실행 방지
        UUID controllerId = controller.getUniqueId();
        if (isDuplicateClick(controllerId)) return;

        if (activeLocks.containsKey(controllerId)) {
            // 고정 해제 로직
            activeLocks.get(controllerId).cancel();
            activeLocks.remove(controllerId);
            controller.sendMessage(plugin.getConfigManager().getMessage("sightlock.unlocked"));
        } else {
            // 고정 시작 로직
            SightLockTask holder = new SightLockTask(controller, target);
            holder.start();
            activeLocks.put(controllerId, holder);
            controller.sendMessage(plugin.getConfigManager().getMessage("sightlock.locked"));
        }
    }

    // 허공을 포함하여 무엇을 우클릭하든 고정을 해제할 수 있음
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID id = player.getUniqueId();
        if (isDuplicateClick(id)) return; // 이벤트 중복 실행 방지

        if (activeLocks.containsKey(id)) {
            // 고정 해제 로직
            activeLocks.get(id).cancel();
            activeLocks.remove(id);
            player.sendMessage(plugin.getConfigManager().getMessage("sightlock.unlocked"));
        }
    }

    private boolean isDuplicateClick(UUID playerId) {
        long now = System.currentTimeMillis();
        if (lastClickTime.containsKey(playerId) && now - lastClickTime.get(playerId) < 300) return true;
        lastClickTime.put(playerId, now);
        return false;
    }

    // 사망 시 자동 해제
    @EventHandler
    public void onPlayerDeath(EntityDeathEvent event) {
        UUID id = event.getEntity().getUniqueId();

        // 컨트롤러가 사망하는 경우
        if (activeLocks.containsKey(id)) {
            activeLocks.get(id).cancel();
            activeLocks.remove(id);
        }

        // 타겟이 사망하는 경우
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

    // 퇴장 시 자동 해제
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();

        // 컨트롤러가 퇴장하는 경우
        if (activeLocks.containsKey(id)) {
            activeLocks.get(id).cancel();
            activeLocks.remove(id);
        }

        // 타겟이 퇴장하는 경우
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
