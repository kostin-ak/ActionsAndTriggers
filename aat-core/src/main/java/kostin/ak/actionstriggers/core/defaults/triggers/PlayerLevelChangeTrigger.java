package kostin.ak.actionstriggers.core.defaults.triggers;

import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.api.trigger.BukkitEventTrigger;
import kostin.ak.actionstriggers.core.CoreKeys;
import kostin.ak.actionstriggers.core.CoreTriggerKeys;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerLevelChangeEvent;

public class PlayerLevelChangeTrigger extends BukkitEventTrigger<PlayerLevelChangeEvent> {
    public PlayerLevelChangeTrigger() {
        declare(CoreKeys.PLAYER, PlayerLevelChangeEvent::getPlayer);
        declare(CoreKeys.LEVEL, PlayerLevelChangeEvent::getNewLevel);
    }
    @EventHandler public void onEvent(PlayerLevelChangeEvent e) { handleEvent(e); }
    @Override public NamespacedKey getKey() { return CoreTriggerKeys.PLAYER_LEVEL_CHANGE; }
}