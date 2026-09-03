package kostin.ak.actionstriggers.api.gui.widget.impl;

import kostin.ak.actionstriggers.api.ActionTriggerAPI;
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
 * Декоративный виджет-заполнитель фоновых слотов.
 */
public class FillerWidget extends AbstractWidget {

    private String materialStr = "minecraft:gray_stained_glass_pane";
    private String name = " ";
    private List<Integer> slots = new ArrayList<>();

    public FillerWidget() {
        super(0, 0, 1, 1);
    }

    public FillerWidget(String materialStr) {
        super(0, 0, 1, 1);
        this.materialStr = materialStr;
    }

    @Override
    public void render(@NotNull GuiContext ctx, @NotNull Map<Integer, ItemStack> matrix) {
        if (!isVisible(ctx)) return;

        ItemStack item = ActionTriggerAPI.getItems().resolveItem(materialStr);
        if (item == null) item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        else item = item.clone();

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MiniMessage.miniMessage().deserialize(name != null ? name : " "));
            item.setItemMeta(meta);
        }

        if (slots != null && !slots.isEmpty()) {
            for (int slot : slots) {
                matrix.put(slot, item.clone());
            }
        } else {
            matrix.put(toSlot(0, 0), item);
        }
    }

    @Override
    public boolean handleClick(@NotNull ClickContext ctx) {
        return true; // отменяем клик
    }

    @Override
    public boolean occupiesSlot(int slot) {
        if (slots != null && !slots.isEmpty()) {
            return slots.contains(slot);
        }
        return super.occupiesSlot(slot);
    }

    public String getMaterialStr() { return materialStr; }
    public void setMaterialStr(String materialStr) { this.materialStr = materialStr; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Integer> getSlots() { return slots; }
    public void setSlots(List<Integer> slots) { this.slots = slots; }
}
