package kostin.ak.actionstriggers.core.combat;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Слушатель событий урона для автоматической фиксации Combat Tag.
 */
public class CombatListener implements Listener {

    private final CombatTracker tracker;
    private final int defaultCombatSeconds;

    public CombatListener(CombatTracker tracker) {
        this(tracker, 15);
    }

    public CombatListener(CombatTracker tracker, int defaultCombatSeconds) {
        this.tracker = tracker;
        this.defaultCombatSeconds = defaultCombatSeconds;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // 1. Атакующий игрок
        Player attacker = null;
        if (event.getDamager() instanceof Player p) {
            attacker = p;
        } else if (event.getDamager() instanceof Projectile proj && proj.getShooter() instanceof Player p) {
            attacker = p;
        }

        if (attacker != null && attacker.getGameMode() == GameMode.SURVIVAL) {
            tracker.tag(attacker, defaultCombatSeconds);
        }

        // 2. Жертва игрок
        if (event.getEntity() instanceof Player victim && victim.getGameMode() == GameMode.SURVIVAL) {
            tracker.tag(victim, defaultCombatSeconds);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGeneralDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (player.getGameMode() != GameMode.SURVIVAL) return;

            // Игнорируем урон в лобби/креативе
            if (player.getWorld().getName().equalsIgnoreCase("lobby") ||
                player.getWorld().getName().equalsIgnoreCase("creative")) {
                return;
            }

            if (event.getFinalDamage() > 0.0) {
                tracker.tag(player, defaultCombatSeconds);
            }
        }
    }
}
