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
    public PlayerDamageTrigger() {
        declare(CoreKeys.PLAYER, e -> (Player) e.getEntity());
        declare(CoreKeys.DAMAGE, EntityDamageEvent::getDamage);
        declare(CoreKeys.LOCATION, e -> e.getEntity().getLocation());
        declare(CoreKeys.DAMAGE_CAUSE, EntityDamageEvent::getCause);
        declare(CoreKeys.DAMAGER, e -> e instanceof EntityDamageByEntityEvent ev ? ev.getDamager() : null);
    }
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEvent(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) handleEvent(e);
    }
    @Override public NamespacedKey getKey() { return CoreTriggerKeys.PLAYER_DAMAGE; }
}