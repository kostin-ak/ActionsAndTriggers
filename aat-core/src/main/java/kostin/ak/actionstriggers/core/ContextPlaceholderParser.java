package kostin.ak.actionstriggers.core;

import org.bukkit.entity.Player;

/**
 * Расширение шаблонизатора контекста для ядра, регистрирующее серверные свойства
 * (боевой статус, группы LuckPerms и метаданные).
 */
public class ContextPlaceholderParser extends kostin.ak.actionstriggers.api.context.ContextPlaceholderParser {

    static {
        registerFormatter(Player.class, (p, prop) -> {
            if (prop == null) return p.getName();
            return switch (prop.toLowerCase()) {
                case "name" -> p.getName();
                case "uuid" -> p.getUniqueId().toString();
                case "health" -> String.format(java.util.Locale.ROOT, "%.1f", p.getHealth());
                case "combat_remaining" -> String.valueOf(kostin.ak.actionstriggers.ActionsTriggers.getCombatTracker().getRemainingSeconds(p));
                case "group" -> kostin.ak.actionstriggers.core.hook.LuckPermsHook.getPrimaryGroup(p);
                case "prefix" -> kostin.ak.actionstriggers.core.hook.LuckPermsHook.getPrefix(p);
                case "suffix" -> kostin.ak.actionstriggers.core.hook.LuckPermsHook.getSuffix(p);
                case "weight" -> String.valueOf(kostin.ak.actionstriggers.core.hook.LuckPermsHook.getWeight(p));
                default -> p.getName();
            };
        });
    }
}