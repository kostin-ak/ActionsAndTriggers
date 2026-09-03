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
 * Контейнер вкладок (TabContainerWidget).
 * Позволяет переключать визуальные страницы/вкладки в рамках одного GUI окна.
 */
public class TabContainerWidget extends AbstractWidget {

    public record Tab(String id, ItemStack icon, List<Widget> widgets) {}

    private String activeTabKey = "active_tab";
    private final List<Tab> tabs = new ArrayList<>();
    private final Map<Integer, String> tabButtonSlots = new HashMap<>();

    public TabContainerWidget() {
        super(0, 0, 9, 6);
    }

    public void addTab(String id, int buttonSlot, ItemStack icon, List<Widget> widgets) {
        tabs.add(new Tab(id, icon, widgets));
        tabButtonSlots.put(buttonSlot, id);
    }

    @Override
    public void render(@NotNull GuiContext ctx, @NotNull Map<Integer, ItemStack> matrix) {
        if (!isVisible(ctx)) return;

        String currentTab = getActiveTab(ctx);
        if (currentTab == null && !tabs.isEmpty()) {
            currentTab = tabs.get(0).id();
            ctx.getHolder().getSessionState().put(activeTabKey, currentTab);
        }

        // 1. Отрисовываем кнопки вкладок
        for (Map.Entry<Integer, String> entry : tabButtonSlots.entrySet()) {
            int slot = entry.getKey();
            String tabId = entry.getValue();
            for (Tab tab : tabs) {
                if (tab.id().equalsIgnoreCase(tabId)) {
                    matrix.put(slot, tab.icon());
                    break;
                }
            }
        }

        // 2. Отрисовываем виджеты активной вкладки
        for (Tab tab : tabs) {
            if (tab.id().equalsIgnoreCase(currentTab)) {
                for (Widget w : tab.widgets()) {
                    w.render(ctx, matrix);
                }
                break;
            }
        }
    }

    @Override
    public boolean handleClick(@NotNull ClickContext ctx) {
        int clickedSlot = ctx.getSlot();
        GuiContext gCtx = ctx.getGuiContext();

        // Проверяем клик по переключателю вкладки
        if (tabButtonSlots.containsKey(clickedSlot)) {
            String newTab = tabButtonSlots.get(clickedSlot);
            gCtx.getHolder().getSessionState().put(activeTabKey, newTab);
            gCtx.refreshGui();
            return true;
        }

        // Перенаправляем клик виджетам активной вкладки
        String currentTab = getActiveTab(gCtx);
        for (Tab tab : tabs) {
            if (tab.id().equalsIgnoreCase(currentTab)) {
                for (Widget w : tab.widgets()) {
                    if (w.occupiesSlot(clickedSlot)) {
                        return w.handleClick(ctx);
                    }
                }
                break;
            }
        }

        return true;
    }

    private String getActiveTab(GuiContext ctx) {
        Object val = ctx.getHolder().getSessionState().get(activeTabKey);
        return val != null ? val.toString() : null;
    }

    public String getActiveTabKey() { return activeTabKey; }
    public void setActiveTabKey(String activeTabKey) { this.activeTabKey = activeTabKey; }
    public List<Tab> getTabs() { return tabs; }
}
