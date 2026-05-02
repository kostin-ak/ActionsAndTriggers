package kostin.ak.actionstriggers.api.trigger;

import kostin.ak.actionstriggers.api.context.ExecutionContext;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;

public abstract class BukkitEventTrigger<T extends Event> extends Trigger implements Listener {

    protected abstract ExecutionContext buildContext(T event);

    protected void handleEvent(T event) {
        ExecutionContext context = buildContext(event);

        dispatch(context);

        if (context.isCancelled() && event instanceof Cancellable cancellable) {
            cancellable.setCancelled(true);
        }
    }
}