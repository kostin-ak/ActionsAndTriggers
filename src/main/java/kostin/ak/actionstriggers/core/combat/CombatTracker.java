package kostin.ak.actionstriggers.core.combat;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Менеджер боевого режима (Combat Tag).
 * Отслеживает состояние входа в бой при получении или нанесении урона.
 */
public class CombatTracker {

    private final Map<UUID, Long> combatExpirations = new ConcurrentHashMap<>();

    /**
     * Помечает игрока как находящегося в бою на указанное количество секунд.
     */
    public void tag(@NotNull Player player, int seconds) {
        long expireAt = System.currentTimeMillis() + (seconds * 1000L);
        combatExpirations.put(player.getUniqueId(), expireAt);
    }

    /**
     * Проверяет, находится ли игрок сейчас в боевом режиме.
     */
    public boolean isInCombat(@NotNull Player player) {
        Long expireAt = combatExpirations.get(player.getUniqueId());
        if (expireAt == null) return false;
        if (System.currentTimeMillis() >= expireAt) {
            combatExpirations.remove(player.getUniqueId());
            return false;
        }
        return true;
    }

    /**
     * Возвращает количество оставшихся секунд боя.
     */
    public int getRemainingSeconds(@NotNull Player player) {
        Long expireAt = combatExpirations.get(player.getUniqueId());
        if (expireAt == null) return 0;
        long diff = expireAt - System.currentTimeMillis();
        if (diff <= 0) {
            combatExpirations.remove(player.getUniqueId());
            return 0;
        }
        return (int) Math.ceil(diff / 1000.0);
    }

    /**
     * Снимает боевой режим с игрока досрочно.
     */
    public void untag(@NotNull Player player) {
        combatExpirations.remove(player.getUniqueId());
    }

    public void cleanup() {
        long now = System.currentTimeMillis();
        combatExpirations.entrySet().removeIf(entry -> now >= entry.getValue());
    }

    /**
     * Полностью сбрасывает состояние боевого режима для всех игроков.
     */
    public void clear() {
        combatExpirations.clear();
    }
}
