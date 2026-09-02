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

    private boolean transparent = false;
    private String materialStr = "oraxen:gui_slot_cover";
    private String fallbackMaterial = "minecraft:light_gray_stained_glass_pane";

    public SlotCoverWidget() {
        super(0, 0, 1, 1);
    }

    public SlotCoverWidget(int x, int y) {
        super(x, y, 1, 1);
    }

    public boolean isTransparent() { return transparent; }
    public void setTransparent(boolean transparent) {
        this.transparent = transparent;
        if (transparent) {
            this.materialStr = "oraxen:gui_transparent_slot";
        }
    }

    @Override
    public void render(@NotNull GuiContext ctx, @NotNull Map<Integer, ItemStack> matrix) {
        if (!isVisible(ctx)) return;

        ItemStack item = null;
        
        // 1. Попытка получить кастомный предмет (Oraxen / ItemsAdder / Custom)
        if (materialStr != null && !materialStr.isEmpty()) {
            item = ActionTriggerAPI.getItems().resolveItem(materialStr);
            
            // Если указан Oraxen, но он не установлен — пробуем аналог в ItemsAdder
            if (item == null && materialStr.startsWith("oraxen:")) {
                String iaId = "itemsadder:" + materialStr.substring("oraxen:".length());
                item = ActionTriggerAPI.getItems().resolveItem(iaId);
            }
        }

        // 2. Глобальный первичный предмет из config.yml (если настроен)
        if (item == null) {
            String globalPrimary = kostin.ak.actionstriggers.ActionsTriggers.getInstance().getConfig().getString("gui.slot_cover.primary");
            if (globalPrimary != null && !globalPrimary.isEmpty() && !globalPrimary.equalsIgnoreCase(materialStr)) {
                item = ActionTriggerAPI.getItems().resolveItem(globalPrimary);
            }
        }

        // 3. Локальный или глобальный fallback (ванильный материал)
        if (item == null && fallbackMaterial != null && !fallbackMaterial.isEmpty()) {
            item = ActionTriggerAPI.getItems().resolveItem(fallbackMaterial);
        }
        if (item == null) {
            String globalFallback = kostin.ak.actionstriggers.ActionsTriggers.getInstance().getConfig().getString("gui.slot_cover.fallback");
            if (globalFallback != null && !globalFallback.isEmpty()) {
                item = ActionTriggerAPI.getItems().resolveItem(globalFallback);
            }
        }

        // 4. Железный ванильный фолбэк
        if (item == null) {
            item = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
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

    public String getFallbackMaterial() { return fallbackMaterial; }
    public void setFallbackMaterial(String fallbackMaterial) { this.fallbackMaterial = fallbackMaterial; }
}
