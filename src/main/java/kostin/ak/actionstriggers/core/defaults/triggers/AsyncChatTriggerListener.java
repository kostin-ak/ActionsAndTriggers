package kostin.ak.actionstriggers.core.defaults.triggers;

import io.papermc.paper.event.player.AsyncChatEvent;
import kostin.ak.actionstriggers.api.ActionAPI;
import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.core.CoreKeys;
import kostin.ak.actionstriggers.core.CoreTriggerKeys;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class AsyncChatTriggerListener implements Listener {

    private static final NamespacedKey KEY = CoreTriggerKeys.PLAYER_CHAT;

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        ExecutionContext context = new ExecutionContext();
        context.set(CoreKeys.PLAYER, event.getPlayer());
        context.set(CoreKeys.LOCATION, event.getPlayer().getLocation());

        // Превращаем красивые компоненты Adventure обратно в обычный текст для простоты фильтрации
        String plainMessage = PlainTextComponentSerializer.plainText().serialize(event.message());
        context.set(CoreKeys.MESSAGE, plainMessage);

        ActionAPI.getTriggers().dispatch(KEY, context);

        // Позволяем отменить отправку сообщения в чат (например, если это был тайный пароль)
        if (context.isCancelled()) {
            event.setCancelled(true);
        }
    }
}