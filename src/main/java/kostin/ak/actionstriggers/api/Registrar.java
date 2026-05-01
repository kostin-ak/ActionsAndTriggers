package kostin.ak.actionstriggers.api;

import kostin.ak.actionstriggers.api.action.ActionFactory;
import kostin.ak.actionstriggers.api.trigger.Trigger;
import kostin.ak.actionstriggers.api.trigger.TriggerRegistry;
import lombok.Getter;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class Registrar {
    @Getter
    private final static Registrar instance = new Registrar();
    private Registrar() {}

    public void registerActions(ActionFactory ... actions){
        for(ActionFactory action : actions){
            ActionTriggerAPI.getActions().register(action);
        }
    }
    public void registerTriggers(Plugin plugin, TriggerRegistry triggerRegistry, Trigger... triggers){
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        for(Trigger trigger : triggers){
            pluginManager.registerEvents(trigger, plugin);
            triggerRegistry.register(trigger);
        }
    }
}
