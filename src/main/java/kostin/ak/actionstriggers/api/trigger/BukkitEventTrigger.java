package kostin.ak.actionstriggers.api.trigger;

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
        contextRules.forEach((key, extractor) -> {
            Object value = extractor.apply(event);
            if (value != null) {
                applyToContext(context, (ContextKey<Object>) key, value);
            }
        });
        return context;
    }

    private <V> void applyToContext(ExecutionContext context, ContextKey<V> key, V value) {
        context.set(key, value);
    }

    protected void handleEvent(T event) {
        ExecutionContext context = buildContext(event);
        dispatch(context);
        if (context.isCancelled() && event instanceof Cancellable cancellable) {
            cancellable.setCancelled(true);
        }
    }
}