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
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteractTrigger extends BukkitEventTrigger<PlayerInteractEvent> {
    public PlayerInteractTrigger() {
        declare(CoreKeys.PLAYER, PlayerInteractEvent::getPlayer);
        declare(CoreKeys.ACTION, PlayerInteractEvent::getAction);
        declare(CoreKeys.BUTTON_TYPE, e -> e.getAction().isLeftClick() ? CoreKeys.ButtonType.LEFT : CoreKeys.ButtonType.RIGHT);

        // Предмет в руке
        declare(CoreKeys.ITEM_IN_HAND_ID, e -> e.getItem() != null ?
                ActionTriggerAPI.getItems().getFullId(e.getItem()) : "minecraft:air");

        // Логика блока
        declare(CoreKeys.HAS_BLOCK, e -> e.getClickedBlock() != null);
        declare(CoreKeys.BLOCK, PlayerInteractEvent::getClickedBlock);
        declare(CoreKeys.BLOCK_MATERIAL, e -> e.getClickedBlock() != null ? e.getClickedBlock().getType() : null);
        declare(CoreKeys.CLICKED_BLOCK_ID, e -> e.getClickedBlock() != null ?
                ActionTriggerAPI.getBlocks().getFullId(e.getClickedBlock()) : null);

        // Динамическая локация: блок или игрок
        declare(CoreKeys.LOCATION, e -> e.getClickedBlock() != null ?
                e.getClickedBlock().getLocation() : e.getPlayer().getLocation());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onEvent(PlayerInteractEvent e) { handleEvent(e); }

    @Override public NamespacedKey getKey() { return CoreTriggerKeys.PLAYER_INTERACT; }
}