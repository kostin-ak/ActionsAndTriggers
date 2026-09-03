package kostin.ak.actionstriggers;

import kostin.ak.actionstriggers.api.ActionTriggerAPI;
import kostin.ak.actionstriggers.api.action.ActionRegistry;
import kostin.ak.actionstriggers.api.action.ActionScheduler;
import kostin.ak.actionstriggers.api.filter.FilterRegistry;
import kostin.ak.actionstriggers.api.meta.ActionParameterMeta;
import kostin.ak.actionstriggers.api.provider.impl.*;
import kostin.ak.actionstriggers.api.trigger.TriggerRegistry;
import kostin.ak.actionstriggers.core.config.YamlTriggerLoader;
import kostin.ak.actionstriggers.core.defaults.actions.*;
import kostin.ak.actionstriggers.core.defaults.triggers.*;
import kostin.ak.actionstriggers.core.command.AATTabCompleter;
import kostin.ak.actionstriggers.core.command.ActionCommand;
import kostin.ak.actionstriggers.core.defaults.filters.DefaultFilterParsers;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.bukkit.BukkitCommandHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class ActionsTriggers extends JavaPlugin {

    private static ActionsTriggers instance;

    public static ActionsTriggers getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        kostin.ak.actionstriggers.core.i18n.I18n.init(this);

        ActionRegistry actionRegistry = new ActionRegistry(getLogger());
        TriggerRegistry triggerRegistry = new TriggerRegistry(getLogger());
        FilterRegistry filterRegistry = new FilterRegistry(getLogger());
        ActionScheduler actionScheduler = new ActionScheduler(this);

        ActionTriggerAPI.init(actionRegistry, triggerRegistry, filterRegistry, actionScheduler);
        kostin.ak.actionstriggers.core.ContextPlaceholderParser.init();
        ActionTriggerAPI.getRegistrar().registerActions(DefaultActionParsers.class);

        ActionTriggerAPI.getRegistrar().registerTriggers(this,
                new BlockBreakTrigger(), new PlayerDamageTrigger(), new PlayerJoinTrigger(),
                new PlayerInteractTrigger(), new EntityDeathTrigger(), new PlayerToggleSneakTrigger(),
                new BlockPlaceTrigger(), new PlayerConsumeTrigger(), new AsyncChatTrigger(),
                new PlayerDeathTrigger(), new PlayerLevelChangeTrigger(), new PlayerQuitTrigger(),
                new CraftItemTrigger(), new PlayerJumpTrigger(), new PlayerSwapHandItemsTrigger(),
                new PlayerDropItemTrigger(), new BlockDamageTrigger(), new PlayerAdvancementDoneTrigger(),
                new PlayerToggleFlightTrigger(), new PlayerWorldChangeTrigger());

        periodicTrigger = new kostin.ak.actionstriggers.core.defaults.triggers.PeriodicIntervalTrigger();
        periodicTrigger.start(this);
        ActionTriggerAPI.getRegistrar().registerTriggers(this, periodicTrigger);

        ActionTriggerAPI.getRegistrar().registerFilters(DefaultFilterParsers.class);

        ActionTriggerAPI.getRegistrar().registerItemProvider(new VanillaItemProvider());
        if (Bukkit.getPluginManager().getPlugin("Oraxen") != null) {
            ActionTriggerAPI.getItems().register(new OraxenItemProvider());
            ActionTriggerAPI.getBlocks().register(new OraxenBlockProvider());
            getLogger().info("Oraxen integration enabled!");
        }


        // Конфигурационный провайдер для виджетов GUI
        kostin.ak.actionstriggers.api.gui.widget.impl.SlotCoverWidget.setConfigProvider(key -> getConfig().getString(key));

        // Интеграция с ItemsAdder
        if (Bukkit.getPluginManager().getPlugin("ItemsAdder") != null) {
            ActionTriggerAPI.getItems().register(new ItemsAdderItemProvider());
            ActionTriggerAPI.getBlocks().register(new ItemsAdderBlockProvider());
            getLogger().info("ItemsAdder integration enabled!");
        }

        // Интеграция с LuckPerms
        kostin.ak.actionstriggers.core.hook.LuckPermsHook.initialize();
        if (kostin.ak.actionstriggers.core.hook.LuckPermsHook.isEnabled()) {
            getLogger().info("LuckPerms integration enabled!");
        }

        // 4. Инициализация Revxrsal Commands и продвинутого динамического автокомплита
        AATTabCompleter tabCompleter = new AATTabCompleter(GUI_REGISTRY);
        getServer().getPluginManager().registerEvents(tabCompleter, this);

        CommandHandler handler = BukkitCommandHandler.create(this);
        handler.getAutoCompleter().registerSuggestion("actions", (args, sender, cmd) -> ActionTriggerAPI.getActions().asList());
        handler.getAutoCompleter().registerSuggestion("triggers", (args, sender, cmd) -> ActionTriggerAPI.getTriggers().asList());
        handler.getAutoCompleter().registerSuggestion("guis", (args, sender, cmd) -> GUI_REGISTRY.getAvailableIds());
        handler.getAutoCompleter().registerSuggestion("get", (args, sender, cmd) -> List.of("params"));
        handler.getAutoCompleter().registerSuggestion("action_args", (args, sender, cmd) -> tabCompleter.complete(args.toArray(new String[0])));
        handler.getAutoCompleter().registerSuggestion("trigger_args", (args, sender, cmd) -> tabCompleter.complete(args.toArray(new String[0])));

        handler.register(new ActionCommand(this));

        YamlTriggerLoader.load(this, "triggers");

        getServer().getPluginManager().registerEvents(new kostin.ak.actionstriggers.core.gui.GuiListener(GUI_REGISTRY), this);
        kostin.ak.actionstriggers.core.gui.YamlGuiLoader guiLoader = new kostin.ak.actionstriggers.core.gui.YamlGuiLoader(GUI_REGISTRY, getLogger());
        guiLoader.loadAll(this, "guis");

        getServer().getPluginManager().registerEvents(new kostin.ak.actionstriggers.core.combat.CombatListener(COMBAT_TRACKER), this);

        getLogger().info("Actions&Triggers initialized successfully.");
    }

    private static final kostin.ak.actionstriggers.core.gui.GuiRegistry GUI_REGISTRY = new kostin.ak.actionstriggers.core.gui.GuiRegistry();
    private static final kostin.ak.actionstriggers.core.combat.CombatTracker COMBAT_TRACKER = new kostin.ak.actionstriggers.core.combat.CombatTracker();

    public static kostin.ak.actionstriggers.core.gui.GuiRegistry getGuiRegistry() {
        return GUI_REGISTRY;
    }

    public static kostin.ak.actionstriggers.core.combat.CombatTracker getCombatTracker() {
        return COMBAT_TRACKER;
    }

    private kostin.ak.actionstriggers.core.defaults.triggers.PeriodicIntervalTrigger periodicTrigger;

    @Override
    public void onDisable() {
        if (periodicTrigger != null) {
            periodicTrigger.stop();
        }
        Bukkit.getScheduler().cancelTasks(this);
        ActionTriggerAPI.getScripts().clear();
        ActionTriggerAPI.getTriggers().unsubscribeAll(this);
        GUI_REGISTRY.clear();
        getLogger().info("Actions&Triggers disabled cleanly.");
    }
}