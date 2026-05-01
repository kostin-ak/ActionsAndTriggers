package kostin.ak.actionstriggers.core.defaults.triggers;

import kostin.ak.actionstriggers.api.ActionTriggerAPI;
import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.api.trigger.Trigger;
import kostin.ak.actionstriggers.core.CoreKeys;
import kostin.ak.actionstriggers.core.CoreTriggerKeys;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class PlayerDamageTriggerListener extends Trigger {

    private static final NamespacedKey KEY = CoreTriggerKeys.PLAYER_DAMAGE;

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {
        // Проверяем, что урон получил именно Игрок
        if (event.getEntity() instanceof Player player) {

            ExecutionContext context = new ExecutionContext();
            context.set(CoreKeys.PLAYER, player);
            context.set(CoreKeys.DAMAGE, event.getDamage());
            context.set(CoreKeys.LOCATION, player.getLocation());

            context.set(CoreKeys.DAMAGE_CAUSE, event.getCause());

            if (event instanceof EntityDamageByEntityEvent damageByEntityEvent) {
                context.set(CoreKeys.DAMAGER, damageByEntityEvent.getDamager());
            }


            // Вызываем триггер!
            ActionTriggerAPI.getTriggers().dispatch(KEY, context);

            // Если кто-то из подписчиков отменил урон — применяем это к ивенту
            if (context.isCancelled()) {
                event.setCancelled(true);
            }
        }
    }
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }
}