package kostin.ak.actionstriggers.core.defaults.actions;

import kostin.ak.actionstriggers.api.action.Action;
import kostin.ak.actionstriggers.api.action.ActionFactory;
import kostin.ak.actionstriggers.core.CoreActionKeys;
import kostin.ak.actionstriggers.core.CoreKeys;
import kostin.ak.actionstriggers.core.CoreActionParams;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.Map;

public class CommandActionFactory implements ActionFactory {

    private static final NamespacedKey KEY = CoreActionKeys.COMMAND;

    @Override
    public @NotNull NamespacedKey getKey() { return KEY; }

    @Override
    public @NotNull Action create(@NotNull Map<String, Object> params) {
        String commandTpl = (String) params.getOrDefault(CoreActionParams.COMMAND, "say Привет");
        boolean asConsole = Boolean.parseBoolean(params.getOrDefault(CoreActionParams.AS_CONSOLE, "true").toString());

        return context -> {
            Player player = context.get(CoreKeys.PLAYER);

            // Простая встроенная замена (чтобы не тянуть тяжелый PlaceholderAPI для базовых нужд)
            String finalCmd = commandTpl;
            if (player != null) {
                finalCmd = finalCmd.replace("%player%", player.getName());
            }

            if (asConsole) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd);
            } else if (player != null && player.isOnline()) {
                player.performCommand(finalCmd);
            } else {
                return false;
            }
            return true;
        };
    }
}