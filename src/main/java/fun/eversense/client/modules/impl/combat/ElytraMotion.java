package fun.eversense.client.modules.impl.combat;

import net.minecraft.util.math.Vec3d;
import fun.eversense.api.events.EventLink;
import fun.eversense.api.events.implement.EventMove;
import fun.eversense.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.eversense.client.modules.Module;
import fun.eversense.client.modules.settings.implement.FloatSetting;

public class ElytraMotion extends Module {

    public static ElytraMotion INSTANCE = new ElytraMotion();

    public FloatSetting distance = new FloatSetting("Дистанция до игрока", 3, 0, 6, 0.1f);
    
    public ElytraMotion() {
        super("ElytraMotion", "Зависает рядом с игроком на эликах", ModuleCategory.COMBAT);
        addSettings(distance);
    }
    @EventLink
    public void onMove(EventMove e) {
        if (!isEnable()) return;

        Aura aura = ModuleClass.aura;
        if (mc.player == null || mc.world == null || aura.getTarget() == null) return;
        if (mc.player.isGliding() && mc.player.distanceTo(aura.getTarget()) < distance.getValue().floatValue()) {
            e.setMovePos(Vec3d.ZERO);
        }
    }
    @Override
    public void onDisable() {}
}
