package kostin.ak.actionstriggers.api.meta;

import org.jetbrains.annotations.NotNull;

/**
 * Хранит информацию о параметре экшена (считано из аннотации @ActionParam).
 */
public record ActionParameterMeta(String key, Class<?> type, boolean required, String description) {

    @Override
    @NotNull
    public String key() {
        return key;
    }

    @Override
    @NotNull
    public Class<?> type() {
        return type;
    }

    @Override
    @NotNull
    public String description() {
        return description;
    }
}