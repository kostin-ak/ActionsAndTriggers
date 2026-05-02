package kostin.ak.actionstriggers.core.defaults.triggers;

import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.api.trigger.BukkitEventTrigger;
import kostin.ak.actionstriggers.core.CoreKeys;
import kostin.ak.actionstriggers.core.CoreTriggerKeys;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerLevelChangeEvent;

public class PlayerLevelChangeTrigger extends BukkitEventTrigger<PlayerLevelChangeEvent> {

    private static final NamespacedKey KEY = CoreTriggerKeys.PLAYER_LEVEL_CHANGE;

    @EventHandler
    public void onLevelChange(PlayerLevelChangeEvent event) {
        handleEvent(event);
    }

    @Override
    protected ExecutionContext buildContext(PlayerLevelChangeEvent event) {
        ExecutionContext context = new ExecutionContext();
        context.set(CoreKeys.PLAYER, event.getPlayer());
        context.set(CoreKeys.LEVEL, event.getNewLevel());
        return context;
    }

    @Override
    public NamespacedKey getKey() {
        return KEY;
    }
}