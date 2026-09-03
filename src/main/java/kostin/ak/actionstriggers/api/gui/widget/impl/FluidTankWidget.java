package kostin.ak.actionstriggers.api.gui.widget.impl;

import kostin.ak.actionstriggers.api.gui.ClickContext;
import kostin.ak.actionstriggers.api.gui.GuiContext;
import kostin.ak.actionstriggers.api.gui.widget.AbstractWidget;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Виджет вертикального резервуара жидкостей / энергии (FluidTankWidget).
 * Отображает столбец уровней жидкости или криогенного охладителя.
 */
public class FluidTankWidget extends AbstractWidget {

    private String levelKey = "tank_level";
    private int maxLevel = 100;
    private String filledMaterial = "minecraft:cyan_stained_glass_pane";
    private String emptyMaterial = "minecraft:gray_stained_glass_pane";
    private String title = "<gradient:#70E1F5:#FFD194><b>Уровень жидкости</b></gradient>";

    public FluidTankWidget() {
        super(0, 0, 1, 3);
    }

    public FluidTankWidget(int x, int y, int height, String levelKey, int maxLevel) {
        super(x, y, 1, height);
        this.levelKey = levelKey;
        this.maxLevel = Math.max(1, maxLevel);
    }

    @Override
    public void render(@NotNull GuiContext ctx, @NotNull Map<Integer, ItemStack> matrix) {
        if (!isVisible(ctx)) return;

        Object rawVal = ctx.getHolder().getSessionState().get(levelKey);
        int currentLevel = 0;
        if (rawVal instanceof Number n) {
            currentLevel = n.intValue();
        }

        double fraction = Math.max(0.0, Math.min(1.0, (double) currentLevel / maxLevel));
        int filledSegments = (int) Math.round(fraction * getHeight());

        Material fillMat = Material.matchMaterial(filledMaterial.replace("minecraft:", "").toUpperCase());
        if (fillMat == null) fillMat = Material.CYAN_STAINED_GLASS_PANE;

        Material emptyMat = Material.matchMaterial(emptyMaterial.replace("minecraft:", "").toUpperCase());
        if (emptyMat == null) emptyMat = Material.GRAY_STAINED_GLASS_PANE;

        for (int row = 0; row < getHeight(); row++) {
            int slot = (getY() + row) * 9 + getX();
            // Вертикальный резервуар заполняется снизу вверх
            boolean isFilled = (getHeight() - 1 - row) < filledSegments;

            try {
                ItemStack item = new ItemStack(isFilled ? fillMat : emptyMat);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.displayName(MiniMessage.miniMessage().deserialize(title));
                    List<Component> lore = new ArrayList<>();
                    lore.add(MiniMessage.miniMessage().deserialize("<gray>Объем: <aqua>" + currentLevel + " / " + maxLevel + " mB</aqua></gray>"));
                    lore.add(MiniMessage.miniMessage().deserialize("<dark_gray>Заполнение: " + (int) (fraction * 100) + "%</dark_gray>"));
                    meta.lore(lore);
                    item.setItemMeta(meta);
                }
                matrix.put(slot, item);
            } catch (Throwable ignored) {
                // Защита для тестов или отсутствия фабрики предметов
            }
        }
    }

    @Override
    public boolean handleClick(@NotNull ClickContext ctx) {
        return true; // Индикатор только для чтения
    }

    public String getLevelKey() { return levelKey; }
    public void setLevelKey(String levelKey) { this.levelKey = levelKey; }

    public int getMaxLevel() { return maxLevel; }
    public void setMaxLevel(int maxLevel) { this.maxLevel = maxLevel; }

    public String getFilledMaterial() { return filledMaterial; }
    public void setFilledMaterial(String filledMaterial) { this.filledMaterial = filledMaterial; }

    public String getEmptyMaterial() { return emptyMaterial; }
    public void setEmptyMaterial(String emptyMaterial) { this.emptyMaterial = emptyMaterial; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}
