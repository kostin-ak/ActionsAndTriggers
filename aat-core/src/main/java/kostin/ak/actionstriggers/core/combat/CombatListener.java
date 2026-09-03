package kostin.ak.actionstriggers.core.combat;

import kostin.ak.actionstriggers.api.gui.AATGuiHolder;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.InventoryView;

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

    private boolean isTrackable(Player player) {
        if (player == null || !player.isOnline()) return false;
        // В бою участвуют игроки в выживании и приключении
        GameMode gm = player.getGameMode();
        return gm == GameMode.SURVIVAL || gm == GameMode.ADVENTURE;
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

        if (attacker != null && isTrackable(attacker)) {
            tracker.tag(attacker, defaultCombatSeconds);
        }

        // 2. Жертва игрок
        if (event.getEntity() instanceof Player victim && isTrackable(victim)) {
            tracker.tag(victim, defaultCombatSeconds);
            checkAndCloseAtlas(victim);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGeneralDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (!isTrackable(player)) return;

            // Игнорируем только творческий мир
            if (player.getWorld().getName().equalsIgnoreCase("creative")) {
                return;
            }

            if (event.getFinalDamage() > 0.0 || event.getDamage() > 0.0) {
                tracker.tag(player, defaultCombatSeconds);
                checkAndCloseAtlas(player);
            }
        }
    }

    private void checkAndCloseAtlas(Player player) {
        try {
            InventoryView openView = player.getOpenInventory();
            if (openView != null && openView.getTopInventory().getHolder() instanceof AATGuiHolder holder) {
                if (holder.getGuiDefinition() != null && "astral_atlas".equalsIgnoreCase(holder.getGuiDefinition().getId())) {
                    player.closeInventory();
                    player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.8f, 1.4f);
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 0.9f);
                    player.sendActionBar(kostin.ak.actionstriggers.core.i18n.I18n.component("combat.atlas_interrupted"));
                }
            }
        } catch (Exception ignored) {}
    }
}
