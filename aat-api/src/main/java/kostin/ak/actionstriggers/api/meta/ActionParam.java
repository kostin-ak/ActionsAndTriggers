package kostin.ak.actionstriggers.api.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) // Вешаем на методы-парсеры (как @ConfigAction)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ActionParams.class)
public @interface ActionParam {
    String key(); // Например: CoreActionParams.MATERIAL
    Class<?> type() default String.class; // Ожидаемый тип данных (String.class, Integer.class)
    boolean required() default false; // Обязателен ли параметр?
    String description() default "";
}
