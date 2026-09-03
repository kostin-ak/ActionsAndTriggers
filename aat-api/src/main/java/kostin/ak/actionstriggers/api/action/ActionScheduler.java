package kostin.ak.actionstriggers.api.action;

import kostin.ak.actionstriggers.api.context.ContextKey;
import kostin.ak.actionstriggers.api.context.ExecutionContext;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Планировщик для отложенного, периодического и запланированного выполнения экшенов
 * с изолированным сохранением контекста и поддержкой кулдаунов.
 */
public class ActionScheduler {

    public static final ContextKey<Integer> ITERATION_KEY = ContextKey.of("iteration", Integer.class);

    private final Plugin plugin;
    private final Map<String, BukkitTask> namedTasks = new ConcurrentHashMap<>();
    private final Map<String, Long> cooldowns = new ConcurrentHashMap<>();

    public ActionScheduler(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Выполняет задачу через указанное количество тиков с клонированным контекстом.
     */
    public void runLater(@NotNull ExecutionContext context, long delayTicks, @NotNull Consumer<ExecutionContext> task) {
        ExecutionContext clonedContext = context.clone();
        Bukkit.getScheduler().runTaskLater(plugin, () -> task.accept(clonedContext), Math.max(0, delayTicks));
    }

    public BukkitTask runRepeatingTask(@NotNull Runnable task, long delayTicks, long periodTicks) {
        return Bukkit.getScheduler().runTaskTimer(plugin, task, Math.max(0, delayTicks), Math.max(1, periodTicks));
    }

    /**
     * Запускает периодическую задачу с ограничением по числу повторов.
     *
     * @param context Исходный контекст
     * @param delayTicks Задержка первого запуска
     * @param periodTicks Интервал между повторами
     * @param maxRepetitions Максимальное число выполнений (<= 0 для бесконечного цикла)
     * @param task Потребитель, принимающий контекст и номер текущей итерации
     */
    public BukkitTask runRepeating(
            @NotNull ExecutionContext context,
            long delayTicks,
            long periodTicks,
            int maxRepetitions,
            @NotNull BiConsumer<ExecutionContext, Integer> task
    ) {
        ExecutionContext clonedContext = context.clone();
        AtomicInteger counter = new AtomicInteger(0);

        BukkitTask[] taskHolder = new BukkitTask[1];
        taskHolder[0] = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            int current = counter.incrementAndGet();
            clonedContext.set(ITERATION_KEY, current);
            try {
                task.accept(clonedContext, current);
            } catch (Exception e) {
                plugin.getLogger().warning("Error executing repeating action task: " + e.getMessage());
            }

            if (maxRepetitions > 0 && current >= maxRepetitions) {
                taskHolder[0].cancel();
            }
        }, Math.max(0, delayTicks), Math.max(1, periodTicks));

        return taskHolder[0];
    }

    /**
     * Регистрирует именованную отложенную задачу.
     */
    public void scheduleNamed(
            @NotNull String id,
            @NotNull ExecutionContext context,
            long delayTicks,
            @NotNull Consumer<ExecutionContext> task
    ) {
        cancelNamed(id);
        ExecutionContext clonedContext = context.clone();

        BukkitTask bTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            namedTasks.remove(id);
            task.accept(clonedContext);
        }, Math.max(0, delayTicks));

        namedTasks.put(id, bTask);
    }

    /**
     * Отменяет именованную задачу по ее идентификатору.
     */
    public boolean cancelNamed(@NotNull String id) {
        BukkitTask task = namedTasks.remove(id);
        if (task != null && !task.isCancelled()) {
            task.cancel();
            return true;
        }
        return false;
    }

    /**
     * Отменяет все активные задачи планировщика.
     */
    public void cancelAll() {
        for (BukkitTask task : namedTasks.values()) {
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
        }
        namedTasks.clear();
    }

    // --- КУЛДАУНЫ ---

    public boolean isOnCooldown(@NotNull UUID targetId, @NotNull String key) {
        String fullKey = targetId + ":" + key;
        Long expireAt = cooldowns.get(fullKey);
        if (expireAt == null) return false;

        if (System.currentTimeMillis() >= expireAt) {
            cooldowns.remove(fullKey);
            return false;
        }
        return true;
    }

    public int getRemainingCooldown(@NotNull UUID targetId, @NotNull String key) {
        String fullKey = targetId + ":" + key;
        Long expireAt = cooldowns.get(fullKey);
        if (expireAt == null) return 0;

        long diff = expireAt - System.currentTimeMillis();
        if (diff <= 0) {
            cooldowns.remove(fullKey);
            return 0;
        }
        return (int) Math.ceil(diff / 1000.0);
    }

    public void setCooldown(@NotNull UUID targetId, @NotNull String key, int seconds) {
        String fullKey = targetId + ":" + key;
        cooldowns.put(fullKey, System.currentTimeMillis() + (seconds * 1000L));
    }

    public void clearCooldowns() {
        cooldowns.clear();
    }
}
