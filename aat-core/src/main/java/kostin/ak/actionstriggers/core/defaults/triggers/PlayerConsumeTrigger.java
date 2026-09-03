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
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class PlayerConsumeTrigger extends BukkitEventTrigger<PlayerItemConsumeEvent> {
    public PlayerConsumeTrigger() {
        declare(CoreKeys.PLAYER, PlayerItemConsumeEvent::getPlayer);
        declare(CoreKeys.ITEM, PlayerItemConsumeEvent::getItem);
        declare(CoreKeys.LOCATION, e -> e.getPlayer().getLocation());
        declare(CoreKeys.ITEM_ID, e -> ActionTriggerAPI.getItems().getFullId(e.getItem()));
    }
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEvent(PlayerItemConsumeEvent e) { handleEvent(e); }
    @Override public NamespacedKey getKey() { return CoreTriggerKeys.PLAYER_CONSUME; }
}

