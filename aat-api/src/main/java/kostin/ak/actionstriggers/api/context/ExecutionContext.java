package kostin.ak.actionstriggers.api.context;

import lombok.Builder;
import lombok.Singular;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Универсальное хранилище контекста (DataBag), передаваемое между Триггерами, Фильтрами и Экшенами.
 */
public class ExecutionContext implements Cloneable {

    private final Map<String, Object> data;
    private boolean cancelled;

    public ExecutionContext() {
        this.data = new ConcurrentHashMap<>(8, 0.75f, 1);
        this.cancelled = false;
    }

    private ExecutionContext(@NotNull Map<String, Object> existingData, boolean cancelled) {
        this.data = new ConcurrentHashMap<>(Math.max(8, existingData.size() + 2), 0.75f, 1);
        this.data.putAll(existingData);
        this.cancelled = cancelled;
    }

    /**
     * Записывает типизированное значение в контекст.
     */
    public <T> void set(@NotNull ContextKey<T> key, @NotNull T value) {
        data.put(key.getId(), value);
    }

    /**
     * Получает значение из контекста по ключу.
     * @return Значение нужного типа, либо null, если ключа нет или тип не совпадает.
     */
    @Nullable
    public <T> T get(@NotNull ContextKey<T> key) {
        Object obj = data.get(key.getId());
        if (key.getType().isInstance(obj)) {
            return key.getType().cast(obj);
        }
        return null;
    }

    /**
     * Строгий метод get. Бросает ошибку, если обязательного ключа нет.
     * Удобно для экшенов, которым жизненно необходим параметр.
     */
    @NotNull
    public <T> T getOrThrow(@NotNull ContextKey<T> key) {
        T value = get(key);
        if (value == null) {
            throw new IllegalArgumentException("Отсутствует обязательный параметр в контексте: " + key.getId());
        }
        return value;
    }

    /**
     * Проверяет, существует ли в контексте ключ и соответствует ли он заявленному типу.
     */
    public boolean has(@NotNull ContextKey<?> key) {
        Object obj = data.get(key.getId());
        return key.getType().isInstance(obj);
    }

    @NotNull
    public <T> T getOrDefault(@NotNull ContextKey<T> key, @NotNull T defaultValue) {
        T val = get(key);
        return val != null ? val : defaultValue;
    }

    /**
     * Запрашивает отмену родительского Bukkit Event'а.
     */
    public void cancel() {
        this.cancelled = true;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Создает полную, независимую копию текущего контекста.
     * Идеально подходит для создания отложенных (delayed) задач и пайплайнов.
     */
    @Override
    @NotNull
    public ExecutionContext clone() {
        try {
            super.clone(); // Формальность для интерфейса Cloneable
        } catch (CloneNotSupportedException ignored) {}

        return new ExecutionContext(this.data, this.cancelled);
    }

    @NotNull
    public String dump() {
        if (data.isEmpty()) return "[]";

        StringBuilder sb = new StringBuilder("[\n");
        data.forEach((key, value) -> {
            String valueStr = "null";
            if (value != null) {
                // Если это объект Bukkit, пытаемся вытащить имя или координаты для компактности

                switch (value){
                    case Player p:
                        valueStr = "Player(" + p.getName() + ")";
                        break;
                    case Location loc:
                        valueStr = String.format("Loc(%.1f, %.1f, %.1f)", loc.getX(), loc.getY(), loc.getZ());
                        break;
                    case Block b:
                        valueStr = "Block(" + b.getType() + " at " + b.getX() + "," + b.getY() + "," + b.getZ() + ")";
                        break;
                    case Entity e:
                        valueStr = "Entity(" + e.getType() + " id=" + e.getEntityId() + ")";
                        break;
                    case Action action:
                        valueStr = "Action(" + action.name() + ")";
                        break;
                    default:
                        valueStr = value.toString();
                        break;
                }

            }
            sb.append("    ").append(key).append(": ").append(valueStr).append("\n");
        });
        sb.append("]");
        return sb.toString();
    }

    /**
     * Получает сырое значение из контекста по строковому ID ключа (для плейсхолдеров).
     */
    @Nullable
    public Object getRaw(@NotNull String keyId) {
        return data.get(keyId);
    }
}
