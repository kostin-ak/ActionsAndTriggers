package kostin.ak.actionstriggers.api.trigger;

import kostin.ak.actionstriggers.api.context.ContextKey;
import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.api.filter.Filter;
import kostin.ak.actionstriggers.api.filter.Filters;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Реестр Триггеров (Event Bus). Управляет подписками и рассылкой событий.
 */
public class TriggerRegistry {

    private final Map<NamespacedKey, Trigger> triggers = new ConcurrentHashMap<>();
    private final Map<NamespacedKey, List<TriggerSubscription>> subscriptions = new ConcurrentHashMap<>();

    // ГЛОБАЛЬНЫЕ СЛУШАТЕЛИ (Дебаг)
    private final List<BiConsumer<NamespacedKey, ExecutionContext>> globalListeners = new CopyOnWriteArrayList<>();

    // НОВОЕ ПОЛЕ: Кэш поставляемого контекста
    private final Map<NamespacedKey, List<ContextKey<?>>> triggerContextCache = new ConcurrentHashMap<>();

    private final Logger logger;


    public TriggerRegistry(@NotNull Logger logger) {
        this.logger = logger;
    }

    public void subscribe(@NotNull NamespacedKey key, @NotNull Plugin plugin, @NotNull Filter filter, @NotNull Consumer<ExecutionContext> callback) {
        subscriptions.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>())
                .add(new TriggerSubscription(plugin, filter, callback));
    }

    public void subscribe(@NotNull NamespacedKey key, @NotNull Plugin plugin, @NotNull Consumer<ExecutionContext> callback) {
        subscribe(key, plugin, Filters.alwaysTrue(), callback);
    }

    public void subscribeGlobal(@NotNull BiConsumer<NamespacedKey, ExecutionContext> callback) {
        globalListeners.add(callback);
    }

    public void unsubscribeGlobal(@NotNull BiConsumer<NamespacedKey, ExecutionContext> callback) {
        globalListeners.remove(callback);
    }

    /**
     * Отписывает всех слушателей, зарегистрированных указанным плагином.
     * Критически важно для предотвращения дублирования триггеров при /aat reload.
     */
    public void unsubscribeAll(@NotNull Plugin plugin) {
        for (List<TriggerSubscription> list : subscriptions.values()) {
            list.removeIf(sub -> sub.plugin().equals(plugin));
        }
    }

    /**
     * Полная очистка всех подписок на события.
     */
    public void clearSubscriptions() {
        subscriptions.clear();
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
        return triggers.keySet().stream().map(NamespacedKey::toString).collect(Collectors.toList());
    }

    @NotNull
    public List<ContextKey<?>> getProvidedContext(@NotNull NamespacedKey key) {
        return triggerContextCache.getOrDefault(key, Collections.emptyList());
    }

    public void register(Trigger trigger) {
        triggers.putIfAbsent(trigger.getKey(), trigger);
        triggerContextCache.putIfAbsent(trigger.getKey(), trigger.getProvidedContext());
    }
}