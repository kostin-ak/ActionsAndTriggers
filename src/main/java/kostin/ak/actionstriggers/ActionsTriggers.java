package kostin.ak.actionstriggers;

import kostin.ak.actionstriggers.api.ActionTriggerAPI;
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
        ActionTriggerAPI.init(actionRegistry, triggerRegistry, actionScheduler);

        // 3. Регистрируем базовые экшены и триггеры

        ActionTriggerAPI.getRegistrar().registerActions(new MessageActionFactory(), new SoundActionFactory(), new TeleportActionFactory(),
                     new GiveItemActionFactory(), new PotionEffectActionFactory(), new DamageActionFactory(), new KillActionFactory(),
                    new CommandActionFactory(), new ParticleActionFactory(), new FireworkActionFactory());

        ActionTriggerAPI.getRegistrar().registerTriggers(this, triggerRegistry,
                new BlockBreakTriggerListener(), new PlayerDamageTriggerListener(), new PlayerJoinTriggerListener(),
                new PlayerInteractTriggerListener(), new EntityDeathTriggerListener(), new PlayerToggleSneakTriggerListener(),
                new BlockPlaceTriggerListener(), new  PlayerConsumeTriggerListener(), new AsyncChatTriggerListener(),
                new PlayerDeathTriggerListener(), new PlayerLevelChangeTriggerListener(), new PlayerQuitTriggerListener());


        // 4. Инициализация Revxrsal Commands
        BukkitCommandHandler handler = BukkitCommandHandler.create(this);
        handler.register(new ActionCommand(actionRegistry, triggerRegistry));


        getLogger().info("Actions&Triggers API успешно загружен!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Actions&Triggers выключен.");
    }
}