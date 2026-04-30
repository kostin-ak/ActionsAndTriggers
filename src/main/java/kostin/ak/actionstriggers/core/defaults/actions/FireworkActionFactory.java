package kostin.ak.actionstriggers.core.defaults.actions;

import kostin.ak.actionstriggers.api.action.Action;
import kostin.ak.actionstriggers.api.action.ActionFactory;
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

import java.util.Map;

public class FireworkActionFactory implements ActionFactory {

    private static final NamespacedKey KEY = CoreActionKeys.FIREWORK;

    @Override
    public @NotNull NamespacedKey getKey() { return KEY; }

    @Override
    public @NotNull Action create(@NotNull Map<String, Object> params) {
        int power = Integer.parseInt(params.getOrDefault(CoreActionParams.POWER, "1").toString());

        return context -> {
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
        };
    }
}