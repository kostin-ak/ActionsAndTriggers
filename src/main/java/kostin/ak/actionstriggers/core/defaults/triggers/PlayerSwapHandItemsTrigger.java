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
    public PlayerSwapHandItemsTrigger() {
        declare(CoreKeys.PLAYER, PlayerSwapHandItemsEvent::getPlayer);
        declare(CoreKeys.LOCATION, e -> e.getPlayer().getLocation());
        declare(CoreKeys.ITEM_IN_HAND, PlayerSwapHandItemsEvent::getMainHandItem);
        declare(CoreKeys.MAIN_HAND_ITEM_ID, e -> ActionTriggerAPI.getItems().getFullId(e.getMainHandItem()));
        declare(CoreKeys.OFF_HAND_ITEM_ID, e -> ActionTriggerAPI.getItems().getFullId(e.getOffHandItem()));
    }
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEvent(PlayerSwapHandItemsEvent e) { handleEvent(e); }
    @Override public NamespacedKey getKey() { return CoreTriggerKeys.SWAP_ITEMS; }
}