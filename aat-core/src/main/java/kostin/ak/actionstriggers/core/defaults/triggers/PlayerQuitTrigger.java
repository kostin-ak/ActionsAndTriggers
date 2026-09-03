package kostin.ak.actionstriggers.core.defaults.triggers;

import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.api.trigger.BukkitEventTrigger;
import kostin.ak.actionstriggers.core.CoreKeys;
import kostin.ak.actionstriggers.core.CoreTriggerKeys;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitTrigger extends BukkitEventTrigger<PlayerQuitEvent> {
    public PlayerQuitTrigger() {
        declare(CoreKeys.PLAYER, PlayerQuitEvent::getPlayer);
        declare(CoreKeys.LOCATION, e -> e.getPlayer().getLocation());
    }
    @EventHandler public void onEvent(PlayerQuitEvent e) { handleEvent(e); }
    @Override public NamespacedKey getKey() { return CoreTriggerKeys.PLAYER_QUIT; }
}