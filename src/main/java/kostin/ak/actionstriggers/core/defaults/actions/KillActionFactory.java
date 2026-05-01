package kostin.ak.actionstriggers.core.defaults.actions;

import kostin.ak.actionstriggers.api.action.ActionFactory;
import kostin.ak.actionstriggers.api.action.ActionParameters;
import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.core.CoreActionKeys;
import kostin.ak.actionstriggers.core.CoreKeys;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class KillActionFactory extends ActionFactory {

    private static final NamespacedKey KEY = CoreActionKeys.KILL;

    @Override
    public @NotNull NamespacedKey getKey() { return KEY; }

    @Override
    protected boolean execute(@NotNull ExecutionContext context, @NotNull ActionParameters params) {
        Player player = context.get(CoreKeys.PLAYER);
        if (player != null && player.isOnline() && !player.isDead()) {
            player.setHealth(0.0);
            return true;
        }
        return false;
    }
}