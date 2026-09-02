package kostin.ak.actionstriggers.api.gui.widget.impl;

import kostin.ak.actionstriggers.api.ActionTriggerAPI;
import kostin.ak.actionstriggers.api.action.Action;
import kostin.ak.actionstriggers.api.gui.AATGuiHolder;
import kostin.ak.actionstriggers.api.gui.ClickContext;
import kostin.ak.actionstriggers.api.gui.GuiContext;
import kostin.ak.actionstriggers.api.gui.widget.AbstractWidget;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Одиночная интерактивная кнопка с поддержкой кастомных предметов (Oraxen/ItemsAdder)
 * и форматирования MiniMessage.
 */
public class ButtonWidget extends AbstractWidget {

    private String materialStr = "minecraft:stone";
    private String name;
    private List<String> lore = new ArrayList<>();
    private int amount = 1;
    private int customModelData = 0;
    private List<Action> actions = new ArrayList<>();

    public ButtonWidget() {
        super(0, 0, 1, 1);
    }

    public ButtonWidget(int x, int y) {
        super(x, y, 1, 1);
    }

    @Override
    public void render(@NotNull GuiContext ctx, @NotNull Map<Integer, ItemStack> matrix) {
        if (!isVisible(ctx)) return;

        ItemStack item = ActionTriggerAPI.getItems().resolveItem(materialStr);
        if (item == null) {
            item = new ItemStack(Material.STONE);
        } else {
            item = item.clone();
        }

        item.setAmount(Math.max(1, amount));

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            MiniMessage mm = MiniMessage.miniMessage();
            if (name != null && !name.isEmpty()) {
                meta.displayName(mm.deserialize(formatText(name, ctx)));
            }
            if (lore != null && !lore.isEmpty()) {
                List<Component> loreComponents = new ArrayList<>();
                for (String l : lore) {
                    loreComponents.add(mm.deserialize(formatText(l, ctx)));
                }
                meta.lore(loreComponents);
            }
            if (customModelData > 0) {
                meta.setCustomModelData(customModelData);
            }
            item.setItemMeta(meta);
        }

        matrix.put(toSlot(0, 0), item);
    }

    private String formatText(String text, GuiContext ctx) {
        if (text == null || text.isEmpty()) return "";
        text = text.replace("{player}", ctx.getPlayer().getName());
        AATGuiHolder holder = ctx.getHolder();
        if (holder != null) {
            text = text.replace("{progress_status}", String.valueOf(holder.getSessionState().getOrDefault("progress_status", "<green>В норме</green>")));
            text = text.replace("{progress_temp}", String.valueOf(holder.getSessionState().getOrDefault("progress_temp", "-180°C")));
            text = text.replace("{progress_stage}", String.valueOf(holder.getSessionState().getOrDefault("progress_stage", "Ожидание сырья")));
            text = text.replace("{progress_percent}", String.valueOf(holder.getSessionState().getOrDefault("progress_percent", "0")));

            for (Map.Entry<String, Object> entry : holder.getSessionState().entrySet()) {
                text = text.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
            }
        }
        return text;
    }

    @Override
    public boolean handleClick(@NotNull ClickContext ctx) {
        if (!isVisible(ctx.getGuiContext())) return true;

        ctx.executeActions(actions);
        return true; // отменяем клик, чтобы не забирать кнопку
    }

    public String getMaterialStr() { return materialStr; }
    public void setMaterialStr(String materialStr) { this.materialStr = materialStr; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<String> getLore() { return lore; }
    public void setLore(List<String> lore) { this.lore = lore; }

    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }

    public int getCustomModelData() { return customModelData; }
    public void setCustomModelData(int customModelData) { this.customModelData = customModelData; }

    public List<Action> getActions() { return actions; }
    public void setActions(List<Action> actions) { this.actions = actions; }
}
