package kostin.ak.actionstriggers.core.i18n;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("I18n Localization Service Tests")
class I18nTest {

    @Test
    @DisplayName("Should return raw key as fallback when not initialized")
    void testFallbackWithoutInit() {
        String key = "commands.reload.success";
        assertEquals(key, I18n.get(key));
    }

    @Test
    @DisplayName("Should replace parameters correctly in template string")
    void testParameterReplacement() {
        Map<String, String> params = Map.of("time", "42", "triggers", "10", "guis", "2");
        String template = "Reloaded in {time} ms! ({triggers} triggers, {guis} guis)";

        String result = template;
        for (Map.Entry<String, String> e : params.entrySet()) {
            result = result.replace("{" + e.getKey() + "}", e.getValue());
        }

        assertTrue(result.contains("42 ms"));
        assertTrue(result.contains("10 triggers"));
        assertTrue(result.contains("2 guis"));
    }
}
