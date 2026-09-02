package kostin.ak.actionstriggers.core;

import kostin.ak.actionstriggers.api.context.ExecutionContext;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Умный шаблонизатор. Автоматически парсит любые ключи из Контекста
 * и поддерживает вложенные свойства (например, {player.name} или {location.x}).
 */
public class ContextPlaceholderParser {

    // Ищет паттерны вида {key} или {key.property}
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([a-zA-Z0-9_]+)(?:\\.([a-zA-Z0-9_]+))?\\}");

    // Реестр правил преобразования объектов в текст
    private static final Map<Class<?>, PlaceholderFormatter<?>> formatters = new ConcurrentHashMap<>();

    // Функциональный интерфейс для форматтеров
    @FunctionalInterface
    public interface PlaceholderFormatter<T> {
        String format(T value, String property);
    }

    static {
        // --- РЕГИСТРАЦИЯ ДЕФОЛТНЫХ ФОРМАТТЕРОВ ---

        // Для Игрока (поддерживает {player}, {player.name}, {player.uuid}, {player.health})
        registerFormatter(Player.class, (p, prop) -> {
            if (prop == null) return p.getName();
            return switch (prop.toLowerCase()) {
                case "name" -> p.getName();
                case "uuid" -> p.getUniqueId().toString();
                case "health" -> String.format("%.1f", p.getHealth());
                case "combat_remaining" -> String.valueOf(kostin.ak.actionstriggers.ActionsTriggers.getCombatTracker().getRemainingSeconds(p));
                default -> p.getName();
            };
        });

        // Для Сущностей (мобы)
        registerFormatter(Entity.class, (e, prop) -> {
            if (prop == null) return e.getName();
            return switch (prop.toLowerCase()) {
                case "name" -> e.getName();
                case "type" -> e.getType().name();
                case "uuid" -> e.getUniqueId().toString();
                default -> e.getName();
            };
        });

        // Для Локаций ({location}, {location.x}, {location.world})
        registerFormatter(Location.class, (loc, prop) -> {
            if (prop == null) return loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();
            return switch (prop.toLowerCase()) {
                case "x" -> String.format("%.2f", loc.getX());
                case "y" -> String.format("%.2f", loc.getY());
                case "z" -> String.format("%.2f", loc.getZ());
                case "block_x" -> String.valueOf(loc.getBlockX());
                case "block_y" -> String.valueOf(loc.getBlockY());
                case "block_z" -> String.valueOf(loc.getBlockZ());
                case "world" -> loc.getWorld() != null ? loc.getWorld().getName() : "unknown";
                default -> loc.toString();
            };
        });

        // Для Блоков
        registerFormatter(Block.class, (b, prop) -> {
            if (prop == null || prop.equalsIgnoreCase("type")) return b.getType().name();
            return b.getType().name();
        });

        // Для Предметов (ItemStack)
        registerFormatter(ItemStack.class, (item, prop) -> {
            if (prop == null || prop.equalsIgnoreCase("type")) return item.getType().name();
            if (prop.equalsIgnoreCase("amount")) return String.valueOf(item.getAmount());
            return item.getType().name();
        });

        // Для Дробных чисел (чтобы урон 4.5000001 выводился как 4.5)
        registerFormatter(Double.class, (d, prop) -> String.format("%.1f", d));
        registerFormatter(Float.class, (f, prop) -> String.format("%.1f", f));

        // Для Enum (Например, DamageCause или Action)
        registerFormatter(Enum.class, (e, prop) -> e.name());
    }

    /**
     * Позволяет сторонним плагинам регистрировать свои правила форматирования!
     */
    public static <T> void registerFormatter(@NotNull Class<T> clazz, @NotNull PlaceholderFormatter<T> formatter) {
        formatters.put(clazz, formatter);
    }

    /**
     * Главный метод. Находит все плейсхолдеры и заменяет их.
     */
    @NotNull
    public static String resolve(@NotNull String template, @NotNull ExecutionContext context) {
        if (!template.contains("{")) return template;

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String keyStr = matcher.group(1);
            String propStr = matcher.group(2);

            Object rawValue = context.getRaw(keyStr);

            if (rawValue != null) {
                String replacement = formatValue(rawValue, propStr);
                // Matcher.quoteReplacement защищает от ошибок, если в строке есть знаки $ или \
                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
            } else {
                // ИЗМЕНЕНИЕ: Заменяем отсутствующие ключи на пустую строку, чтобы не ломать логику фильтров и чат
                matcher.appendReplacement(result, "");
            }
        }
        matcher.appendTail(result);

        String parsed = result.toString();
        Player player = context.get(CoreKeys.PLAYER);
        if (player != null) {
            parsed = kostin.ak.actionstriggers.core.hook.PapiHook.parse(player, parsed);
        }
        return parsed;
    }

    /**
     * Подбирает нужный форматтер с учетом наследования.
     */
    @SuppressWarnings("unchecked")
    private static String formatValue(@NotNull Object value, String prop) {
        Class<?> clazz = value.getClass();

        // 1. Ищем прямое совпадение
        if (formatters.containsKey(clazz)) {
            return ((PlaceholderFormatter<Object>) formatters.get(clazz)).format(value, prop);
        }

        // 2. Ищем по наследованию/интерфейсам (Например, CraftPlayer -> Player.class)
        for (Map.Entry<Class<?>, PlaceholderFormatter<?>> entry : formatters.entrySet()) {
            if (entry.getKey().isAssignableFrom(clazz)) {
                return ((PlaceholderFormatter<Object>) entry.getValue()).format(value, prop);
            }
        }

        // 3. Фолбэк, если форматтер не найден
        return value.toString();
    }
}