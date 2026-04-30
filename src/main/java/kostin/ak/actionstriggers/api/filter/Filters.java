package kostin.ak.actionstriggers.api.filter;

import kostin.ak.actionstriggers.api.context.ContextKey;
import kostin.ak.actionstriggers.api.context.ExecutionContext;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;

/**
 * Фабрика для создания и комбинирования фильтров (Conditions).
 */
public final class Filters {

    private Filters() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Пропускает, если ВСЕ переданные фильтры вернули true (Логическое И).
     */
    @NotNull
    public static Filter and(@NotNull Filter... filters) {
        return context -> {
            for (Filter filter : filters) {
                if (!filter.test(context)) {
                    return false;
                }
            }
            return true;
        };
    }

    /**
     * Пропускает, если ХОТЯ БЫ ОДИН переданный фильтр вернул true (Логическое ИЛИ).
     */
    @NotNull
    public static Filter or(@NotNull Filter... filters) {
        return context -> {
            for (Filter filter : filters) {
                if (filter.test(context)) {
                    return true;
                }
            }
            return filters.length == 0; // Если фильтров нет, по умолчанию пропускаем (или можно false, зависит от логики)
        };
    }

    /**
     * Инвертирует результат переданного фильтра (Логическое НЕ).
     */
    @NotNull
    public static Filter not(@NotNull Filter filter) {
        return context -> !filter.test(context);
    }

    /**
     * Проверяет, что в контексте просто СУЩЕСТВУЕТ указанный ключ с правильным типом.
     */
    @NotNull
    public static <T> Filter has(@NotNull ContextKey<T> key) {
        return context -> context.has(key);
    }

    /**
     * Проверяет, что значение в контексте РАВНО указанному значению.
     */
    @NotNull
    public static <T> Filter eq(@NotNull ContextKey<T> key, @NotNull T expectedValue) {
        return context -> {
            T actualValue = context.get(key);
            return expectedValue.equals(actualValue);
        };
    }

    /**
     * Проверяет, что значение в контексте НАХОДИТСЯ В ПЕРЕДАННОМ СПИСКЕ (коллекции).
     * Тот самый метод для работы с десятками видов руды или предметов!
     */
    @NotNull
    public static <T> Filter in(@NotNull ContextKey<T> key, @NotNull Collection<T> allowedValues) {
        return context -> {
            T actualValue = context.get(key);
            return actualValue != null && allowedValues.contains(actualValue);
        };
    }

    /**
     * Пропускает всегда. Полезно как заглушка по умолчанию.
     */
    @NotNull
    public static Filter alwaysTrue() {
        return context -> true;
    }
}