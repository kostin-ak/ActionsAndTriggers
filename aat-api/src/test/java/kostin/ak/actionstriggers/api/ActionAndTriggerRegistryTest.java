package kostin.ak.actionstriggers.api;

import kostin.ak.actionstriggers.api.action.Action;
import kostin.ak.actionstriggers.api.action.ActionRegistry;
import kostin.ak.actionstriggers.api.context.ContextKey;
import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.api.filter.Filter;
import kostin.ak.actionstriggers.api.trigger.Trigger;
import kostin.ak.actionstriggers.api.trigger.TriggerRegistry;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Action and Trigger Registry Unit Tests")
class ActionAndTriggerRegistryTest {

    private ActionRegistry actionRegistry;
    private TriggerRegistry triggerRegistry;
    private Plugin mockPlugin;

    @BeforeEach
    void setUp() {
        Logger logger = Logger.getLogger("AATTest");
        actionRegistry = new ActionRegistry(logger);
        triggerRegistry = new TriggerRegistry(logger);
        mockPlugin = Mockito.mock(Plugin.class);
    }

    @Test
    @DisplayName("Should register and execute custom actions correctly")
    void testActionRegistrationAndExecution() {
        NamespacedKey actionKey = new NamespacedKey("test", "increment");
        AtomicInteger counter = new AtomicInteger(0);

        actionRegistry.register(actionKey, (params) -> (ctx) -> {
            counter.incrementAndGet();
            return true;
        });

        assertTrue(actionRegistry.asList().contains(actionKey.toString()));

        Action action = actionRegistry.create(actionKey, Map.of());
        assertNotNull(action);

        ExecutionContext ctx = new ExecutionContext();
        boolean result = action.execute(ctx);

        assertTrue(result);
        assertEquals(1, counter.get());
    }

    @Test
    @DisplayName("Should register trigger and invoke subscribers when filter matches")
    void testTriggerSubscriptionAndDispatch() {
        NamespacedKey triggerKey = new NamespacedKey("test", "custom_event");

        Trigger trigger = new Trigger() {
            @Override
            public NamespacedKey getKey() {
                return triggerKey;
            }

            @Override
            public List<ContextKey<?>> getProvidedContext() {
                return List.of();
            }
        };

        triggerRegistry.register(trigger);
        assertTrue(triggerRegistry.asList().contains(triggerKey.toString()));

        AtomicBoolean callbackExecuted = new AtomicBoolean(false);
        Filter allowAll = (ctx) -> true;

        triggerRegistry.subscribe(triggerKey, mockPlugin, allowAll, (ctx) -> {
            callbackExecuted.set(true);
        });

        ExecutionContext eventCtx = new ExecutionContext();
        triggerRegistry.dispatch(triggerKey, eventCtx);

        assertTrue(callbackExecuted.get());
    }

    @Test
    @DisplayName("Should NOT invoke trigger subscriber when filter fails")
    void testTriggerFilteredOut() {
        NamespacedKey triggerKey = new NamespacedKey("test", "filtered_event");

        Trigger trigger = new Trigger() {
            @Override
            public NamespacedKey getKey() {
                return triggerKey;
            }

            @Override
            public List<ContextKey<?>> getProvidedContext() {
                return List.of();
            }
        };

        triggerRegistry.register(trigger);

        AtomicBoolean callbackExecuted = new AtomicBoolean(false);
        Filter denyAll = (ctx) -> false;

        triggerRegistry.subscribe(triggerKey, mockPlugin, denyAll, (ctx) -> {
            callbackExecuted.set(true);
        });

        ExecutionContext eventCtx = new ExecutionContext();
        triggerRegistry.dispatch(triggerKey, eventCtx);

        assertFalse(callbackExecuted.get());
    }
}
