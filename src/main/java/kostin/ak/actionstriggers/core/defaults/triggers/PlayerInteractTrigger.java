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

    private static final NamespacedKey KEY = CoreTriggerKeys.PLAYER_INTERACT;

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onInteract(PlayerInteractEvent event) {
        handleEvent(event);
    }

    @Override
    protected ExecutionContext buildContext(PlayerInteractEvent event) {
        ExecutionContext context = new ExecutionContext();
        context.set(CoreKeys.PLAYER, event.getPlayer());

        // Регистрируем действие и кнопку ВСЕГДА
        context.set(CoreKeys.ACTION, event.getAction());
        context.set(CoreKeys.BUTTON_TYPE, event.getAction().isLeftClick() ?
                CoreKeys.ButtonType.LEFT : CoreKeys.ButtonType.RIGHT);

        // Добавляем ID предмета в руке (может быть null/air)
        if (event.getItem() != null) {
            context.set(ContextKey.of("item_in_hand_id", String.class), ActionTriggerAPI.getItems().getFullId(event.getItem()));
        } else {
            context.set(ContextKey.of("item_in_hand_id", String.class), "minecraft:air");
        }

        // Устанавливаем наш новый флаг
        boolean hasBlock = event.getClickedBlock() != null;
        context.set(CoreKeys.HAS_BLOCK, hasBlock);

        if (hasBlock) {
            context.set(CoreKeys.BLOCK, event.getClickedBlock());
            context.set(CoreKeys.BLOCK_MATERIAL, event.getClickedBlock().getType());
            context.set(CoreKeys.LOCATION, event.getClickedBlock().getLocation());
            // Строковый ID кликнутого блока
            context.set(ContextKey.of("clicked_block_id", String.class), ActionTriggerAPI.getBlocks().getFullId(event.getClickedBlock()));
        } else {
            context.set(CoreKeys.LOCATION, event.getPlayer().getLocation());
        }

        return context;
    }

    @Override
    public NamespacedKey getKey() {
        return KEY;
    }
}