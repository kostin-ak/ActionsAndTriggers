package kostin.ak.actionstriggers.core.defaults.triggers;

import kostin.ak.actionstriggers.api.context.ContextKey;
import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.api.trigger.Trigger;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

/**
 * Периодический триггер ядра (core:interval), срабатывающий каждую секунду (20 тиков).
 */
public class PeriodicIntervalTrigger extends Trigger {

    public static final NamespacedKey KEY = new NamespacedKey("core", "interval");
    public static final ContextKey<Long> TICK_KEY = ContextKey.of("tick", Long.class);

    private BukkitTask task;
    private long tickCounter = 0;

    public void start(Plugin plugin) {
        if (task != null) {
            task.cancel();
        }
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            tickCounter += 20;
            ExecutionContext ctx = new ExecutionContext();
            ctx.set(TICK_KEY, tickCounter);
            dispatch(ctx);
        }, 20L, 20L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @Override
    public List<ContextKey<?>> getProvidedContext() {
        return List.of(TICK_KEY);
    }
}
