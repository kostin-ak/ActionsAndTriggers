package kostin.ak.actionstriggers.core.defaults.actions;

import kostin.ak.actionstriggers.api.action.Action;
import kostin.ak.actionstriggers.api.action.ActionFactory;
import kostin.ak.actionstriggers.core.CoreActionParams;
import kostin.ak.actionstriggers.core.CoreActionKeys;
import kostin.ak.actionstriggers.core.CoreKeys;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ParticleActionFactory implements ActionFactory {

    private static final NamespacedKey KEY = CoreActionKeys.PARTICLE;

    @Override
    public @NotNull NamespacedKey getKey() { return KEY; }

    @Override
    public @NotNull Action create(@NotNull Map<String, Object> params) {
        String particleName = ((String) params.getOrDefault(CoreActionParams.PARTICLE, "FLAME")).toUpperCase();
        int count = Integer.parseInt(params.getOrDefault(CoreActionParams.COUNT, "10").toString());

        // Смещение центральной точки (относительно ног игрока)
        double dx = Double.parseDouble(params.getOrDefault(CoreActionParams.dx, "0.0").toString());
        double dy = Double.parseDouble(params.getOrDefault(CoreActionParams.dy, "1.0").toString());
        double dz = Double.parseDouble(params.getOrDefault(CoreActionParams.dz, "0.0").toString());

        // Разброс (насколько широко они разлетаются)
        double spreadX = Double.parseDouble(params.getOrDefault(CoreActionParams.SPREAD_X, "0.5").toString());
        double spreadY = Double.parseDouble(params.getOrDefault(CoreActionParams.SPREAD_Y, "0.5").toString());
        double spreadZ = Double.parseDouble(params.getOrDefault(CoreActionParams.SPREAD_Z, "0.5").toString());

        // Скорость (для некоторых партиклов это меняет цвет или дальность разлета)
        double speed = Double.parseDouble(params.getOrDefault(CoreActionParams.SPEED, "0.0").toString());

        return context -> {
            Player player = context.get(CoreKeys.PLAYER);
            if (player != null && player.isOnline()) {
                try {
                    Particle particle = Particle.valueOf(particleName);
                    Location targetLoc = player.getLocation().add(dx, dy, dz);

                    player.getWorld().spawnParticle(
                            particle, targetLoc, count,
                            spreadX, spreadY, spreadZ, speed
                    );
                    return true;
                } catch (IllegalArgumentException ignored) {
                    return false;
                }
            }
            return false;
        };
    }
}