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

    private static final NamespacedKey KEY = CoreTriggerKeys.PLAYER_CHAT;

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        handleEvent(event);
    }

    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @Override
    protected ExecutionContext buildContext(AsyncChatEvent event) {
        ExecutionContext context = new ExecutionContext();
        context.set(CoreKeys.PLAYER, event.getPlayer());
        context.set(CoreKeys.LOCATION, event.getPlayer().getLocation());

        String plainMessage = PlainTextComponentSerializer.plainText().serialize(event.message());
        context.set(CoreKeys.MESSAGE, plainMessage);

        return context;
    }
}