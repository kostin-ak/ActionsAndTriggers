package kostin.ak.actionstriggers.api.action;

import kostin.ak.actionstriggers.api.context.ExecutionContext;
import org.jetbrains.annotations.NotNull;

/**
 * Исполняемое действие (уже созданное фабрикой на основе параметров).
 */
@FunctionalInterface
public interface Action {

    /**
     * Выполняет действие с переданным контекстом.
     *
     * @param context Текущий контекст выполнения (DataBag).
     * @return true, если действие выполнено успешно, иначе false.
     */
    boolean execute(@NotNull ExecutionContext context);
}
