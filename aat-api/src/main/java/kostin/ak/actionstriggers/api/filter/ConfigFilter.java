package kostin.ak.actionstriggers.api.filter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Указывает, что метод используется для парсинга фильтра из конфигурации.
 * Метод должен быть статическим, принимать Map<String, Object> и возвращать Filter.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigFilter {
    /**
     * @return Уникальный ключ фильтра (например, "core:chance")
     */
    String value();
}
