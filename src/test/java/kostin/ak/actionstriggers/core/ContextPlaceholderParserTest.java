package kostin.ak.actionstriggers.core;

import kostin.ak.actionstriggers.api.context.ContextKey;
import kostin.ak.actionstriggers.api.context.ExecutionContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ContextPlaceholderParser Unit Tests")
class ContextPlaceholderParserTest {

    private static final ContextKey<String> KEY_PLAYER_NAME = ContextKey.of("player_name", String.class);
    private static final ContextKey<Double> KEY_DAMAGE = ContextKey.of("damage", Double.class);
    private static final ContextKey<Integer> KEY_TIER = ContextKey.of("tier", Integer.class);

    @Test
    @DisplayName("Should return unchanged text if template does not contain braces")
    void testNoBraces() {
        ExecutionContext ctx = new ExecutionContext();
        String plain = "A pure string with no templates or braces.";
        assertEquals(plain, ContextPlaceholderParser.resolve(plain, ctx));
    }

    @Test
    @DisplayName("Should resolve single and multiple context variables correctly")
    void testResolveSingleAndMultiple() {
        ExecutionContext ctx = new ExecutionContext();
        ctx.set(KEY_PLAYER_NAME, "Steve");
        ctx.set(KEY_DAMAGE, 12.5);
        ctx.set(KEY_TIER, 3);

        String template = "Player {player_name} took {damage} damage from tier {tier} catalyst!";
        String result = ContextPlaceholderParser.resolve(template, ctx);

        assertEquals("Player Steve took 12.5 damage from tier 3 catalyst!", result);
    }

    @Test
    @DisplayName("Should replace absent placeholders with empty string without crashing")
    void testMissingPlaceholders() {
        ExecutionContext ctx = new ExecutionContext();
        ctx.set(KEY_PLAYER_NAME, "Alex");

        String template = "Hello {player_name}! World is {missing_world}, damage is {missing_dmg}.";
        String result = ContextPlaceholderParser.resolve(template, ctx);

        assertEquals("Hello Alex! World is , damage is .", result);
    }

    @Test
    @DisplayName("Should allow custom formatter registration and format complex objects")
    void testCustomFormatter() {
        record CustomEnergy(int current, int max) {}

        ContextPlaceholderParser.registerFormatter(CustomEnergy.class, (energy, prop) -> {
            if ("percent".equalsIgnoreCase(prop)) {
                return (int) ((double) energy.current / energy.max * 100) + "%";
            }
            return energy.current + "/" + energy.max;
        });

        ExecutionContext ctx = new ExecutionContext();
        ctx.set(ContextKey.of("energy", CustomEnergy.class), new CustomEnergy(75, 100));

        String rawResult = ContextPlaceholderParser.resolve("Power: {energy}", ctx);
        String propResult = ContextPlaceholderParser.resolve("Percentage: {energy.percent}", ctx);

        assertEquals("Power: 75/100", rawResult);
        assertEquals("Percentage: 75%", propResult);
    }
}
