package kostin.ak.actionstriggers.core.defaults.actions;

import kostin.ak.actionstriggers.api.action.AbstractActionFactory;
import kostin.ak.actionstriggers.api.action.Action;
import kostin.ak.actionstriggers.api.action.ActionFactory;
import kostin.ak.actionstriggers.api.action.ActionParameters;
import kostin.ak.actionstriggers.api.context.ExecutionContext;
import kostin.ak.actionstriggers.core.CoreActionKeys;
import kostin.ak.actionstriggers.core.CoreKeys;
import kostin.ak.actionstriggers.core.CoreActionParams;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class GiveItemActionFactory extends AbstractActionFactory {

    private static final NamespacedKey KEY = CoreActionKeys.GIVE_ITEM;

    @Override
    public @NotNull NamespacedKey getKey() {
        return KEY;
    }

    @Override
    protected boolean execute(@NotNull ExecutionContext context, @NotNull ActionParameters params) {
        String materialName = ((String) params.getString(CoreActionParams.MATERIAL, "stone")).toLowerCase();
        int amount = Integer.parseInt(params.getString(CoreActionParams.AMOUNT, "1"));

            Player player = context.get(CoreKeys.PLAYER);
            if (player == null || !player.isOnline()) return false;

            ItemStack itemToGive = null;

            // 1. Проверяем, это Oraxen или ванилла?
            if (materialName.startsWith("oraxen:")) {
                String oraxenId = materialName.substring(7); // отрезаем "oraxen:"
                itemToGive = getOraxenItem(oraxenId);
                if (itemToGive != null) itemToGive.setAmount(amount);
            }
            // 2. Ванильный предмет
            else {
                NamespacedKey matKey = materialName.contains(":") ?
                        NamespacedKey.fromString(materialName) : NamespacedKey.minecraft(materialName);

                if (matKey != null) {
                    Material material = Registry.MATERIAL.get(matKey);
                    if (material != null && material.isItem()) {
                        itemToGive = new ItemStack(material, amount);
                    }
                }
            }

            if (itemToGive != null) {
                player.getInventory().addItem(itemToGive);
                return true;
            }

            return false;
    }

    /**
     * Безопасное получение предмета из Oraxen через Reflection (без хард-зависимости).
     */
    private ItemStack getOraxenItem(String id) {
        if (!Bukkit.getPluginManager().isPluginEnabled("Oraxen")) return null;
        try {
            // Вызываем: OraxenItems.getItemById(id).build();
            Class<?> oraxenItemsClass = Class.forName("io.th0rgal.oraxen.api.OraxenItems");
            Object itemBuilder = oraxenItemsClass.getMethod("getItemById", String.class).invoke(null, id);

            if (itemBuilder != null) {
                return (ItemStack) itemBuilder.getClass().getMethod("build").invoke(itemBuilder);
            }
        } catch (Exception ignored) {
            // Если Oraxen обновит API, мы просто не выдадим предмет, но сервер не упадет
        }
        return null;
    }
}