package kostin.ak.actionstriggers.core.defaults.actions;

import kostin.ak.actionstriggers.api.action.Action;
import kostin.ak.actionstriggers.api.action.ActionFactory;
import kostin.ak.actionstriggers.core.CoreActionKeys;
import kostin.ak.actionstriggers.core.CoreKeys;
import kostin.ak.actionstriggers.core.CoreActionParams;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.Map;

public class DamageActionFactory implements ActionFactory {

    private static final NamespacedKey KEY = CoreActionKeys.DAMAGE;

    @Override
    public @NotNull NamespacedKey getKey() { return KEY; }

    @Override
    public @NotNull Action create(@NotNull Map<String, Object> params) {
        double amount = Double.parseDouble(params.getOrDefault(CoreActionParams.AMOUNT, "1.0").toString());

        return context -> {
            Player player = context.get(CoreKeys.PLAYER);
            if (player != null && player.isOnline() && !player.isDead()) {
                player.damage(amount);
                return true;
            }
            return false;
        };
    }
}