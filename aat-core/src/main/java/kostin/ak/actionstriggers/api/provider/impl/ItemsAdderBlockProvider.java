package kostin.ak.actionstriggers.api.provider.impl;

import dev.lone.itemsadder.api.CustomBlock;
import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.ItemsAdder;
import kostin.ak.actionstriggers.api.provider.BlockProvider;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ItemsAdderBlockProvider implements BlockProvider {

    @Override
    public @NotNull String getNamespace() {
        return "itemsadder";
    }

    @Override
    public void setBlock(@NotNull Block block, @NotNull String id) {
        CustomBlock.place(id, block.getLocation());
    }

    @Override
    public @Nullable String getId(@NotNull Block block) {
        CustomBlock customBlock = CustomBlock.byAlreadyPlaced(block);
        return customBlock != null ? customBlock.getNamespacedID() : null;
    }

    @Override
    public @NotNull List<String> getAvailableIds() {
        List<String> ids = new ArrayList<>();
        if (ItemsAdder.getAllItems() != null) {
            for (CustomStack stack : ItemsAdder.getAllItems()) {
                if (stack != null && stack.isBlock()) {
                    ids.add(stack.getNamespacedID());
                }
            }
        }
        return ids;
    }
}
