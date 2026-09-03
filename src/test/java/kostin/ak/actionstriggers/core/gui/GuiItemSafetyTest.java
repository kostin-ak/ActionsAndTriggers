package kostin.ak.actionstriggers.core.gui;

import kostin.ak.actionstriggers.api.gui.AATGuiHolder;
import kostin.ak.actionstriggers.api.gui.widget.Widget;
import kostin.ak.actionstriggers.api.gui.widget.impl.InputSlotWidget;
import kostin.ak.actionstriggers.api.gui.widget.impl.MaskWidget;
import kostin.ak.actionstriggers.api.gui.widget.impl.OutputSlotWidget;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GUI Item Safety and Mask Unpacking Tests")
class GuiItemSafetyTest {

    @Test
    @DisplayName("Should correctly resolve child widgets inside MaskWidget")
    void testMaskWidgetChildResolution() {
        InputSlotWidget inputWidget = new InputSlotWidget();
        OutputSlotWidget outputWidget = new OutputSlotWidget();

        List<String> pattern = List.of(
                "#########",
                "#W#C#>#O#",
                "#########"
        );

        Map<Character, Widget> components = Map.of(
                'W', inputWidget,
                'O', outputWidget
        );

        MaskWidget mask = new MaskWidget(pattern, components);

        // Slot 10 corresponds to row 1, col 1 ('W')
        // Slot 16 corresponds to row 1, col 7 ('O')
        AATGuiHolder holder = new AATGuiHolder("test_gui", 3, Mockito.mock(Player.class), null);

        // Simulate rendering
        Map<Integer, ItemStack> matrix = new java.util.HashMap<>();
        mask.render(new kostin.ak.actionstriggers.api.gui.GuiContext(holder.getPlayer(), holder), matrix);

        // Verify that mask unpacked its slots
        Widget slot10 = mask.getWidgetAt(10);
        Widget slot16 = mask.getWidgetAt(16);

        assertNotNull(slot10, "Slot 10 should be resolved to InputSlotWidget");
        assertSame(inputWidget, slot10);

        assertNotNull(slot16, "Slot 16 should be resolved to OutputSlotWidget");
        assertSame(outputWidget, slot16);
    }
}
