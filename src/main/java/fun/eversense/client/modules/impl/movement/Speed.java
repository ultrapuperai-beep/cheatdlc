package fun.eversense.client.modules.impl.movement;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import fun.eversense.eversense;
import fun.eversense.api.QClient;
import fun.eversense.api.events.EventLink;
import fun.eversense.api.events.implement.EventUpdate;
import fun.eversense.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.eversense.api.utils.combat.PredictUtils;
import fun.eversense.client.modules.Module;
import fun.eversense.client.modules.impl.combat.Aura;
import fun.eversense.client.modules.settings.implement.BooleanSetting;
import fun.eversense.client.modules.settings.implement.FloatSetting;
import fun.eversense.client.modules.settings.implement.ModeSetting;

public class Speed extends Module implements QClient {

    public static Speed INSTANCE = new Speed();

    private final ModeSetting mode = new ModeSetting("Режим", "Collision", "Collision", "Damage Boost");
    private final FloatSetting speed = new FloatSetting("Скорость", 1.0f, 0.1f, 2.0f, 0.01f);
    private final FloatSetting radius = new FloatSetting("Радиус", 1.0f, 0.01f, 3.0f, 0.1f)
            .visible(() -> mode.is("Collision"));
    private final FloatSetting predict = new FloatSetting("Предикт", 1.0f, 0.0f, 5.0f, 0.1f)
            .visible(() -> mode.is("Collision"));
    private final BooleanSetting onlyElytra = new BooleanSetting("Только на элитре", false)
            .visible(() -> mode.is("Collision"));
    private final FloatSetting damageMultiplier = new FloatSetting("Множитель урона", 1.5f, 1.0f, 3.0f, 0.1f)
            .visible(() -> mode.is("Damage Boost"));
    private final BooleanSetting onlyAura = new BooleanSetting("Только с аурой", true)
            .visible(() -> mode.is("Damage Boost"));

    public Speed() {
        super("Speed", "Дополнительное ускорение", ModuleCategory.MOVEMENT);
        addSettings(mode, speed, radius, predict, onlyElytra, damageMultiplier, onlyAura);
    }

    @EventLink
    private void onUpdate(EventUpdate event) {
        if (mc.player == null || mc.world == null) return;

        if (mode.is("Collision")) {
            collisionSpeed();
        } else if (mode.is("Damage Boost")) {
            damageBoostSpeed();
        }
    }

    private void damageBoostSpeed() {
        // Проверяем, получил ли игрок урон недавно
        if (mc.player.hurtTime <= 0) return;

        // Проверяем, включена ли аура (если требуется)
        if (onlyAura.isState()) {
            Aura aura = ModuleClass.INSTANCE.aura;
            if (aura == null || !aura.isEnable()) return;
            
            LivingEntity target = aura.getTarget();
            if (target == null || target == mc.player) return;
        }

        // Проверяем, что игрок движется
        if (mc.player.input.movementForward == 0 && mc.player.input.movementSideways == 0) return;

        // Вычисляем множитель на основе hurtTime (чем больше урона, тем больше буст)
        // hurtTime максимум 10 тиков после получения урона
        float hurtProgress = mc.player.hurtTime / 10.0f;
        double multiplier = 1.0 + (hurtProgress * (damageMultiplier.getValue().floatValue() - 1.0f));

        // Получаем текущую скорость
        Vec3d velocity = mc.player.getVelocity();
        
        // Вычисляем направление движения
        float yaw = mc.player.getYaw();
        float forward = mc.player.input.movementForward;
        float strafe = mc.player.input.movementSideways;
        
        // Нормализуем направление
        float length = (float) Math.sqrt(forward * forward + strafe * strafe);
        if (length < 0.01f) return;
        
        forward /= length;
        strafe /= length;
        
        // Вычисляем угол движения
        double angle = Math.atan2(strafe, forward);
        double moveYaw = Math.toRadians(yaw) + angle;
        
        // Базовая скорость с учетом множителя
        double baseSpeed = 0.2873 * speed.getValue().doubleValue() * multiplier;
        
        // Применяем ускорение в направлении движения
        double motionX = -Math.sin(moveYaw) * baseSpeed;
        double motionZ = Math.cos(moveYaw) * baseSpeed;
        
        // Устанавливаем новую скорость (сохраняем Y для прыжков)
        mc.player.setVelocity(motionX, velocity.y, motionZ);
    }

    private void collisionSpeed() {
        Aura aura = ModuleClass.INSTANCE.aura;
        if (aura == null || !aura.isEnable()) return;

        LivingEntity target = aura.getTarget();
        if (target == null || target == mc.player) return;

        if (onlyElytra.isState() && !mc.player.isGliding()) return;

        Box expandedBox = mc.player.getBoundingBox().expand(radius.getValue().doubleValue());

        boolean canSpeed = false;

        if (mc.player.isGliding() || target.getBoundingBox().intersects(expandedBox)) {
            if (mc.player.isGliding()) {
                Vec3d predictedPos = PredictUtils.predict(target, target.getPos(), predict.getValue().intValue());
                double distanceToPredict = mc.player.getEyePos().distanceTo(predictedPos);
                double distanceToTarget = mc.player.getEyePos().distanceTo(target.getBoundingBox().getCenter());

                if (distanceToPredict <= 2.5D || distanceToTarget <= 2.5D) {
                    canSpeed = true;
                }
            } else {
                canSpeed = true;
            }
        }

        if (canSpeed) {
            Vec3d newVelocity = calculateVelocity(target);
            mc.player.setVelocity(newVelocity);
        }
    }

    
    @NotNull
    private Vec3d calculateVelocity(LivingEntity target) {
        double deltaX;
        double deltaZ;

        Vec3d predictedPos = PredictUtils.predict(target, target.getPos(), predict.getValue().intValue());
        deltaX = predictedPos.x - mc.player.getX();
        deltaZ = predictedPos.z - mc.player.getZ();

        float targetYaw = (float)(Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0D);
        double radYaw = Math.toRadians(targetYaw);

        double force = 0.072D * speed.getValue().doubleValue();

        Vec3d currentVelocity = mc.player.getVelocity();

        return new Vec3d(
                currentVelocity.x + -Math.sin(radYaw) * force,
                currentVelocity.y,
                currentVelocity.z + Math.cos(radYaw) * force
        );
    }
}
