package kostin.ak.actionstriggers.api.gui.widget.impl;

import kostin.ak.actionstriggers.api.gui.ClickContext;
import kostin.ak.actionstriggers.api.gui.GuiContext;
import kostin.ak.actionstriggers.api.gui.widget.AbstractWidget;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Виджет постраничного списка (PagedListWidget).
 * Позволяет листать коллекции элементов с автоматическим расчетом страниц.
 */
public class PagedListWidget extends AbstractWidget {

    private String pageSessionKey = "paged_list_page";
    private List<ItemStack> items = new ArrayList<>();
    private int prevSlot = -1;
    private int nextSlot = -1;

    public PagedListWidget() {
        super(0, 0, 9, 3);
    }

    public PagedListWidget(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    public void render(@NotNull GuiContext ctx, @NotNull Map<Integer, ItemStack> matrix) {
        if (!isVisible(ctx)) return;

        int page = getCurrentPage(ctx);
        int pageSize = getWidth() * getHeight();
        int totalPages = Math.max(1, (int) Math.ceil((double) items.size() / pageSize));

        int startIndex = page * pageSize;
        int endIndex = Math.min(startIndex + pageSize, items.size());

        int index = startIndex;
        for (int row = 0; row < getHeight(); row++) {
            for (int col = 0; col < getWidth(); col++) {
                int slot = (getY() + row) * 9 + (getX() + col);
                if (index < endIndex) {
                    matrix.put(slot, items.get(index));
                    index++;
                }
            }
        }

        // Кнопка Предыдущая страница
        if (prevSlot >= 0 && page > 0) {
            try {
                ItemStack prevItem = new ItemStack(Material.ARROW);
                ItemMeta meta = prevItem.getItemMeta();
                if (meta != null) {
                    meta.displayName(MiniMessage.miniMessage().deserialize("<yellow>◀ Назад (Страница " + page + ")</yellow>"));
                    prevItem.setItemMeta(meta);
                }
                matrix.put(prevSlot, prevItem);
            } catch (Throwable ignored) {}
        }

        // Кнопка Следующая страница
        if (nextSlot >= 0 && (page + 1) < totalPages) {
            try {
                ItemStack nextItem = new ItemStack(Material.ARROW);
                ItemMeta meta = nextItem.getItemMeta();
                if (meta != null) {
                    meta.displayName(MiniMessage.miniMessage().deserialize("<yellow>Вперед (Страница " + (page + 2) + ") ▶</yellow>"));
                    nextItem.setItemMeta(meta);
                }
                matrix.put(nextSlot, nextItem);
            } catch (Throwable ignored) {}
        }
    }

    @Override
    public boolean handleClick(@NotNull ClickContext ctx) {
        int clickedSlot = ctx.getSlot();
        GuiContext gCtx = ctx.getGuiContext();
        int page = getCurrentPage(gCtx);
        int pageSize = getWidth() * getHeight();
        int totalPages = Math.max(1, (int) Math.ceil((double) items.size() / pageSize));

        if (clickedSlot == prevSlot && page > 0) {
            gCtx.getHolder().getSessionState().put(pageSessionKey, page - 1);
            gCtx.refreshGui();
            return true;
        }

        if (clickedSlot == nextSlot && (page + 1) < totalPages) {
            gCtx.getHolder().getSessionState().put(pageSessionKey, page + 1);
            gCtx.refreshGui();
            return true;
        }

        return true;
    }

    private int getCurrentPage(GuiContext ctx) {
        Object val = ctx.getHolder().getSessionState().get(pageSessionKey);
        if (val instanceof Number n) {
            return Math.max(0, n.intValue());
        }
        return 0;
    }

    public List<ItemStack> getItems() { return items; }
    public void setItems(List<ItemStack> items) { this.items = items; }

    public int getPrevSlot() { return prevSlot; }
    public void setPrevSlot(int prevSlot) { this.prevSlot = prevSlot; }

    public int getNextSlot() { return nextSlot; }
    public void setNextSlot(int nextSlot) { this.nextSlot = nextSlot; }

    public String getPageSessionKey() { return pageSessionKey; }
    public void setPageSessionKey(String pageSessionKey) { this.pageSessionKey = pageSessionKey; }
}
