package kostin.ak.actionstriggers.api.parser;

import kostin.ak.actionstriggers.api.ActionTriggerAPI;
import kostin.ak.actionstriggers.api.action.Action;
import kostin.ak.actionstriggers.api.context.ContextKey;
import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.api.filter.Filter;
import kostin.ak.actionstriggers.core.ContextPlaceholderParser;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class AATParser {

    private final ParserOptions options;

    public AATParser() {
        this.options = new ParserOptions(); // Дефолтные настройки
    }

    public AATParser(@NotNull ParserOptions options) {
        this.options = options;
    }

    // ========================================================================
    // 1. ПАРСИНГ ИНЪЕКЦИЙ КОНТЕКСТА
    // ========================================================================

    @SuppressWarnings("unchecked")
    public Consumer<ExecutionContext> parseInjector(Object injectConfig) {
        if (!(injectConfig instanceof List)) {
            throw new AATParseException("Блок " + options.injectContextKey + " должен быть списком (List)!");
        }

        List<Map<String, Object>> injectList = (List<Map<String, Object>>) injectConfig;

        return context -> {
            for (Map<String, Object> entry : injectList) {
                String key = String.valueOf(entry.get("key"));
                String rawValue = String.valueOf(entry.get("value"));
                boolean overwrite = entry.containsKey("overwrite") && Boolean.parseBoolean(String.valueOf(entry.get("overwrite")));

                ContextKey<String> contextKey = ContextKey.of(key, String.class);

                // Если перезапись разрешена ИЛИ ключа еще нет в контексте
                if (overwrite || !context.has(contextKey)) {
                    // Парсим плейсхолдеры в значении (например, "{player.name} сломал блок")
                    String resolvedValue = ContextPlaceholderParser.resolve(rawValue, context);
                    context.set(contextKey, resolvedValue);
                }
            }
        };
    }

    // ========================================================================
    // 2. ПАРСИНГ ЭКШЕНОВ (С ПРОКСИРОВАНИЕМ ИНЪЕКЦИЙ)
    // ========================================================================

    public Action parseAction(@NotNull Map<String, Object> map) {
        String actionId = options.findKey(map, options.actionKeys);

        if (actionId == null) {
            throw new AATParseException("В мапе экшена не найден идентификатор! Ожидались ключи: " + options.actionKeys);
        }

        NamespacedKey key = NamespacedKey.fromString(actionId);
        if (key == null) {
            throw new AATParseException("Невалидный NamespacedKey для экшена: " + actionId);
        }

        // 1. Создаем базовый экшен через реестр
        Action rawAction = ActionTriggerAPI.getActions().create(key, map);

        // 2. Если у экшена есть свой локальный inject_context, оборачиваем его (паттерн Proxy/Decorator)
        if (map.containsKey(options.injectContextKey)) {
            Consumer<ExecutionContext> localInjector = parseInjector(map.get(options.injectContextKey));

            return context -> {
                localInjector.accept(context); // Сначала вливаем новые переменные
                return rawAction.execute(context); // Затем выполняем сам экшен
            };
        }

        return rawAction;
    }

    @SuppressWarnings("unchecked")
    public List<Action> parseActions(@NotNull Object actionsConfig) {
        if (!(actionsConfig instanceof List)) {
            throw new AATParseException("Блок экшенов должен быть списком (List)!");
        }

        List<Map<String, Object>> mapList = (List<Map<String, Object>>) actionsConfig;
        List<Action> parsedActions = new ArrayList<>();

        for (Map<String, Object> map : mapList) {
            parsedActions.add(parseAction(map));
        }

        return parsedActions;
    }

    // ========================================================================
    // 3. ПАРСИНГ УСЛОВИЙ (ФИЛЬТРОВ)
    // ========================================================================

    public Filter parseCondition(@NotNull Map<String, Object> map) {
        String conditionId = options.findKey(map, options.conditionKeys);
        if (conditionId == null) {
            throw new AATParseException("В мапе условия не найден идентификатор! Ожидались ключи: " + options.conditionKeys);
        }

        NamespacedKey key = NamespacedKey.fromString(conditionId);
        if (key == null) throw new AATParseException("Невалидный ключ условия: " + conditionId);

        // ActionTriggerAPI.getFilters() берет мапу и прогоняет ее через метод с @ConfigFilter
        return ActionTriggerAPI.getFilters().create(key, map);
    }

    @SuppressWarnings("unchecked")
    public Filter parseConditions(Object conditionsConfig) {
        if (conditionsConfig == null) return kostin.ak.actionstriggers.api.filter.Filters.alwaysTrue();
        if (!(conditionsConfig instanceof List)) {
            throw new AATParseException("Блок условий должен быть списком (List)!");
        }

        List<Map<String, Object>> mapList = (List<Map<String, Object>>) conditionsConfig;
        if (mapList.isEmpty()) return kostin.ak.actionstriggers.api.filter.Filters.alwaysTrue();

        // Парсим каждый фильтр из списка
        Filter[] parsedFilters = mapList.stream()
                .map(this::parseCondition)
                .toArray(Filter[]::new);

        // По умолчанию список фильтров всегда объединяется логическим "И"
        return kostin.ak.actionstriggers.api.filter.Filters.and(parsedFilters);
    }

    // ========================================================================
    // 4. ПАРСИНГ ТРИГГЕРОВ
    // ========================================================================

    // Удобный объект для возврата готовой подписки и ее ключа
    public record ParsedTrigger(NamespacedKey triggerKey, kostin.ak.actionstriggers.api.trigger.TriggerSubscription subscription) {}

    public ParsedTrigger parseTrigger(
            @NotNull org.bukkit.plugin.Plugin plugin,
            @NotNull Map<String, Object> map,
            @NotNull Consumer<ExecutionContext> developerCallback) {

        String triggerId = options.findKey(map, options.triggerKeys);
        if (triggerId == null) {
            throw new AATParseException("В мапе триггера не найден идентификатор! Ожидались ключи: " + options.triggerKeys);
        }

        NamespacedKey triggerKey = NamespacedKey.fromString(triggerId);
        if (triggerKey == null) throw new AATParseException("Невалидный ключ триггера: " + triggerId);

        // 1. Парсим условия
        Filter filter = parseConditions(map.get("conditions")); // Ключ "conditions" можно вынести в ParserOptions

        // 2. Парсим инъекции
        Consumer<ExecutionContext> injector = null;
        if (map.containsKey(options.injectContextKey)) {
            injector = parseInjector(map.get(options.injectContextKey));
        }

        final Consumer<ExecutionContext> finalInjector = injector;

        // 3. Собираем итоговый коллбэк (Сначала инъекция, затем код стороннего разработчика)
        Consumer<ExecutionContext> finalCallback = context -> {
            if (finalInjector != null) {
                finalInjector.accept(context);
            }
            developerCallback.accept(context);
        };

        // Возвращаем объект, готовый для регистрации сторонним разработчиком
        return new ParsedTrigger(triggerKey, new kostin.ak.actionstriggers.api.trigger.TriggerSubscription(plugin, filter, finalCallback));
    }
}