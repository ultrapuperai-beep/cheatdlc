package fun.eversense.api.storages;


import fun.eversense.eversense;
import fun.eversense.api.QClient;
import fun.eversense.api.events.EventInvoker;
import fun.eversense.api.storages.implement.*;
import fun.eversense.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.eversense.api.utils.tps.TPSCalc;
import fun.eversense.client.modules.impl.render.TotemAngel;

public class InitializeStorage implements QClient {

    public void onInitialize() {
        EventInvoker.register(this);
        this.initStorages();
    }

    
    public void initStorages() {
        eversense.INSTANCE.moduleStorage = new ModuleStorage();
        eversense.INSTANCE.themeStorage = new ThemeStorage();
        eversense.INSTANCE.tpsCalc = new TPSCalc();
        EventInvoker.register(eversense.INSTANCE.tpsCalc);
        eversense.INSTANCE.localizationStorage = new LocalizationStorage();
        eversense.INSTANCE.freeLookStorage = new FreeLookStorage();
        eversense.INSTANCE.rotationStorage = new RotationStorage();
        // eversense.INSTANCE.serverStorage = new ServerStorage();
        eversense.INSTANCE.friendStorage = new FriendStorage();
        eversense.INSTANCE.macroStorage = new MacroStorage();
        eversense.INSTANCE.staffStorage = new StaffStorage();
        eversense.INSTANCE.waypointStorage = new WaypointStorage();
        eversense.INSTANCE.commandStorage = new CommandStorage();
        eversense.INSTANCE.configStorage = new ConfigStorage();
    }
}
