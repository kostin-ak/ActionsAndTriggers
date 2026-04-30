package kostin.ak.actionstriggers.core.defaults.actions;

import kostin.ak.actionstriggers.api.action.Action;
import kostin.ak.actionstriggers.api.action.ActionFactory;
import kostin.ak.actionstriggers.core.CoreActionKeys;
import kostin.ak.actionstriggers.core.CoreKeys;
import kostin.ak.actionstriggers.core.CoreActionParams;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class MessageActionFactory implements ActionFactory {

    private static final NamespacedKey KEY = CoreActionKeys.MESSAGE;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Override
    public @NotNull NamespacedKey getKey() {
        return KEY;
    }

    @Override
    public @NotNull Action create(@NotNull Map<String, Object> params) {
        String rawMessage = (String) params.getOrDefault(CoreActionParams.TEXT, "<red>Текст не задан</red>");
        String rawSubtitle = (String) params.getOrDefault(CoreActionParams.SUBTITLE, "");
        String type = ((String) params.getOrDefault(CoreActionParams.TYPE, "chat")).toLowerCase();

        return context -> {
            Player player = context.get(CoreKeys.PLAYER);
            if (player != null && player.isOnline()) {
                var component = miniMessage.deserialize(rawMessage);

                switch (type) {
                    case CoreActionParams.MessageTypes.ACTIONBAR:
                        player.sendActionBar(component);
                        break;
                    case CoreActionParams.MessageTypes.TITLE:
                        var subComponent = miniMessage.deserialize(rawSubtitle);
                        player.showTitle(Title.title(component, subComponent));
                        break;
                    case CoreActionParams.MessageTypes.CHAT:
                    default:
                        player.sendMessage(component);
                        break;
                }
                return true;
            }
            return false;
        };
    }
}