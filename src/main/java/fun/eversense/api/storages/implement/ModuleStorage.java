package fun.eversense.api.storages.implement;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import fun.eversense.api.QClient;
import fun.eversense.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.eversense.client.modules.Module;
import fun.eversense.client.modules.impl.combat.*;
import fun.eversense.client.modules.impl.misc.*;
import fun.eversense.client.modules.impl.movement.*;
import fun.eversense.client.modules.impl.player.*;
import fun.eversense.client.modules.impl.render.*;
import java.util.Arrays;

@Getter
@Setter
public class ModuleStorage implements QClient {

    public ModuleStorage() {
        this.initModules();
    }

    private void initModules() {
        ModuleClass.INSTANCE.initialize();
    }
}
