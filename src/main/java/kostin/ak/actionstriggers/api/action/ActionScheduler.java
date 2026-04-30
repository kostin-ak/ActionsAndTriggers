package kostin.ak.actionstriggers.api.action;

import kostin.ak.actionstriggers.api.context.ExecutionContext;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Планировщик для отложенного выполнения экшенов с сохранением контекста.
 */
public class ActionScheduler {

    private final Plugin plugin;

    public ActionScheduler(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Выполняет задачу через указанное количество тиков (20 тиков = 1 секунда).
     * Автоматически клонирует контекст, чтобы защитить его от изменений в основном потоке.
     *
     * @param context Оригинальный контекст (будет клонирован)
     * @param delayTicks Задержка в тиках
     * @param task Задача, которая примет КЛОНИРОВАННЫЙ контекст
     */
    public void runLater(@NotNull ExecutionContext context, long delayTicks, @NotNull Consumer<ExecutionContext> task) {
        // Клонируем DataBag СРАЗУ, до начала ожидания
        ExecutionContext clonedContext = context.clone();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            task.accept(clonedContext);
        }, delayTicks);
    }
}