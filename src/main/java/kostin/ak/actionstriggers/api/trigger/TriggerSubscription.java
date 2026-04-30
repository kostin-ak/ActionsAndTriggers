package kostin.ak.actionstriggers.api.trigger;

import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.api.filter.Filter;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Модель данных (record), хранящая информацию о подписчике на триггер.
 */
public record TriggerSubscription(
        @NotNull Plugin plugin,
        @NotNull Filter filter,
        @NotNull Consumer<ExecutionContext> callback
) {}