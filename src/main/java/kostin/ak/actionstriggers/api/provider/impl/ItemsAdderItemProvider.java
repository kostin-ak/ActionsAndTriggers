package kostin.ak.actionstriggers.api.provider.impl;

import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.ItemsAdder;
import kostin.ak.actionstriggers.api.provider.ItemProvider;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ItemsAdderItemProvider implements ItemProvider {

    @Override
    public @NotNull String getNamespace() {
        return "itemsadder";
    }

    @Override
    public @Nullable ItemStack getItem(@NotNull String id) {
        CustomStack stack = CustomStack.getInstance(id);
        return stack != null ? stack.getItemStack() : null;
    }

    @Override
    public @Nullable String getId(@NotNull ItemStack item) {
        CustomStack stack = CustomStack.byItemStack(item);
        // Вернет локальный ID ItemsAdder (например: "myaddon:ruby_sword")
        return stack != null ? stack.getNamespacedID() : null;
    }

    @Override
    public @NotNull List<String> getAvailableIds() {
        List<String> ids = new ArrayList<>();
        // Обязательная проверка на null, так как API ItemsAdder иногда возвращает null до полной загрузки
        if (ItemsAdder.getAllItems() != null) {
            for (CustomStack stack : ItemsAdder.getAllItems()) {
                if (stack != null) {
                    ids.add(stack.getNamespacedID());
                }
            }
        }
        return ids;
    }
}