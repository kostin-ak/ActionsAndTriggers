package kostin.ak.actionstriggers.api.provider.impl;

import io.th0rgal.oraxen.api.OraxenBlocks;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.mechanics.Mechanic;
import kostin.ak.actionstriggers.api.provider.BlockProvider;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OraxenBlockProvider implements BlockProvider {

    @Override
    public @NotNull String getNamespace() {
        return "oraxen";
    }

    @Override
    public void setBlock(@NotNull Block block, @NotNull String id) {
        OraxenBlocks.place(id, block.getLocation());
    }

    @Override
    public @Nullable String getId(@NotNull Block block) {
        if (OraxenBlocks.isOraxenBlock(block)) {
            Mechanic mechanic = OraxenBlocks.getOraxenBlock(block.getBlockData());
            if (mechanic != null) {
                return mechanic.getItemID();
            }
            mechanic = OraxenBlocks.getOraxenBlock(block.getLocation());
            if (mechanic != null) {
                return mechanic.getItemID();
            }
            var noteMech = OraxenBlocks.getNoteBlockMechanic(block);
            if (noteMech != null) {
                return noteMech.getItemID();
            }
            var stringMech = OraxenBlocks.getStringMechanic(block);
            if (stringMech != null) {
                return stringMech.getItemID();
            }
        }
        return null;
    }

    @Override
    public @NotNull List<String> getAvailableIds() {
        return new ArrayList<>(OraxenItems.getEntriesAsMap().keySet());
    }
}
