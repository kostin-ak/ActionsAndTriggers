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

    private static final NamespacedKey KEY = CoreTriggerKeys.ENTITY_DEATH;

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDeath(EntityDeathEvent event) {
        handleEvent(event);
    }

    @Override
    protected ExecutionContext buildContext(EntityDeathEvent event) {
        ExecutionContext context = new ExecutionContext();

        // Кладем саму умершую сущность
        context.set(CoreKeys.ENTITY, event.getEntity());
        context.set(CoreKeys.LOCATION, event.getEntity().getLocation());

        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            context.set(CoreKeys.PLAYER, killer); // Игрок как участник ивента
            context.set(CoreKeys.KILLER, killer); // Он же как убийца явно
            context.set(CoreKeys.ITEM_IN_HAND, killer.getInventory().getItemInMainHand());
        }

        return context;
    }

    @Override
    public NamespacedKey getKey() {
        return KEY;
    }
}