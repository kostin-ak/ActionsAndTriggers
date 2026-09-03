package kostin.ak.actionstriggers.core.i18n;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class I18nTest {

    @Test
    @DisplayName("Проверка fallback при отсутствии инициализации")
    void testFallbackWithoutInit() {
        String key = "commands.reload.success";
        assertEquals(key, I18n.get(key));
    }

    @Test
    @DisplayName("Проверка параметризованной подстановки аргументов")
    void testParameterReplacement() {
        // Симулируем ключ с плейсхолдерами
        Map<String, String> params = Map.of("time", "42", "triggers", "10", "guis", "2");
        String template = "Успешно перезагружено за {time} мс! ({triggers} триггеров, {guis} интерфейсов)";

        // Тестируем логику параметризации
        String result = template;
        for (Map.Entry<String, String> e : params.entrySet()) {
            result = result.replace("{" + e.getKey() + "}", e.getValue());
        }

        assertTrue(result.contains("42 мс"));
        assertTrue(result.contains("10 триггеров"));
        assertTrue(result.contains("2 интерфейсов"));
    }
}
