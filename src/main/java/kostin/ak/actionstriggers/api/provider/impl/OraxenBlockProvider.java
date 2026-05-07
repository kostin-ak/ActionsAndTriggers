package kostin.ak.actionstriggers.api.provider.impl;

import io.th0rgal.oraxen.api.OraxenBlocks;
import io.th0rgal.oraxen.mechanics.Mechanic;
import kostin.ak.actionstriggers.api.provider.BlockProvider;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OraxenBlockProvider implements BlockProvider {

    @Override
    public @NotNull String getNamespace() {
        return "oraxen";
    }

    @Override
    public void setBlock(@NotNull Block block, @NotNull String id) {
        // Устанавливаем кастомный блок Oraxen по координатам
        OraxenBlocks.place(id, block.getLocation());
    }

    @Override
    public @Nullable String getId(@NotNull Block block) {
        // Проверяем, является ли блок кастомным блоком Oraxen
        if (OraxenBlocks.isOraxenBlock(block)) {
            // В зависимости от версии Oraxen API метод может немного отличаться.
            // Обычно это getOraxenBlock(Block) или getOraxenBlock(Location)
            Mechanic mechanic = OraxenBlocks.getOraxenBlock(block.getLocation());
            if (mechanic != null) {
                return mechanic.getItemID();
            }
        }
        return null;
    }
}