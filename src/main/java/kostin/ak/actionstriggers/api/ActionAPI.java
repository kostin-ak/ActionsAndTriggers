package kostin.ak.actionstriggers.api;

import kostin.ak.actionstriggers.api.action.ActionRegistry;
import kostin.ak.actionstriggers.api.action.ActionScheduler;
import kostin.ak.actionstriggers.api.trigger.TriggerRegistry;
import org.jetbrains.annotations.NotNull;

/**
 * Единая точка доступа ко всему функционалу библиотеки (Фасад).
 * Сторонние плагины используют этот класс для взаимодействия с ядром.
 */
public final class ActionAPI {

    private static ActionRegistry actionRegistry;
    private static TriggerRegistry triggerRegistry;
    private static ActionScheduler actionScheduler;

    private ActionAPI() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Инициализация API. Вызывается ТОЛЬКО нашим плагином внутри onEnable().
     */
    public static void init(@NotNull ActionRegistry actions, @NotNull TriggerRegistry triggers, @NotNull ActionScheduler scheduler) {
        actionRegistry = actions;
        triggerRegistry = triggers;
        actionScheduler = scheduler;
    }

    @NotNull
    public static ActionRegistry getActions() {
        if (actionRegistry == null) throw new IllegalStateException("ActionAPI еще не инициализирован!");
        return actionRegistry;
    }

    @NotNull
    public static TriggerRegistry getTriggers() {
        if (triggerRegistry == null) throw new IllegalStateException("ActionAPI еще не инициализирован!");
        return triggerRegistry;
    }

    @NotNull
    public static ActionScheduler getScheduler() {
        if (actionScheduler == null) throw new IllegalStateException("ActionAPI еще не инициализирован!");
        return actionScheduler;
    }
}