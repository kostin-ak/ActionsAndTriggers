package kostin.ak.actionstriggers.core.defaults.triggers;

import kostin.ak.actionstriggers.api.ActionTriggerAPI;
import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.api.trigger.Trigger;
import kostin.ak.actionstriggers.core.CoreKeys;
import kostin.ak.actionstriggers.core.CoreTriggerKeys;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class PlayerToggleSneakTriggerListener extends Trigger {

    private static final NamespacedKey KEY = CoreTriggerKeys.PLAYER_SNEAK;

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        // Срабатывает только когда игрок ПРИСЕДАЕТ, а не встает
        if (event.isSneaking()) {
            ExecutionContext context = new ExecutionContext();
            context.set(CoreKeys.PLAYER, event.getPlayer());
            context.set(CoreKeys.LOCATION, event.getPlayer().getLocation());

            ActionTriggerAPI.getTriggers().dispatch(KEY, context);
        }
    }

    @Override
    public NamespacedKey getKey() {
        return KEY;
    }
}