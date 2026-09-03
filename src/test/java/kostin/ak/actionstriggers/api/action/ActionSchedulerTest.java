package kostin.ak.actionstriggers.api.action;

import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ActionScheduler and Cooldown Unit Tests")
class ActionSchedulerTest {

    private ActionScheduler scheduler;
    private Plugin mockPlugin;
    private UUID testPlayerId;

    @BeforeEach
    void setUp() {
        mockPlugin = Mockito.mock(Plugin.class);
        scheduler = new ActionScheduler(mockPlugin);
        testPlayerId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should report no cooldown initially")
    void testInitialCooldownState() {
        assertFalse(scheduler.isOnCooldown(testPlayerId, "freeze_burst"));
        assertEquals(0, scheduler.getRemainingCooldown(testPlayerId, "freeze_burst"));
    }

    @Test
    @DisplayName("Should correctly record and enforce cooldown with remaining seconds")
    void testSetAndCheckCooldown() {
        scheduler.setCooldown(testPlayerId, "freeze_burst", 10);

        assertTrue(scheduler.isOnCooldown(testPlayerId, "freeze_burst"));
        int remaining = scheduler.getRemainingCooldown(testPlayerId, "freeze_burst");
        assertTrue(remaining >= 9 && remaining <= 10);

        // Different key should not be on cooldown
        assertFalse(scheduler.isOnCooldown(testPlayerId, "other_spell"));
    }

    @Test
    @DisplayName("Should isolate cooldowns across different players")
    void testPlayerIsolation() {
        UUID otherPlayer = UUID.randomUUID();
        scheduler.setCooldown(testPlayerId, "teleport", 15);

        assertTrue(scheduler.isOnCooldown(testPlayerId, "teleport"));
        assertFalse(scheduler.isOnCooldown(otherPlayer, "teleport"));
    }

    @Test
    @DisplayName("Should clear all cooldowns on reset")
    void testClearCooldowns() {
        scheduler.setCooldown(testPlayerId, "spell_1", 20);
        scheduler.setCooldown(testPlayerId, "spell_2", 30);

        assertTrue(scheduler.isOnCooldown(testPlayerId, "spell_1"));
        assertTrue(scheduler.isOnCooldown(testPlayerId, "spell_2"));

        scheduler.clearCooldowns();

        assertFalse(scheduler.isOnCooldown(testPlayerId, "spell_1"));
        assertFalse(scheduler.isOnCooldown(testPlayerId, "spell_2"));
    }

    @Test
    @DisplayName("Should return false when cancelling non-existent named task")
    void testCancelNonExistentTask() {
        assertFalse(scheduler.cancelNamed("ghost_task_404"));
    }
}
