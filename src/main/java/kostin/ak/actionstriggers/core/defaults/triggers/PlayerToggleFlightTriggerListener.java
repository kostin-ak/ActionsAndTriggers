package kostin.ak.actionstriggers.core.defaults.triggers;

import kostin.ak.actionstriggers.api.ActionTriggerAPI;
import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.api.trigger.Trigger;
import kostin.ak.actionstriggers.core.CoreKeys;
import kostin.ak.actionstriggers.core.CoreTriggerKeys;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerToggleFlightEvent;

public class PlayerToggleFlightTriggerListener extends Trigger {
    private static final NamespacedKey KEY = CoreTriggerKeys.PLAYER_FLIGHT;

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        ExecutionContext context = new ExecutionContext();
        context.set(CoreKeys.PLAYER, event.getPlayer());
        context.set(CoreKeys.LOCATION, event.getPlayer().getLocation());
        context.set(CoreKeys.IS_FLYING, event.isFlying());

        ActionTriggerAPI.getTriggers().dispatch(KEY, context);

        if (context.isCancelled()) {
            event.setCancelled(true);
        }
    }

    @Override
    public NamespacedKey getKey() { return KEY; }
}