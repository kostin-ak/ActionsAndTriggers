package kostin.ak.actionstriggers.core.gui;

import kostin.ak.actionstriggers.api.action.Action;
import kostin.ak.actionstriggers.api.gui.widget.Widget;

import java.util.ArrayList;
import java.util.List;

/**
 * Описание графического интерфейса GUI (загруженное из YAML или созданное через API).
 */
public class GuiDefinition {

    private String id;
    private String title = "Menu";
    private int rows = 3;
    private String targetBlock;

    private List<Widget> widgets = new ArrayList<>();
    private List<Action> onOpenActions = new ArrayList<>();
    private List<Action> onCloseActions = new ArrayList<>();

    public GuiDefinition() {}

    public GuiDefinition(String id, String title, int rows) {
        this.id = id;
        this.title = title;
        this.rows = rows;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getRows() { return rows; }
    public void setRows(int rows) { this.rows = Math.max(1, Math.min(6, rows)); }

    public String getTargetBlock() { return targetBlock; }
    public void setTargetBlock(String targetBlock) { this.targetBlock = targetBlock; }

    public List<Widget> getWidgets() { return widgets; }
    public void setWidgets(List<Widget> widgets) { this.widgets = widgets; }

    public List<Action> getOnOpenActions() { return onOpenActions; }
    public void setOnOpenActions(List<Action> onOpenActions) { this.onOpenActions = onOpenActions; }

    public List<Action> getOnCloseActions() { return onCloseActions; }
    public void setOnCloseActions(List<Action> onCloseActions) { this.onCloseActions = onCloseActions; }
}
