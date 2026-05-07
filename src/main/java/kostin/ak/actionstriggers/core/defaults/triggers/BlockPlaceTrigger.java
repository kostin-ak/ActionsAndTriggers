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
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlaceTrigger extends BukkitEventTrigger<BlockPlaceEvent> {

    private static final NamespacedKey KEY = CoreTriggerKeys.BLOCK_PLACE;

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        handleEvent(event);
    }

    @Override
    protected ExecutionContext buildContext(BlockPlaceEvent event) {
        ExecutionContext context = new ExecutionContext();
        context.set(CoreKeys.PLAYER, event.getPlayer());
        context.set(CoreKeys.BLOCK, event.getBlockPlaced());
        context.set(CoreKeys.BLOCK_MATERIAL, event.getBlockPlaced().getType());
        context.set(CoreKeys.LOCATION, event.getBlockPlaced().getLocation());

        // В событии BlockPlaceEvent предмет, которым ставили блок, находится в руке
        context.set(ContextKey.of("block_id", String.class), ActionTriggerAPI.getBlocks().getFullId(event.getBlockPlaced()));
        context.set(ContextKey.of("item_in_hand_id", String.class), ActionTriggerAPI.getItems().getFullId(event.getItemInHand()));

        return context;
    }

    @Override
    public NamespacedKey getKey() {
        return KEY;
    }
}