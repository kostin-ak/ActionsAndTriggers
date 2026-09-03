package kostin.ak.actionstriggers.api.gui;

import kostin.ak.actionstriggers.api.gui.widget.Widget;
import kostin.ak.actionstriggers.api.gui.widget.impl.ButtonWidget;
import kostin.ak.actionstriggers.api.gui.widget.impl.FillerWidget;
import kostin.ak.actionstriggers.api.gui.widget.impl.MaskWidget;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Главная точка входа Fluent API для программного создания GUI-интерфейсов.
 */
public final class AATGui {

    private AATGui() {}

    @FunctionalInterface
    public interface GuiOpener {
        boolean open(@NotNull Player player, @NotNull GuiDefinition def, @Nullable Block boundBlock);
    }

    private static GuiOpener opener;

    public static void setOpener(GuiOpener o) {
        opener = o;
    }

    public static Builder builder(@NotNull String id) {
        return new Builder(id);
    }

    public static final class Builder {
        private final GuiDefinition definition;
        private Block boundBlock;

        private Builder(@NotNull String id) {
            this.definition = new GuiDefinition(id, id, 3);
        }

        public Builder title(@NotNull String miniMessageTitle) {
            this.definition.setTitle(miniMessageTitle);
            return this;
        }

        public Builder title(@NotNull Component title) {
            this.definition.setTitle(MiniMessage.miniMessage().serialize(title));
            return this;
        }

        public Builder rows(int rows) {
            this.definition.setRows(rows);
            return this;
        }

        public Builder bindTo(@NotNull Block block) {
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
            if (opener != null) {
                return opener.open(player, def, boundBlock);
            }
            return false;
        }
    }
}
