package kostin.ak.actionstriggers.api.gui.widget;

import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.api.filter.Filter;
import kostin.ak.actionstriggers.api.gui.GuiContext;
import kostin.ak.actionstriggers.core.CoreKeys;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Базовый абстрактный класс виджета с реализацией Bounding Box и условий видимости.
 */
public abstract class AbstractWidget implements Widget {

    protected int x = 0;
    protected int y = 0;
    protected int width = 1;
    protected int height = 1;
    protected Filter condition;

    public AbstractWidget() {}

    public AbstractWidget(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    @Override
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    @Override
    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }

    @Override
    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }

    public @Nullable Filter getCondition() { return condition; }
    public void setCondition(@Nullable Filter condition) { this.condition = condition; }

    /**
     * Проверяет, удовлетворяет ли контекст условию видимости виджета.
     */
    protected boolean isVisible(GuiContext ctx) {
        if (condition == null) return true;

        ExecutionContext execCtx = new ExecutionContext();
        execCtx.set(CoreKeys.PLAYER, ctx.getPlayer());
        execCtx.set(CoreKeys.LOCATION, ctx.getPlayer().getLocation());
        execCtx.set(CoreKeys.WORLD, ctx.getPlayer().getWorld().getName());
        if (ctx.getBoundBlock() != null) {
            execCtx.set(CoreKeys.BLOCK, ctx.getBoundBlock());
        }

        return condition.test(execCtx);
    }

    /**
     * Преобразует локальные координаты (relX, relY) внутри виджета в глобальный слот инвентаря.
     */
    protected int toSlot(int relX, int relY) {
        return (y + relY) * 9 + (x + relX);
    }
}
