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
                String msg = "\n<yellow>[A&T DEBUG] " + key + "</yellow>\n<gray>" + ctx.dump() + "</gray>";
                Bukkit.getConsoleSender().sendMessage(mm.deserialize(msg));
                if (actor.isPlayer()) actor.requirePlayer().sendMessage(mm.deserialize(msg));
            };
            ActionTriggerAPI.getTriggers().subscribeGlobal(debugListener);
            actor.reply(mm.deserialize("<green>[A&T] Дебаг ВКЛЮЧЕН.</green>"));
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

        Component finalMessage = header
                .append(buildSection("Экшены", actionComps, "#FF5555")).append(mm.deserialize("<br><br>"))
                .append(buildSection("Триггеры", triggerComps, "#55FF55")).append(mm.deserialize("<br><br>"))
                .append(mm.deserialize("<color:#FFFF55><b>Условия:</b></color> " + String.join(", ", ActionTriggerAPI.getFilters().asList())));

        actor.reply(finalMessage);
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
        ActionTriggerAPI.getScripts().clear();
        YamlTriggerLoader.load(plugin, "triggers");
        actor.reply(mm.deserialize("<green>✔ Скрипты перезагружены!</green>"));
    }

    private ExecutionContext buildMergedContext(BukkitCommandActor actor, String input) {
        ExecutionContext ctx = new ExecutionContext();
        if (actor.isPlayer()) {
            Player p = actor.requirePlayer();
            ctx.set(CoreKeys.PLAYER, p);
            ctx.set(CoreKeys.LOCATION, p.getLocation());
        }
        parseBlock(input, "context").forEach((k, v) -> {
            if (k.equals("player")) {
                Player t = Bukkit.getPlayer(v);
                if (t != null) { ctx.set(CoreKeys.PLAYER, t); ctx.set(CoreKeys.LOCATION, t.getLocation()); }
            } else ctx.set(ContextKey.of(k, String.class), v);
        });
        return ctx;
    }

    private Map<String, String> parseBlock(String input, String name) {
        Map<String, String> res = new HashMap<>();
        Matcher m = Pattern.compile(name + "=\\{(.*?)\\}").matcher(input);
        if (m.find()) {
            for (String p : m.group(1).split(",\\s*")) {
                String[] kv = p.split("[:=]", 2);
                if (kv.length == 2) res.put(kv[0].trim(), kv[1].trim().replace("\"", ""));
            }
        }
        return res;
    }
}