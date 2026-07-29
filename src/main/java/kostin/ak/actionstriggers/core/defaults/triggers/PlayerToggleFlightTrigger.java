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
    public PlayerToggleFlightTrigger() {
        declare(CoreKeys.PLAYER, PlayerToggleFlightEvent::getPlayer);
        declare(CoreKeys.LOCATION, e -> e.getPlayer().getLocation());
        declare(CoreKeys.IS_FLYING, PlayerToggleFlightEvent::isFlying);
    }
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEvent(PlayerToggleFlightEvent e) { handleEvent(e); }
    @Override public NamespacedKey getKey() { return CoreTriggerKeys.PLAYER_FLIGHT; }
}