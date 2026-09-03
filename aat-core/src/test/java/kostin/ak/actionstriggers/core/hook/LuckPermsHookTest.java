package kostin.ak.actionstriggers.core.hook;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LuckPerms Hook & Fallback Unit Tests")
class LuckPermsHookTest {

    @Test
    @DisplayName("Should gracefully fall back to defaults when LuckPerms is not installed")
    void testFallbackWhenDisabled() {
        // Without LuckPerms provider initialized, hook should remain safely disabled
        assertFalse(LuckPermsHook.isEnabled());

        Player mockPlayer = Mockito.mock(Player.class);

        // Fallback checks
        assertEquals("default", LuckPermsHook.getPrimaryGroup(mockPlayer));
        assertEquals("", LuckPermsHook.getPrefix(mockPlayer));
        assertEquals("", LuckPermsHook.getSuffix(mockPlayer));
        assertEquals(0, LuckPermsHook.getWeight(mockPlayer));
        assertFalse(LuckPermsHook.inGroup(mockPlayer, "admin"));

        // Modifying permissions or groups when disabled must not throw exceptions
        assertDoesNotThrow(() -> LuckPermsHook.addPermission(mockPlayer, "test.permission"));
        assertDoesNotThrow(() -> LuckPermsHook.removePermission(mockPlayer, "test.permission"));
        assertDoesNotThrow(() -> LuckPermsHook.setGroup(mockPlayer, "vip"));
    }
}
