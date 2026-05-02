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
    private static final NamespacedKey KEY = CoreTriggerKeys.PLAYER_JUMP;

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onJump(PlayerJumpEvent event) {
        handleEvent(event);
    }

    @Override
    protected ExecutionContext buildContext(PlayerJumpEvent event) {
        ExecutionContext context = new ExecutionContext();
        context.set(CoreKeys.PLAYER, event.getPlayer());
        context.set(CoreKeys.LOCATION, event.getFrom());
        return context;
    }

    @Override
    public NamespacedKey getKey() { return KEY; }
}