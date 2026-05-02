package kostin.ak.actionstriggers.api.parser;

import java.util.Arrays;
import java.util.List;

public class ParserOptions {
    // Сторонний разработчик может поменять эти списки, если захочет другие ключи в конфигах
    public List<String> actionKeys = Arrays.asList("action", "type", "id");
    public List<String> conditionKeys = Arrays.asList("type", "condition", "id");
    public List<String> triggerKeys = Arrays.asList("trigger", "event", "type");
    public String injectContextKey = "inject_context";

    // Возвращает первый найденный валидный ключ из мапы, убедившись, что это именно строка
    public String findKey(java.util.Map<String, Object> map, List<String> possibleKeys) {
        for (String k : possibleKeys) {
            Object value = map.get(k);
            // Если значение существует и является строкой (а не вложенной мапой/списком)
            if (value instanceof String) {
                return (String) value;
            }
        }
        return null;
    }
}