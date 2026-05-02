package kostin.ak.actionstriggers.core.defaults.triggers;

import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.api.trigger.BukkitEventTrigger;
import kostin.ak.actionstriggers.core.CoreKeys;
import kostin.ak.actionstriggers.core.CoreTriggerKeys;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.CraftItemEvent;

public class CraftItemTrigger extends BukkitEventTrigger<CraftItemEvent> {

    private static final NamespacedKey KEY = CoreTriggerKeys.ITEM_CRAFT;

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onCraft(CraftItemEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            handleEvent(event);
        }
    }

    @Override
    protected ExecutionContext buildContext(CraftItemEvent event) {
        ExecutionContext context = new ExecutionContext();
        Player player = (Player) event.getWhoClicked();

        context.set(CoreKeys.PLAYER, player);
        context.set(CoreKeys.ITEM, event.getRecipe().getResult());

        if (event.getInventory().getLocation() != null) {
            context.set(CoreKeys.LOCATION, event.getInventory().getLocation());
        } else {
            context.set(CoreKeys.LOCATION, player.getLocation());
        }

        return context;
    }

    @Override
    public NamespacedKey getKey() {
        return KEY;
    }
}