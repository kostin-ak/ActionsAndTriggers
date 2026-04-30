package kostin.ak.actionstriggers.core.command;

import kostin.ak.actionstriggers.api.ActionAPI;
import kostin.ak.actionstriggers.api.context.ContextKey;
import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.core.CoreKeys;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Парсер консольных команд с использованием Revxrsal Commands.
 */
public class ActionCommand {

    private BiConsumer<NamespacedKey, ExecutionContext> debugListener = null;

    @Command("actionapi run")
    @CommandPermission("actionstriggers.admin")
    public void runAction(
            BukkitCommandActor actor, // Универсальный отправитель (Консоль или Игрок)
            String actionKeyStr,      // Ключ (например, core:message)
            @Optional String argsString // Lamp автоматически поместит сюда ВЕСЬ оставшийся текст
    ) {
        NamespacedKey actionKey = NamespacedKey.fromString(actionKeyStr);
        if (actionKey == null) {
            actor.error("Неверный формат ключа экшена: " + actionKeyStr);
            return;
        }

        ExecutionContext context = new ExecutionContext();
        Map<String, Object> params = new HashMap<>();

        // Если есть доп. аргументы, разбиваем строку по пробелам и парсим
        if (argsString != null && !argsString.trim().isEmpty()) {
            String[] args = argsString.trim().split("\\s+");
            for (String arg : args) {
                String[] parts = arg.split("=", 2);
                if (parts.length == 2) {
                    String key = parts[0].toLowerCase();
                    String value = parts[1]; // Больше не делаем replace для всех подряд!

                    if (key.equals("player")) {
                        Player target = Bukkit.getPlayer(value);
                        if (target != null) {
                            context.set(CoreKeys.PLAYER, target);
                        } else {
                            actor.reply("§e[Внимание] Игрок " + value + " не найден на сервере.");
                        }
                    } else if (key.equals("text") || key.equals("subtitle") || key.equals("command")) {
                        params.put(key, smartDecode(value));
                    } else {
                        params.put(key, value);
                    }
                }
            }
        }

        // Если команду вызвал игрок, кладем ЕГО в контекст как дефолтного (если player=... не указан)
        if (actor.isPlayer() && !context.has(CoreKeys.PLAYER)) {
            context.set(CoreKeys.PLAYER, actor.requirePlayer());
        }

        if (context.has(CoreKeys.PLAYER)) {
            Player p = context.get(CoreKeys.PLAYER);
            context.set(CoreKeys.LOCATION, p.getLocation());
        }
        // Выполняем экшен!
        boolean success = ActionAPI.getActions().execute(actionKey, context, params);

        if (success) {
            actor.reply("§aЭкшен " + actionKey + " успешно выполнен!");
        } else {
            actor.error("Не удалось выполнить экшен " + actionKey + ".");
        }
    }

    @Command("actionapi trigger")
    @CommandPermission("actionstriggers.admin")
    public void dispatchTrigger(
            BukkitCommandActor actor,
            String triggerKeyStr,
            @Optional String argsString
    ) {
        NamespacedKey triggerKey = NamespacedKey.fromString(triggerKeyStr);
        if (triggerKey == null) {
            actor.error("Неверный формат ключа триггера.");
            return;
        }

        ExecutionContext context = new ExecutionContext();

        // Парсим параметры точно так же, как в runAction
        if (argsString != null && !argsString.trim().isEmpty()) {
            String[] args = argsString.trim().split("\\s+");
            for (String arg : args) {
                String[] parts = arg.split("=", 2);
                if (parts.length == 2) {
                    String key = parts[0].toLowerCase();
                    String value = parts[1].replace("_", " ");

                    if (key.equals("player")) {
                        Player target = Bukkit.getPlayer(value);
                        if (target != null) context.set(CoreKeys.PLAYER, target);
                    } else {
                        // В будущем здесь можно добавить парсинг блоков, сущностей и т.д.
                        context.set(ContextKey.of(key, String.class), value);
                    }
                }
            }
        }

        if (actor.isPlayer() && !context.has(CoreKeys.PLAYER)) {
            context.set(CoreKeys.PLAYER, actor.requirePlayer());
        }

        // Вызываем триггер (бросаем его в Event Bus)
        ActionAPI.getTriggers().dispatch(triggerKey, context);
        actor.reply("§aТриггер " + triggerKey + " успешно вызван!");
    }

    @Command("actionapi debug")
    @CommandPermission("actionstriggers.admin")
    public void toggleDebug(BukkitCommandActor actor) {
        if (debugListener != null) {
            // Если включен — выключаем
            ActionAPI.getTriggers().unsubscribeGlobal(debugListener);
            debugListener = null;
            actor.reply("§c[Actions&Triggers] Режим дебага ВЫКЛЮЧЕН.");
        } else {
            // Если выключен — включаем
            debugListener = (triggerKey, context) -> {
                // Формируем красивое сообщение для консоли
                String message = String.format("\n[A&T DEBUG] Сработал триггер: §e%s§r\nКонтекст: %s",
                        triggerKey.toString(), context.dump());

                // Всегда пишем в консоль
                Bukkit.getConsoleSender().sendMessage(message);

                // Если команду ввел игрок, дублируем ему в чат
                if (actor.isPlayer()) {
                    actor.requirePlayer().sendMessage(message);
                }
            };

            ActionAPI.getTriggers().subscribeGlobal(debugListener);
            actor.reply("§a[Actions&Triggers] Режим дебага ВКЛЮЧЕН. Теперь все триггеры логируются.");
        }
    }
    /**
     * Заменяет '_' на пробел, но ИГНОРИРУЕТ всё, что находится внутри { }
     */
    private String smartDecode(String input) {
        StringBuilder sb = new StringBuilder();
        boolean insideBrackets = false;
        for (char c : input.toCharArray()) {
            if (c == '{') insideBrackets = true;
            if (c == '}') insideBrackets = false;

            if (c == '_' && !insideBrackets) {
                sb.append(' ');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}