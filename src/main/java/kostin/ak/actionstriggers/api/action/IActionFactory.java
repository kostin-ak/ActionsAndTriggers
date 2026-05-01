package kostin.ak.actionstriggers.api.action;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Фабрика, отвечающая за сборку конкретного Экшена из сырых параметров (из конфига/кода).
 */
public interface IActionFactory {

    /**
     * @return Уникальный ключ этого экшена (например, core:teleport).
     */
    @NotNull
    NamespacedKey getKey();

    /**
     * Создает готовый к выполнению Экшен на основе параметров.
     *
     * @param parameters Параметры (например, volume: 1.0, sound: LEVELUP).
     * @return Инстанс Action.
     */
    @NotNull
    Action create(@NotNull Map<String, Object> parameters);
}