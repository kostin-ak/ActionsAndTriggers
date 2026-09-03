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
 * Виджет переключателя (ВКЛ / ВЫКЛ) с автоматической синхронизацией с PDC блока или сессией.
 */
public class ToggleWidget extends AbstractWidget {

    public static class StateVisual {
        private String materialStr = "minecraft:gray_dye";
        private String name;
        private List<String> lore = new ArrayList<>();

        public StateVisual() {}
        public StateVisual(String materialStr, String name) {
            this.materialStr = materialStr;
            this.name = name;
        }

        public String getMaterialStr() { return materialStr; }
        public void setMaterialStr(String materialStr) { this.materialStr = materialStr; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public List<String> getLore() { return lore; }
        public void setLore(List<String> lore) { this.lore = lore; }
    }

    private String persistentKey;
    private String sessionKey;
    private boolean defaultState = false;

    private StateVisual onState = new StateVisual("minecraft:lime_dye", "<green>ВКЛ</green>");
    private StateVisual offState = new StateVisual("minecraft:gray_dye", "<gray>ВЫКЛ</gray>");

    private List<Action> onChange = new ArrayList<>();

    public ToggleWidget() {
        super(0, 0, 1, 1);
    }

    public boolean getCurrentState(GuiContext ctx) {
        if (persistentKey != null && !persistentKey.isEmpty()) {
            return ctx.getPdcBoolean(persistentKey, defaultState);
        }
        if (sessionKey != null && !sessionKey.isEmpty()) {
            Object val = ctx.getState(sessionKey, defaultState);
            return val instanceof Boolean ? (Boolean) val : defaultState;
        }
        return defaultState;
    }

    public void setCurrentState(GuiContext ctx, boolean state) {
        if (persistentKey != null && !persistentKey.isEmpty()) {
            ctx.setPdcBoolean(persistentKey, state);
        }
        if (sessionKey != null && !sessionKey.isEmpty()) {
            ctx.setState(sessionKey, state);
        }
    }

    @Override
    public void render(@NotNull GuiContext ctx, @NotNull Map<Integer, ItemStack> matrix) {
        if (!isVisible(ctx)) return;

        boolean active = getCurrentState(ctx);
        StateVisual visual = active ? onState : offState;

        ItemStack item = ActionTriggerAPI.getItems().resolveItem(visual.getMaterialStr());
        if (item == null) item = new ItemStack(Material.STONE);
        else item = item.clone();

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            MiniMessage mm = MiniMessage.miniMessage();
            if (visual.getName() != null) {
                meta.displayName(mm.deserialize(visual.getName()));
            }
            if (visual.getLore() != null && !visual.getLore().isEmpty()) {
                List<Component> loreList = new ArrayList<>();
                for (String l : visual.getLore()) {
                    loreList.add(mm.deserialize(l));
                }
                meta.lore(loreList);
            }
            item.setItemMeta(meta);
        }

        matrix.put(toSlot(0, 0), item);
    }

    @Override
    public boolean handleClick(@NotNull ClickContext ctx) {
        if (!isVisible(ctx.getGuiContext())) return true;

        boolean current = getCurrentState(ctx.getGuiContext());
        boolean next = !current;
        setCurrentState(ctx.getGuiContext(), next);

        ctx.executeActions(onChange);

        // Перерисовываем слот сразу
        Map<Integer, ItemStack> matrix = new HashMap<>();
        render(ctx.getGuiContext(), matrix);
        for (Map.Entry<Integer, ItemStack> entry : matrix.entrySet()) {
            ctx.getEvent().getInventory().setItem(entry.getKey(), entry.getValue());
        }

        return true;
    }

    public String getPersistentKey() { return persistentKey; }
    public void setPersistentKey(String persistentKey) { this.persistentKey = persistentKey; }

    public String getSessionKey() { return sessionKey; }
    public void setSessionKey(String sessionKey) { this.sessionKey = sessionKey; }

    public boolean isDefaultState() { return defaultState; }
    public void setDefaultState(boolean defaultState) { this.defaultState = defaultState; }

    public StateVisual getOnState() { return onState; }
    public void setOnState(StateVisual onState) { this.onState = onState; }

    public StateVisual getOffState() { return offState; }
    public void setOffState(StateVisual offState) { this.offState = offState; }

    public List<Action> getOnChange() { return onChange; }
    public void setOnChange(List<Action> onChange) { this.onChange = onChange; }
}
