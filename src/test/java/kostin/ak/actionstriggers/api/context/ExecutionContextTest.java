package kostin.ak.actionstriggers.api.context;

import kostin.ak.actionstriggers.core.CoreKeys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ExecutionContext Unit Tests")
class ExecutionContextTest {

    private static final ContextKey<String> TEST_KEY = ContextKey.of("test_string", String.class);
    private static final ContextKey<Integer> COUNT_KEY = ContextKey.of("count", Integer.class);

    @Test
    @DisplayName("Should store and retrieve strongly typed values correctly")
    void testSetAndGet() {
        ExecutionContext ctx = new ExecutionContext();
        ctx.set(TEST_KEY, "astral_value");
        ctx.set(COUNT_KEY, 42);

        assertEquals("astral_value", ctx.get(TEST_KEY));
        assertEquals(42, ctx.get(COUNT_KEY));
        assertNull(ctx.get(CoreKeys.WORLD));
    }

    @Test
    @DisplayName("Should return raw value by string key identifier")
    void testGetRaw() {
        ExecutionContext ctx = new ExecutionContext();
        ctx.set(TEST_KEY, "raw_sample");

        assertEquals("raw_sample", ctx.getRaw("test_string"));
        assertNull(ctx.getRaw("non_existent"));
    }

    @Test
    @DisplayName("Should return default value when key is absent")
    void testGetOrDefault() {
        ExecutionContext ctx = new ExecutionContext();
        assertEquals("fallback", ctx.getOrDefault(TEST_KEY, "fallback"));

        ctx.set(TEST_KEY, "present");
        assertEquals("present", ctx.getOrDefault(TEST_KEY, "fallback"));
    }

    @Test
    @DisplayName("Should throw exception on require when required key is missing")
    void testRequire() {
        ExecutionContext ctx = new ExecutionContext();
        assertThrows(IllegalArgumentException.class, () -> ctx.getOrThrow(TEST_KEY));

        ctx.set(TEST_KEY, "present");
        assertDoesNotThrow(() -> ctx.getOrThrow(TEST_KEY));
        assertEquals("present", ctx.getOrThrow(TEST_KEY));
    }

    @Test
    @DisplayName("Should clone context independently without mutating origin")
    void testCloning() {
        ExecutionContext origin = new ExecutionContext();
        origin.set(TEST_KEY, "initial");

        ExecutionContext clone = origin.clone();
        assertEquals("initial", clone.get(TEST_KEY));

        clone.set(TEST_KEY, "modified_in_clone");
        assertEquals("initial", origin.get(TEST_KEY));
        assertEquals("modified_in_clone", clone.get(TEST_KEY));
    }

    @Test
    @DisplayName("Should manage cancellation state accurately")
    void testCancellation() {
        ExecutionContext ctx = new ExecutionContext();
        assertFalse(ctx.isCancelled());

        ctx.setCancelled(true);
        assertTrue(ctx.isCancelled());
    }
}
