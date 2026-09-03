package kostin.ak.actionstriggers.api.gui;

import kostin.ak.actionstriggers.api.ActionTriggerAPI;
import kostin.ak.actionstriggers.api.gui.widget.Widget;
import kostin.ak.actionstriggers.api.gui.widget.impl.ButtonWidget;
import kostin.ak.actionstriggers.api.gui.widget.impl.FillerWidget;
import kostin.ak.actionstriggers.api.gui.widget.impl.MaskWidget;
import kostin.ak.actionstriggers.core.gui.GuiDefinition;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Главная точка входа Fluent API для программного создания GUI-интерфейсов.
 */
public final class AATGui {

    private AATGui() {}

    public static Builder builder(@NotNull String id) {
        return new Builder(id);
    }

    public static class Builder {
        private final GuiDefinition definition;
        private Block boundBlock;

        public Builder(String id) {
            this.definition = new GuiDefinition(id, "Menu", 3);
        }

        public Builder title(String miniMessageTitle) {
            this.definition.setTitle(miniMessageTitle);
            return this;
        }

        public Builder title(Component titleComponent) {
            this.definition.setTitle(MiniMessage.miniMessage().serialize(titleComponent));
            return this;
        }

        public Builder rows(int rows) {
            this.definition.setRows(rows);
            return this;
        }

        public Builder bindBlock(Block block) {
            this.boundBlock = block;
            return this;
        }

        public Builder widget(Widget widget) {
            this.definition.getWidgets().add(widget);
            return this;
        }

        public Builder button(int slot, String material, Consumer<ButtonWidget> configurator) {
            ButtonWidget btn = new ButtonWidget(slot % 9, slot / 9);
            btn.setMaterialStr(material);
            configurator.accept(btn);
            this.definition.getWidgets().add(btn);
            return this;
        }

        public Builder filler(int[] slots, String material) {
            FillerWidget filler = new FillerWidget(material);
            java.util.List<Integer> list = new java.util.ArrayList<>();
            for (int s : slots) list.add(s);
            filler.setSlots(list);
            this.definition.getWidgets().add(filler);
            return this;
        }

        public Builder mask(MaskWidget mask) {
            this.definition.getWidgets().add(mask);
            return this;
        }

        public GuiDefinition build() {
            return definition;
        }

        public boolean open(Player player) {
            GuiDefinition def = build();
            kostin.ak.actionstriggers.ActionsTriggers.getGuiRegistry().register(def);
            return kostin.ak.actionstriggers.ActionsTriggers.getGuiRegistry().openGui(player, def.getId(), boundBlock);
        }
    }
}
