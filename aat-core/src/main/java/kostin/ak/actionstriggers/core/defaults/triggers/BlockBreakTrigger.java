package kostin.ak.actionstriggers.core.defaults.triggers;

import kostin.ak.actionstriggers.api.ActionTriggerAPI;
import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.api.trigger.BukkitEventTrigger;
import kostin.ak.actionstriggers.core.CoreKeys;
import kostin.ak.actionstriggers.core.CoreTriggerKeys;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakTrigger extends BukkitEventTrigger<BlockBreakEvent> {

    public BlockBreakTrigger() {

        declare(CoreKeys.PLAYER, BlockBreakEvent::getPlayer);
        declare(CoreKeys.BLOCK, BlockBreakEvent::getBlock);
        declare(CoreKeys.BLOCK_MATERIAL, e -> e.getBlock().getType());
        declare(CoreKeys.LOCATION, e -> e.getBlock().getLocation());
        declare(CoreKeys.ITEM_IN_HAND, e -> e.getPlayer().getInventory().getItemInMainHand());
        declare(CoreKeys.BLOCK_ID, e -> ActionTriggerAPI.getBlocks().getFullId(e.getBlock()));
        declare(CoreKeys.ITEM_IN_HAND_ID, e ->
                ActionTriggerAPI.getItems().getFullId(e.getPlayer().getInventory().getItemInMainHand())
        );
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        handleEvent(event);
    }

    @Override
    public NamespacedKey getKey() {
        return CoreTriggerKeys.BLOCK_BREAK;
    }
}