package kostin.ak.actionstriggers.core.defaults.triggers;

import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.api.trigger.BukkitEventTrigger;
import kostin.ak.actionstriggers.core.CoreKeys;
import kostin.ak.actionstriggers.core.CoreTriggerKeys;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;

public class EntityDeathTrigger extends BukkitEventTrigger<EntityDeathEvent> {
    public EntityDeathTrigger() {
        declare(CoreKeys.ENTITY, EntityDeathEvent::getEntity);
        declare(CoreKeys.LOCATION, e -> e.getEntity().getLocation());
        declare(CoreKeys.PLAYER, e -> e.getEntity().getKiller());
        declare(CoreKeys.KILLER, e -> e.getEntity().getKiller());
        declare(CoreKeys.ITEM_IN_HAND, e -> e.getEntity().getKiller() != null ? e.getEntity().getKiller().getInventory().getItemInMainHand() : null);
    }
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEvent(EntityDeathEvent e) { handleEvent(e); }
    @Override public NamespacedKey getKey() { return CoreTriggerKeys.ENTITY_DEATH; }
}