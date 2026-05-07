package kostin.ak.actionstriggers;

import kostin.ak.actionstriggers.api.ActionTriggerAPI;
import kostin.ak.actionstriggers.api.action.ActionRegistry;
import kostin.ak.actionstriggers.api.action.ActionScheduler;
import kostin.ak.actionstriggers.api.context.ContextKey;
import kostin.ak.actionstriggers.api.filter.FilterRegistry;
import kostin.ak.actionstriggers.api.meta.ActionParameterMeta;
import kostin.ak.actionstriggers.api.provider.impl.OraxenBlockProvider;
import kostin.ak.actionstriggers.api.provider.impl.OraxenItemProvider;
import kostin.ak.actionstriggers.api.provider.impl.VanillaItemProvider;
import kostin.ak.actionstriggers.api.trigger.TriggerRegistry;
import kostin.ak.actionstriggers.core.config.YamlTriggerLoader;
import kostin.ak.actionstriggers.core.defaults.actions.*;
import kostin.ak.actionstriggers.core.defaults.triggers.*;
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

        ActionTriggerAPI.getRegistrar().registerItemProvider(new VanillaItemProvider());
        if (Bukkit.getPluginManager().getPlugin("Oraxen") != null) {
            ActionTriggerAPI.getItems().register(new OraxenItemProvider());
            ActionTriggerAPI.getBlocks().register(new OraxenBlockProvider());
            getLogger().info("Oraxen integration enabled!");
        }

        // 4. Инициализация Revxrsal Commands
        CommandHandler handler = BukkitCommandHandler.create(this);

// 1. Подсказки ID
        handler.getAutoCompleter().registerSuggestion("actions", (args, sender, command) -> ActionTriggerAPI.getActions().asList());
        handler.getAutoCompleter().registerSuggestion("triggers", (args, sender, command) -> ActionTriggerAPI.getTriggers().asList());

// 2. Подсказки аргументов для Экшенов (/aat run ...)
        handler.getAutoCompleter().registerSuggestion("action_args", (args, sender, command) -> {
            if (args.size() < 3) return Collections.emptyList();
            NamespacedKey actionKey = NamespacedKey.fromString(args.get(2));
            if (actionKey == null) return Collections.emptyList();

            List<ActionParameterMeta> metadata = ActionTriggerAPI.getActions().getMetadata(actionKey);
            List<String> suggestions = new ArrayList<>();

            if (args.size() == 3 || (args.size() == 4 && args.get(3).isEmpty())) {
                suggestions.add("args={"); suggestions.add("context={");
                return suggestions;
            }
            for (ActionParameterMeta meta : metadata) {
                if (!String.join(" ", args).contains(meta.getKey() + "=")) {
                    suggestions.add(meta.getKey() + "=");
                }
            }
            return suggestions;
        });

// 3. НОВОЕ: Подсказки аргументов для Триггеров (/aat trigger ...)
        handler.getAutoCompleter().registerSuggestion("trigger_args", (args, sender, command) -> {
            if (args.size() < 3) return Collections.emptyList();
            NamespacedKey triggerKey = NamespacedKey.fromString(args.get(2));
            if (triggerKey == null) return Collections.emptyList();

            // Достаем поставляемый контекст этого триггера
            List<ContextKey<?>> ctxList = ActionTriggerAPI.getTriggers().getProvidedContext(triggerKey);
            List<String> suggestions = new ArrayList<>();

            if (args.size() == 3 || (args.size() == 4 && args.get(3).isEmpty())) {
                suggestions.add("context={");
                return suggestions;
            }

            for (ContextKey<?> ctxKey : ctxList) {
                if (!String.join(" ", args).contains(ctxKey.getId() + "=")) {
                    suggestions.add(ctxKey.getId() + "=");
                }
            }
            return suggestions;
        });

        handler.register(new ActionCommand(this));

       //ThirdPartyShowcase.loadShowcase(this);


        // Пытаемся загрузить триггеры из папки "triggers" (если она существует)
        YamlTriggerLoader.load(this, "triggers");

        getLogger().info("Actions&Triggers API успешно загружен!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Actions&Triggers выключен.");
    }
}