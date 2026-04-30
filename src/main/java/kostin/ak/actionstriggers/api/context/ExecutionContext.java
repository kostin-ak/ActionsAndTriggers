package kostin.ak.actionstriggers.api.context;

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

    // Используем ConcurrentHashMap для потокобезопасности (отложенные экшены, async ивенты)
    private final Map<String, Object> data;
    private boolean cancelled;

    public ExecutionContext() {
        this.data = new ConcurrentHashMap<>();
        this.cancelled = false;
    }

    /**
     * Приватный конструктор для клонирования контекста.
     */
    private ExecutionContext(@NotNull Map<String, Object> existingData, boolean cancelled) {
        this.data = new ConcurrentHashMap<>(existingData);
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

    /**
     * Запрашивает отмену родительского Bukkit Event'а.
     */
    public void cancel() {
        this.cancelled = true;
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