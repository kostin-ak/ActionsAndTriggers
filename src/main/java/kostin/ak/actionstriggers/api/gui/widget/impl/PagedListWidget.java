package kostin.ak.actionstriggers.api.gui.widget.impl;

import kostin.ak.actionstriggers.api.ActionTriggerAPI;
import kostin.ak.actionstriggers.api.action.Action;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Виджет автопагинации динамических списков (PagedListWidget).
 */
public class PagedListWidget extends AbstractWidget {

    public static class PagedItem {
        private String materialStr;
        private String name;
        private List<String> lore = new ArrayList<>();
        private List<Action> actions = new ArrayList<>();

        public PagedItem() {}
        public PagedItem(String materialStr, String name, List<Action> actions) {
            this.materialStr = materialStr;
            this.name = name;
            this.actions = actions;
        }

        public String getMaterialStr() { return materialStr; }
        public void setMaterialStr(String materialStr) { this.materialStr = materialStr; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public List<String> getLore() { return lore; }
        public void setLore(List<String> lore) { this.lore = lore; }
        public List<Action> getActions() { return actions; }
        public void setActions(List<Action> actions) { this.actions = actions; }
    }

    private List<PagedItem> items = new ArrayList<>();
    private int prevSlot = -1;
    private int nextSlot = -1;
    private String prevButtonMaterial = "minecraft:arrow";
    private String nextButtonMaterial = "minecraft:arrow";
    private String prevButtonName = "<yellow>◀ Предыдущая страница</yellow>";
    private String nextButtonName = "<yellow>Следующая страница ▶</yellow>";

    // Карта слот -> элемент на текущей странице
    private final Map<Integer, PagedItem> activePageItems = new HashMap<>();

    public PagedListWidget() {
        super(1, 1, 7, 4);
    }

    public PagedListWidget(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public int getPageCapacity() {
        return width * height;
    }

    public int getTotalPages() {
        int cap = getPageCapacity();
        if (cap <= 0) return 1;
        return Math.max(1, (int) Math.ceil((double) items.size() / cap));
    }

    @Override
    public void render(@NotNull GuiContext ctx, @NotNull Map<Integer, ItemStack> matrix) {
        if (!isVisible(ctx)) return;

        activePageItems.clear();
        int maxPages = getTotalPages();
        ctx.setMaxPages(maxPages);

        int currentPage = Math.max(1, Math.min(ctx.getPage(), maxPages));
        ctx.setPage(currentPage);

        int capacity = getPageCapacity();
        int startIndex = (currentPage - 1) * capacity;
        int endIndex = Math.min(startIndex + capacity, items.size());

        MiniMessage mm = MiniMessage.miniMessage();

        // 1. Отрисовываем элементы текущей страницы
        int currentItemIndex = startIndex;
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                if (currentItemIndex >= endIndex) break;

                PagedItem pagedItem = items.get(currentItemIndex);
                int slot = toSlot(c, r);
                activePageItems.put(slot, pagedItem);

                ItemStack item = ActionTriggerAPI.getItems().resolveItem(pagedItem.getMaterialStr());
                if (item == null) item = new ItemStack(Material.PAPER);
                else item = item.clone();

                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    if (pagedItem.getName() != null) {
                        meta.displayName(mm.deserialize(pagedItem.getName()));
                    }
                    if (pagedItem.getLore() != null && !pagedItem.getLore().isEmpty()) {
                        List<Component> loreList = new ArrayList<>();
                        for (String l : pagedItem.getLore()) {
                            loreList.add(mm.deserialize(l));
                        }
                        meta.lore(loreList);
                    }
                    item.setItemMeta(meta);
                }

                matrix.put(slot, item);
                currentItemIndex++;
            }
        }

        // 2. Кнопка «Предыдущая страница»
        if (prevSlot >= 0 && currentPage > 1) {
            ItemStack prevItem = ActionTriggerAPI.getItems().resolveItem(prevButtonMaterial);
            if (prevItem == null) prevItem = new ItemStack(Material.ARROW);
            ItemMeta meta = prevItem.getItemMeta();
            if (meta != null) {
                meta.displayName(mm.deserialize(prevButtonName));
                prevItem.setItemMeta(meta);
            }
            matrix.put(prevSlot, prevItem);
        }

        // 3. Кнопка «Следующая страница»
        if (nextSlot >= 0 && currentPage < maxPages) {
            ItemStack nextItem = ActionTriggerAPI.getItems().resolveItem(nextButtonMaterial);
            if (nextItem == null) nextItem = new ItemStack(Material.ARROW);
            ItemMeta meta = nextItem.getItemMeta();
            if (meta != null) {
                meta.displayName(mm.deserialize(nextButtonName));
                nextItem.setItemMeta(meta);
            }
            matrix.put(nextSlot, nextItem);
        }
    }

    @Override
    public boolean handleClick(@NotNull ClickContext ctx) {
        int slot = ctx.getSlot();

        // Клик по кнопке «Назад»
        if (slot == prevSlot && ctx.getGuiContext().getPage() > 1) {
            ctx.getGuiContext().setPage(ctx.getGuiContext().getPage() - 1);
            ctx.playSound("ui.button.click", 0.8f, 1.2f);
            refreshInventory(ctx);
            return true;
        }

        // Клик по кнопке «Вперед»
        if (slot == nextSlot && ctx.getGuiContext().getPage() < getTotalPages()) {
            ctx.getGuiContext().setPage(ctx.getGuiContext().getPage() + 1);
            ctx.playSound("ui.button.click", 0.8f, 1.2f);
            refreshInventory(ctx);
            return true;
        }

        // Клик по элементу списка
        PagedItem clickedItem = activePageItems.get(slot);
        if (clickedItem != null) {
            ctx.executeActions(clickedItem.getActions());
            return true;
        }

        return true;
    }

    private void refreshInventory(ClickContext ctx) {
        Map<Integer, ItemStack> matrix = new HashMap<>();
        render(ctx.getGuiContext(), matrix);

        // Очищаем область списка
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                ctx.getEvent().getInventory().setItem(toSlot(c, r), null);
            }
        }
        if (prevSlot >= 0) ctx.getEvent().getInventory().setItem(prevSlot, null);
        if (nextSlot >= 0) ctx.getEvent().getInventory().setItem(nextSlot, null);

        // Заполняем новыми элементами
        for (Map.Entry<Integer, ItemStack> entry : matrix.entrySet()) {
            ctx.getEvent().getInventory().setItem(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public boolean occupiesSlot(int slot) {
        if (slot == prevSlot || slot == nextSlot) return true;
        return super.occupiesSlot(slot);
    }

    public List<PagedItem> getItems() { return items; }
    public void setItems(List<PagedItem> items) { this.items = items; }

    public int getPrevSlot() { return prevSlot; }
    public void setPrevSlot(int prevSlot) { this.prevSlot = prevSlot; }

    public int getNextSlot() { return nextSlot; }
    public void setNextSlot(int nextSlot) { this.nextSlot = nextSlot; }

    public String getPrevButtonMaterial() { return prevButtonMaterial; }
    public void setPrevButtonMaterial(String prevButtonMaterial) { this.prevButtonMaterial = prevButtonMaterial; }

    public String getNextButtonMaterial() { return nextButtonMaterial; }
    public void setNextButtonMaterial(String nextButtonMaterial) { this.nextButtonMaterial = nextButtonMaterial; }

    public String getPrevButtonName() { return prevButtonName; }
    public void setPrevButtonName(String prevButtonName) { this.prevButtonName = prevButtonName; }

    public String getNextButtonName() { return nextButtonName; }
    public void setNextButtonName(String nextButtonName) { this.nextButtonName = nextButtonName; }
}
