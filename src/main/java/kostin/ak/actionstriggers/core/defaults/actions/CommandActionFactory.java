package kostin.ak.actionstriggers.core.defaults.actions;

import kostin.ak.actionstriggers.api.action.ActionFactory;
import kostin.ak.actionstriggers.api.action.ActionParameters;
import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.core.CoreActionKeys;
import kostin.ak.actionstriggers.core.CoreKeys;
import kostin.ak.actionstriggers.core.CoreActionParams;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandActionFactory extends ActionFactory {

    private static final NamespacedKey KEY = CoreActionKeys.COMMAND;

    @Override
    public @NotNull NamespacedKey getKey() { return KEY; }

    @Override
    protected boolean execute(@NotNull ExecutionContext context, @NotNull ActionParameters params) {
        String finalCmd = params.getString(CoreActionParams.COMMAND, "say Привет");
        boolean asConsole = params.getBoolean(CoreActionParams.AS_CONSOLE, true);

        Player player = context.get(CoreKeys.PLAYER);

        if (asConsole) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd);
            return true;
        } else if (player != null && player.isOnline()) {
            player.performCommand(finalCmd);
            return true;
        }

        return false;
    }
}