package kostin.ak.actionstriggers.core.defaults.actions;

import kostin.ak.actionstriggers.api.action.ActionFactory;
import kostin.ak.actionstriggers.api.action.ActionParameters;
import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.core.CoreActionParams;
import kostin.ak.actionstriggers.core.CoreActionKeys;
import kostin.ak.actionstriggers.core.CoreKeys;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TeleportActionFactory extends ActionFactory {

    private static final NamespacedKey KEY = CoreActionKeys.TELEPORT;

    @Override
    public @NotNull NamespacedKey getKey() {
        return KEY;
    }

    @Override
    protected boolean execute(@NotNull ExecutionContext context, @NotNull ActionParameters params) {
        Player player = context.get(CoreKeys.PLAYER);
        if (player != null && player.isOnline()) {
            try {
                double x = params.getDouble(CoreActionParams.X, 0.0);
                double y = params.getDouble(CoreActionParams.Y, 0.0);
                double z = params.getDouble(CoreActionParams.Z, 0.0);

                Location targetLoc = new Location(player.getWorld(), x, y, z);
                player.teleportAsync(targetLoc);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
}