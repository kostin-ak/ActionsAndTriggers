package kostin.ak.actionstriggers.core.defaults.triggers;

import kostin.ak.actionstriggers.api.ActionTriggerAPI;
import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.api.trigger.Trigger;
import kostin.ak.actionstriggers.core.CoreKeys;
import kostin.ak.actionstriggers.core.CoreTriggerKeys;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteractTriggerListener extends Trigger {

    private static final NamespacedKey KEY = CoreTriggerKeys.PLAYER_INTERACT;

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInteract(PlayerInteractEvent event) {
        ExecutionContext context = new ExecutionContext();
        context.set(CoreKeys.PLAYER, event.getPlayer());

        // Если кликнули по блоку — добавляем блок в контекст
        if (event.getClickedBlock() != null) {
            context.set(CoreKeys.BLOCK, event.getClickedBlock());
            context.set(CoreKeys.BLOCK_MATERIAL, event.getClickedBlock().getType());
            context.set(CoreKeys.LOCATION, event.getClickedBlock().getLocation());
            context.set(CoreKeys.ACTION, event.getAction());
            context.set(CoreKeys.BUTTON_TYPE, event.getAction().isLeftClick() ? CoreKeys.ButtonType.LEFT : CoreKeys.ButtonType.RIGHT);
        } else {
            context.set(CoreKeys.LOCATION, event.getPlayer().getLocation());
        }

        ActionTriggerAPI.getTriggers().dispatch(KEY, context);

        if (context.isCancelled()) {
            event.setCancelled(true);
        }
    }

    @Override
    public NamespacedKey getKey() {
        return KEY;
    }
}