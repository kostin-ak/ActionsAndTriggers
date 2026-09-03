package kostin.ak.actionstriggers.core.defaults.filters;

import kostin.ak.actionstriggers.api.context.ContextKey;
import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.api.filter.Filter;
import kostin.ak.actionstriggers.api.filter.Filters;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DefaultFilterParsers Unit Tests")
class FilterParsersTest {

    private static final ContextKey<String> KEY_WORLD = ContextKey.of("world", String.class);

    @Test
    @DisplayName("Should evaluate equality filter core:eq correctly")
    void testEqualityFilter() {
        Filter eqFilter = DefaultFilterParsers.parseEq(Map.of("key", "world", "value", "world_nether"));

        ExecutionContext matchingCtx = new ExecutionContext();
        matchingCtx.set(KEY_WORLD, "world_nether");
        assertTrue(eqFilter.test(matchingCtx));

        ExecutionContext nonMatchingCtx = new ExecutionContext();
        nonMatchingCtx.set(KEY_WORLD, "world_the_end");
        assertFalse(eqFilter.test(nonMatchingCtx));
    }

    @Test
    @DisplayName("Should evaluate string match filter core:match correctly")
    void testStringMatchFilter() {
        Filter matchFilter = DefaultFilterParsers.parseMatch(Map.of("template", "{world}", "value", "world_nether"));

        ExecutionContext netherCtx = new ExecutionContext();
        netherCtx.set(KEY_WORLD, "world_nether");
        assertTrue(matchFilter.test(netherCtx));

        ExecutionContext overworldCtx = new ExecutionContext();
        overworldCtx.set(KEY_WORLD, "world_overworld");
        assertFalse(matchFilter.test(overworldCtx));
    }

    @Test
    @DisplayName("Should evaluate containment filter core:in correctly")
    void testInFilter() {
        Filter inFilter = DefaultFilterParsers.parseIn(Map.of(
                "key", "world",
                "values", List.of("world_nether", "world_mining", "world_spawn")
        ));

        ExecutionContext validCtx = new ExecutionContext();
        validCtx.set(KEY_WORLD, "world_mining");
        assertTrue(inFilter.test(validCtx));

        ExecutionContext invalidCtx = new ExecutionContext();
        invalidCtx.set(KEY_WORLD, "secret_dungeon");
        assertFalse(inFilter.test(invalidCtx));
    }

    @Test
    @DisplayName("Should evaluate composite boolean operators and/or/not correctly")
    void testCompositeFilters() {
        Filter f1 = ctx -> "true".equals(ctx.getRaw("a"));
        Filter f2 = ctx -> "true".equals(ctx.getRaw("b"));

        Filter andFilter = Filters.and(f1, f2);
        Filter orFilter = Filters.or(f1, f2);
        Filter notFilter = Filters.not(f1);

        ExecutionContext bothTrue = new ExecutionContext();
        bothTrue.set(ContextKey.of("a", String.class), "true");
        bothTrue.set(ContextKey.of("b", String.class), "true");

        ExecutionContext onlyOne = new ExecutionContext();
        onlyOne.set(ContextKey.of("a", String.class), "true");
        onlyOne.set(ContextKey.of("b", String.class), "false");

        assertTrue(andFilter.test(bothTrue));
        assertFalse(andFilter.test(onlyOne));

        assertTrue(orFilter.test(bothTrue));
        assertTrue(orFilter.test(onlyOne));

        assertFalse(notFilter.test(bothTrue));
    }

    @Test
    @DisplayName("Should evaluate chance filter bounds strictly")
    void testChanceFilter() {
        Filter alwaysFilter = DefaultFilterParsers.parseChance(Map.of("chance", 1.0));
        Filter neverFilter = DefaultFilterParsers.parseChance(Map.of("chance", 0.0));

        ExecutionContext ctx = new ExecutionContext();
        assertTrue(alwaysFilter.test(ctx));
        assertFalse(neverFilter.test(ctx));
    }
}
