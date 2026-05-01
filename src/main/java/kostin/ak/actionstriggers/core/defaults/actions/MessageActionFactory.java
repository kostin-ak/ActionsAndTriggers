package kostin.ak.actionstriggers.core.defaults.actions;

import kostin.ak.actionstriggers.api.action.ActionFactory;
import kostin.ak.actionstriggers.api.action.ActionParameters;
import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.core.CoreActionKeys;
import kostin.ak.actionstriggers.core.CoreKeys;
import kostin.ak.actionstriggers.core.CoreActionParams;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MessageActionFactory extends ActionFactory {

    private static final NamespacedKey KEY = CoreActionKeys.MESSAGE;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Override
    public @NotNull NamespacedKey getKey() {
        return KEY;
    }

    @Override
    protected boolean execute(@NotNull ExecutionContext context, @NotNull ActionParameters params) {
        Player player = context.get(CoreKeys.PLAYER);
        if (player == null || !player.isOnline()) return false;

        String text = params.getString(CoreActionParams.TEXT, "<red>Текст не задан</red>");
        String subtitle = params.getString(CoreActionParams.SUBTITLE, "");
        String type = params.getString(CoreActionParams.TYPE, "chat").toLowerCase();

        var component = miniMessage.deserialize(text);

        switch (type) {
            case CoreActionParams.MessageTypes.ACTIONBAR:
                player.sendActionBar(component);
                break;
            case CoreActionParams.MessageTypes.TITLE:
                var subComponent = miniMessage.deserialize(subtitle);
                player.showTitle(Title.title(component, subComponent));
                break;
            case CoreActionParams.MessageTypes.CHAT:
            default:
                player.sendMessage(component);
                break;
        }
        return true;
    }
}