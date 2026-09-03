package kostin.ak.actionstriggers.core.command;

import kostin.ak.actionstriggers.api.ActionTriggerAPI;
import kostin.ak.actionstriggers.api.context.ContextKey;
import kostin.ak.actionstriggers.api.meta.ActionParameterMeta;
import kostin.ak.actionstriggers.core.gui.GuiRegistry;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Интеллектуальный динамический движок автодополнения команд (/aat и /actionapi).
 * Анализирует ввод пользователя на лету, контекст, метаданные экшенов, триггеров и GUI.
 */
public class AATTabCompleter implements TabCompleter, Listener {

    private static final List<String> SUBCOMMANDS = List.of(
            "run", "trigger", "open", "list", "guis", "get", "providers", "provider_items", "debug", "reload"
    );

    private static final List<String> STANDARD_CONTEXT_KEYS = List.of(
            "player=", "target=", "block_id=", "block_material=", "item_in_hand_id=",
            "item_id=", "world=", "location=", "damage_cause=", "damage=", "level=",
            "button=", "is_flying=", "has_block=", "group="
    );

    private final GuiRegistry guiRegistry;

    public AATTabCompleter(@NotNull GuiRegistry guiRegistry) {
        this.guiRegistry = guiRegistry;
    }

    /**
     * Стандартный Bukkit TabCompleter
     */
    @Override
    @Nullable
    public List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            @NotNull String[] args
    ) {
        return complete(args);
    }

    /**
     * Paper AsyncTabCompleteEvent для бесшовного, мгновенного дополнения в реальном времени.
     */
    @EventHandler
    public void onAsyncTabComplete(com.destroystokyo.paper.event.server.AsyncTabCompleteEvent event) {
        String buffer = event.getBuffer();
        if (buffer.startsWith("/")) {
            buffer = buffer.substring(1);
        }

        String[] parts = buffer.split(" ", -1);
        if (parts.length == 0) return;

        String root = parts[0].toLowerCase();
        if (!root.equals("aat") && !root.equals("actionapi")) {
            return;
        }

        String[] subArgs = Arrays.copyOfRange(parts, 1, parts.length);
        List<String> suggestions = complete(subArgs);

        if (!suggestions.isEmpty()) {
            event.setCompletions(suggestions);
            event.setHandled(true);
        }
    }

    /**
     * Основной алгоритм динамического подбора вариантов.
     */
    public List<String> complete(String[] args) {
        if (args == null || args.length == 0 || (args.length == 1 && !args[0].contains(" "))) {
            String prefix = (args != null && args.length > 0) ? args[0].toLowerCase() : "";
            return filterPrefix(SUBCOMMANDS, prefix);
        }

        String sub = args[0].toLowerCase();
        String currentToken = args[args.length - 1];

        switch (sub) {
            case "run" -> {
                return completeRun(args);
            }
            case "trigger" -> {
                return completeTrigger(args);
            }
            case "open" -> {
                if (args.length == 2) {
                    return filterPrefix(guiRegistry.getAvailableIds(), currentToken);
                }
            }
            case "get" -> {
                if (args.length == 2) {
                    return filterPrefix(List.of("params"), currentToken);
                }
                if (args.length == 3) {
                    return filterPrefix(ActionTriggerAPI.getActions().asList(), currentToken);
                }
            }
            case "provider_items" -> {
                if (args.length == 2) {
                    List<String> namespaces = ActionTriggerAPI.getItems().getProviders().stream()
                            .map(p -> p.getNamespace())
                            .toList();
                    return filterPrefix(namespaces, currentToken);
                }
            }
        }

        return Collections.emptyList();
    }

    private List<String> completeRun(String[] args) {
        // args[0] = "run"
        if (args.length == 2) {
            // Выбор экшена
            return filterPrefix(ActionTriggerAPI.getActions().asList(), args[1]);
        }

        // Экшен уже введен: args[1] = "core:damage"
        String actionId = args[1];
        NamespacedKey actionKey = NamespacedKey.fromString(actionId);

        String currentToken = args[args.length - 1];
        String fullRemaining = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

        // Если находимся внутри блока args={...}
        if (isInBlock(fullRemaining, "args")) {
            return completeInsideArgsBlock(actionKey, currentToken, fullRemaining);
        }

        // Если находимся внутри блока context={...}
        if (isInBlock(fullRemaining, "context")) {
            return completeInsideContextBlock(null, currentToken, fullRemaining);
        }

        // Если начинаем писать новый блок
        List<String> options = new ArrayList<>();
        if (!fullRemaining.contains("args={")) {
            options.add("args={");
        }
        if (!fullRemaining.contains("context={")) {
            options.add("context={");
        }

        return filterPrefix(options, currentToken);
    }

    private List<String> completeInsideArgsBlock(NamespacedKey actionKey, String currentToken, String fullRemaining) {
        if (actionKey == null) return Collections.emptyList();

        List<ActionParameterMeta> metaList = ActionTriggerAPI.getActions().getMetadata(actionKey);
        Set<String> alreadyUsedKeys = extractUsedKeysFromBlock(fullRemaining, "args");

        String blockPrefix = "";
        if (currentToken.contains("{")) {
            blockPrefix = currentToken.substring(0, currentToken.lastIndexOf('{') + 1);
        }

        String insideToken = extractActiveTokenInsideBlock(currentToken);

        // Если пользователь вводит значение после знака =
        if (insideToken.contains("=")) {
            String[] kv = insideToken.split("=", 2);
            String paramKey = kv[0];
            String valPrefix = kv.length > 1 ? kv[1] : "";

            List<String> valueSuggestions = suggestValuesForActionParam(actionKey, paramKey, metaList);
            String prefixToKeep = blockPrefix + paramKey + "=";
            return valueSuggestions.stream()
                    .filter(v -> v.toLowerCase().startsWith(valPrefix.toLowerCase()))
                    .map(v -> prefixToKeep + v)
                    .toList();
        }

        // Иначе подсказываем оставшиеся параметры
        List<String> suggestions = new ArrayList<>();
        for (ActionParameterMeta meta : metaList) {
            if (!alreadyUsedKeys.contains(meta.key().toLowerCase())) {
                suggestions.add(blockPrefix + meta.key() + "=");
            }
        }

        return filterPrefix(suggestions, currentToken);
    }

    private List<String> completeTrigger(String[] args) {
        if (args.length == 2) {
            return filterPrefix(ActionTriggerAPI.getTriggers().asList(), args[1]);
        }

        String triggerId = args[1];
        NamespacedKey triggerKey = NamespacedKey.fromString(triggerId);

        String currentToken = args[args.length - 1];
        String fullRemaining = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

        if (isInBlock(fullRemaining, "context")) {
            return completeInsideContextBlock(triggerKey, currentToken, fullRemaining);
        }

        if (!fullRemaining.contains("context={")) {
            return filterPrefix(List.of("context={"), currentToken);
        }

        return Collections.emptyList();
    }

    private List<String> completeInsideContextBlock(NamespacedKey triggerKey, String currentToken, String fullRemaining) {
        String blockPrefix = "";
        if (currentToken.contains("{")) {
            blockPrefix = currentToken.substring(0, currentToken.lastIndexOf('{') + 1);
        }

        String insideToken = extractActiveTokenInsideBlock(currentToken);

        // Если пользователь пишет значение после знака = (например context={block= или context={world=)
        if (insideToken.contains("=")) {
            String[] kv = insideToken.split("=", 2);
            String ctxKey = kv[0];
            String valPrefix = kv.length > 1 ? kv[1] : "";

            List<String> valueSuggestions = suggestContextValues(ctxKey, valPrefix);
            String prefixToKeep = blockPrefix + ctxKey + "=";
            return valueSuggestions.stream()
                    .map(v -> prefixToKeep + v)
                    .toList();
        }

        // Иначе предлагаем доступные ключи контекста
        List<String> availableKeys = new ArrayList<>();
        if (triggerKey != null) {
            for (ContextKey<?> ck : ActionTriggerAPI.getTriggers().getProvidedContext(triggerKey)) {
                availableKeys.add(ck.getId() + "=");
            }
        }
        if (availableKeys.isEmpty()) {
            availableKeys.addAll(STANDARD_CONTEXT_KEYS);
        }

        Set<String> used = extractUsedKeysFromBlock(fullRemaining, "context");
        List<String> suggestions = new ArrayList<>();
        for (String k : availableKeys) {
            if (!used.contains(k.replace("=", "").toLowerCase())) {
                suggestions.add(blockPrefix + k);
            }
        }

        return filterPrefix(suggestions, currentToken);
    }

    private List<String> suggestContextValues(String key, String valPrefix) {
        String lower = key.toLowerCase();
        List<String> values = new ArrayList<>();

        if (lower.equals("player") || lower.equals("target") || lower.equals("killer") || lower.equals("damager")) {
            values.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
        } else if (lower.equals("block_id") || lower.equals("clicked_block_id")) {
            // Идентификаторы блоков с неймспейсом: minecraft:stone, oraxen:ruby_ore, itemsadder:ruby_block
            values.addAll(List.of(
                    "minecraft:stone", "minecraft:deepslate", "minecraft:dirt", "minecraft:oak_log",
                    "minecraft:diamond_ore", "minecraft:iron_ore", "minecraft:copper_ore", "minecraft:gold_ore",
                    "minecraft:chest", "minecraft:crafting_table", "minecraft:furnace", "minecraft:blue_ice",
                    "minecraft:packed_ice", "minecraft:ice", "minecraft:dispenser", "minecraft:beacon",
                    "minecraft:sponge", "minecraft:anvil", "minecraft:obsidian", "minecraft:amethyst_cluster",
                    "minecraft:bookshelf", "minecraft:spawner", "minecraft:cobblestone", "minecraft:tnt"
            ));
            try {
                for (var provider : ActionTriggerAPI.getBlocks().getProviders()) {
                    for (String b : provider.getAvailableIds()) {
                        values.add(provider.getNamespace() + ":" + b);
                    }
                }
            } catch (Throwable ignored) {}
        } else if (lower.equals("block_material") || lower.equals("material") || lower.equals("block") || lower.equals("block_type")) {
            // Имена материалов ваниллы в верхнем регистре (STONE, BLUE_ICE...)
            values.addAll(List.of(
                    "STONE", "DEEPSLATE", "DIRT", "OAK_LOG", "DIAMOND_ORE", "IRON_ORE", "COPPER_ORE", "GOLD_ORE",
                    "CHEST", "CRAFTING_TABLE", "FURNACE", "BLUE_ICE", "PACKED_ICE", "ICE",
                    "DISPENSER", "BEACON", "SPONGE", "ANVIL", "OBSIDIAN", "AMETHYST_CLUSTER",
                    "BOOKSHELF", "SPAWNER", "COBBLESTONE", "TNT"
            ));
        } else if (lower.equals("item_in_hand_id") || lower.equals("item_id") || lower.equals("main_hand_item_id") || lower.equals("off_hand_item_id")) {
            // Идентификаторы предметов с неймспейсом: minecraft:diamond_sword, oraxen:ruby_sword
            values.addAll(List.of(
                    "minecraft:diamond_sword", "minecraft:netherite_sword", "minecraft:iron_sword",
                    "minecraft:diamond_pickaxe", "minecraft:netherite_pickaxe", "minecraft:bow",
                    "minecraft:crossbow", "minecraft:shield", "minecraft:compass", "minecraft:recovery_compass",
                    "minecraft:water_bucket", "minecraft:lava_bucket", "minecraft:golden_apple",
                    "minecraft:enchanted_golden_apple", "minecraft:amethyst_shard", "minecraft:totem_of_undying",
                    "minecraft:firework_rocket", "minecraft:elytra", "minecraft:arrow", "minecraft:potion"
            ));
            try {
                for (var provider : ActionTriggerAPI.getItems().getProviders()) {
                    for (String itm : provider.getAvailableIds()) {
                        values.add(provider.getNamespace() + ":" + itm);
                    }
                }
            } catch (Throwable ignored) {}
        } else if (lower.equals("item") || lower.equals("item_in_hand")) {
            values.addAll(List.of(
                    "minecraft:compass", "minecraft:diamond_sword", "minecraft:netherite_sword",
                    "minecraft:bow", "minecraft:water_bucket", "minecraft:amethyst_shard",
                    "minecraft:golden_apple", "minecraft:shield", "minecraft:potion"
            ));
            try {
                for (var provider : ActionTriggerAPI.getItems().getProviders()) {
                    for (String itm : provider.getAvailableIds()) {
                        values.add(provider.getNamespace() + ":" + itm);
                    }
                }
            } catch (Throwable ignored) {}
        } else if (lower.equals("world") || lower.equals("world_name") || lower.equals("from_world") || lower.equals("to_world")) {
            values.addAll(Bukkit.getWorlds().stream().map(World::getName).toList());
        } else if (lower.equals("location") || lower.equals("loc")) {
            values.addAll(List.of("~,~,~", "0,64,0", "100,64,100", "0,100,0", "~,1,~", "world,0,64,0"));
        } else if (lower.equals("damage_cause") || lower.equals("cause")) {
            values.addAll(List.of(
                    "ENTITY_ATTACK", "PROJECTILE", "FALL", "FIRE", "FIRE_TICK", "LAVA",
                    "MAGIC", "WITHER", "VOID", "LIGHTNING", "CONTACT", "BLOCK_EXPLOSION",
                    "ENTITY_EXPLOSION", "DROWNING", "STARVATION", "POISON", "THORNS",
                    "DRAGON_BREATH", "FLY_INTO_WALL", "HOT_FLOOR", "FREEZE", "SONIC_BOOM"
            ));
        } else if (lower.equals("button")) {
            values.addAll(List.of("LEFT", "RIGHT"));
        } else if (lower.equals("action")) {
            values.addAll(List.of("RIGHT_CLICK_AIR", "RIGHT_CLICK_BLOCK", "LEFT_CLICK_AIR", "LEFT_CLICK_BLOCK"));
        } else if (lower.equals("advancement_key")) {
            values.addAll(List.of(
                    "minecraft:story/root", "minecraft:story/mine_stone", "minecraft:story/upgrade_tools",
                    "minecraft:adventure/kill_a_mob", "minecraft:nether/root", "minecraft:end/root"
            ));
        } else if (lower.equals("damage") || lower.equals("amount") || lower.equals("count") || lower.equals("level")) {
            values.addAll(List.of("1", "2", "5", "10", "20", "30", "50", "100"));
        } else if (lower.equals("is_flying") || lower.equals("is_sneak") || lower.equals("is_sneaking") || lower.equals("has_block") || lower.equals("cancelled")) {
            values.addAll(List.of("true", "false"));
        } else if (lower.equals("group")) {
            values.addAll(List.of("default", "vip", "premium", "veteran", "hero", "admin"));
        }

        String pLower = valPrefix.toLowerCase();
        return values.stream()
                .filter(v -> v.toLowerCase().startsWith(pLower))
                .toList();
    }

    private List<String> suggestValuesForActionParam(NamespacedKey actionKey, String paramKey, List<ActionParameterMeta> metadata) {
        String lower = paramKey.toLowerCase();
        String actionStr = (actionKey != null) ? actionKey.toString().toLowerCase() : "";

        // Специфичные подсказки по типу экшена
        if (actionStr.equals("core:message")) {
            if (lower.equals("type")) {
                return List.of("chat", "actionbar", "title");
            }
            if (lower.equals("text")) {
                return List.of("<green>Успешно!</green>", "<red>Ошибка!</red>", "<yellow>Внимание!</yellow>");
            }
        } else if (actionStr.equals("core:damage")) {
            if (lower.equals("type")) {
                return List.of("PHYSICAL", "MAGIC", "FIRE", "VOID", "FALL", "PROJECTILE", "LIGHTNING");
            }
            if (lower.equals("amount")) {
                return List.of("1", "2", "5", "10", "20");
            }
        } else if (actionStr.equals("core:firework")) {
            if (lower.equals("type")) {
                return List.of("BALL", "BALL_LARGE", "STAR", "BURST", "CREEPER");
            }
            if (lower.equals("power")) {
                return List.of("1", "2", "3");
            }
            if (lower.equals("colors")) {
                return List.of("[\"#74B9FF\", \"#FDCB6E\"]", "[\"#FF0000\"]");
            }
        } else if (actionStr.equals("core:potion_effect")) {
            if (lower.equals("effect")) {
                return List.of("SPEED", "SLOWNESS", "HASTE", "STRENGTH", "REGENERATION", "RESISTANCE",
                        "FIRE_RESISTANCE", "WATER_BREATHING", "INVISIBILITY", "BLINDNESS", "NIGHT_VISION",
                        "HUNGER", "WEAKNESS", "POISON", "WITHER", "ABSORPTION", "LEVITATION", "GLOWING", "DARKNESS");
            }
            if (lower.equals("duration")) {
                return List.of("5", "10", "30", "60", "120");
            }
            if (lower.equals("amplifier")) {
                return List.of("0", "1", "2", "3");
            }
        } else if (actionStr.equals("core:sound")) {
            if (lower.equals("sound")) {
                return List.of("entity.player.levelup", "entity.enderman.teleport", "block.note_block.chime",
                        "ui.toast.challenge_complete", "block.anvil.land", "entity.experience_orb.pickup",
                        "item.book.page_turn", "block.chest.open", "entity.villager.no", "entity.lightning_bolt.thunder");
            }
            if (lower.equals("volume")) {
                return List.of("1.0", "0.5", "2.0");
            }
            if (lower.equals("pitch")) {
                return List.of("1.0", "0.8", "1.2", "1.5");
            }
        } else if (actionStr.equals("core:particle")) {
            if (lower.equals("particle")) {
                return List.of("FLAME", "SOUL_FIRE_FLAME", "ELECTRIC_SPARK", "PORTAL", "HEART",
                        "ENCHANT", "HAPPY_VILLAGER", "CLOUD", "CRIT", "SMOKE", "EXPLOSION");
            }
            if (lower.equals("count")) {
                return List.of("10", "20", "50", "100");
            }
            if (lower.equals("speed")) {
                return List.of("0.05", "0.1", "0.2", "0.5");
            }
        } else if (actionStr.equals("core:open_gui")) {
            if (lower.equals("gui")) {
                return new ArrayList<>(guiRegistry.getAvailableIds());
            }
        } else if (actionStr.equals("core:command")) {
            if (lower.equals("as_console")) {
                return List.of("true", "false");
            }
        } else if (actionStr.equals("core:set_cooldown")) {
            if (lower.equals("duration")) {
                return List.of("5", "10", "15", "30", "60");
            }
        } else if (actionStr.equals("core:tag_combat")) {
            if (lower.equals("seconds")) {
                return List.of("10", "15", "20", "30");
            }
        } else if (actionStr.equals("core:set_group") || actionStr.equals("core:in_group")) {
            if (lower.equals("group")) {
                return List.of("default", "vip", "premium", "veteran", "hero", "admin");
            }
        }

        // Общие проверки типов по метаданным
        if (metadata != null) {
            for (ActionParameterMeta m : metadata) {
                if (m.key().equalsIgnoreCase(paramKey)) {
                    if (m.type() == boolean.class || m.type() == Boolean.class) {
                        return List.of("true", "false");
                    }
                }
            }
        }

        if (lower.equals("player") || lower.equals("target")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
        if (lower.equals("tier")) {
            return List.of("1", "2", "3");
        }
        if (lower.equals("cancel") || lower.equals("as_console") || lower.equals("if_absent") || lower.equals("ambient") || lower.equals("particles")) {
            return List.of("true", "false");
        }
        if (lower.equals("material") || lower.equals("item")) {
            return List.of("minecraft:diamond", "minecraft:compass", "minecraft:netherite_sword", "minecraft:water_bucket", "minecraft:amethyst_shard");
        }

        return Collections.emptyList();
    }

    private boolean isInBlock(String text, String blockName) {
        int idx = text.lastIndexOf(blockName + "={");
        if (idx == -1) return false;
        int closeIdx = text.indexOf('}', idx);
        return closeIdx == -1; // Блок открыт и еще не закрыт фигурной скобкой
    }

    private Set<String> extractUsedKeysFromBlock(String text, String blockName) {
        Set<String> keys = new HashSet<>();
        int idx = text.lastIndexOf(blockName + "={");
        if (idx == -1) return keys;

        String content = text.substring(idx + blockName.length() + 2);
        int close = content.indexOf('}');
        if (close != -1) {
            content = content.substring(0, close);
        }

        Matcher matcher = Pattern.compile("([a-zA-Z0-9_]+)\\s*[:=]").matcher(content);
        while (matcher.find()) {
            keys.add(matcher.group(1).toLowerCase());
        }
        return keys;
    }

    private String extractActiveTokenInsideBlock(String token) {
        if (token == null) return "";
        if (token.contains("{")) {
            token = token.substring(token.lastIndexOf('{') + 1);
        }
        int eqIdx = token.indexOf('=');
        if (eqIdx == -1) {
            if (token.contains(",")) {
                token = token.substring(token.lastIndexOf(',') + 1).trim();
            }
        }
        return token.trim();
    }

    private List<String> filterPrefix(Collection<String> items, String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return new ArrayList<>(items);
        }
        String pLower = prefix.toLowerCase();
        return items.stream()
                .filter(s -> s.toLowerCase().startsWith(pLower))
                .toList();
    }
}
