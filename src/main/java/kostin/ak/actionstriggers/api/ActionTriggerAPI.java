package kostin.ak.actionstriggers.api;

import kostin.ak.actionstriggers.api.action.ActionRegistry;
import kostin.ak.actionstriggers.api.action.ActionScheduler;
import kostin.ak.actionstriggers.api.filter.FilterRegistry;
import kostin.ak.actionstriggers.api.trigger.TriggerRegistry;
import org.jetbrains.annotations.NotNull;

/**
 * Единая точка доступа ко всему функционалу библиотеки (Фасад).
 * Сторонние плагины используют этот класс для взаимодействия с ядром.
 */
public final class ActionTriggerAPI {

    private static ActionRegistry actionRegistry;
    private static TriggerRegistry triggerRegistry;
    private static ActionScheduler actionScheduler;
    private static FilterRegistry filterRegistry;
    private static Registrar registrar;

    private ActionTriggerAPI() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Инициализация API. Вызывается ТОЛЬКО нашим плагином внутри onEnable().
     */
    public static void init(@NotNull ActionRegistry actions, @NotNull TriggerRegistry triggers, @NotNull FilterRegistry filters,  @NotNull ActionScheduler scheduler) {
        actionRegistry = actions;
        triggerRegistry = triggers;
        filterRegistry = filters;
        actionScheduler = scheduler;
        registrar = Registrar.getInstance();
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
    public static FilterRegistry getFilters() {
        if (filterRegistry == null) throw new IllegalStateException("ActionAPI еще не инициализирован!");
        return filterRegistry;
    }

    @NotNull
    public static ActionScheduler getScheduler() {
        if (actionScheduler == null) throw new IllegalStateException("ActionAPI еще не инициализирован!");
        return actionScheduler;
    }

    @NotNull
    public static Registrar getRegistrar() {
        if (registrar == null) throw new IllegalStateException("ActionAPI еще не инициализирован!");
        return registrar;
    }
}