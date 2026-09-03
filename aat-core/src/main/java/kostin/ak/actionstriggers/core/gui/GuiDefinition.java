package kostin.ak.actionstriggers.core.gui;

/**
 * @deprecated Используйте {@link kostin.ak.actionstriggers.api.gui.GuiDefinition} из модуля API.
 * Сохранено для 100% обратной совместимости.
 */
@Deprecated
public class GuiDefinition extends kostin.ak.actionstriggers.api.gui.GuiDefinition {

    public GuiDefinition() {
        super();
    }

    public GuiDefinition(String id, String title, int rows) {
        super(id, title, rows);
    }
}
