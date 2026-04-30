package kostin.ak.actionstriggers.core.defaults.actions;

import kostin.ak.actionstriggers.api.action.Action;
import kostin.ak.actionstriggers.api.action.ActionFactory;
import kostin.ak.actionstriggers.core.CoreActionParams;
import kostin.ak.actionstriggers.core.CoreActionKeys;
import kostin.ak.actionstriggers.core.CoreKeys;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class TeleportActionFactory implements ActionFactory {

    private static final NamespacedKey KEY = CoreActionKeys.TELEPORT;

    @Override
    public @NotNull NamespacedKey getKey() {
        return KEY;
    }

    @Override
    public @NotNull Action create(@NotNull Map<String, Object> params) {
        return context -> {
            Player player = context.get(CoreKeys.PLAYER);
            if (player != null && player.isOnline()) {
                try {
                    double x = Double.parseDouble(params.get(CoreActionParams.X).toString());
                    double y = Double.parseDouble(params.get(CoreActionParams.Y).toString());
                    double z = Double.parseDouble(params.get(CoreActionParams.Z).toString());

                    Location targetLoc = new Location(player.getWorld(), x, y, z);
                    player.teleportAsync(targetLoc);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
            return false;
        };
    }
}