package kostin.ak.actionstriggers.core.defaults.triggers;

import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.api.trigger.BukkitEventTrigger;
import kostin.ak.actionstriggers.core.CoreKeys;
import kostin.ak.actionstriggers.core.CoreTriggerKeys;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class PlayerConsumeTrigger extends BukkitEventTrigger<PlayerItemConsumeEvent> {

    private static final NamespacedKey KEY = CoreTriggerKeys.PLAYER_CONSUME;

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        handleEvent(event);
    }

    @Override
    protected ExecutionContext buildContext(PlayerItemConsumeEvent event) {
        ExecutionContext context = new ExecutionContext();
        context.set(CoreKeys.PLAYER, event.getPlayer());
        context.set(CoreKeys.ITEM, event.getItem());
        context.set(CoreKeys.LOCATION, event.getPlayer().getLocation());
        return context;
    }

    @Override
    public NamespacedKey getKey() {
        return KEY;
    }
}