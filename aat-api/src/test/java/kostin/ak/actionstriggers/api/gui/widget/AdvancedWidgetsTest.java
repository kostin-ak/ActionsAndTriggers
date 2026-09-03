package kostin.ak.actionstriggers.api.gui.widget;

import kostin.ak.actionstriggers.api.gui.AATGuiHolder;
import kostin.ak.actionstriggers.api.gui.GuiContext;
import kostin.ak.actionstriggers.api.gui.widget.impl.FluidTankWidget;
import kostin.ak.actionstriggers.api.gui.widget.impl.PagedListWidget;
import kostin.ak.actionstriggers.api.gui.widget.impl.TabContainerWidget;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Advanced GUI Widgets (FluidTank, PagedList, TabContainer) Unit Tests")
class AdvancedWidgetsTest {

    private Player mockPlayer;
    private AATGuiHolder holder;
    private GuiContext ctx;

    @BeforeEach
    void setUp() {
        mockPlayer = Mockito.mock(Player.class);
        holder = new AATGuiHolder("advanced_test", 6, mockPlayer, null);
        ctx = new GuiContext(mockPlayer, holder);
    }

    @Test
    @DisplayName("Should configure fluid tank properties correctly")
    void testFluidTankProperties() {
        FluidTankWidget tank = new FluidTankWidget(2, 1, 4, "coolant_level", 200);
        assertEquals("coolant_level", tank.getLevelKey());
        assertEquals(200, tank.getMaxLevel());
        assertEquals(4, tank.getHeight());
        assertEquals(1, tank.getWidth());
    }

    @Test
    @DisplayName("Should correctly partition items across pages in PagedListWidget")
    void testPagedListPagination() {
        PagedListWidget pagedList = new PagedListWidget(0, 0, 9, 2); // 18 items per page
        pagedList.setPrevSlot(18);
        pagedList.setNextSlot(26);

        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            items.add(Mockito.mock(ItemStack.class));
        }
        pagedList.setItems(items);

        // Page 0
        Map<Integer, ItemStack> matrix = new HashMap<>();
        pagedList.render(ctx, matrix);

        // 18 items placed
        assertEquals(18, matrix.size());

        // Page 1
        holder.getSessionState().put(pagedList.getPageSessionKey(), 1);
        matrix.clear();
        pagedList.render(ctx, matrix);

        // 12 remaining items (30 - 18)
        assertEquals(12, matrix.size());
    }

    @Test
    @DisplayName("Should switch and render active tab widgets in TabContainerWidget")
    void testTabContainerSwitching() {
        TabContainerWidget tabContainer = new TabContainerWidget();

        Widget tab1Widget = Mockito.mock(Widget.class);
        Widget tab2Widget = Mockito.mock(Widget.class);
        ItemStack icon1 = Mockito.mock(ItemStack.class);
        ItemStack icon2 = Mockito.mock(ItemStack.class);

        tabContainer.addTab("general", 0, icon1, List.of(tab1Widget));
        tabContainer.addTab("upgrades", 1, icon2, List.of(tab2Widget));

        Map<Integer, ItemStack> matrix = new HashMap<>();
        tabContainer.render(ctx, matrix);

        // Default should render tab 'general'
        Mockito.verify(tab1Widget, Mockito.atLeastOnce()).render(Mockito.eq(ctx), Mockito.anyMap());
        Mockito.verify(tab2Widget, Mockito.never()).render(Mockito.eq(ctx), Mockito.anyMap());

        // Switch to 'upgrades' tab
        holder.getSessionState().put(tabContainer.getActiveTabKey(), "upgrades");
        tabContainer.render(ctx, matrix);
        Mockito.verify(tab2Widget, Mockito.atLeastOnce()).render(Mockito.eq(ctx), Mockito.anyMap());
    }
}
