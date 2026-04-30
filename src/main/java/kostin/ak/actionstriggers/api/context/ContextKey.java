package kostin.ak.actionstriggers.api.context;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Типизированный ключ для безопасного доступа к данным в ExecutionContext.
 * @param <T> Тип данных, который хранится по этому ключу.
 */
public final class ContextKey<T> {

    private final String id;
    private final Class<T> type;

    private ContextKey(@NotNull String id, @NotNull Class<T> type) {
        this.id = id;
        this.type = type;
    }

    /**
     * Создает новый типизированный ключ.
     * @param id Уникальный строковый идентификатор ключа (например, "player")
     * @param type Класс типа данных (например, Player.class)
     */
    @NotNull
    public static <T> ContextKey<T> of(@NotNull String id, @NotNull Class<T> type) {
        return new ContextKey<>(id, type);
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public Class<T> getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContextKey<?> that = (ContextKey<?>) o;
        return id.equals(that.id) && type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type);
    }
}