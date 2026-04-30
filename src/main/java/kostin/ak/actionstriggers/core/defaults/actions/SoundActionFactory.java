package kostin.ak.actionstriggers.core.defaults.actions;

import kostin.ak.actionstriggers.api.action.Action;
import kostin.ak.actionstriggers.api.action.ActionFactory;
import kostin.ak.actionstriggers.core.CoreActionParams;
import kostin.ak.actionstriggers.core.CoreActionKeys;
import kostin.ak.actionstriggers.core.CoreKeys;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class SoundActionFactory implements ActionFactory {

    private static final NamespacedKey KEY = CoreActionKeys.SOUND;

    @Override
    public @NotNull NamespacedKey getKey() { return KEY; }

    @Override
    public @NotNull Action create(@NotNull Map<String, Object> params) {
        String rawSound = (String) params.getOrDefault(CoreActionParams.SOUND, "minecraft:entity.player.levelup");

        // Автоматически чиним старый формат (ENTITY_PLAYER_LEVELUP -> minecraft:entity.player.levelup)
        if (!rawSound.contains(":") && rawSound.contains("_") && rawSound.toUpperCase().equals(rawSound)) {
            rawSound = "minecraft:" + rawSound.toLowerCase().replace("_", ".");
        }

        String finalSound = rawSound;
        float volume = Float.parseFloat(params.getOrDefault(CoreActionParams.VOLUME, "1.0").toString());
        float pitch = Float.parseFloat(params.getOrDefault(CoreActionParams.PITCH, "1.0").toString());

        return context -> {
            Player player = context.get(CoreKeys.PLAYER);
            if (player != null && player.isOnline()) {
                // Идеальный метод для кастомных звуков! Никаких проверок на стороне сервера.
                player.playSound(player.getLocation(), finalSound, volume, pitch);
                return true;
            }
            return false;
        };
    }
}