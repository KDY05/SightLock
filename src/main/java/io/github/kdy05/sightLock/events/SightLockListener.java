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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SightLockListener implements Listener {

    private final SightLock plugin;
    private final Map<UUID, SightLockTask> holders = new HashMap<>();
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
        long now = System.currentTimeMillis();
        UUID controllerId = controller.getUniqueId();
        if (lastClickTime.containsKey(controllerId) && now - lastClickTime.get(controllerId) < 300)
            return;
        lastClickTime.put(controllerId, now);

        UUID targetId = target.getUniqueId();

        if (holders.containsKey(targetId)) {
            // 고정 해제 로직
            holders.get(targetId).cancel();
            holders.remove(targetId);
            controller.sendMessage(plugin.getConfigManager().getMessage("sightlock.unlocked"));
        } else {
            // 고정 시작 로직
            SightLockTask holder = new SightLockTask(controller, target);
            holder.start();
            holders.put(targetId, holder);
            controller.sendMessage(plugin.getConfigManager().getMessage("sightlock.locked"));
        }
    }

    // 사망 시 자동 해제
    @EventHandler
    public void onTargetDeath(EntityDeathEvent event) {
        UUID id = event.getEntity().getUniqueId();
        if (holders.containsKey(id)) {
            holders.get(id).cancel();
            holders.remove(id);
        }
    }

    // 퇴장 시 자동 해제
    @EventHandler
    public void onTargetQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        if (holders.containsKey(id)) {
            holders.get(id).cancel();
            holders.remove(id);
        }
    }

}

