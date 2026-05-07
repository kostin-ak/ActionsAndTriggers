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
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class PlayerSwapHandItemsTrigger extends BukkitEventTrigger<PlayerSwapHandItemsEvent> {
    private static final NamespacedKey KEY = CoreTriggerKeys.SWAP_ITEMS;

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onSwap(PlayerSwapHandItemsEvent event) {
        handleEvent(event);
    }

    @Override
    protected ExecutionContext buildContext(PlayerSwapHandItemsEvent event) {
        ExecutionContext context = new ExecutionContext();
        context.set(CoreKeys.PLAYER, event.getPlayer());
        context.set(CoreKeys.LOCATION, event.getPlayer().getLocation());
        context.set(CoreKeys.ITEM_IN_HAND, event.getMainHandItem()); // Основная рука ПОСЛЕ смены

        // Регистрируем предметы в обеих руках
        context.set(ContextKey.of("main_hand_item_id", String.class), ActionTriggerAPI.getItems().getFullId(event.getMainHandItem()));
        context.set(ContextKey.of("off_hand_item_id", String.class), ActionTriggerAPI.getItems().getFullId(event.getOffHandItem()));

        return context;
    }

    @Override
    public NamespacedKey getKey() { return KEY; }
}