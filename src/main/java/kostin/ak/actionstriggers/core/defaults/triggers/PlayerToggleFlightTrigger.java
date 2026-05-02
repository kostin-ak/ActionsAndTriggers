package kostin.ak.actionstriggers.core.defaults.triggers;

import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.api.trigger.BukkitEventTrigger;
import kostin.ak.actionstriggers.core.CoreKeys;
import kostin.ak.actionstriggers.core.CoreTriggerKeys;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerToggleFlightEvent;

public class PlayerToggleFlightTrigger extends BukkitEventTrigger<PlayerToggleFlightEvent> {
    private static final NamespacedKey KEY = CoreTriggerKeys.PLAYER_FLIGHT;

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        handleEvent(event);
    }

    @Override
    protected ExecutionContext buildContext(PlayerToggleFlightEvent event) {
        ExecutionContext context = new ExecutionContext();
        context.set(CoreKeys.PLAYER, event.getPlayer());
        context.set(CoreKeys.LOCATION, event.getPlayer().getLocation());
        context.set(CoreKeys.IS_FLYING, event.isFlying());
        return context;
    }

    @Override
    public NamespacedKey getKey() { return KEY; }
}