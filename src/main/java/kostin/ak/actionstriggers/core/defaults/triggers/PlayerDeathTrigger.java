package kostin.ak.actionstriggers.core.defaults.triggers;

import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.api.trigger.BukkitEventTrigger;
import kostin.ak.actionstriggers.core.CoreKeys;
import kostin.ak.actionstriggers.core.CoreTriggerKeys;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathTrigger extends BukkitEventTrigger<PlayerDeathEvent> {

    private static final NamespacedKey KEY = CoreTriggerKeys.PLAYER_DEATH;

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        handleEvent(event);
    }

    @Override
    protected ExecutionContext buildContext(PlayerDeathEvent event) {
        ExecutionContext context = new ExecutionContext();
        context.set(CoreKeys.PLAYER, event.getEntity());
        context.set(CoreKeys.LOCATION, event.getEntity().getLocation());
        context.set(CoreKeys.DAMAGE_SOURCE, event.getDamageSource());
        context.set(CoreKeys.ITEM_DROPS, event.getDrops());

        // Если убил другой игрок, можно положить его в контекст (например, под новым ключом KILLER)
        if (event.getEntity().getKiller() != null) {
            // Пока просто заменяем "игрока" на убитого, но в будущем можно добавить CoreKeys.KILLER
        }

        return context;
    }

    @Override
    public NamespacedKey getKey() {
        return KEY;
    }
}