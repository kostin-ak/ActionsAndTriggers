package kostin.ak.actionstriggers;

import kostin.ak.actionstriggers.api.ActionAPI;
import kostin.ak.actionstriggers.api.action.ActionRegistry;
import kostin.ak.actionstriggers.api.action.ActionScheduler;
import kostin.ak.actionstriggers.api.trigger.TriggerRegistry;
import kostin.ak.actionstriggers.core.defaults.actions.*;
import kostin.ak.actionstriggers.core.defaults.triggers.*;
import kostin.ak.actionstriggers.core.command.ActionCommand;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.bukkit.BukkitCommandHandler;

public final class ActionsTriggers extends JavaPlugin {

    @Override
    public void onEnable() {
        // 1. Инициализируем реестры и шедулер
        ActionRegistry actionRegistry = new ActionRegistry(getLogger());
        TriggerRegistry triggerRegistry = new TriggerRegistry(getLogger());
        ActionScheduler actionScheduler = new ActionScheduler(this);

        // 2. Инжектим их в публичный фасад API
        ActionAPI.init(actionRegistry, triggerRegistry, actionScheduler);

        // 3. Регистрируем базовые экшены и триггеры
        ActionAPI.getActions().register(new MessageActionFactory());
        ActionAPI.getActions().register(new SoundActionFactory());
        ActionAPI.getActions().register(new TeleportActionFactory());
        ActionAPI.getActions().register(new GiveItemActionFactory());
        ActionAPI.getActions().register(new PotionEffectActionFactory());
        ActionAPI.getActions().register(new DamageActionFactory());
        ActionAPI.getActions().register(new KillActionFactory());
        ActionAPI.getActions().register(new CommandActionFactory());
        ActionAPI.getActions().register(new ParticleActionFactory());
        ActionAPI.getActions().register(new FireworkActionFactory());

        getServer().getPluginManager().registerEvents(new BlockBreakTriggerListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerDamageTriggerListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinTriggerListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractTriggerListener(), this);
        getServer().getPluginManager().registerEvents(new EntityDeathTriggerListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerToggleSneakTriggerListener(), this);
        getServer().getPluginManager().registerEvents(new BlockPlaceTriggerListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerConsumeTriggerListener(), this);
        getServer().getPluginManager().registerEvents(new AsyncChatTriggerListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathTriggerListener(), this);

        // 4. Инициализация Revxrsal Commands
        BukkitCommandHandler handler = BukkitCommandHandler.create(this);
        handler.register(new ActionCommand());


        getLogger().info("Actions&Triggers API успешно загружен!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Actions&Triggers выключен.");
    }
}