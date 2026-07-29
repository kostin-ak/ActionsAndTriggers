package kostin.ak.actionstriggers.core.defaults.triggers;

import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.api.trigger.BukkitEventTrigger;
import kostin.ak.actionstriggers.core.CoreKeys;
import kostin.ak.actionstriggers.core.CoreTriggerKeys;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

public class PlayerAdvancementDoneTrigger extends BukkitEventTrigger<PlayerAdvancementDoneEvent> {
    public PlayerAdvancementDoneTrigger() {
        declare(CoreKeys.PLAYER, PlayerAdvancementDoneEvent::getPlayer);
        declare(CoreKeys.LOCATION, e -> e.getPlayer().getLocation());
        declare(CoreKeys.ADVANCEMENT_KEY, e -> e.getAdvancement().getKey().toString());
    }
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEvent(PlayerAdvancementDoneEvent e) { handleEvent(e); }
    @Override public NamespacedKey getKey() { return CoreTriggerKeys.ADVANCEMENT_DONE; }
}