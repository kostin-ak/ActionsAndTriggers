package kostin.ak.actionstriggers.core.defaults.triggers;

import kostin.ak.actionstriggers.api.ActionTriggerAPI;
import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.api.trigger.Trigger;
import kostin.ak.actionstriggers.core.CoreKeys;
import kostin.ak.actionstriggers.core.CoreTriggerKeys;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerLevelChangeEvent;

public class PlayerLevelChangeTriggerListener extends Trigger {

    private static final NamespacedKey KEY = CoreTriggerKeys.PLAYER_LEVEL_CHANGE;

    @EventHandler
    public void onLevelChange(PlayerLevelChangeEvent event) {
        ExecutionContext context = new ExecutionContext();
        context.set(CoreKeys.PLAYER, event.getPlayer());
        context.set(CoreKeys.LEVEL, event.getNewLevel());
        ActionTriggerAPI.getTriggers().dispatch(KEY, context);
    }

    @Override
    public NamespacedKey getKey() {
        return KEY;
    }
}