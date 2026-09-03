package kostin.ak.actionstriggers.core.command;

import kostin.ak.actionstriggers.api.ActionTriggerAPI;
import kostin.ak.actionstriggers.api.action.Action;
import kostin.ak.actionstriggers.api.context.ContextKey;
import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.api.meta.ActionParameterMeta;
import kostin.ak.actionstriggers.api.parser.AATParser;
import kostin.ak.actionstriggers.core.CoreKeys;
import kostin.ak.actionstriggers.core.config.YamlTriggerLoader;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import revxrsal.commands.annotation.*;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Command({"actionapi", "aat"})
@CommandPermission("actionstriggers.admin")
public class ActionCommand {

    private final Plugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private BiConsumer<NamespacedKey, ExecutionContext> debugListener = null;

    public ActionCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Subcommand("run")
    @AutoComplete("@actions @action_args")
    public void run(BukkitCommandActor actor, String actionId, @Optional String argsString) {
        String fullInput = argsString == null ? "" : argsString;
        Map<String, String> argsRaw = parseBlock(fullInput, "args");
        ExecutionContext context = buildMergedContext(actor, fullInput);

        try {
            Map<String, Object> actionMap = new HashMap<>(argsRaw);
            actionMap.put("id", actionId);
            AATParser parser = new AATParser();
            Action action = parser.parseAction(actionMap);

            if (action.execute(context)) {
                actor.reply(mm.deserialize("<green>✔ Экшен выполнен!</green>"));
            } else {
                actor.reply(mm.deserialize("<yellow>⚠ Экшен вернул false.</yellow>"));
            }
        } catch (Exception e) {
            actor.reply(mm.deserialize("<red>✖ Ошибка: " + e.getMessage() + "</red>"));
        }
    }

    @Subcommand("trigger")
    @AutoComplete("@triggers @trigger_args") // ПРАВИЛЬНО
    public void trigger(BukkitCommandActor actor, String triggerId, @Optional String argsString) {
        NamespacedKey key = NamespacedKey.fromString(triggerId);
        if (key == null) return;

        String fullInput = argsString == null ? "" : argsString;
        ExecutionContext context = buildMergedContext(actor, fullInput);
        ActionTriggerAPI.getTriggers().dispatch(key, context);
        actor.reply(mm.deserialize("<green>✔ Триггер запущен!</green>"));
    }

    @Subcommand("debug")
    public void toggleDebug(BukkitCommandActor actor) {
        if (debugListener != null) {
            ActionTriggerAPI.getTriggers().unsubscribeGlobal(debugListener);
            debugListener = null;
            actor.reply(mm.deserialize("<red>[A&T] Дебаг ВЫКЛЮЧЕН.</red>"));
        } else {
            debugListener = (key, ctx) -> {
                // Игнорируем ежесекундный фоновый тикер ядра, чтобы не спамить в чат и консоль
                if (key.equals(kostin.ak.actionstriggers.core.defaults.triggers.PeriodicIntervalTrigger.KEY)) {
                    return;
                }
                String msg = "\n<yellow>[A&T DEBUG] " + key + "</yellow>\n<gray>" + ctx.dump() + "</gray>";
                Bukkit.getConsoleSender().sendMessage(mm.deserialize(msg));
                if (actor.isPlayer()) actor.requirePlayer().sendMessage(mm.deserialize(msg));
            };
            ActionTriggerAPI.getTriggers().subscribeGlobal(debugListener);
            actor.reply(mm.deserialize("<green>[A&T] Дебаг ВКЛЮЧЕН (периодический core:interval скрыт от спама).</green>"));
        }
    }

    @Subcommand("list")
    public void asList(BukkitCommandActor actor) {
        Component header = mm.deserialize("\n<gradient:#FF5555:#FFAA00><strikethrough>-------</strikethrough> [ A&T Registry ] <strikethrough>-------</strikethrough></gradient>\n");

        // Секция Экшенов с Hover-описанием параметров
        List<Component> actionComps = ActionTriggerAPI.getActions().asList().stream().sorted().map(id -> {
            NamespacedKey key = NamespacedKey.fromString(id);
            List<ActionParameterMeta> meta = ActionTriggerAPI.getActions().getMetadata(key);
            String hover = "<color:#FFaaaa><b>" + id + "</b></color><br>" +
                    meta.stream().map(m -> "<gray>▪</gray> <white>" + m.key() + "</white> <dark_gray>(" + m.type().getSimpleName() + ")</dark_gray>" + (m.required() ? " <red>*</red>" : "") + (m.description().isEmpty() ? "" : "<br>  <gray><i>" + m.description() + "</i></gray>")).collect(Collectors.joining("<br>"));
            return mm.deserialize("<hover:show_text:'" + hover + "'><color:#FFaaaa>" + id + "</color></hover>");
        }).collect(Collectors.toList());

        // Секция Триггеров с Hover-контекстом
        List<Component> triggerComps = ActionTriggerAPI.getTriggers().asList().stream().sorted().map(id -> {
            NamespacedKey key = NamespacedKey.fromString(id);
            List<ContextKey<?>> ctx = ActionTriggerAPI.getTriggers().getProvidedContext(key);
            String hover = "<color:#aaFFaa><b>" + id + "</b></color><br><gray>Контекст:</gray><br>" +
                    ctx.stream().map(c -> "<gray>▪</gray> <white>" + c.getId() + "</white> <dark_gray>(" + c.getType().getSimpleName() + ")</dark_gray>").collect(Collectors.joining("<br>"));
            return mm.deserialize("<hover:show_text:'" + hover + "'><color:#aaFFaa>" + id + "</color></hover>");
        }).collect(Collectors.toList());

        // Секция GUI интерфейсов с кликабельным открытием
        List<Component> guiComps = kostin.ak.actionstriggers.ActionsTriggers.getGuiRegistry().getAvailableIds().stream().sorted().map(guiId -> {
            var def = kostin.ak.actionstriggers.ActionsTriggers.getGuiRegistry().get(guiId);
            String title = def != null ? def.getTitle() : guiId;
            String hover = "<color:#70E1F5><b>" + guiId + "</b></color><br>" +
                    "<gray>Заголовок: </gray>" + title + "<br>" +
                    "<gray>Рядов: </gray><white>" + (def != null ? def.getRows() : 0) + "</white><br><br>" +
                    "<yellow>➤ Нажмите, чтобы открыть этот GUI</yellow>";
            return mm.deserialize("<hover:show_text:'" + hover + "'><click:run_command:'/aat open " + guiId + "'><color:#70E1F5><u>" + guiId + "</u></color></click></hover>");
        }).collect(Collectors.toList());

        Component finalMessage = header
                .append(buildSection("Экшены", actionComps, "#FF5555")).append(mm.deserialize("<br><br>"))
                .append(buildSection("Триггеры", triggerComps, "#55FF55")).append(mm.deserialize("<br><br>"))
                .append(buildSection("Интерфейсы (GUI)", guiComps, "#70E1F5")).append(mm.deserialize("<br><br>"))
                .append(mm.deserialize("<color:#FFFF55><b>Условия:</b></color> " + String.join(", ", ActionTriggerAPI.getFilters().asList())));

        actor.reply(finalMessage);
    }

    @Subcommand("guis")
    public void listGuis(BukkitCommandActor actor) {
        var guis = kostin.ak.actionstriggers.ActionsTriggers.getGuiRegistry().getGuis();
        if (guis.isEmpty()) {
            actor.reply(mm.deserialize("<yellow>Нет зарегистрированных GUI интерфейсов в guis/*.yml.</yellow>"));
            return;
        }

        Component msg = mm.deserialize("\n<gradient:#70E1F5:#FFD194><b>[ Доступные Графические Интерфейсы (GUI) ]</b></gradient>\n<gray>Нажмите на название интерфейса для его открытия:</gray>\n\n");

        for (var entry : guis.entrySet()) {
            String id = entry.getKey();
            var def = entry.getValue();
            Component line = mm.deserialize(" <dark_gray>▪</dark_gray> <hover:show_text:'<green>Нажмите для открытия GUI</green><br><gray>ID: " + id + "<br>Рядов: " + def.getRows() + "'><click:run_command:'/aat open " + id + "'><color:#70E1F5><u><b>" + id + "</b></u></color></click></hover> <dark_gray>—</dark_gray> " + def.getTitle() + " <dark_gray>(" + def.getRows() + " рядов)</dark_gray>\n");
            msg = msg.append(line);
        }

        actor.reply(msg);
    }

    @Subcommand("get")
    @AutoComplete("@get @actions")
    public void get(BukkitCommandActor actor, String type, String actionId) {
        if (type.equals("params")) {
            NamespacedKey key = NamespacedKey.fromString(actionId);
            List<ActionParameterMeta> meta = ActionTriggerAPI.getActions().getMetadata(key);
            String hover = "<color:#FFaaaa><b>" + actionId + "</b></color><br>" +
                    meta.stream().map(m -> "<gray>▪</gray> <white>" + m.key() + "</white> <dark_gray>(" + m.type().getSimpleName() + ")</dark_gray>" + (m.required() ? " <red>*</red>" : "") + (m.description().isEmpty() ? "" : "<br>  <gray><i>" + m.description() + "</i></gray>")).collect(Collectors.joining("<br>"));
            actor.reply(mm.deserialize(hover));
        }
    }

    private Component buildSection(String title, List<Component> items, String color) {
        return mm.deserialize("<color:" + color + "><b>" + title + ":</b></color> ")
                .append(Component.join(JoinConfiguration.separator(mm.deserialize(" <dark_gray>|</dark_gray> ")), items));
    }

    @Subcommand("reload")
    public void reload(BukkitCommandActor actor) {
        long start = System.currentTimeMillis();
        plugin.reloadConfig();
        kostin.ak.actionstriggers.core.i18n.I18n.reload();
        ActionTriggerAPI.getScripts().clear();
        int triggers = YamlTriggerLoader.load(plugin, "triggers");
        kostin.ak.actionstriggers.ActionsTriggers.getGuiRegistry().clear();
        kostin.ak.actionstriggers.core.gui.YamlGuiLoader guiLoader = new kostin.ak.actionstriggers.core.gui.YamlGuiLoader(kostin.ak.actionstriggers.ActionsTriggers.getGuiRegistry(), plugin.getLogger());
        int guis = guiLoader.loadAll(plugin, "guis");
        long elapsed = System.currentTimeMillis() - start;

        actor.reply(kostin.ak.actionstriggers.core.i18n.I18n.prefixed("commands.reload.success", Map.of(
                "time", elapsed,
                "triggers", triggers,
                "guis", guis
        )));
    }

    private static final Pattern PAIR_PATTERN = Pattern.compile("([a-zA-Z0-9_]+)\\s*[:=]\\s*(?:\"([^\"]*)\"|'([^']*)'|((?:[^, \\t\\r\\n=]|,(?!\\s*[a-zA-Z0-9_]+\\s*[:=]))+))");

    private ExecutionContext buildMergedContext(BukkitCommandActor actor, String input) {
        ExecutionContext ctx = new ExecutionContext();
        org.bukkit.Location actorLoc = null;
        if (actor.isPlayer()) {
            Player p = actor.requirePlayer();
            ctx.set(CoreKeys.PLAYER, p);
            actorLoc = p.getLocation();
            ctx.set(CoreKeys.LOCATION, actorLoc);
        }
        final org.bukkit.Location baseLoc = actorLoc;

        Map<String, String> parsed = parseBlock(input, "context");

        // 1. Сначала ищем мир и location, чтобы они были согласованы
        org.bukkit.World contextWorld = null;
        if (parsed.containsKey("world")) {
            contextWorld = Bukkit.getWorld(parsed.get("world"));
        } else if (parsed.containsKey("world_name")) {
            contextWorld = Bukkit.getWorld(parsed.get("world_name"));
        }

        org.bukkit.Location customLoc = null;
        if (parsed.containsKey("location") || parsed.containsKey("loc")) {
            String locStr = parsed.getOrDefault("location", parsed.get("loc"));
            customLoc = parseLocationString(locStr, baseLoc, contextWorld);
            if (customLoc != null) {
                ctx.set(CoreKeys.LOCATION, customLoc);
            }
        }
        final org.bukkit.Location effectiveLoc = customLoc != null ? customLoc : baseLoc;
        final org.bukkit.Location fixedCustomLoc = customLoc;

        // 2. Обрабатываем остальные параметры
        parsed.forEach((k, v) -> {
            String lower = k.toLowerCase();
            if (lower.equals("player") || lower.equals("target")) {
                Player t = Bukkit.getPlayer(v);
                if (t != null) {
                    ctx.set(CoreKeys.PLAYER, t);
                    if (fixedCustomLoc == null) {
                        ctx.set(CoreKeys.LOCATION, t.getLocation());
                    }
                } else {
                    ctx.set(ContextKey.of(k, String.class), v);
                }
            } else if (lower.equals("killer")) {
                Player t = Bukkit.getPlayer(v);
                if (t != null) ctx.set(CoreKeys.KILLER, t);
            } else if (lower.equals("damager")) {
                Player t = Bukkit.getPlayer(v);
                if (t != null) ctx.set(CoreKeys.DAMAGER, t);
            } else if (lower.equals("block_material") || lower.equals("material") || lower.equals("block")) {
                org.bukkit.Material mat = org.bukkit.Material.matchMaterial(v.replace("minecraft:", ""));
                if (mat != null) {
                    ctx.set(CoreKeys.BLOCK_MATERIAL, mat);
                }
                if (effectiveLoc != null && effectiveLoc.getWorld() != null) {
                    try {
                        ctx.set(CoreKeys.BLOCK, effectiveLoc.getBlock());
                    } catch (Throwable ignored) {}
                }
                ctx.set(ContextKey.of("block", String.class), v);
            } else if (lower.equals("location") || lower.equals("loc")) {
                // Уже обработано выше
            } else if (lower.equals("damage")) {
                try { ctx.set(CoreKeys.DAMAGE, Double.parseDouble(v)); } catch (Exception ignored) {}
            } else if (lower.equals("level")) {
                try { ctx.set(CoreKeys.LEVEL, Integer.parseInt(v)); } catch (Exception ignored) {}
            } else if (lower.equals("is_flying")) {
                ctx.set(CoreKeys.IS_FLYING, Boolean.parseBoolean(v));
            } else if (lower.equals("has_block")) {
                ctx.set(CoreKeys.HAS_BLOCK, Boolean.parseBoolean(v));
            } else if (lower.equals("button")) {
                try { ctx.set(CoreKeys.BUTTON_TYPE, CoreKeys.ButtonType.valueOf(v.toUpperCase())); } catch (Exception ignored) {}
            } else if (lower.equals("block_id")) {
                ctx.set(CoreKeys.BLOCK_ID, v);
            } else if (lower.equals("item_in_hand_id")) {
                ctx.set(CoreKeys.ITEM_IN_HAND_ID, v);
            } else if (lower.equals("item_id")) {
                ctx.set(CoreKeys.ITEM_ID, v);
            } else if (lower.equals("clicked_block_id")) {
                ctx.set(CoreKeys.CLICKED_BLOCK_ID, v);
            } else if (lower.equals("world") || lower.equals("world_name")) {
                ctx.set(CoreKeys.WORLD, v);
                ctx.set(CoreKeys.WORLD_NAME, v);
            } else {
                ctx.set(ContextKey.of(k, String.class), v);
            }
        });
        return ctx;
    }

    private org.bukkit.Location parseLocationString(String input, org.bukkit.Location base, org.bukkit.World contextWorld) {
        if (input == null || input.isBlank()) return null;
        String clean = input.replace("\"", "").replace("'", "");
        String[] parts = clean.contains(",") ? clean.split(",") : clean.split("\\s+");
        if (parts.length >= 3) {
            org.bukkit.World world = contextWorld != null ? contextWorld
                    : (base != null ? base.getWorld() : (Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0)));
            int startIdx = 0;
            if (parts.length >= 4) {
                org.bukkit.World w = Bukkit.getWorld(parts[0]);
                if (w != null) {
                    world = w;
                    startIdx = 1;
                }
            }
            try {
                double x = parts[startIdx].equals("~") ? (base != null ? base.getX() : 0) : Double.parseDouble(parts[startIdx]);
                double y = parts[startIdx + 1].equals("~") ? (base != null ? base.getY() : 64) : Double.parseDouble(parts[startIdx + 1]);
                double z = parts[startIdx + 2].equals("~") ? (base != null ? base.getZ() : 0) : Double.parseDouble(parts[startIdx + 2]);
                return new org.bukkit.Location(world, x, y, z);
            } catch (Exception ignored) {}
        }
        return null;
    }

    private Map<String, String> parseBlock(String input, String name) {
        Map<String, String> res = new LinkedHashMap<>();
        Matcher m = Pattern.compile(name + "=\\{(.*?)\\}").matcher(input);
        if (m.find()) {
            String content = m.group(1).trim();
            Matcher pairMatcher = PAIR_PATTERN.matcher(content);
            while (pairMatcher.find()) {
                String key = pairMatcher.group(1);
                String val = pairMatcher.group(2) != null ? pairMatcher.group(2)
                        : (pairMatcher.group(3) != null ? pairMatcher.group(3) : pairMatcher.group(4));
                if (val != null) {
                    res.put(key.trim(), val.trim());
                }
            }
        }
        return res;
    }

    @Subcommand("providers")
    @CommandPermission("actionstriggers.admin")
    public void listProviders(BukkitCommandActor actor) {
        // Мрачный, атмосферный градиент для заголовка
        Component msg = mm.deserialize("\n<gradient:#5C1616:#A1290B><strikethrough>--------</strikethrough> [ Providers Registry ] <strikethrough>--------</strikethrough></gradient>\n");

        msg = msg.append(mm.deserialize("<color:#B02E0C><b>:</b></color>\n"));
        for (kostin.ak.actionstriggers.api.provider.ItemProvider provider : ActionTriggerAPI.getItems().getProviders()) {
            int count = provider.getAvailableIds().size();
            msg = msg.append(mm.deserialize(" <dark_gray>-</dark_gray> <gold>" + provider.getNamespace() + "</gold> " + (count > 0 ? "<gray>(" + count + " )</gray>" : "") + "\n"));
        }

        msg = msg.append(mm.deserialize("\n<color:#B02E0C><b>:</b></color>\n"));
        for (kostin.ak.actionstriggers.api.provider.BlockProvider provider : ActionTriggerAPI.getBlocks().getProviders()) {
            int count = provider.getAvailableIds().size();
            msg = msg.append(mm.deserialize(" <dark_gray>-</dark_gray> <gold>" + provider.getNamespace() + "</gold> " + (count > 0 ? "<gray>(" + count + " )</gray>" : "") + "\n"));
        }

        actor.reply(msg);
        actor.reply(mm.deserialize("<dark_gray>     : <gold>/aat provider_items <namespace></gold></dark_gray>"));
    }

    @Subcommand("provider_items")
    @CommandPermission("actionstriggers.admin")
    public void listProviderItems(BukkitCommandActor actor, String namespace) {
        kostin.ak.actionstriggers.api.provider.ItemProvider provider = ActionTriggerAPI.getItems().getProvider(namespace);

        if (provider == null) {
            actor.reply(mm.deserialize("<dark_red>  '" + namespace + "'  .</dark_red>"));
            return;
        }

        List<String> ids = provider.getAvailableIds();
        if (ids.isEmpty()) {
            actor.reply(mm.deserialize("<gray>  " + namespace + "        API.</gray>"));
            return;
        }

        String joined = String.join(", ", ids);

        // Защита от переполнения чата (1000 символов — безопасный предел)
        if (joined.length() > 1000) {
            joined = joined.substring(0, 1000) + "... <dark_gray>(   " + ids.size() + " )</dark_gray>";
        }

        actor.reply(mm.deserialize("<color:#B02E0C><b>  " + namespace + ":</b></color>\n<gray>" + joined + "</gray>"));
    }

    @Subcommand("hand")
    @CommandPermission("actionstriggers.admin")
    public void identifyHand(BukkitCommandActor actor) {
        if (!actor.isPlayer()) {
            actor.reply(mm.deserialize("<red>Эта команда доступна только игрокам.</red>"));
            return;
        }

        Player player = actor.requirePlayer();

        // Получаем предмет в руке
        org.bukkit.inventory.ItemStack item = player.getInventory().getItemInMainHand();
        String itemId = ActionTriggerAPI.getItems().getFullId(item);

        // Получаем блок, на который смотрит игрок (дистанция 5 блоков)
        org.bukkit.block.Block block = player.getTargetBlockExact(5);
        String blockId = block != null ? ActionTriggerAPI.getBlocks().getFullId(block) : "minecraft:air";

        // Формируем интерактивное сообщение
        Component msg = mm.deserialize("\n<gradient:#5C1616:#A1290B><strikethrough>--------</strikethrough> [ Inspector ] <strikethrough>--------</strikethrough></gradient>\n")
                .append(mm.deserialize("<color:#B02E0C><b>В руке:</b></color> <gray>" + itemId + "</gray> " +
                        "<hover:show_text:'<green>Нажми, чтобы скопировать</green>'><click:copy_to_clipboard:'" + itemId + "'><dark_gray>[ Скопировать ]</dark_gray></click></hover>\n"))
                .append(mm.deserialize("<color:#B02E0C><b>Взгляд на блок:</b></color> <gray>" + blockId + "</gray> " +
                        "<hover:show_text:'<green>Нажми, чтобы скопировать</green>'><click:copy_to_clipboard:'" + blockId + "'><dark_gray>[ Скопировать ]</dark_gray></click></hover>\n"));

        actor.reply(msg);
    }

    @Subcommand("search")
    @CommandPermission("actionstriggers.admin")
    public void searchAll(BukkitCommandActor actor, String query, @Optional SearchCategory category) {
        String lowerQuery = query.toLowerCase();

        List<Component> actions = new ArrayList<>();
        List<Component> triggers = new ArrayList<>();
        List<Component> filters = new ArrayList<>();
        List<Component> items = new ArrayList<>();
        List<Component> blocks = new ArrayList<>();

        // 1. Поиск по Действиям
        if (category == null || category == SearchCategory.ACTIONS) {
            for (String id : ActionTriggerAPI.getActions().asList()) {
                if (id.toLowerCase().contains(lowerQuery)) {
                    actions.add(createCopyableComponent(id, "#FF5555")); // Передаем только цвет
                }
            }
        }

        // 2. Поиск по Триггерам
        if (category == null || category == SearchCategory.TRIGGERS) {
            for (String id : ActionTriggerAPI.getTriggers().asList()) {
                if (id.toLowerCase().contains(lowerQuery)) {
                    triggers.add(createCopyableComponent(id, "#55FF55"));
                }
            }
        }

        // 3. Поиск по Условиям
        if (category == null || category == SearchCategory.FILTERS) {
            for (String id : ActionTriggerAPI.getFilters().asList()) {
                if (id.toLowerCase().contains(lowerQuery)) {
                    filters.add(createCopyableComponent(id, "#FFFF55"));
                }
            }
        }

        // 4. Поиск по Предметам
        if (category == null || category == SearchCategory.ITEMS) {
            for (kostin.ak.actionstriggers.api.provider.ItemProvider provider : ActionTriggerAPI.getItems().getProviders()) {
                for (String id : provider.getAvailableIds()) {
                    if (id.toLowerCase().contains(lowerQuery)) {
                        items.add(createCopyableComponent(provider.getNamespace() + ":" + id, "gray"));
                    }
                }
            }
        }

        // 5. Поиск по Блокам
        if (category == null || category == SearchCategory.BLOCKS) {
            for (kostin.ak.actionstriggers.api.provider.BlockProvider provider : ActionTriggerAPI.getBlocks().getProviders()) {
                for (String id : provider.getAvailableIds()) {
                    if (id.toLowerCase().contains(lowerQuery)) {
                        Component comp = createCopyableComponent(provider.getNamespace() + ":" + id, "gray");
                        if (!blocks.contains(comp)) blocks.add(comp);
                    }
                }
            }
        }

        // Проверка: если ничего не найдено
        if (actions.isEmpty() && triggers.isEmpty() && filters.isEmpty() && items.isEmpty() && blocks.isEmpty()) {
            actor.reply(mm.deserialize("<dark_red>Ничего не найдено по запросу: '" + query + "'.</dark_red>"));
            return;
        }

        // Собираем итоговое сообщение по блокам
        Component msg = mm.deserialize("\n<gradient:#5C1616:#A1290B><strikethrough>--------</strikethrough> [ Search Results ] <strikethrough>--------</strikethrough></gradient>\n");

        msg = appendCategory(msg, "Действия", actions);
        msg = appendCategory(msg, "Триггеры", triggers);
        msg = appendCategory(msg, "Условия", filters);
        msg = appendCategory(msg, "Предметы", items);
        msg = appendCategory(msg, "Блоки", blocks);

        actor.reply(msg);
    }

    // --- Исправленный хелпер ---
    private Component createCopyableComponent(String text, String color) {
        // Теперь мы используем строгий тег <color:наш_цвет>, который корректно закроется через </color>
        return mm.deserialize("<hover:show_text:'<green>Нажми, чтобы скопировать</green>'><click:copy_to_clipboard:'" + text + "'><color:" + color + ">" + text + "</color></click></hover>");
    }

    private Component appendCategory(Component base, String title, List<Component> list) {
        if (list.isEmpty()) return base;

        int limit = Math.min(list.size(), 50);

        Component section = mm.deserialize("<color:#B02E0C><b>" + title + ":</b></color> ")
                .append(Component.join(JoinConfiguration.separator(mm.deserialize(" <dark_gray>|</dark_gray> ")), list.subList(0, limit)));

        if (list.size() > 50) {
            section = section.append(mm.deserialize(" <dark_gray><i>...и еще " + (list.size() - limit) + "</i></dark_gray>"));
        }

        return base.append(section).append(mm.deserialize("\n"));
    }
    @Subcommand("open")
    @AutoComplete("@guis")
    public void openGui(BukkitCommandActor actor, String guiId, @Optional Player target) {
        Player player = target != null ? target : (actor.isPlayer() ? actor.getAsPlayer() : null);
        if (player == null) {
            actor.reply(mm.deserialize("<red>Укажите целевого игрока!</red>"));
            return;
        }
        boolean ok = kostin.ak.actionstriggers.ActionsTriggers.getGuiRegistry().openGui(player, guiId);
        if (ok) {
            actor.reply(mm.deserialize("<green>✔ Открыт интерфейс " + guiId + " для " + player.getName() + "!</green>"));
        } else {
            actor.reply(mm.deserialize("<red>✖ Интерфейс '" + guiId + "' не найден в guis/*.yml!</red>"));
        }
    }

    public enum SearchCategory {
        ACTIONS, TRIGGERS, FILTERS, ITEMS, BLOCKS
    }
}