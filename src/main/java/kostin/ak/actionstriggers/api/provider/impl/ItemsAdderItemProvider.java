package kostin.ak.actionstriggers.api.provider.impl;

import dev.lone.itemsadder.api.CustomStack;
import kostin.ak.actionstriggers.api.provider.ItemProvider;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemsAdderItemProvider implements ItemProvider {

    @Override
    public @NotNull String getNamespace() {
        return "itemsadder";
    }

    @Override
    public @Nullable ItemStack getItem(@NotNull String id) {
        // Получаем объект CustomStack из ItemsAdder по его ID
        CustomStack stack = CustomStack.getInstance(id);
        return stack != null ? stack.getItemStack() : null;
    }

    @Override
    public @Nullable String getId(@NotNull ItemStack item) {
        // Проверяем, является ли ванильный ItemStack кастомным предметом ItemsAdder
        CustomStack stack = CustomStack.byItemStack(item);
        // getNamespacedID() вернет строку в формате "namespace:id" (например, "myitems:ruby_sword")
        return stack != null ? stack.getNamespacedID() : null;
    }
}