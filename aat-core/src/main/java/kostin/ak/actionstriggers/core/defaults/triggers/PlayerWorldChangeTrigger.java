package kostin.ak.actionstriggers.core.defaults.triggers;

import kostin.ak.actionstriggers.api.trigger.BukkitEventTrigger;
import kostin.ak.actionstriggers.core.CoreKeys;
import kostin.ak.actionstriggers.core.CoreTriggerKeys;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class PlayerWorldChangeTrigger extends BukkitEventTrigger<PlayerChangedWorldEvent> {

    public PlayerWorldChangeTrigger() {
        declare(CoreKeys.PLAYER, PlayerChangedWorldEvent::getPlayer);
        declare(CoreKeys.FROM_WORLD, e -> e.getFrom() != null ? e.getFrom().getName() : "");
        declare(CoreKeys.TO_WORLD, e -> e.getPlayer().getWorld().getName());
        declare(CoreKeys.WORLD, e -> e.getPlayer().getWorld().getName());
        declare(CoreKeys.WORLD_NAME, e -> e.getPlayer().getWorld().getName());
        declare(CoreKeys.LOCATION, e -> e.getPlayer().getLocation());
    }

    @EventHandler
    public void onEvent(PlayerChangedWorldEvent e) {
        handleEvent(e);
    }

    @Override
    public NamespacedKey getKey() {
        return CoreTriggerKeys.PLAYER_WORLD_CHANGE;
    }
}
