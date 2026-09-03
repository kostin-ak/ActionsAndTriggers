package kostin.ak.actionstriggers.core.command;

import kostin.ak.actionstriggers.api.ActionTriggerAPI;
import kostin.ak.actionstriggers.api.action.ActionRegistry;
import kostin.ak.actionstriggers.api.action.ActionScheduler;
import kostin.ak.actionstriggers.api.filter.FilterRegistry;
import kostin.ak.actionstriggers.api.gui.GuiDefinition;
import kostin.ak.actionstriggers.api.provider.impl.VanillaItemProvider;
import kostin.ak.actionstriggers.api.trigger.TriggerRegistry;
import kostin.ak.actionstriggers.core.defaults.actions.DefaultActionParsers;
import kostin.ak.actionstriggers.core.defaults.triggers.BlockBreakTrigger;
import kostin.ak.actionstriggers.core.defaults.triggers.PlayerDamageTrigger;
import kostin.ak.actionstriggers.core.gui.GuiRegistry;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AATTabCompleter Dynamic Autocomplete Tests")
class AATTabCompleterTest {

    private AATTabCompleter tabCompleter;
    private GuiRegistry guiRegistry;

    @BeforeEach
    void setUp() {
        Plugin mockPlugin = Mockito.mock(Plugin.class);
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger("AATTest");
        ActionRegistry actionRegistry = new ActionRegistry(logger);
        TriggerRegistry triggerRegistry = new TriggerRegistry(logger);
        FilterRegistry filterRegistry = new FilterRegistry(logger);
        ActionScheduler scheduler = new ActionScheduler(mockPlugin);

        ActionTriggerAPI.init(actionRegistry, triggerRegistry, filterRegistry, scheduler);
        ActionTriggerAPI.getRegistrar().registerActions(DefaultActionParsers.class);
        ActionTriggerAPI.getRegistrar().registerItemProvider(new VanillaItemProvider());

        // Регистрируем тестовые триггеры
        triggerRegistry.register(new BlockBreakTrigger());
        triggerRegistry.register(new PlayerDamageTrigger());

        guiRegistry = new GuiRegistry();
        guiRegistry.register(new GuiDefinition("world_navigator_gui", "Navigator", 3));
        guiRegistry.register(new GuiDefinition("ice_fabricator_gui", "Fabricator", 6));

        tabCompleter = new AATTabCompleter(guiRegistry);
    }

    @Test
    @DisplayName("Should suggest all subcommands when root command is typed without arguments")
    void testRootSubcommands() {
        List<String> resultsEmpty = tabCompleter.complete(new String[]{""});
        assertTrue(resultsEmpty.contains("run"));
        assertTrue(resultsEmpty.contains("trigger"));
        assertTrue(resultsEmpty.contains("open"));
        assertTrue(resultsEmpty.contains("reload"));
        assertTrue(resultsEmpty.contains("list"));
        assertTrue(resultsEmpty.contains("guis"));

        List<String> resultsFiltered = tabCompleter.complete(new String[]{"r"});
        assertTrue(resultsFiltered.contains("run"));
        assertTrue(resultsFiltered.contains("reload"));
        assertFalse(resultsFiltered.contains("trigger"));
    }

    @Test
    @DisplayName("Should suggest registered actions when 'run' subcommand is used")
    void testActionCompletion() {
        List<String> actions = tabCompleter.complete(new String[]{"run", ""});
        assertTrue(actions.contains("core:damage"));
        assertTrue(actions.contains("core:message"));
        assertTrue(actions.contains("core:teleport"));

        List<String> filteredActions = tabCompleter.complete(new String[]{"run", "core:dam"});
        assertEquals(1, filteredActions.size());
        assertEquals("core:damage", filteredActions.get(0));
    }

    @Test
    @DisplayName("Should suggest 'args={' and 'context={' blocks after action ID is chosen")
    void testActionArgumentBlocks() {
        List<String> blocks = tabCompleter.complete(new String[]{"run", "core:sound", ""});
        assertTrue(blocks.contains("args={"));
        assertTrue(blocks.contains("context={"));
    }

    @Test
    @DisplayName("Should dynamically suggest parameters inside args={} based on action metadata")
    void testInsideArgsBlockMetadata() {
        // Пользователь открыл блок args={ для core:sound
        List<String> params = tabCompleter.complete(new String[]{"run", "core:sound", "args={"});
        assertTrue(params.contains("args={sound="));
        assertTrue(params.contains("args={volume="));
        assertTrue(params.contains("args={pitch="));

        // Пользователь уже ввел sound=minecraft:entity.player.levelup, и через пробел запрашивает следующие параметры
        List<String> remaining = tabCompleter.complete(new String[]{"run", "core:sound", "args={sound=minecraft:entity.player.levelup", ""});
        assertFalse(remaining.contains("sound="), "Already specified parameter 'sound' must be excluded");
        assertTrue(remaining.contains("volume="));
        assertTrue(remaining.contains("pitch="));
    }

    @Test
    @DisplayName("Should suggest parameter values when typing after '=' inside args={}")
    void testParameterValueSuggestions() {
        // Для логического параметра as_console в core:command
        List<String> booleanValues = tabCompleter.complete(new String[]{"run", "core:command", "args={as_console="});
        assertTrue(booleanValues.contains("args={as_console=true"));
        assertTrue(booleanValues.contains("args={as_console=false"));
    }

    @Test
    @DisplayName("Should suggest registered triggers when 'trigger' subcommand is used")
    void testTriggerCompletion() {
        List<String> triggers = tabCompleter.complete(new String[]{"trigger", ""});
        assertTrue(triggers.contains("core:block_break"));
        assertTrue(triggers.contains("core:player_damage"));

        List<String> filtered = tabCompleter.complete(new String[]{"trigger", "core:block"});
        assertTrue(filtered.contains("core:block_break"));
        assertFalse(filtered.contains("core:player_damage"));
    }

    @Test
    @DisplayName("Should dynamically suggest provided context keys for triggers")
    void testTriggerContextKeys() {
        // После выбора триггера предлагается context={
        List<String> block = tabCompleter.complete(new String[]{"trigger", "core:block_break", ""});
        assertTrue(block.contains("context={"));

        // Внутри context={ подсказываются ключи контекста триггера
        List<String> ctxKeys = tabCompleter.complete(new String[]{"trigger", "core:block_break", "context={"});
        assertTrue(ctxKeys.contains("context={player="));
        assertTrue(ctxKeys.contains("context={block="));
    }

    @Test
    @DisplayName("Should suggest registered GUI IDs when 'open' subcommand is used")
    void testGuiCompletion() {
        List<String> guis = tabCompleter.complete(new String[]{"open", ""});
        assertTrue(guis.contains("world_navigator_gui"));
        assertTrue(guis.contains("ice_fabricator_gui"));

        List<String> filtered = tabCompleter.complete(new String[]{"open", "ice"});
        assertEquals(1, filtered.size());
        assertEquals("ice_fabricator_gui", filtered.get(0));
    }

    @Test
    @DisplayName("Should suggest provider namespaces for provider_items subcommand")
    void testProviderItemsCompletion() {
        List<String> namespaces = tabCompleter.complete(new String[]{"provider_items", ""});
        assertTrue(namespaces.contains("minecraft"));
    }

    @Test
    @DisplayName("Should suggest chat/actionbar/title for core:message type parameter, NOT damage types")
    void testMessageActionTypeValues() {
        List<String> typeSuggestions = tabCompleter.complete(new String[]{"run", "core:message", "args={type="});
        assertTrue(typeSuggestions.contains("args={type=chat"));
        assertTrue(typeSuggestions.contains("args={type=actionbar"));
        assertTrue(typeSuggestions.contains("args={type=title"));
        assertFalse(typeSuggestions.contains("args={type=FALL"), "Damage types must not be suggested for core:message");
        assertFalse(typeSuggestions.contains("args={type=PHYSICAL"), "Damage types must not be suggested for core:message");
    }

    @Test
    @DisplayName("Should suggest block types and world options inside context={}")
    void testContextBlockValues() {
        // block_id
        List<String> blockIdSuggestions = tabCompleter.complete(new String[]{"run", "core:message", "context={block_id="});
        assertTrue(blockIdSuggestions.contains("context={block_id=minecraft:stone"));
        assertTrue(blockIdSuggestions.contains("context={block_id=minecraft:blue_ice"));

        // block_material
        List<String> blockMatSuggestions = tabCompleter.complete(new String[]{"run", "core:message", "context={block_material="});
        assertTrue(blockMatSuggestions.contains("context={block_material=STONE"));
        assertTrue(blockMatSuggestions.contains("context={block_material=BLUE_ICE"));

        // item_in_hand_id
        List<String> itemSuggestions = tabCompleter.complete(new String[]{"run", "core:message", "context={item_in_hand_id="});
        assertTrue(itemSuggestions.contains("context={item_in_hand_id=minecraft:diamond_sword"));
        assertTrue(itemSuggestions.contains("context={item_in_hand_id=minecraft:compass"));

        // location
        List<String> locSuggestions = tabCompleter.complete(new String[]{"run", "core:message", "context={location="});
        assertTrue(locSuggestions.contains("context={location=~,~,~"));
        assertTrue(locSuggestions.contains("context={location=0,64,0"));

        // cause
        List<String> causeSuggestions = tabCompleter.complete(new String[]{"run", "core:message", "context={damage_cause="});
        assertTrue(causeSuggestions.contains("context={damage_cause=ENTITY_ATTACK"));
        assertTrue(causeSuggestions.contains("context={damage_cause=FALL"));
    }
}
