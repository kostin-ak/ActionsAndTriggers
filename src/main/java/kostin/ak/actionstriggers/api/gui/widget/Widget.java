package kostin.ak.actionstriggers.api.gui.widget;

import kostin.ak.actionstriggers.api.gui.ClickContext;
import kostin.ak.actionstriggers.api.gui.GuiContext;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Базовый интерфейс для всех виджетов AAT GUI.
 */
public interface Widget {

    /**
     * Отрисовывает виджет в матрицу слотов инвентаря.
     * @param ctx Контекст окна
     * @param matrix Карта номер слота -> ItemStack
     */
    void render(@NotNull GuiContext ctx, @NotNull Map<Integer, ItemStack> matrix);

    /**
     * Обрабатывает клик игрока по слоту.
     * @param ctx Контекст клика
     * @return true если событие клика должно быть отменено (чтобы не забирать предмет), false иначе.
     */
    boolean handleClick(@NotNull ClickContext ctx);

    /**
     * Обрабатывает перетаскивание предметов через мышь.
     */
    default boolean handleDrag(@NotNull GuiContext ctx, @NotNull InventoryDragEvent event) {
        return true; // по умолчанию отменяем перетаскивание
    }

    /**
     * Колбек при открытии окна игроком.
     */
    default void onOpen(@NotNull GuiContext ctx) {}

    /**
     * Колбек при закрытии окна игроком.
     */
    default void onClose(@NotNull GuiContext ctx) {}

    int getX();
    int getY();
    int getWidth();
    int getHeight();

    /**
     * Проверяет, занимает ли данный виджет указанный индекс слота.
     */
    default boolean occupiesSlot(int slot) {
        int slotX = slot % 9;
        int slotY = slot / 9;
        return slotX >= getX() && slotX < (getX() + getWidth())
                && slotY >= getY() && slotY < (getY() + getHeight());
    }
}
