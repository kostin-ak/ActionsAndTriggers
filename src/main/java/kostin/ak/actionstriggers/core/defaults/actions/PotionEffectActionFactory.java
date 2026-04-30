package kostin.ak.actionstriggers.core.defaults.actions;

import kostin.ak.actionstriggers.api.action.Action;
import kostin.ak.actionstriggers.api.action.ActionFactory;
import kostin.ak.actionstriggers.core.CoreActionParams;
import kostin.ak.actionstriggers.core.CoreActionKeys;
import kostin.ak.actionstriggers.core.CoreKeys;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import java.util.Map;

public class PotionEffectActionFactory implements ActionFactory {

    private static final NamespacedKey KEY = CoreActionKeys.POTION_EFFECT;

    @Override
    public @NotNull NamespacedKey getKey() { return KEY; }

    @Override
    public @NotNull Action create(@NotNull Map<String, Object> params) {
        String effectName = ((String) params.getOrDefault(CoreActionParams.EFFECT, "speed")).toLowerCase();
        int duration = Integer.parseInt(params.getOrDefault(CoreActionParams.DURATION, "200").toString()); // В тиках
        int amplifier = Integer.parseInt(params.getOrDefault(CoreActionParams.AMPLIFIER, "0").toString()); // Уровень 1 = 0
        boolean particles = Boolean.parseBoolean(params.getOrDefault(CoreActionParams.PARTICLES, "true").toString());

        return context -> {
            Player player = context.get(CoreKeys.PLAYER);
            if (player != null && player.isOnline()) {
                NamespacedKey effectKey = NamespacedKey.minecraft(effectName);
                PotionEffectType type = Registry.EFFECT.get(effectKey);
                if (type != null) {
                    player.addPotionEffect(new PotionEffect(type, duration, amplifier, false, particles));
                    return true;
                }
            }
            return false;
        };
    }
}