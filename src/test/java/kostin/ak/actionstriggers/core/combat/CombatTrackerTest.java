package kostin.ak.actionstriggers.core.combat;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CombatTracker Unit Tests")
class CombatTrackerTest {

    private CombatTracker tracker;
    private Player player;
    private UUID playerUuid;

    @BeforeEach
    void setUp() {
        tracker = new CombatTracker();
        playerUuid = UUID.randomUUID();
        player = Mockito.mock(Player.class);
        Mockito.when(player.getUniqueId()).thenReturn(playerUuid);
    }

    @Test
    @DisplayName("Should report player is not in combat initially")
    void testInitialState() {
        assertFalse(tracker.isInCombat(player));
        assertEquals(0, tracker.getRemainingSeconds(player));
    }

    @Test
    @DisplayName("Should successfully tag player into combat with duration")
    void testTagPlayer() {
        tracker.tag(player, 15);

        assertTrue(tracker.isInCombat(player));
        int remaining = tracker.getRemainingSeconds(player);
        assertTrue(remaining >= 14 && remaining <= 15);
    }

    @Test
    @DisplayName("Should untag player immediately upon call")
    void testUntagPlayer() {
        tracker.tag(player, 30);
        assertTrue(tracker.isInCombat(player));

        tracker.untag(player);
        assertFalse(tracker.isInCombat(player));
        assertEquals(0, tracker.getRemainingSeconds(player));
    }

    @Test
    @DisplayName("Should clear all combat tags when clear is called")
    void testClear() {
        Player player2 = Mockito.mock(Player.class);
        Mockito.when(player2.getUniqueId()).thenReturn(UUID.randomUUID());

        tracker.tag(player, 15);
        tracker.tag(player2, 20);

        assertTrue(tracker.isInCombat(player));
        assertTrue(tracker.isInCombat(player2));

        tracker.clear();

        assertFalse(tracker.isInCombat(player));
        assertFalse(tracker.isInCombat(player2));
    }
}
