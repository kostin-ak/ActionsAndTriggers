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
 * Циклический селектор (CycleButton).
 * При каждом клике переключает состояние на следующее из списка (1 -> 2 -> 3 -> 1).
 */
public class CycleButtonWidget extends AbstractWidget {

    public static class StateEntry {
        private String id;
        private String materialStr;
        private String name;
        private List<String> lore = new ArrayList<>();
        private List<Action> actions = new ArrayList<>();

        public StateEntry() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getMaterialStr() { return materialStr; }
        public void setMaterialStr(String materialStr) { this.materialStr = materialStr; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public List<String> getLore() { return lore; }
        public void setLore(List<String> lore) { this.lore = lore; }
        public List<Action> getActions() { return actions; }
        public void setActions(List<Action> actions) { this.actions = actions; }
    }

    private String persistentKey;
    private String sessionKey;
    private List<StateEntry> states = new ArrayList<>();

    public CycleButtonWidget() {
        super(0, 0, 1, 1);
    }

    public int getCurrentIndex(GuiContext ctx) {
        if (states.isEmpty()) return 0;

        String currentId = null;
        if (persistentKey != null && !persistentKey.isEmpty()) {
            currentId = ctx.getPdcString(persistentKey, states.get(0).getId());
        } else if (sessionKey != null && !sessionKey.isEmpty()) {
            Object val = ctx.getState(sessionKey, states.get(0).getId());
            if (val != null) currentId = val.toString();
        }

        if (currentId != null) {
            for (int i = 0; i < states.size(); i++) {
                if (states.get(i).getId().equalsIgnoreCase(currentId)) {
                    return i;
                }
            }
        }
        return 0;
    }

    public void setCurrentIndex(GuiContext ctx, int index) {
        if (states.isEmpty()) return;
        int safeIndex = index % states.size();
        String id = states.get(safeIndex).getId();

        if (persistentKey != null && !persistentKey.isEmpty()) {
            ctx.setPdcString(persistentKey, id);
        }
        if (sessionKey != null && !sessionKey.isEmpty()) {
            ctx.setState(sessionKey, id);
        }
    }

    @Override
    public void render(@NotNull GuiContext ctx, @NotNull Map<Integer, ItemStack> matrix) {
        if (!isVisible(ctx) || states.isEmpty()) return;

        int index = getCurrentIndex(ctx);
        StateEntry entry = states.get(index);

        ItemStack item = ActionTriggerAPI.getItems().resolveItem(entry.getMaterialStr());
        if (item == null) item = new ItemStack(Material.STONE);
        else item = item.clone();

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            MiniMessage mm = MiniMessage.miniMessage();
            if (entry.getName() != null) {
                meta.displayName(mm.deserialize(entry.getName()));
            }
            if (entry.getLore() != null && !entry.getLore().isEmpty()) {
                List<Component> loreList = new ArrayList<>();
                for (String l : entry.getLore()) {
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
        if (!isVisible(ctx.getGuiContext()) || states.isEmpty()) return true;

        int current = getCurrentIndex(ctx.getGuiContext());
        int next = (current + 1) % states.size();
        setCurrentIndex(ctx.getGuiContext(), next);

        StateEntry newEntry = states.get(next);
        ctx.executeActions(newEntry.getActions());

        // Мгновенная перерисовка слота: обновляем именно слот клика ctx.getSlot()
        Map<Integer, ItemStack> matrix = new HashMap<>();
        render(ctx.getGuiContext(), matrix);
        ItemStack newItem = matrix.get(toSlot(0, 0));
        if (newItem == null && !matrix.isEmpty()) {
            newItem = matrix.values().iterator().next();
        }
        if (newItem != null) {
            ctx.getEvent().getInventory().setItem(ctx.getSlot(), newItem);
        }

        return true;
    }

    public String getPersistentKey() { return persistentKey; }
    public void setPersistentKey(String persistentKey) { this.persistentKey = persistentKey; }

    public String getSessionKey() { return sessionKey; }
    public void setSessionKey(String sessionKey) { this.sessionKey = sessionKey; }

    public List<StateEntry> getStates() { return states; }
    public void setStates(List<StateEntry> states) { this.states = states; }
}
