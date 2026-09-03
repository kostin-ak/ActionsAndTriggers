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
import org.bukkit.event.block.BlockDamageEvent;

public class BlockDamageTrigger extends BukkitEventTrigger<BlockDamageEvent> {

    public BlockDamageTrigger() {
        // Мапим ключи контекста на методы события
        declare(CoreKeys.PLAYER, BlockDamageEvent::getPlayer);
        declare(CoreKeys.BLOCK, BlockDamageEvent::getBlock);
        declare(CoreKeys.BLOCK_MATERIAL, e -> e.getBlock().getType());
        declare(CoreKeys.LOCATION, e -> e.getBlock().getLocation());
        declare(CoreKeys.ITEM_IN_HAND, BlockDamageEvent::getItemInHand);

        // Интеграция с API через лямбды
        declare(CoreKeys.BLOCK_ID, e -> ActionTriggerAPI.getBlocks().getFullId(e.getBlock()));
        declare(CoreKeys.ITEM_IN_HAND_ID, e -> ActionTriggerAPI.getItems().getFullId(e.getItemInHand()));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent event) {
        handleEvent(event);
    }

    @Override
    public NamespacedKey getKey() {
        return CoreTriggerKeys.BLOCK_DAMAGE;
    }
}