package fun.eversense.client.modules.impl.movement;


import fun.eversense.client.modules.Module;
import fun.eversense.client.modules.settings.implement.FloatSetting;

public class Timer extends Module {

    public static Timer INSTANCE = new Timer();

    public FloatSetting speed = new FloatSetting("Скорость", 1.0f, 1.0f, 2.0f, 0.01f);

    public Timer() {
        super("Timer", "Ускоряет время в игре", ModuleCategory.MOVEMENT);
        addSettings(speed);
    }
}
