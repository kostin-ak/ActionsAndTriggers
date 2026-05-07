package kostin.ak.actionstriggers.api.meta;

import org.jetbrains.annotations.NotNull;

/**
 * Хранит информацию о параметре экшена (считано из аннотации @ActionParam).
 */
public class ActionParameterMeta {
    private final String key;
    private final Class<?> type;
    private final boolean required;
    private final String description;

    public ActionParameterMeta(String key, Class<?> type, boolean required, String description) {
        this.key = key;
        this.type = type;
        this.required = required;
        this.description = description;
    }

    @NotNull public String getKey() { return key; }
    @NotNull public Class<?> getType() { return type; }
    public boolean isRequired() { return required; }
    @NotNull public String getDescription() { return description; }
}