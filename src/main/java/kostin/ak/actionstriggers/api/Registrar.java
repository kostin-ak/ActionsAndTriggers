package kostin.ak.actionstriggers.api;

import kostin.ak.actionstriggers.api.action.ActionFactory;
import kostin.ak.actionstriggers.api.action.ActionRegistry;
import kostin.ak.actionstriggers.api.action.IActionParsers;
import kostin.ak.actionstriggers.api.filter.FilterRegistry;
import kostin.ak.actionstriggers.api.filter.IFilterParsers;
import kostin.ak.actionstriggers.api.trigger.Trigger;
import kostin.ak.actionstriggers.api.trigger.TriggerRegistry;
import lombok.Getter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class Registrar {
    @Getter
    private final static Registrar instance = new Registrar();
    private Registrar() {}

    public <T extends IActionParsers> void registerActions(Class<T> clazz){
        /*for(ActionFactory action : actions){
            ActionTriggerAPI.getActions().register(action);
        }*/
        ActionTriggerAPI.getActions().scanAndRegister(clazz);
    }
    public void registerTriggers(Plugin plugin, Trigger ... triggers){
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        for(Trigger trigger : triggers){
            if (trigger instanceof Listener) {
                pluginManager.registerEvents((Listener) trigger, plugin);
            };
            ActionTriggerAPI.getTriggers().register(trigger);
        }
    }

    public <T extends IFilterParsers> void registerFilters(Class<T> clazz){
        ActionTriggerAPI.getFilters().scanAndRegister(clazz);
    }

}
