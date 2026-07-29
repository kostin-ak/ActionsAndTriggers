package kostin.ak.actionstriggers.api.provider.impl;

import dev.lone.itemsadder.api.CustomBlock;
import kostin.ak.actionstriggers.api.provider.BlockProvider;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemsAdderBlockProvider implements BlockProvider {

    @Override
    public @NotNull String getNamespace() {
        return "itemsadder";
    }

    @Override
    public void setBlock(@NotNull Block block, @NotNull String id) {
        // Размещаем кастомный блок ItemsAdder по локации
        CustomBlock.place(id, block.getLocation());
    }

    @Override
    public @Nullable String getId(@NotNull Block block) {
        // Проверяем, стоит ли по этим координатам блок из ItemsAdder
        CustomBlock customBlock = CustomBlock.byAlreadyPlaced(block);
        return customBlock != null ? customBlock.getNamespacedID() : null;
    }
}