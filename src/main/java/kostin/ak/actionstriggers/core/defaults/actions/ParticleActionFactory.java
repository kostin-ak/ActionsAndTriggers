package kostin.ak.actionstriggers.core.defaults.actions;

import kostin.ak.actionstriggers.api.action.ActionFactory;
import kostin.ak.actionstriggers.api.action.ActionParameters;
import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.core.CoreActionParams;
import kostin.ak.actionstriggers.core.CoreActionKeys;
import kostin.ak.actionstriggers.core.CoreKeys;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ParticleActionFactory extends ActionFactory {

    private static final NamespacedKey KEY = CoreActionKeys.PARTICLE;

    @Override
    public @NotNull NamespacedKey getKey() { return KEY; }

    @Override
    protected boolean execute(@NotNull ExecutionContext context, @NotNull ActionParameters params) {
        Player player = context.get(CoreKeys.PLAYER);
        if (player == null || !player.isOnline()) return false;

        String particleName = params.getString(CoreActionParams.PARTICLE, "FLAME").toUpperCase();
        int count = params.getInt(CoreActionParams.COUNT, 10);

        double dx = params.getDouble(CoreActionParams.dx, 0.0);
        double dy = params.getDouble(CoreActionParams.dy, 1.0);
        double dz = params.getDouble(CoreActionParams.dz, 0.0);

        double spreadX = params.getDouble(CoreActionParams.SPREAD_X, 0.5);
        double spreadY = params.getDouble(CoreActionParams.SPREAD_Y, 0.5);
        double spreadZ = params.getDouble(CoreActionParams.SPREAD_Z, 0.5);

        double speed = params.getDouble(CoreActionParams.SPEED, 0.0);

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
}