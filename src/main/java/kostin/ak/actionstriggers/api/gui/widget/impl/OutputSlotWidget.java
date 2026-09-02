package kostin.ak.actionstriggers.api.gui.widget.impl;

import kostin.ak.actionstriggers.api.action.Action;
import kostin.ak.actionstriggers.api.gui.ClickContext;
import kostin.ak.actionstriggers.api.gui.GuiContext;
import kostin.ak.actionstriggers.api.gui.widget.AbstractWidget;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Слот выдачи готовой продукции (OutputSlot).
 * Игрок может ТОЛЬКО извлекать предметы. Помещение предметов заблокировано.
 */
public class OutputSlotWidget extends AbstractWidget {

    private List<Action> onTake = new ArrayList<>();

    public OutputSlotWidget() {
        super(0, 0, 1, 1);
    }

    public OutputSlotWidget(int x, int y) {
        super(x, y, 1, 1);
    }

    @Override
    public void render(@NotNull GuiContext ctx, @NotNull Map<Integer, ItemStack> matrix) {
        // Отрисовывается логикой станка или начальным состоянием
    }

    @Override
    public boolean handleClick(@NotNull ClickContext ctx) {
        InventoryClickEvent event = ctx.getEvent();
        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        // 1. Попытка положить предмет в слот выхода - БЛОКИРУЕМ!
        if (cursor != null && cursor.getType() != Material.AIR) {
            ctx.playSound("entity.villager.no", 1.0f, 1.0f);
            return true; // отменяем клик
        }

        // 2. Попытка забрать готовый предмет
        if (current != null && current.getType() != Material.AIR) {
            ctx.executeActions(onTake);
            return false; // разрешаем забрать предмет
        }

        return true;
    }

    public List<Action> getOnTake() { return onTake; }
    public void setOnTake(List<Action> onTake) { this.onTake = onTake; }
}
