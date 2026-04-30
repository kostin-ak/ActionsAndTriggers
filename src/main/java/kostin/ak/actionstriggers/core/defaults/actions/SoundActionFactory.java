package kostin.ak.actionstriggers.core.defaults.actions;

import kostin.ak.actionstriggers.api.action.AbstractActionFactory;
import kostin.ak.actionstriggers.api.action.Action;
import kostin.ak.actionstriggers.api.action.ActionFactory;
import kostin.ak.actionstriggers.api.action.ActionParameters;
import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.core.CoreActionParams;
import kostin.ak.actionstriggers.core.CoreActionKeys;
import kostin.ak.actionstriggers.core.CoreKeys;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class SoundActionFactory extends AbstractActionFactory {

    private static final NamespacedKey KEY = CoreActionKeys.SOUND;

    @Override
    public @NotNull NamespacedKey getKey() { return KEY; }

    @Override
    protected boolean execute(@NotNull ExecutionContext context, @NotNull ActionParameters params) {
        Player player = context.get(CoreKeys.PLAYER);
        if (player == null || !player.isOnline()) return false;

        // Смотри, как просто! Плейсхолдеры в строках и числах раскроются сами!
        String sound = params.getString(CoreActionParams.SOUND, "minecraft:entity.player.levelup");
        float volume = params.getFloat(CoreActionParams.VOLUME, 1.0f);
        float pitch = params.getFloat(CoreActionParams.PITCH, 1.0f);

        // Чиним старый Bukkit формат
        if (!sound.contains(":") && sound.contains("_") && sound.toUpperCase().equals(sound)) {
            sound = "minecraft:" + sound.toLowerCase().replace("_", ".");
        }

        player.playSound(player.getLocation(), sound, volume, pitch);
        return true;
    }
}