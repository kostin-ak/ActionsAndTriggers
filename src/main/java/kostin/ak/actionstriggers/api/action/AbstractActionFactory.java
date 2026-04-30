package kostin.ak.actionstriggers.api.action;

import kostin.ak.actionstriggers.api.context.ExecutionContext;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Базовый класс для всех Экшенов.
 * Берет на себя рутину по созданию лямбд и умных параметров.
 */
public abstract class AbstractActionFactory implements ActionFactory {

    @Override
    public @NotNull Action create(@NotNull Map<String, Object> params) {
        return context -> {
            ActionParameters smartParams = new ActionParameters(params, context);
            return execute(context, smartParams);
        };
    }

    /**
     * Основной метод логики Экшена.
     * @param context Живой контекст события (игрок, блок и т.д.)
     * @param params Умные параметры (уже с работающими плейсхолдерами)
     * @return Успешно ли выполнился экшен
     */
    protected abstract boolean execute(@NotNull ExecutionContext context, @NotNull ActionParameters params);
}