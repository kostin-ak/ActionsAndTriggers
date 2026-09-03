package kostin.ak.actionstriggers.api.action;

import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.api.context.ContextPlaceholderParser;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Умная обертка для параметров экшена.
 * Автоматически пропускает любые запрашиваемые значения через шаблонизатор.
 */
public class ActionParameters {

    private final Map<String, Object> rawParams;
    private final ExecutionContext context;

    public ActionParameters(@NotNull Map<String, Object> rawParams, @NotNull ExecutionContext context) {
        this.rawParams = rawParams;
        this.context = context;
    }

    /**
     * Получает строку, автоматически заменяя в ней все {плейсхолдеры}.
     */
    public String getString(String key, String def) {
        Object raw = rawParams.get(key);
        String rawStr = raw != null ? raw.toString() : def;
        return ContextPlaceholderParser.resolve(rawStr, context);
    }

    public int getInt(String key, int def) {
        try {
            // Запрашиваем как строку (чтобы плейсхолдер раскрылся) и парсим
            return Integer.parseInt(getString(key, String.valueOf(def)));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public double getDouble(String key, double def) {
        try {
            // Заменяем запятые на точки на случай, если локаль Java шалит
            String resolved = getString(key, String.valueOf(def)).replace(",", ".");
            return Double.parseDouble(resolved);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public float getFloat(String key, float def) {
        try {
            String resolved = getString(key, String.valueOf(def)).replace(",", ".");
            return Float.parseFloat(resolved);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public boolean getBoolean(String key, boolean def) {
        String resolved = getString(key, String.valueOf(def));
        return Boolean.parseBoolean(resolved);
    }
}
