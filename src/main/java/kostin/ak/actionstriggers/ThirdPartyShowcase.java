package kostin.ak.actionstriggers;

import kostin.ak.actionstriggers.api.ActionTriggerAPI;
import kostin.ak.actionstriggers.api.action.Action;
import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.api.filter.ConfigFilter;
import kostin.ak.actionstriggers.api.filter.Filter;
import kostin.ak.actionstriggers.api.filter.IFilterParsers;
import kostin.ak.actionstriggers.api.parser.AATParser;
import kostin.ak.actionstriggers.core.CoreKeys;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.function.Consumer;

/**
 * Симуляция стороннего плагина, использующего Actions & Triggers API.
 */
public class ThirdPartyShowcase implements IFilterParsers {

    // ========================================================================
    // 1. КАСТОМНЫЙ ФИЛЬТР СТОРОННЕГО РАЗРАБОТЧИКА
    // ========================================================================

    // Пишем свой фильтр на проверку взгляда на воду и регистрируем в реестре
    @ConfigFilter("showcase:looking_at_water")
    public static Filter parseLookingAtWater(Map<String, Object> params) {
        return context -> {
            Player p = context.get(CoreKeys.PLAYER);
            if (p == null) return false;

            // FluidCollisionMode.ALWAYS позволяет "увидеть" воду
            Block b = p.getTargetBlockExact(5, FluidCollisionMode.ALWAYS);
            return b != null && b.getType() == Material.WATER;
        };
    }

    // ========================================================================
    // 2. ИНИЦИАЛИЗАЦИЯ И ЗАГРУЗКА (Вызывается при старте плагина)
    // ========================================================================

    public static void loadShowcase(Plugin plugin) {
        // Сторонний разработчик регистрирует свои фильтры в НАШЕМ API
        ActionTriggerAPI.getFilters().scanAndRegister(ThirdPartyShowcase.class);

        AATParser parser = new AATParser();

        // Симулируем чтение конфигов (YAML/JSON)
        loadWaterDrinkingScenario(plugin, parser);
        loadDiamondMinerScenario(plugin, parser);
        loadWelcomeScenario(plugin, parser);
        loadCowardQuitScenario(plugin, parser);
        loadThorHammerScenario(plugin, parser);
        loadJumpPadScenario(plugin, parser);
        loadCancelSwapScenario(plugin, parser);
    }

    // ========================================================================
    // 3. СЦЕНАРИИ
    // ========================================================================

    // СЦЕНАРИЙ 1: Попить водички (Сложные условия + кастомный фильтр)
    private static void loadWaterDrinkingScenario(Plugin plugin, AATParser parser) {
        Map<String, Object> triggerMap = new HashMap<>();
        triggerMap.put("trigger", "core:player_interact");

        // Условие: ПКМ + наш кастомный фильтр
        List<Map<String, Object>> conditions = new ArrayList<>();
        conditions.add(Map.of("type", "core:eq", "key", "button", "value", CoreKeys.ButtonType.LEFT));
        conditions.add(Map.of("type", "showcase:looking_at_water"));
        triggerMap.put("conditions", conditions);

        // Действия: Звук питья + Сообщение в Actionbar
        List<Map<String, Object>> actionsList = List.of(
                Map.of("action", "core:sound", "sound", "entity.generic.drink"),
                Map.of("action", "core:message", "type", "actionbar", "text", "<aqua>Буль-буль... Вы попили воды!")
        );

        registerScenario(plugin, parser, triggerMap, actionsList);
    }

    // СЦЕНАРИЙ 2: Добыча алмазов (Инъекция контекста + Шанс)
    private static void loadDiamondMinerScenario(Plugin plugin, AATParser parser) {
        Map<String, Object> triggerMap = new HashMap<>();
        triggerMap.put("trigger", "core:block_break");

        // Условие: Блок = Алмазная руда
        triggerMap.put("conditions", List.of(
                Map.of("type", "core:eq", "key", "block_material", "value", Material.DIAMOND_ORE)
        ));

        // Инъекция контекста: Запоминаем высоту, на которой нашли алмазы
        triggerMap.put("inject_context", List.of(
                Map.of("key", "depth_msg", "value", "Ты нашел алмазы на высоте {location.y}!", "overwrite", "true")
        ));

        // Действия: Фейерверк и Тайтл (но фейерверк только с шансом 50%)
        List<Map<String, Object>> actionsList = List.of(
                Map.of(
                        "action", "core:message",
                        "type", "title",
                        "text", "<aqua>АЛМАЗЫ!</aqua>",
                        "subtitle", "<gray>{depth_msg}</gray>"
                ),
                Map.of(
                        "action", "core:firework",
                        "power", "1",
                        // Локальный inject_context для конкретного экшена (работает как фильтр, если бы у нас был экшен-кондишн,
                        // но в данном случае мы просто покажем, что экшен срабатывает)
                        "conditions", List.of(Map.of("type", "core:chance", "chance", "0.5")) // Заметка: мы не реализовали парсинг conditions внутри экшенов, поэтому шанс пока опустим для чистоты эксперимента!
                )
        );

        registerScenario(plugin, parser, triggerMap, actionsList);
    }

    // СЦЕНАРИЙ 3: Приветствие
    private static void loadWelcomeScenario(Plugin plugin, AATParser parser) {
        Map<String, Object> triggerMap = Map.of("trigger", "core:player_join");
        List<Map<String, Object>> actionsList = List.of(
                Map.of("action", "core:sound", "sound", "entity.player.levelup"),
                Map.of("action", "core:message", "text", "<green>Добро пожаловать, {player.name}!</green>")
        );
        registerScenario(plugin, parser, triggerMap, actionsList);
    }

    // СЦЕНАРИЙ 4: Выход с сервера (Консольная команда)
    private static void loadCowardQuitScenario(Plugin plugin, AATParser parser) {
        Map<String, Object> triggerMap = Map.of("trigger", "core:player_quit");
        List<Map<String, Object>> actionsList = List.of(
                Map.of(
                        "action", "core:command",
                        "as_console", "true",
                        "command", "say Игрок {player.name} покинул сервер!"
                )
        );
        registerScenario(plugin, parser, triggerMap, actionsList);
    }

    // Вспомогательный метод для сборки и регистрации
    private static void registerScenario(Plugin plugin, AATParser parser, Map<String, Object> triggerMap, List<Map<String, Object>> actionsList) {
        try {
            List<Action> parsedActions = parser.parseActions(actionsList);

            AATParser.ParsedTrigger parsedTrigger = parser.parseTrigger(plugin, triggerMap, context -> {
                for (Action act : parsedActions) {
                    act.execute(context);
                }
            });

            ActionTriggerAPI.getTriggers().subscribe(
                    parsedTrigger.triggerKey(),
                    parsedTrigger.subscription().plugin(),
                    parsedTrigger.subscription().filter(),
                    parsedTrigger.subscription().callback()
            );
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка загрузки сценария: " + e.getMessage());
        }
    }

    // СЦЕНАРИЙ 5: Молот Тора (Начало копания блока + Молния)
    private static void loadThorHammerScenario(Plugin plugin, AATParser parser) {
        Map<String, Object> triggerMap = new HashMap<>();
        triggerMap.put("trigger", "core:block_damage");

        // Теперь используем мощный core:match!
        // Он возьмет плейсхолдер, превратит его в строку и сравнит с GOLDEN_AXE
        triggerMap.put("conditions", List.of(
                Map.of(
                        "type", "core:match",
                        "template", "{item_in_hand.type}",
                        "value", "STONE"
                )
        ));

        List<Map<String, Object>> actionsList = List.of(
                Map.of(
                        "action", "core:spawn_entity",
                        "entity", "LIGHTNING_BOLT",
                        "x", "{location.x}",
                        "y", "{location.y}",
                        "z", "{location.z}"
                )
        );

        registerScenario(plugin, parser, triggerMap, actionsList);
    }

    // СЦЕНАРИЙ 6: Прыжковая платформа (Прыжок + Push)
    private static void loadJumpPadScenario(Plugin plugin, AATParser parser) {
        Map<String, Object> triggerMap = new HashMap<>();
        triggerMap.put("trigger", "core:player_jump"); // Триггер прыжка

        // Условие: Игрок должен стоять на блоке слизи (SLIME_BLOCK)
        // Для этого понадобится фильтр, проверяющий блок под ногами. Пока сделаем без него,
        // чтобы протестировать механику Push при КАЖДОМ прыжке (временно).

        List<Map<String, Object>> actionsList = List.of(
                Map.of(
                        "action", "core:push",
                        "y", "1.2", // Запускаем высоко вверх
                        "add", "true" // Добавляем к текущему импульсу прыжка
                ),
                Map.of("action", "core:sound", "sound", "entity.slime.jump")
        );

        registerScenario(plugin, parser, triggerMap, actionsList);
    }

    // СЦЕНАРИЙ 7: Блокировка смены предметов (Кнопка F + Cancel)
    private static void loadCancelSwapScenario(Plugin plugin, AATParser parser) {
        Map<String, Object> triggerMap = new HashMap<>();
        triggerMap.put("trigger", "core:swap_items"); // Нажатие клавиши 'F'

        List<Map<String, Object>> actionsList = List.of(
                Map.of("action", "core:cancel_event"), // Блокируем ванильную смену предмета в левую руку
                Map.of(
                        "action", "core:message",
                        "type", "chat",
                        "text", "<red>Смена предметов запрещена!</red>"
                )
        );

        registerScenario(plugin, parser, triggerMap, actionsList);
    }
}