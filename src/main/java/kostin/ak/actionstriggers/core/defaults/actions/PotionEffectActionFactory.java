package kostin.ak.actionstriggers.core.defaults.actions;

import kostin.ak.actionstriggers.api.action.ActionFactory;
import kostin.ak.actionstriggers.api.action.ActionParameters;
import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.core.CoreActionParams;
import kostin.ak.actionstriggers.core.CoreActionKeys;
import kostin.ak.actionstriggers.core.CoreKeys;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class PotionEffectActionFactory extends ActionFactory {

    private static final NamespacedKey KEY = CoreActionKeys.POTION_EFFECT;

    @Override
    public @NotNull NamespacedKey getKey() { return KEY; }

    @Override
    protected boolean execute(@NotNull ExecutionContext context, @NotNull ActionParameters params) {
        String effectName = ((String) params.getString(CoreActionParams.EFFECT, "speed")).toLowerCase();
        int duration = Integer.parseInt(params.getString(CoreActionParams.DURATION, "200").toString());
        int amplifier = Integer.parseInt(params.getString(CoreActionParams.AMPLIFIER, "0").toString());
        boolean particles = Boolean.parseBoolean(params.getString(CoreActionParams.PARTICLES, "true").toString());

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
    }
}