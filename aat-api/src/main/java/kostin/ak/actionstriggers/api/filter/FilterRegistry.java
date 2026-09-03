package kostin.ak.actionstriggers.api.filter;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class FilterRegistry {
    // Храним фабрики как обычные функции
    private final Map<NamespacedKey, Function<Map<String, Object>, Filter>> factories = new ConcurrentHashMap<>();
    private final Logger logger;

    public FilterRegistry(@NotNull Logger logger) {
        this.logger = logger;
    }

    /**
     * Ручная регистрация (если кому-то не нравятся аннотации).
     */
    public void register(@NotNull NamespacedKey key, @NotNull Function<Map<String, Object>, Filter> factory) {
        factories.put(key, factory);
        logger.info("Зарегистрирован фильтр: " + key);
    }

    /**
     * Магия рефлексии: сканируем класс и регистрируем все методы с @ConfigFilter.
     */
    @SuppressWarnings("unchecked")
    public <T extends IFilterParsers>  void scanAndRegister(@NotNull Class<T> clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(ConfigFilter.class)) {

                // Fail-Fast проверки сигнатуры метода
                if (!Modifier.isStatic(method.getModifiers())) {
                    logger.warning("Метод " + method.getName() + " в " + clazz.getSimpleName() + " должен быть static!");
                    continue;
                }
                if (!Filter.class.isAssignableFrom(method.getReturnType())) {
                    logger.warning("Метод " + method.getName() + " в " + clazz.getSimpleName() + " должен возвращать Filter!");
                    continue;
                }
                Class<?>[] params = method.getParameterTypes();
                if (params.length != 1 || !Map.class.isAssignableFrom(params[0])) {
                    logger.warning("Метод " + method.getName() + " в " + clazz.getSimpleName() + " должен принимать только Map<String, Object>!");
                    continue;
                }

                ConfigFilter annotation = method.getAnnotation(ConfigFilter.class);
                NamespacedKey key = NamespacedKey.fromString(annotation.value());
                if (key == null) {
                    logger.warning("Невалидный NamespacedKey для фильтра: " + annotation.value());
                    continue;
                }

                // Разрешаем доступ к private методам
                method.setAccessible(true);

                // Оборачиваем вызов рефлексии в удобную лямбду
                Function<Map<String, Object>, Filter> factory = (map) -> {
                    try {
                        return (Filter) method.invoke(null, map);
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Критическая ошибка при парсинге фильтра '" + key + "'", e);
                    }
                };

                register(key, factory);
            }
        }
    }

    /**
     * Создает фильтр по ключу. Если не найден — жестко выбрасывает исключение.
     */
    @NotNull
    public Filter create(@NotNull NamespacedKey key, @NotNull Map<String, Object> params) {
        Function<Map<String, Object>, Filter> factory = factories.get(key);
        if (factory == null) {
            throw new IllegalArgumentException("Фабрика фильтра не найдена для ключа: " + key);
        }
        return factory.apply(params);
    }

    public List<String> asList() {
        return factories.keySet().stream().map(NamespacedKey::toString).collect(Collectors.toList());
    }
}
