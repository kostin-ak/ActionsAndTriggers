package kostin.ak.actionstriggers.core.defaults.triggers;

import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.api.trigger.BukkitEventTrigger;
import kostin.ak.actionstriggers.core.CoreKeys;
import kostin.ak.actionstriggers.core.CoreTriggerKeys;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitTrigger extends BukkitEventTrigger<PlayerQuitEvent> {

    private static final NamespacedKey KEY = CoreTriggerKeys.PLAYER_QUIT;

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        handleEvent(event);
    }

    @Override
    protected ExecutionContext buildContext(PlayerQuitEvent event) {
        ExecutionContext context = new ExecutionContext();
        context.set(CoreKeys.PLAYER, event.getPlayer());
        context.set(CoreKeys.LOCATION, event.getPlayer().getLocation());
        return context;
    }

    @Override
    public NamespacedKey getKey() {
        return KEY;
    }
}