package kostin.ak.actionstriggers.api.gui.widget.impl;

import kostin.ak.actionstriggers.api.ActionTriggerAPI;
import kostin.ak.actionstriggers.api.gui.ClickContext;
import kostin.ak.actionstriggers.api.gui.GuiContext;
import kostin.ak.actionstriggers.api.gui.widget.AbstractWidget;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Виджет бесшовной заглушки инвентаря (SlotCoverWidget).
 * Перекрывает ванильные границы ячеек сундука/инвентаря и делает
 * неиспользуемые слоты абсолютно плоскими и визуально неотличимыми от фона интерфейса.
 */
public class SlotCoverWidget extends AbstractWidget {

    private String materialStr = "oraxen:gui_slot_cover";

    public SlotCoverWidget() {
        super(0, 0, 1, 1);
    }

    public SlotCoverWidget(int x, int y) {
        super(x, y, 1, 1);
    }

    @Override
    public void render(@NotNull GuiContext ctx, @NotNull Map<Integer, ItemStack> matrix) {
        if (!isVisible(ctx)) return;

        ItemStack item = ActionTriggerAPI.getItems().resolveItem(materialStr);
        if (item == null) {
            // Фолбэк на ванильный серый материал, если Oraxen еще не загружен
            item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        } else {
            item = item.clone();
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.empty());
            meta.lore(null);
            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }

        matrix.put(toSlot(0, 0), item);
    }

    @Override
    public boolean handleClick(@NotNull ClickContext ctx) {
        // Заглушка абсолютно статична и некликабельна
        return true;
    }

    public String getMaterialStr() { return materialStr; }
    public void setMaterialStr(String materialStr) { this.materialStr = materialStr; }
}
