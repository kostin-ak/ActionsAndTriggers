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
import revxrsal.commands.annotation.AutoComplete;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Subcommand;
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

    // =================================================================================
    // ВЫПОЛНЕНИЕ ЭКШЕНОВ
    // =================================================================================

    @Subcommand("run")
    @AutoComplete("@actions @action_args") // Исправленная аннотация на уровне метода
    public void run(
            BukkitCommandActor actor,
            String actionId,
            @Optional String argsString
    ) {
        String fullInput = argsString == null ? "" : argsString;
        Map<String, String> argsRaw = parseBlock(fullInput, "args");
        ExecutionContext context = buildMergedContext(actor, fullInput);

        try {
            Map<String, Object> actionMap = new HashMap<>(argsRaw);
            actionMap.put("id", actionId);

            AATParser parser = new AATParser();
            Action action = parser.parseAction(actionMap);

            boolean result = action.execute(context);
            if (result) {
                actor.reply(mm.deserialize("<green>✔ Экшен успешно выполнен!</green>"));
            } else {
                actor.reply(mm.deserialize("<yellow>⚠ Экшен отработал, но вернул false.</yellow>"));
            }
        } catch (Exception e) {
            actor.reply(mm.deserialize("<red>✖ Ошибка: " + e.getMessage() + "</red>"));
        }
    }

    // =================================================================================
    // ИМИТАЦИЯ ТРИГГЕРОВ
    // =================================================================================

    @Subcommand("trigger")
    @AutoComplete("@triggers @trigger_args") // Исправленная аннотация на уровне метода
    public void trigger(
            BukkitCommandActor actor,
            String triggerId,
            @Optional String argsString
    ) {
        NamespacedKey key = NamespacedKey.fromString(triggerId);
        if (key == null) {
            actor.reply(mm.deserialize("<red>Неверный формат ключа триггера!</red>"));
            return;
        }

        String fullInput = argsString == null ? "" : argsString;
        ExecutionContext context = buildMergedContext(actor, fullInput);

        ActionTriggerAPI.getTriggers().dispatch(key, context);
        actor.reply(mm.deserialize("<green>✔ Триггер <white>" + triggerId + "</white> успешно запущен!</green>"));
    }

    // =================================================================================
    // БАЗОВЫЕ КОМАНДЫ И ИНТЕРФЕЙС (/AAT LIST)
    // =================================================================================

    @Subcommand("list")
    public void asList(BukkitCommandActor actor) {
        // --- 1. Собираем Экшены с красивым Hover (Метаданные) ---
        List<Component> actionComps = ActionTriggerAPI.getActions().asList().stream().sorted().map(id -> {
            NamespacedKey key = NamespacedKey.fromString(id);
            List<ActionParameterMeta> meta = ActionTriggerAPI.getActions().getMetadata(key);

            StringBuilder hover = new StringBuilder("<color:#FFaaaa><b>" + id + "</b></color><br>");
            if (meta.isEmpty()) hover.append("<gray>Нет параметров</gray>");
            for (ActionParameterMeta m : meta) {
                hover.append("<gray>▪</gray> <white>").append(m.getKey()).append("</white> <dark_gray>(").append(m.getType().getSimpleName()).append(")</dark_gray>");
                if (m.isRequired()) hover.append(" <red>*</red>");
                if (!m.getDescription().isEmpty()) hover.append("<br>  <gray><i>").append(m.getDescription()).append("</i></gray>");
                hover.append("<br>");
            }
            return mm.deserialize("<hover:show_text:'" + hover.toString() + "'><color:#FFaaaa>" + id + "</color></hover>");
        }).collect(Collectors.toList());

        // --- 2. Собираем Триггеры с красивым Hover (Контекст) ---
        List<Component> triggerComps = ActionTriggerAPI.getTriggers().asList().stream().sorted().map(id -> {
            NamespacedKey key = NamespacedKey.fromString(id);
            List<ContextKey<?>> ctxList = ActionTriggerAPI.getTriggers().getProvidedContext(key);

            StringBuilder hover = new StringBuilder("<color:#aaFFaa><b>" + id + "</b></color><br>");
            hover.append("<gray>Предоставляет контекст:</gray><br>");
            if (ctxList.isEmpty()) hover.append("<gray><i>(пусто)</i></gray>");
            for (ContextKey<?> c : ctxList) {
                hover.append("<gray>▪</gray> <white>").append(c.getId()).append("</white> <dark_gray>(").append(c.getType().getSimpleName()).append(")</dark_gray><br>");
            }
            return mm.deserialize("<hover:show_text:'" + hover.toString() + "'><color:#aaFFaa>" + id + "</color></hover>");
        }).collect(Collectors.toList());

        // --- 3. Фильтры и Скрипты (оставляем просто строками) ---
        List<Component> filterComps = ActionTriggerAPI.getFilters().asList().stream().sorted()
                .map(id -> mm.deserialize("<color:#ffffaa>" + id + "</color>")).collect(Collectors.toList());
        List<Component> scriptComps = ActionTriggerAPI.getScripts().getLoadedScripts().stream().sorted()
                .map(id -> mm.deserialize("<color:#aaffff>" + id + "</color>")).collect(Collectors.toList());

        // --- 4. Рендер ---
        Component header = mm.deserialize("\n<gradient:#FF5555:#FFAA00><strikethrough>--------</strikethrough> [ Actions & Triggers ] <strikethrough>--------</strikethrough></gradient>\n");
        Component footer = mm.deserialize("\n<dark_gray><strikethrough>                                        </strikethrough></dark_gray>");

        Component finalMessage = header
                .append(buildSection("Экшены", actionComps, "#FF5555")).append(mm.deserialize("<br><br>"))
                .append(buildSection("Триггеры", triggerComps, "#55FF55")).append(mm.deserialize("<br><br>"))
                .append(buildSection("Условия", filterComps, "#FFFF55")).append(mm.deserialize("<br><br>"))
                .append(buildSection("Скрипты", scriptComps, "#55FFFF"))
                .append(footer);

        Bukkit.getConsoleSender().sendMessage(finalMessage);
        if (actor.isPlayer()) actor.requirePlayer().sendMessage(finalMessage);
    }

    private Component buildSection(String title, List<Component> items, String titleColor) {
        Component titleComp = mm.deserialize("<color:" + titleColor + "><b>" + title + ":</b></color>\n");
        if (items == null || items.isEmpty()) return titleComp.append(mm.deserialize("<gray><i>Пусто</i></gray>"));
        return titleComp.append(Component.join(JoinConfiguration.separator(mm.deserialize(" <dark_gray><b>|</b></dark_gray> ")), items));
    }

    @Subcommand("debug")
    public void toggleDebug(BukkitCommandActor actor) {
        if (debugListener != null) {
            ActionTriggerAPI.getTriggers().unsubscribeGlobal(debugListener);
            debugListener = null;
            actor.reply(mm.deserialize("<red>[A&T] Дебаг ВЫКЛЮЧЕН.</red>"));
        } else {
            debugListener = (triggerKey, context) -> {
                String msg = String.format("\n<yellow>[A&T DEBUG] Триггер: %s</yellow>\n<gray>Контекст: %s</gray>", triggerKey, context.dump());
                Bukkit.getConsoleSender().sendMessage(mm.deserialize(msg));
                if (actor.isPlayer()) actor.requirePlayer().sendMessage(mm.deserialize(msg));
            };
            ActionTriggerAPI.getTriggers().subscribeGlobal(debugListener);
            actor.reply(mm.deserialize("<green>[A&T] Дебаг ВКЛЮЧЕН.</green>"));
        }
    }

    @Subcommand("reload")
    public void reload(BukkitCommandActor actor) {
        ActionTriggerAPI.getScripts().clear();
        YamlTriggerLoader.load(plugin, "triggers");
        actor.reply(mm.deserialize("<green><b>✔ Скрипты перезагружены!</b></green>"));
    }

    @DefaultFor({"actionapi", "aat"})
    @Subcommand("help")
    public void help(BukkitCommandActor actor) {
        actor.reply(mm.deserialize("<blue><b>A&T Помощь:</b></blue><br>" +
                "<gray>/aat run <action> context={k=v} args={k=v}</gray><br>" +
                "<gray>/aat trigger <trigger> context={k=v}</gray><br>" +
                "<gray>/aat list | debug | reload</gray>"));
    }

    // =================================================================================
    // ПАРСЕРЫ
    // =================================================================================

    private ExecutionContext buildMergedContext(BukkitCommandActor actor, String fullInput) {
        ExecutionContext context = new ExecutionContext();
        if (actor.isPlayer()) {
            Player player = actor.requirePlayer();
            context.set(CoreKeys.PLAYER, player);
            context.set(CoreKeys.LOCATION, player.getLocation());
            context.set(ContextKey.of("item_in_hand_id", String.class), ActionTriggerAPI.getItems().getFullId(player.getInventory().getItemInMainHand()));
        }

        Map<String, String> customContext = parseBlock(fullInput, "context");
        customContext.forEach((k, v) -> {
            if (k.equalsIgnoreCase("player")) {
                Player target = Bukkit.getPlayer(v);
                if (target != null) {
                    context.set(CoreKeys.PLAYER, target);
                    context.set(CoreKeys.LOCATION, target.getLocation());
                }
            } else {
                context.set(ContextKey.of(k, String.class), v);
            }
        });
        return context;
    }

    private Map<String, String> parseBlock(String input, String blockName) {
        Map<String, String> result = new HashMap<>();
        Pattern pattern = Pattern.compile(blockName + "=\\{(.*?)\\}");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            String[] pairs = matcher.group(1).split(",\\s*");
            for (String pair : pairs) {
                String[] kv = pair.split("[:=]", 2);
                if (kv.length == 2) result.put(kv[0].trim(), kv[1].trim().replace("\"", "").replace("'", ""));
            }
        }
        return result;
    }
}