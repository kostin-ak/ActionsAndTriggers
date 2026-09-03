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

    @Test
    @DisplayName("Should format player properties such as name, health, and combat_remaining correctly")
    void testPlayerFormattersAndCombatRemaining() {
        org.bukkit.entity.Player mockPlayer = org.mockito.Mockito.mock(org.bukkit.entity.Player.class);
        org.mockito.Mockito.when(mockPlayer.getName()).thenReturn("kostin_ak");
        org.mockito.Mockito.when(mockPlayer.getHealth()).thenReturn(20.0);
        java.util.UUID uuid = java.util.UUID.randomUUID();
        org.mockito.Mockito.when(mockPlayer.getUniqueId()).thenReturn(uuid);

        kostin.ak.actionstriggers.core.combat.CombatTracker tracker = new kostin.ak.actionstriggers.core.combat.CombatTracker();
        tracker.tag(mockPlayer, 8);

        // Регистрируем кастомный форматтер с трекером
        ContextPlaceholderParser.registerFormatter(org.bukkit.entity.Player.class, (p, prop) -> {
            if (prop == null) return p.getName();
            return switch (prop.toLowerCase()) {
                case "name" -> p.getName();
                case "uuid" -> p.getUniqueId().toString();
                case "health" -> String.format(java.util.Locale.ROOT, "%.1f", p.getHealth());
                case "combat_remaining", "combat_seconds", "combat_time", "combat" ->
                        String.valueOf(tracker.getRemainingSeconds(p));
                default -> p.getName();
            };
        });

        ExecutionContext ctx = new ExecutionContext();
        ctx.set(CoreKeys.PLAYER, mockPlayer);

        String template = "Подождите {player.combat_remaining} сек. Игрок: {player.name}, Здоровье: {player.health}";
        String result = ContextPlaceholderParser.resolve(template, ctx);

        assertTrue(result.contains("Подождите 8 сек.") || result.contains("Подождите 7 сек."));
        assertTrue(result.contains("Игрок: kostin_ak"));
        assertTrue(result.contains("Здоровье: 20.0"));
        assertFalse(result.contains("Подождите kostin_ak сек."), "combat_remaining must not fallback to player name");
    }
}
