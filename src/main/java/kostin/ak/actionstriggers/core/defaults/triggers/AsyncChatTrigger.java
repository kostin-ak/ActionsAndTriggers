package kostin.ak.actionstriggers.core.defaults.triggers;

import io.papermc.paper.event.player.AsyncChatEvent;
import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.api.trigger.BukkitEventTrigger;
import kostin.ak.actionstriggers.api.trigger.Trigger;
import kostin.ak.actionstriggers.core.CoreKeys;
import kostin.ak.actionstriggers.core.CoreTriggerKeys;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class AsyncChatTrigger extends BukkitEventTrigger<AsyncChatEvent> {
    public AsyncChatTrigger() {
        declare(CoreKeys.PLAYER, AsyncChatEvent::getPlayer);
        declare(CoreKeys.LOCATION, e -> e.getPlayer().getLocation());
        declare(CoreKeys.MESSAGE, e -> PlainTextComponentSerializer.plainText().serialize(e.message()));
    }
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEvent(AsyncChatEvent e) { handleEvent(e); }
    @Override public NamespacedKey getKey() { return CoreTriggerKeys.PLAYER_CHAT; }
}