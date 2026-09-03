package kostin.ak.actionstriggers.api.trigger;

import kostin.ak.actionstriggers.api.ActionTriggerAPI;
import kostin.ak.actionstriggers.api.context.ContextKey;
import kostin.ak.actionstriggers.api.context.ExecutionContext;
import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class BukkitEventTrigger<T extends Event> extends Trigger implements Listener {

    private final Map<ContextKey<?>, Function<T, ?>> contextRules = new LinkedHashMap<>();

    protected <V> void declare(ContextKey<V> key, Function<T, V> extractor) {
        contextRules.put(key, extractor);
    }

    @Override
    public List<ContextKey<?>> getProvidedContext() {
        return new ArrayList<>(contextRules.keySet());
    }

    protected ExecutionContext buildContext(T event) {
        ExecutionContext context = new ExecutionContext();
        for (Map.Entry<ContextKey<?>, Function<T, ?>> entry : contextRules.entrySet()) {
            Object value = entry.getValue().apply(event);
            if (value != null) {
                applyToContext(context, (ContextKey<Object>) entry.getKey(), value);
            }
        }
        return context;
    }

    private <V> void applyToContext(ExecutionContext context, ContextKey<V> key, V value) {
        context.set(key, value);
    }

    protected void handleEvent(T event) {
        // Fast-exit: если нет подписчиков на этот триггер, не выделяем память и не извлекаем контекст
        if (!ActionTriggerAPI.getTriggers().hasSubscriptions(getKey())) {
            return;
        }

        ExecutionContext context = buildContext(event);
        dispatch(context);
        if (context.isCancelled() && event instanceof Cancellable cancellable) {
            cancellable.setCancelled(true);
        }
    }
}