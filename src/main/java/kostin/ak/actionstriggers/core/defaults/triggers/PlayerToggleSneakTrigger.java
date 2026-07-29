package kostin.ak.actionstriggers.core.defaults.triggers;

import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.api.trigger.BukkitEventTrigger;
import kostin.ak.actionstriggers.core.CoreKeys;
import kostin.ak.actionstriggers.core.CoreTriggerKeys;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class PlayerToggleSneakTrigger extends BukkitEventTrigger<PlayerToggleSneakEvent> {
    public PlayerToggleSneakTrigger() {
        declare(CoreKeys.PLAYER, PlayerToggleSneakEvent::getPlayer);
        declare(CoreKeys.LOCATION, e -> e.getPlayer().getLocation());
    }
    @EventHandler public void onEvent(PlayerToggleSneakEvent e) {
        if (e.isSneaking()) handleEvent(e); // Сохраняем логику "только при приседании"
    }
    @Override public NamespacedKey getKey() { return CoreTriggerKeys.PLAYER_SNEAK; }
}