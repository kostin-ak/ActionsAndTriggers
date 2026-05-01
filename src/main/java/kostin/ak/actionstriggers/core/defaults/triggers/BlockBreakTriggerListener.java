package kostin.ak.actionstriggers.core.defaults.triggers;

import kostin.ak.actionstriggers.api.ActionTriggerAPI;
import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.api.trigger.Trigger;
import kostin.ak.actionstriggers.core.CoreKeys;
import kostin.ak.actionstriggers.core.CoreTriggerKeys;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakTriggerListener extends Trigger {

    private static final NamespacedKey KEY = CoreTriggerKeys.BLOCK_BREAK;

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        // 1. Упаковываем Bukkit Event в наш DataBag
        ExecutionContext context = new ExecutionContext();
        context.set(CoreKeys.PLAYER, event.getPlayer());
        context.set(CoreKeys.BLOCK, event.getBlock());
        context.set(CoreKeys.BLOCK_MATERIAL, event.getBlock().getType());
        context.set(CoreKeys.LOCATION, event.getBlock().getLocation());
        context.set(CoreKeys.ITEM_IN_HAND, event.getPlayer().getInventory().getItemInMainHand());

        // 2. Рассылаем по шине
        ActionTriggerAPI.getTriggers().dispatch(KEY, context);

        // 3. Обрабатываем возможную отмену от сторонних плагинов
        if (context.isCancelled()) {
            event.setCancelled(true);
        }
    }
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }
}