package kostin.ak.actionstriggers.core.defaults.triggers;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.api.trigger.BukkitEventTrigger;
import kostin.ak.actionstriggers.core.CoreKeys;
import kostin.ak.actionstriggers.core.CoreTriggerKeys;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class PlayerJumpTrigger extends BukkitEventTrigger<PlayerJumpEvent> {
    public PlayerJumpTrigger() {
        declare(CoreKeys.PLAYER, PlayerJumpEvent::getPlayer);
        declare(CoreKeys.LOCATION, PlayerJumpEvent::getFrom);
    }
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEvent(PlayerJumpEvent e) { handleEvent(e); }
    @Override public NamespacedKey getKey() { return CoreTriggerKeys.PLAYER_JUMP; }
}