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
    private static final NamespacedKey KEY = CoreTriggerKeys.DROP_ITEM;

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        handleEvent(event);
    }

    @Override
    protected ExecutionContext buildContext(PlayerDropItemEvent event) {
        ExecutionContext context = new ExecutionContext();
        context.set(CoreKeys.PLAYER, event.getPlayer());
        context.set(CoreKeys.LOCATION, event.getPlayer().getLocation());
        context.set(CoreKeys.ITEM, event.getItemDrop().getItemStack());

        // Выброшенный предмет
        context.set(ContextKey.of("item_id", String.class), ActionTriggerAPI.getItems().getFullId(event.getItemDrop().getItemStack()));

        return context;
    }

    @Override
    public NamespacedKey getKey() { return KEY; }
}