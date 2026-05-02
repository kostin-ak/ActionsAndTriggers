package kostin.ak.actionstriggers;

import kostin.ak.actionstriggers.api.ActionTriggerAPI;
import kostin.ak.actionstriggers.api.action.ActionRegistry;
import kostin.ak.actionstriggers.api.action.ActionScheduler;
import kostin.ak.actionstriggers.api.filter.FilterRegistry;
import kostin.ak.actionstriggers.api.trigger.TriggerRegistry;
import kostin.ak.actionstriggers.core.defaults.actions.*;
import kostin.ak.actionstriggers.core.defaults.triggers.*;
import kostin.ak.actionstriggers.core.command.ActionCommand;
import kostin.ak.actionstriggers.core.defaults.filters.DefaultFilterParsers;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.bukkit.BukkitCommandHandler;

public final class ActionsTriggers extends JavaPlugin {

    @Override
    public void onEnable() {
        // 1. Инициализируем реестры и шедулер
        ActionRegistry actionRegistry = new ActionRegistry(getLogger());
        TriggerRegistry triggerRegistry = new TriggerRegistry(getLogger());
        FilterRegistry filterRegistry = new FilterRegistry(getLogger());
        ActionScheduler actionScheduler = new ActionScheduler(this);


        // 2. Инжектим их в публичный фасад API
        ActionTriggerAPI.init(actionRegistry, triggerRegistry, filterRegistry, actionScheduler);

        // 3. Регистрируем базовые экшены и триггеры

        ActionTriggerAPI.getRegistrar().registerActions(DefaultActionParsers.class);

        ActionTriggerAPI.getRegistrar().registerTriggers(this,
                new BlockBreakTrigger(), new PlayerDamageTrigger(), new PlayerJoinTrigger(),
                new PlayerInteractTrigger(), new EntityDeathTrigger(), new PlayerToggleSneakTrigger(),
                new BlockPlaceTrigger(), new PlayerConsumeTrigger(), new AsyncChatTrigger(),
                new PlayerDeathTrigger(), new PlayerLevelChangeTrigger(), new PlayerQuitTrigger(),
                new CraftItemTrigger(),new PlayerJumpTrigger(), new PlayerSwapHandItemsTrigger(),
                new PlayerDropItemTrigger(), new BlockDamageTrigger(), new PlayerAdvancementDoneTrigger(),
                new PlayerToggleFlightTrigger());

        ActionTriggerAPI.getRegistrar().registerFilters(DefaultFilterParsers.class);


        // 4. Инициализация Revxrsal Commands
        BukkitCommandHandler handler = BukkitCommandHandler.create(this);
        handler.register(new ActionCommand(actionRegistry, triggerRegistry, filterRegistry));

       ThirdPartyShowcase.loadShowcase(this);


        getLogger().info("Actions&Triggers API успешно загружен!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Actions&Triggers выключен.");
    }
}