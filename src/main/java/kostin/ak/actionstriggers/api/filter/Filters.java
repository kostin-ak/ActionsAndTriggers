package kostin.ak.actionstriggers.api.filter;

import kostin.ak.actionstriggers.api.context.ContextKey;
import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.core.CoreKeys;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;

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

    /**
     * Фильтр по правам игрока (Permission).
     */
    @NotNull
    public static Filter permission(@NotNull String perm) {
        return context -> {
            Player p = context.get(CoreKeys.PLAYER);
            return p != null && p.hasPermission(perm);
        };
    }

    /**
     * Вероятностный фильтр (Шанс срабатывания от 0.0 до 1.0).
     */
    @NotNull
    public static Filter chance(double chance) {
        return context -> ThreadLocalRandom.current().nextDouble() <= chance;
    }

    /**
     * Математическое сравнение: Больше чем (для Double).
     */
    @NotNull
    public static Filter gt(@NotNull ContextKey<Double> key, double min) {
        return context -> {
            Double val = context.get(key);
            return val != null && val > min;
        };
    }

    /**
     * Математическое сравнение: Меньше чем (для Double).
     */
    @NotNull
    public static Filter lt(@NotNull ContextKey<Double> key, double max) {
        return context -> {
            Double val = context.get(key);
            return val != null && val < max;
        };
    }

    /**
     * Сравнение строк через регулярное выражение (Regex).
     * Идеально для проверки сообщений в чате.
     */
    @NotNull
    public static Filter regex(@NotNull ContextKey<String> key, @NotNull String pattern) {
        return context -> {
            String val = context.get(key);
            return val != null && val.matches(pattern);
        };
    }

    /**
     * Проверяет, смотрит ли игрок на блок определенного типа в пределах дистанции.
     */
    @NotNull
    public static Filter lookingAt(@NotNull org.bukkit.Material targetMaterial, int maxDistance) {
        return context -> {
            Player p = context.get(CoreKeys.PLAYER);
            if (p == null) return false;

            org.bukkit.block.Block targetBlock = p.getTargetBlockExact(maxDistance);
            return targetBlock != null && targetBlock.getType() == targetMaterial;
        };
    }
}