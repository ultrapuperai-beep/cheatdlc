package fun.eversense.client.modules.impl.combat.components.rotations;

import fun.eversense.client.modules.impl.combat.components.RotationsSystem;
import fun.eversense.client.modules.settings.implement.FloatSetting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import static net.minecraft.util.math.MathHelper.wrapDegrees;


public class AimAssistRotation extends RotationsSystem {

    public final FloatSetting smoothness = new FloatSetting("Плавность", 0.3f, 0.05f, 1.0f, 0.05f);
    public final FloatSetting acceleration = new FloatSetting("Ускорение", 1.2f, 1.0f, 2.0f, 0.1f);
    public final FloatSetting yawSpeed = new FloatSetting("Скорость Yaw", 30.0f, 5.0f, 100.0f, 1.0f);
    public final FloatSetting pitchSpeed = new FloatSetting("Скорость Pitch", 15.0f, 5.0f, 100.0f, 1.0f);

    private float lastYaw = 0f;
    private float lastPitch = 0f;
    private float targetYaw = 0f;
    private float targetPitch = 0f;
    
    @Override
    public void updateRotations(LivingEntity target) {
        if (mc.player == null) return;

        if (lastYaw == 0f && lastPitch == 0f) {
            lastYaw = mc.player.getYaw();
            lastPitch = mc.player.getPitch();
        }

        Vec3d eyePos = mc.player.getEyePos();
        Vec3d targetPos = target.getPos().add(0, target.getEyeHeight(target.getPose()) * 0.9, 0);
        
        double x = targetPos.x - eyePos.x;
        double y = targetPos.y - eyePos.y;
        double z = targetPos.z - eyePos.z;
        
        double dist = Math.sqrt(x * x + z * z);
        targetYaw = (float) (Math.toDegrees(Math.atan2(z, x)) - 90.0);
        targetPitch = (float) -Math.toDegrees(Math.atan2(y, dist));

        float yawDiff = Math.abs(wrapDegrees(targetYaw - lastYaw));
        float pitchDiff = Math.abs(targetPitch - lastPitch);

        float dynamicSmoothness = calculateDynamicSmoothness(yawDiff, pitchDiff);

        float yawFactor = dynamicSmoothness * (yawSpeed.getValue().floatValue() / 100.0f);
        float pitchFactor = dynamicSmoothness * (pitchSpeed.getValue().floatValue() / 100.0f);
        
        float newYaw = lerpAngle(lastYaw, targetYaw, yawFactor);
        float newPitch = lerpAngle(lastPitch, targetPitch, pitchFactor);

        float gcd = getGCD();
        newYaw -= newYaw % gcd;
        newPitch -= newPitch % gcd;

        lastYaw = newYaw;
        lastPitch = MathHelper.clamp(newPitch, -90.0f, 90.0f);

        fun.eversense.api.storages.implement.RotationStorage.update(
            new fun.eversense.api.utils.rotate.Rotation(lastYaw, lastPitch),
            180, 180, 120, 120, 1, 1,
            fun.eversense.client.modules.impl.combat.Aura.clientLook.isState()
        );
    }
    

    private float calculateDynamicSmoothness(float yawDiff, float pitchDiff) {
        float baseSmoothness = smoothness.getValue().floatValue();
        float totalDiff = (float) Math.sqrt(yawDiff * yawDiff + pitchDiff * pitchDiff);

        if (totalDiff > 30.0f) {
            return baseSmoothness * acceleration.getValue().floatValue();
        } else if (totalDiff > 10.0f) {
            return baseSmoothness * 1.5f;
        }
        
        return baseSmoothness;
    }
    

    private float lerpAngle(float current, float target, float factor) {
        float delta = wrapDegrees(target - current);
        float rotation = delta * factor;

        float minRotation = 0.01f;
        if (Math.abs(rotation) < minRotation && Math.abs(delta) > 0.1f) {
            rotation = Math.copySign(minRotation, rotation);
        }
        
        return current + rotation;
    }
    

    private float getGCD() {
        float sensitivity = mc.options.getMouseSensitivity().getValue().floatValue();
        float gcd = sensitivity * 0.6f + 0.2f;
        return gcd * gcd * gcd * 1.2f;
    }
    
    public void reset() {
        lastYaw = 0f;
        lastPitch = 0f;
        targetYaw = 0f;
        targetPitch = 0f;
    }
    
    public void onAttack() {
    }
}
