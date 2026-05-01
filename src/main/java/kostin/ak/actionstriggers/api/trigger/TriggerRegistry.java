package kostin.ak.actionstriggers.api.trigger;

import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.api.filter.Filter;
import kostin.ak.actionstriggers.api.filter.Filters;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer; // Важно: BiConsumer для передачи ключа триггера!
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Реестр Триггеров (Event Bus). Управляет подписками и рассылкой событий.
 */
public class TriggerRegistry {

    private final Map<NamespacedKey, List<TriggerSubscription>> subscriptions = new ConcurrentHashMap<>();

    // НОВЫЙ СПИСОК ДЛЯ ГЛОБАЛЬНЫХ СЛУШАТЕЛЕЙ (Дебаг)
    private final List<BiConsumer<NamespacedKey, ExecutionContext>> globalListeners = new CopyOnWriteArrayList<>();

    private final Logger logger;

    private List<String> triggers = new ArrayList<>();

    public TriggerRegistry(@NotNull Logger logger) {
        this.logger = logger;
    }

    // --- Существующие методы subscribe остаются без изменений ---
    public void subscribe(@NotNull NamespacedKey key, @NotNull Plugin plugin, @NotNull Filter filter, @NotNull Consumer<ExecutionContext> callback) {
        subscriptions.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>())
                .add(new TriggerSubscription(plugin, filter, callback));
    }

    public void subscribe(@NotNull NamespacedKey key, @NotNull Plugin plugin, @NotNull Consumer<ExecutionContext> callback) {
        subscribe(key, plugin, Filters.alwaysTrue(), callback);
    }

    // НОВЫЙ МЕТОД ДЛЯ ГЛОБАЛЬНОЙ ПОДПИСКИ
    public void subscribeGlobal(@NotNull BiConsumer<NamespacedKey, ExecutionContext> callback) {
        globalListeners.add(callback);
    }

    // НОВЫЙ МЕТОД ДЛЯ ОТПИСКИ
    public void unsubscribeGlobal(@NotNull BiConsumer<NamespacedKey, ExecutionContext> callback) {
        globalListeners.remove(callback);
    }


    /**
     * Вызывает Триггер (бросает событие в шину).
     */
    public void dispatch(@NotNull NamespacedKey key, @NotNull ExecutionContext context) {
        // 1. ГЛОБАЛЬНЫЕ СЛУШАТЕЛИ
        for (BiConsumer<NamespacedKey, ExecutionContext> globalListener : globalListeners) {
            try {
                globalListener.accept(key, context);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Ошибка в глобальном слушателе на триггере " + key, e);
            }
        }

        // 2. ОБЫЧНЫЕ ПОДПИСЧИКИ

        List<TriggerSubscription> subs = subscriptions.get(key);
        if (subs == null || subs.isEmpty()) {
            return;
        }

        for (TriggerSubscription sub : subs) {
            try {
                if (sub.filter().test(context)) {
                    sub.callback().accept(context);
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Ошибка в подписчике " + sub.plugin().getName() + " на триггере " + key, e);
            }
        }
    }

    public List<String> asList() {
        return triggers;
    }

    public void register(Trigger trigger) {
        triggers.add(trigger.getKey().toString());
    }
}