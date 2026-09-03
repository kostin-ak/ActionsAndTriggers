package kostin.ak.actionstriggers.api.filter;

import kostin.ak.actionstriggers.api.context.ExecutionContext;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * Базовый интерфейс для всех условий и предфильтров.
 */
@FunctionalInterface
public interface Filter extends Predicate<ExecutionContext> {

    /**
     * Проверяет, удовлетворяет ли контекст условиям данного фильтра.
     *
     * @param context Текущий контекст выполнения.
     * @return true, если проверка пройдена, иначе false.
     */
    @Override
    boolean test(@NotNull ExecutionContext context);

}
