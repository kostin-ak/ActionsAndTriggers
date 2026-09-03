package kostin.ak.actionstriggers.core.defaults.triggers;

import kostin.ak.actionstriggers.api.ActionTriggerAPI;
import kostin.ak.actionstriggers.api.context.ContextKey;
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
    public CraftItemTrigger() {
        declare(CoreKeys.PLAYER, e -> (Player) e.getWhoClicked());
        declare(CoreKeys.ITEM, e -> e.getRecipe().getResult());
        declare(CoreKeys.LOCATION, e -> e.getInventory().getLocation() != null ? e.getInventory().getLocation() : e.getWhoClicked().getLocation());
        declare(CoreKeys.ITEM_ID, e -> ActionTriggerAPI.getItems().getFullId(e.getRecipe().getResult()));
    }
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEvent(CraftItemEvent e) {
        if (e.getWhoClicked() instanceof Player) handleEvent(e);
    }
    @Override public NamespacedKey getKey() { return CoreTriggerKeys.ITEM_CRAFT; }
}