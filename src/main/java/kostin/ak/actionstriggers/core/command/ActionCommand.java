package kostin.ak.actionstriggers.core.command;

import kostin.ak.actionstriggers.api.ActionTriggerAPI;
import kostin.ak.actionstriggers.api.action.ActionRegistry;
import kostin.ak.actionstriggers.api.context.ContextKey;
import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.api.trigger.TriggerRegistry;
import kostin.ak.actionstriggers.core.CoreKeys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Парсер консольных команд с использованием Revxrsal Commands.
 */
public class ActionCommand {

    private BiConsumer<NamespacedKey, ExecutionContext> debugListener = null;
    private final ActionRegistry actionRegistry;
    private final TriggerRegistry triggerRegistry;

    public ActionCommand(ActionRegistry actionRegistry, TriggerRegistry triggerRegistry) {
        this.actionRegistry = actionRegistry;
        this.triggerRegistry = triggerRegistry;
    }

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
        boolean success = ActionTriggerAPI.getActions().execute(actionKey, context, params);

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
        ActionTriggerAPI.getTriggers().dispatch(triggerKey, context);
        actor.reply("§aТриггер " + triggerKey + " успешно вызван!");
    }

    @Command("actionapi debug")
    @CommandPermission("actionstriggers.admin")
    public void toggleDebug(BukkitCommandActor actor) {
        if (debugListener != null) {
            // Если включен — выключаем
            ActionTriggerAPI.getTriggers().unsubscribeGlobal(debugListener);
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

            ActionTriggerAPI.getTriggers().subscribeGlobal(debugListener);
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
    @Command("actionapi list")
    @CommandPermission("actionstriggers.admin")
    public void asList(BukkitCommandActor actor) {
        MiniMessage mm = MiniMessage.miniMessage();

        // 1. Красивый заголовок с градиентом
        Component header = mm.deserialize("\n<gradient:#FF5555:#FFAA00><strikethrough>--------</strikethrough> [ Actions & Triggers ] <strikethrough>--------</strikethrough></gradient>\n");

        // 2. Формируем списки с помощью вспомогательного метода (см. ниже)
        Component actions = buildSection("Действия", actionRegistry.asList(), "#55FF55", "#55FFFF");
        Component triggers = buildSection("Триггеры", triggerRegistry.asList(), "#FF55FF", "#FFFF55");

        // 3. Подвал для визуального завершения "таблицы"
        Component footer = mm.deserialize("\n<dark_gray><strikethrough>                                        </strikethrough></dark_gray>");

        // 4. Собираем всё в единое сообщение
        Component finalMessage = Component.empty()
                .append(header)
                .append(actions).append(Component.newline()).append(Component.newline())
                .append(triggers)
                .append(footer);

        // Отправка в консоль
        Bukkit.getConsoleSender().sendMessage(finalMessage);

        // Отправка игроку
        if (actor.isPlayer()) {
            actor.requirePlayer().sendMessage(finalMessage);
        }
    }
    private Component buildSection(String title, List<String> items, String titleColor, String itemColor) {
        MiniMessage mm = MiniMessage.miniMessage();

        // Заголовок секции (например, "Действия:")
        Component titleComp = mm.deserialize("<color:" + titleColor + "><b>" + title + ":</b></color>\n");

        if (items == null || items.isEmpty()) {
            return titleComp.append(mm.deserialize("<gray><i>Список пуст</i></gray>"));
        }

        // Превращаем обычные строки в компоненты с цветом и эффектом при наведении (HoverEvent)
        List<Component> itemComponents = items.stream()
                .map(item -> mm.deserialize("<hover:show_text:'<gray>Элемент:</gray> <white>" + item + "</white>'><color:" + itemColor + ">" + item + "</color></hover>"))
                .collect(Collectors.toList());

        // Создаем красивый разделитель для визуальной "таблицы"
        Component separator = mm.deserialize(" <dark_gray><b>|</b></dark_gray> ");

        // Используем встроенный в Adventure инструмент Component.join() для соединения
        Component joinedItems = Component.join(JoinConfiguration.separator(separator), itemComponents);

        return titleComp.append(joinedItems);
    }
}