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

    public PlayerDeathTrigger() {
        declare(CoreKeys.PLAYER, PlayerDeathEvent::getEntity);
        declare(CoreKeys.LOCATION, e -> e.getEntity().getLocation());
        declare(CoreKeys.DAMAGE_SOURCE, PlayerDeathEvent::getDamageSource);
        declare(CoreKeys.ITEM_DROPS, PlayerDeathEvent::getDrops);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        handleEvent(event);
    }

    @Override
    public NamespacedKey getKey() {
        return CoreTriggerKeys.PLAYER_DEATH;
    }
}