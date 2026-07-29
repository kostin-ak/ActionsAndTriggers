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
    public BlockPlaceTrigger() {
        declare(CoreKeys.PLAYER, BlockPlaceEvent::getPlayer);
        declare(CoreKeys.BLOCK, BlockPlaceEvent::getBlockPlaced);
        declare(CoreKeys.BLOCK_MATERIAL, e -> e.getBlockPlaced().getType());
        declare(CoreKeys.LOCATION, e -> e.getBlockPlaced().getLocation());
        declare(CoreKeys.BLOCK_ID, e -> ActionTriggerAPI.getBlocks().getFullId(e.getBlockPlaced()));
        declare(CoreKeys.ITEM_IN_HAND_ID, e -> ActionTriggerAPI.getItems().getFullId(e.getItemInHand()));
    }
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEvent(BlockPlaceEvent e) { handleEvent(e); }
    @Override public NamespacedKey getKey() { return CoreTriggerKeys.BLOCK_PLACE; }
}