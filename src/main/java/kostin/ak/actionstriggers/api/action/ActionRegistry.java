package kostin.ak.actionstriggers.api.action;

import kostin.ak.actionstriggers.api.context.ExecutionContext;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Реестр всех доступных Экшенов. Управляет их регистрацией и безопасным выполнением.
 */
public class ActionRegistry {

    private final Map<NamespacedKey, ActionFactory> factories = new ConcurrentHashMap<>();
    private final Logger logger;

    public ActionRegistry(@NotNull Logger logger) {
        this.logger = logger;
    }

    /**
     * Регистрирует новый тип экшена (может быть вызвано любым сторонним плагином).
     */
    public void register(@NotNull ActionFactory factory) {
        factories.put(factory.getKey(), factory);
        logger.info("Зарегистрирован экшен: " + factory.getKey());
    }

    /**
     * Главный метод выполнения Экшена.
     * Безопасно собирает его через фабрику и вызывает, перехватывая любые ошибки.
     */
    public boolean execute(@NotNull NamespacedKey key, @NotNull ExecutionContext context, @NotNull Map<String, Object> params) {
        ActionFactory factory = factories.get(key);

        if (factory == null) {
            logger.warning("Попытка выполнить неизвестный экшен: " + key);
            return false;
        }

        try {
            Action action = factory.create(params);
            return action.execute(context);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Критическая ошибка при выполнении экшена " + key, e);
            return false;
        }
    }

    /**
     * Перегрузка для выполнения экшена без параметров.
     */
    public boolean execute(@NotNull NamespacedKey key, @NotNull ExecutionContext context) {
        return execute(key, context, Collections.emptyMap());
    }
}