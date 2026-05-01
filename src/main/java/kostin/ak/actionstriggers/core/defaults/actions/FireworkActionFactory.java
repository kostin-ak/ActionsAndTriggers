package kostin.ak.actionstriggers.core.defaults.actions;

import kostin.ak.actionstriggers.api.action.ActionFactory;
import kostin.ak.actionstriggers.api.action.ActionParameters;
import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.core.CoreActionKeys;
import kostin.ak.actionstriggers.core.CoreKeys;
import kostin.ak.actionstriggers.core.CoreActionParams;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.NotNull;

public class FireworkActionFactory extends ActionFactory {

    private static final NamespacedKey KEY = CoreActionKeys.FIREWORK;

    @Override
    public @NotNull NamespacedKey getKey() { return KEY; }

    @Override
    protected boolean execute(@NotNull ExecutionContext context, @NotNull ActionParameters params) {
        int power = params.getInt(CoreActionParams.POWER, 1);

        Player player = context.get(CoreKeys.PLAYER);
        if (player != null && player.isOnline()) {
            Firework firework = (Firework) player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK_ROCKET);
            FireworkMeta meta = firework.getFireworkMeta();

            meta.addEffect(FireworkEffect.builder()
                    .withColor(Color.RED, Color.YELLOW)
                    .withFade(Color.ORANGE)
                    .with(FireworkEffect.Type.BALL_LARGE)
                    .trail(true)
                    .build());
            meta.setPower(power);

            firework.setFireworkMeta(meta);
            return true;
        }
        return false;
    }
}