package kostin.ak.actionstriggers.core.defaults.triggers;

import kostin.ak.actionstriggers.api.ActionTriggerAPI;
import kostin.ak.actionstriggers.api.context.ContextKey;
import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.api.trigger.BukkitEventTrigger;
import kostin.ak.actionstriggers.core.CoreKeys;
import kostin.ak.actionstriggers.core.CoreTriggerKeys;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerDropItemEvent;

public class PlayerDropItemTrigger extends BukkitEventTrigger<PlayerDropItemEvent> {
    public PlayerDropItemTrigger() {
        declare(CoreKeys.PLAYER, PlayerDropItemEvent::getPlayer);
        declare(CoreKeys.LOCATION, e -> e.getPlayer().getLocation());
        declare(CoreKeys.ITEM, e -> e.getItemDrop().getItemStack());
        declare(CoreKeys.ITEM_ID, e -> ActionTriggerAPI.getItems().getFullId(e.getItemDrop().getItemStack()));
    }
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEvent(PlayerDropItemEvent e) { handleEvent(e); }
    @Override public NamespacedKey getKey() { return CoreTriggerKeys.DROP_ITEM; }
}