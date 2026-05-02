package kostin.ak.actionstriggers.api.trigger;

import kostin.ak.actionstriggers.api.ActionTriggerAPI;
import kostin.ak.actionstriggers.api.context.ExecutionContext;
import org.bukkit.NamespacedKey;

public abstract class Trigger {
    public void dispatch(ExecutionContext context){
        ActionTriggerAPI.getTriggers().dispatch(getKey(), context);
    }
    public abstract NamespacedKey getKey();
}
