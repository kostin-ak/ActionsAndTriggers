package kostin.ak.actionstriggers.core.defaults.triggers;

import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.api.trigger.BukkitEventTrigger;
import kostin.ak.actionstriggers.core.CoreKeys;
import kostin.ak.actionstriggers.core.CoreTriggerKeys;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class PlayerDamageTrigger extends BukkitEventTrigger<EntityDamageEvent> {

    private static final NamespacedKey KEY = CoreTriggerKeys.PLAYER_DAMAGE;

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            handleEvent(event);
        }
    }

    @Override
    protected ExecutionContext buildContext(EntityDamageEvent event) {
        ExecutionContext context = new ExecutionContext();
        Player player = (Player) event.getEntity();

        context.set(CoreKeys.PLAYER, player);
        context.set(CoreKeys.DAMAGE, event.getDamage());
        context.set(CoreKeys.LOCATION, player.getLocation());
        context.set(CoreKeys.DAMAGE_CAUSE, event.getCause());

        if (event instanceof EntityDamageByEntityEvent damageByEntityEvent) {
            context.set(CoreKeys.DAMAGER, damageByEntityEvent.getDamager());
        }

        return context;
    }

    @Override
    public NamespacedKey getKey() {
        return KEY;
    }
}