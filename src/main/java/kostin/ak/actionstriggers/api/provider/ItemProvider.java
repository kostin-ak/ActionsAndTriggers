package kostin.ak.actionstriggers.api.provider;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ItemProvider {
    /**
     * Возвращает уникальный неймспейс плагина (например, "oraxen", "item_adder", "minecraft").
     * Должен быть в нижнем регистре.
     */
    @NotNull String getNamespace();

    /**
     * Создает предмет по его локальному ID (без неймспейса).
     * @param id Локальный ID (например, "ruby_sword")
     * @return Готовый ItemStack или null, если такого предмета нет.
     */
    @Nullable ItemStack getItem(@NotNull String id);

    /**
     * Проверяет предмет и возвращает его локальный ID, если он принадлежит этому провайдеру.
     * @param item Предмет для проверки
     * @return Локальный ID или null, если это предмет не из этого плагина.
     */
    @Nullable String getId(@NotNull ItemStack item);
}