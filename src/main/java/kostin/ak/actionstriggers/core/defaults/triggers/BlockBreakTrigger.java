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
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Arrays;
import java.util.List;

public class BlockBreakTrigger extends BukkitEventTrigger<BlockBreakEvent> {

    private static final NamespacedKey KEY = CoreTriggerKeys.BLOCK_BREAK;

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        handleEvent(event);
    }

    @Override
    protected ExecutionContext buildContext(BlockBreakEvent event) {
        ExecutionContext context = new ExecutionContext();
        context.set(CoreKeys.PLAYER, event.getPlayer());
        context.set(CoreKeys.BLOCK, event.getBlock());
        context.set(CoreKeys.BLOCK_MATERIAL, event.getBlock().getType());
        context.set(CoreKeys.LOCATION, event.getBlock().getLocation());
        context.set(CoreKeys.ITEM_IN_HAND, event.getPlayer().getInventory().getItemInMainHand());

        // Новые строковые ID для кастомных блоков/предметов
        context.set(CoreKeys.BLOCK_ID, ActionTriggerAPI.getBlocks().getFullId(event.getBlock()));
        context.set(CoreKeys.ITEM_IN_HAND_ID, ActionTriggerAPI.getItems().getFullId(event.getPlayer().getInventory().getItemInMainHand()));

        return context;
    }

    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @Override
    public List<ContextKey<?>> getProvidedContext() {
        // Просто передаем те самые ключи из CoreKeys!
        return Arrays.asList(
                CoreKeys.PLAYER,
                CoreKeys.BLOCK,
                CoreKeys.BLOCK_MATERIAL,
                CoreKeys.LOCATION,
                CoreKeys.ITEM_IN_HAND,
                CoreKeys.BLOCK_ID,
                CoreKeys.ITEM_IN_HAND_ID
        );
    }
}