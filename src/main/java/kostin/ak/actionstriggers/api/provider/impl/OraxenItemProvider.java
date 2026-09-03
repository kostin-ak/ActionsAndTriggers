package kostin.ak.actionstriggers.api.provider.impl;

import io.th0rgal.oraxen.api.OraxenItems;
import kostin.ak.actionstriggers.api.provider.ItemProvider;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OraxenItemProvider implements ItemProvider {

    @Override
    public @NotNull String getNamespace() {
        return "oraxen";
    }

    @Override
    public @Nullable ItemStack getItem(@NotNull String id) {
        if (OraxenItems.exists(id)) {
            return OraxenItems.getItemById(id).build();
        }
        return null;
    }

    @Override
    public @Nullable String getId(@NotNull ItemStack item) {
        return OraxenItems.getIdByItem(item);
    }

    @Override
    public @NotNull List<String> getAvailableIds() {
        return new ArrayList<>(OraxenItems.getEntriesAsMap().keySet());
    }
}