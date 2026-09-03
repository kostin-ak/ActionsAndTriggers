package kostin.ak.actionstriggers.api.action;

import kostin.ak.actionstriggers.api.meta.ActionParam;
import kostin.ak.actionstriggers.api.meta.ActionParameterMeta;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ActionRegistry {
    private final Map<NamespacedKey, Function<Map<String, Object>, Action>> factories = new ConcurrentHashMap<>();

    // НОВОЕ ПОЛЕ: Кэш метаданных параметров для автодополнения и документации
    private final Map<NamespacedKey, List<ActionParameterMeta>> actionMetadataCache = new ConcurrentHashMap<>();

    private final Logger logger;

    public ActionRegistry(@NotNull Logger logger) {
        this.logger = logger;
    }

    public void register(@NotNull NamespacedKey key, @NotNull Function<Map<String, Object>, Action> factory) {
        factories.put(key, factory);
        logger.info("Зарегистрирован экшен: " + key);
    }

    @SuppressWarnings("unchecked")
    public <T extends IActionParsers> void scanAndRegister(@NotNull Class<T> clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(ConfigAction.class)) {
                if (!Modifier.isStatic(method.getModifiers()) ||
                        !Action.class.isAssignableFrom(method.getReturnType())) {
                    logger.warning("Неверная сигнатура метода " + method.getName());
                    continue;
                }

                ConfigAction annotation = method.getAnnotation(ConfigAction.class);
                NamespacedKey key = NamespacedKey.fromString(annotation.value());
                method.setAccessible(true);

                // --- СБОР МЕТАДАННЫХ ЧЕРЕЗ РЕФЛЕКСИЮ ---
                List<ActionParameterMeta> metaList = new ArrayList<>();
                ActionParam[] params = method.getAnnotationsByType(ActionParam.class);

                for (ActionParam param : params) {
                    metaList.add(new ActionParameterMeta(
                            param.key(),
                            param.type(),
                            param.required(),
                            param.description()
                    ));
                }
                actionMetadataCache.put(key, metaList);
                // ---------------------------------------

                Function<Map<String, Object>, Action> factory = (map) -> {
                    try {
                        return (Action) method.invoke(null, map);
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Ошибка при парсинге экшена '" + key + "'", e);
                    }
                };
                register(key, factory);
            }
        }
    }

    @NotNull
    public Action create(@NotNull NamespacedKey key, @NotNull Map<String, Object> params) {
        Function<Map<String, Object>, Action> factory = factories.get(key);
        if (factory == null) {
            throw new IllegalArgumentException("Фабрика экшена не найдена для ключа: " + key);
        }
        return factory.apply(params);
    }

    public List<String> asList() {
        return factories.keySet().stream().map(NamespacedKey::toString).collect(Collectors.toList());
    }

    @NotNull
    public List<ActionParameterMeta> getMetadata(@NotNull NamespacedKey key) {
        return actionMetadataCache.getOrDefault(key, Collections.emptyList());
    }
}
