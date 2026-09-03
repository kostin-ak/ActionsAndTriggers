package kostin.ak.actionstriggers.api.gui.widget.impl;

import kostin.ak.actionstriggers.api.gui.ClickContext;
import kostin.ak.actionstriggers.api.gui.GuiContext;
import kostin.ak.actionstriggers.api.gui.widget.AbstractWidget;
import kostin.ak.actionstriggers.api.gui.widget.Widget;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Компоновщик-маска (MaskWidget).
 * Позволяет визуально задавать раскладку инвентаря символьными строками (например 9 символов на строку).
 */
public class MaskWidget extends AbstractWidget {

    private List<String> pattern = new ArrayList<>();
    private Map<Character, Widget> components = new HashMap<>();

    // Карта абсолютный слот -> виджет
    private final Map<Integer, Widget> slotToWidget = new HashMap<>();

    public MaskWidget() {
        super(0, 0, 9, 6);
    }

    public MaskWidget(List<String> pattern, Map<Character, Widget> components) {
        super(0, 0, 9, pattern.size());
        this.pattern = pattern;
        this.components = components;
    }

    @Override
    public void render(@NotNull GuiContext ctx, @NotNull Map<Integer, ItemStack> matrix) {
        if (!isVisible(ctx)) return;

        slotToWidget.clear();

        for (int row = 0; row < pattern.size(); row++) {
            String line = pattern.get(row);
            for (int col = 0; col < line.length() && col < 9; col++) {
                char ch = line.charAt(col);
                Widget widget = components.get(ch);
                if (widget != null) {
                    int slot = row * 9 + col;
                    slotToWidget.put(slot, widget);

                    // Временная матрица для отрисовки дочернего элемента
                    Map<Integer, ItemStack> subMatrix = new HashMap<>();
                    widget.render(ctx, subMatrix);

                    if (!subMatrix.isEmpty()) {
                        // Если виджет вернул конкретный предмет, помещаем его в текущий слот
                        ItemStack item = subMatrix.get(widget.getX() + widget.getY() * 9);
                        if (item == null) {
                            item = subMatrix.values().iterator().next();
                        }
                        matrix.put(slot, item);
                    }
                }
            }
        }
    }

    @Override
    public boolean handleClick(@NotNull ClickContext ctx) {
        Widget target = slotToWidget.get(ctx.getSlot());
        if (target != null) {
            return target.handleClick(ctx);
        }
        return true;
    }

    @Override
    public boolean occupiesSlot(int slot) {
        return slotToWidget.containsKey(slot);
    }

    public List<String> getPattern() { return pattern; }
    public void setPattern(List<String> pattern) { this.pattern = pattern; }

    public Map<Character, Widget> getComponents() { return components; }
    public void setComponents(Map<Character, Widget> components) { this.components = components; }
}
