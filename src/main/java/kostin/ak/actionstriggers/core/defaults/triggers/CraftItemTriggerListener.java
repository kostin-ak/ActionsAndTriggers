package kostin.ak.actionstriggers.core.defaults.triggers;

import kostin.ak.actionstriggers.api.ActionTriggerAPI;
import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.api.trigger.Trigger;
import kostin.ak.actionstriggers.core.CoreKeys;
import kostin.ak.actionstriggers.core.CoreTriggerKeys;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;

public class CraftItemTriggerListener extends Trigger {

    private static final NamespacedKey KEY = CoreTriggerKeys.ITEM_CRAFT;

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onCraft(CraftItemEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            ExecutionContext context = new ExecutionContext();

            context.set(CoreKeys.PLAYER, player);
            context.set(CoreKeys.ITEM, event.getRecipe().getResult());

            // Если крафтят в верстаке - берем локацию верстака, иначе локацию игрока (крафт в инвентаре)
            if (event.getInventory().getLocation() != null) {
                context.set(CoreKeys.LOCATION, event.getInventory().getLocation());
            } else {
                context.set(CoreKeys.LOCATION, player.getLocation());
            }

            ActionTriggerAPI.getTriggers().dispatch(KEY, context);

            if (context.isCancelled()) {
                event.setCancelled(true);
            }
        }
    }

    public NamespacedKey getKey() {
        return KEY;
    }
}