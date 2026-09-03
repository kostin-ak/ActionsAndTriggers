package kostin.ak.actionstriggers.core.hook;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Мягкая интеграция с PlaceholderAPI.
 * Поддерживает как стандартный синтаксис %placeholder%, так и фигурные скобки {placeholder}.
 */
public class PapiHook {

    private static boolean initialized = false;
    private static boolean enabled = false;
    private static Method setPlaceholdersMethod = null;

    private static final Pattern PAPI_BRACKET_PATTERN = Pattern.compile("\\{([a-zA-Z0-9_]+)\\}");

    private static void checkInit() {
        if (initialized) return;
        initialized = true;
        try {
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                Class<?> papiClass = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
                setPlaceholdersMethod = papiClass.getMethod("setPlaceholders", Player.class, String.class);
                enabled = true;
            }
        } catch (Throwable ignored) {
            enabled = false;
        }
    }

    /**
     * Парсит плейсхолдеры для игрока через PlaceholderAPI
     */
    public static String parse(Player player, String text) {
        if (text == null || text.isEmpty() || player == null) return text;
        if (!initialized) checkInit();
        if (!enabled || setPlaceholdersMethod == null) return text;

        try {
            if (text.indexOf('%') != -1) {
                text = (String) setPlaceholdersMethod.invoke(null, player, text);
            }

            int firstBrace = text.indexOf('{');
            if (firstBrace != -1) {
                Matcher matcher = PAPI_BRACKET_PATTERN.matcher(text);
                StringBuilder sb = new StringBuilder(text.length() + 16);
                while (matcher.find()) {
                    String key = matcher.group(1);
                    String papiFormatted = "%" + key + "%";
                    String resolved = (String) setPlaceholdersMethod.invoke(null, player, papiFormatted);
                    if (resolved != null && !resolved.equals(papiFormatted)) {
                        matcher.appendReplacement(sb, Matcher.quoteReplacement(resolved));
                    } else {
                        matcher.appendReplacement(sb, Matcher.quoteReplacement("{" + key + "}"));
                    }
                }
                matcher.appendTail(sb);
                text = sb.toString();
            }
        } catch (Throwable ignored) {}

        return text;
    }

    public static boolean isEnabled() {
        checkInit();
        return enabled;
    }
}
