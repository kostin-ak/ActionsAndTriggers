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
    private static final NamespacedKey KEY = CoreTriggerKeys.ADVANCEMENT_DONE;

    @EventHandler(priority = EventPriority.NORMAL)
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        handleEvent(event);
    }

    @Override
    protected ExecutionContext buildContext(PlayerAdvancementDoneEvent event) {
        ExecutionContext context = new ExecutionContext();
        context.set(CoreKeys.PLAYER, event.getPlayer());
        context.set(CoreKeys.LOCATION, event.getPlayer().getLocation());
        context.set(CoreKeys.ADVANCEMENT_KEY, event.getAdvancement().getKey().toString());
        return context;
    }

    @Override
    public NamespacedKey getKey() { return KEY; }
}