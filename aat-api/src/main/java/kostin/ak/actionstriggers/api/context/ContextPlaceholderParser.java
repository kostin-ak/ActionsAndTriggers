package kostin.ak.actionstriggers.api.context;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Высокопроизводительный линейный шаблонизатор контекста без использования регулярных выражений (Zero-Regex).
 * Поддерживает регистрацию динамических форматтеров для любых типов объектов.
 */
public class ContextPlaceholderParser {

    private static final Map<Class<?>, PlaceholderFormatter<?>> formatters = new ConcurrentHashMap<>();

    @FunctionalInterface
    public interface PlaceholderFormatter<T> {
        String format(T value, String property);
    }

    static {
        // Базовые форматтеры
        registerFormatter(Player.class, (p, prop) -> {
            if (prop == null) return p.getName();
            return switch (prop.toLowerCase()) {
                case "name" -> p.getName();
                case "uuid" -> p.getUniqueId().toString();
                case "health" -> String.format(java.util.Locale.ROOT, "%.1f", p.getHealth());
                default -> p.getName();
            };
        });

        registerFormatter(Entity.class, (e, prop) -> {
            if (prop == null) return e.getName();
            return switch (prop.toLowerCase()) {
                case "name" -> e.getName();
                case "type" -> e.getType().name();
                case "uuid" -> e.getUniqueId().toString();
                default -> e.getName();
            };
        });

        registerFormatter(Location.class, (loc, prop) -> {
            if (prop == null) return loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();
            return switch (prop.toLowerCase()) {
                case "x" -> String.format(java.util.Locale.ROOT, "%.2f", loc.getX());
                case "y" -> String.format(java.util.Locale.ROOT, "%.2f", loc.getY());
                case "z" -> String.format(java.util.Locale.ROOT, "%.2f", loc.getZ());
                case "block_x" -> String.valueOf(loc.getBlockX());
                case "block_y" -> String.valueOf(loc.getBlockY());
                case "block_z" -> String.valueOf(loc.getBlockZ());
                case "world" -> loc.getWorld() != null ? loc.getWorld().getName() : "unknown";
                default -> loc.toString();
            };
        });

        registerFormatter(Block.class, (b, prop) -> {
            if (prop == null) return b.getType().name();
            return switch (prop.toLowerCase()) {
                case "type" -> b.getType().name();
                case "x" -> String.valueOf(b.getX());
                case "y" -> String.valueOf(b.getY());
                case "z" -> String.valueOf(b.getZ());
                case "world" -> b.getWorld().getName();
                default -> b.getType().name();
            };
        });

        registerFormatter(ItemStack.class, (item, prop) -> {
            if (prop == null) return item.getType().name();
            return switch (prop.toLowerCase()) {
                case "type" -> item.getType().name();
                case "amount" -> String.valueOf(item.getAmount());
                default -> item.getType().name();
            };
        });
    }

    public static <T> void registerFormatter(@NotNull Class<T> type, @NotNull PlaceholderFormatter<T> formatter) {
        formatters.put(type, formatter);
    }

    public static String resolve(String template, ExecutionContext context) {
        if (template == null || template.isEmpty() || context == null) {
            return template;
        }

        int firstOpen = template.indexOf('{');
        if (firstOpen == -1) {
            return template;
        }

        StringBuilder sb = new StringBuilder(template.length() + 16);
        int cursor = 0;
        int len = template.length();

        while (cursor < len) {
            int openIdx = template.indexOf('{', cursor);
            if (openIdx == -1) {
                sb.append(template, cursor, len);
                break;
            }

            int closeIdx = template.indexOf('}', openIdx + 1);
            if (closeIdx == -1) {
                sb.append(template, cursor, len);
                break;
            }

            sb.append(template, cursor, openIdx);

            String token = template.substring(openIdx + 1, closeIdx);
            String replacement = resolveToken(token, context);

            if (replacement != null) {
                sb.append(replacement);
            }

            cursor = closeIdx + 1;
        }

        return sb.toString();
    }

    private static String resolveToken(String token, ExecutionContext context) {
        int dotIdx = token.indexOf('.');
        String keyName;
        String property = null;

        if (dotIdx != -1) {
            keyName = token.substring(0, dotIdx);
            property = token.substring(dotIdx + 1);
        } else {
            keyName = token;
        }

        Object value = context.getRaw(keyName);
        if (value == null) {
            return null;
        }

        return formatObject(value, property);
    }

    @SuppressWarnings("unchecked")
    private static String formatObject(Object value, String property) {
        if (value == null) return "";

        Class<?> clazz = value.getClass();
        PlaceholderFormatter<Object> formatter = (PlaceholderFormatter<Object>) formatters.get(clazz);

        if (formatter == null) {
            for (Map.Entry<Class<?>, PlaceholderFormatter<?>> entry : formatters.entrySet()) {
                if (entry.getKey().isAssignableFrom(clazz)) {
                    formatter = (PlaceholderFormatter<Object>) entry.getValue();
                    break;
                }
            }
        }

        if (formatter != null) {
            return formatter.format(value, property);
        }

        return value.toString();
    }
}
