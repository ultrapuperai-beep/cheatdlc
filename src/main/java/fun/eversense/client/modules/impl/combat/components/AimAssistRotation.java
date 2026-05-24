package fun.eversense.client.modules.impl.combat.components;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class AimAssistRotation {
    
    private float currentYaw;
    private float currentPitch;
    private float targetYaw;
    private float targetPitch;

    private float smoothness = 0.3f;
    private float acceleration = 1.2f;
    private float minRotation = 0.01f;
    
    public AimAssistRotation() {
        this.currentYaw = 0;
        this.currentPitch = 0;
    }

    public void updateCurrent(float yaw, float pitch) {
        this.currentYaw = yaw;
        this.currentPitch = pitch;
    }
    

    public float[] calculateSmoothRotation(LivingEntity target, Vec3d eyePos, float yawSpeed, float pitchSpeed) {
        Vec3d targetPos = target.getPos().add(0, target.getEyeHeight(target.getPose()) * 0.9, 0);
        float[] targetAngles = calculateAngles(eyePos, targetPos);
        this.targetYaw = targetAngles[0];
        this.targetPitch = targetAngles[1];

        float yawDiff = Math.abs(MathHelper.wrapDegrees(targetYaw - currentYaw));
        float pitchDiff = Math.abs(targetPitch - currentPitch);

        float dynamicSmoothness = calculateDynamicSmoothness(yawDiff, pitchDiff);
        float newYaw = lerpAngle(currentYaw, targetYaw, dynamicSmoothness * (yawSpeed / 100.0f));
        float newPitch = lerpAngle(currentPitch, targetPitch, dynamicSmoothness * (pitchSpeed / 100.0f));

        this.currentYaw = newYaw;
        this.currentPitch = MathHelper.clamp(newPitch, -90.0f, 90.0f);
        
        return new float[]{this.currentYaw, this.currentPitch};
    }
    

    private float[] calculateAngles(Vec3d from, Vec3d to) {
        double x = to.x - from.x;
        double y = to.y - from.y;
        double z = to.z - from.z;
        
        double dist = Math.sqrt(x * x + z * z);
        float yaw = (float) (Math.toDegrees(Math.atan2(z, x)) - 90.0);
        float pitch = (float) -Math.toDegrees(Math.atan2(y, dist));
        
        return new float[]{yaw, pitch};
    }
    

    private float calculateDynamicSmoothness(float yawDiff, float pitchDiff) {
        float totalDiff = (float) Math.sqrt(yawDiff * yawDiff + pitchDiff * pitchDiff);

        if (totalDiff > 30.0f) {
            return smoothness * acceleration;
        } else if (totalDiff > 10.0f) {
            return smoothness * 1.5f;
        }
        
        return smoothness;
    }

    private float lerpAngle(float current, float target, float factor) {
        float delta = MathHelper.wrapDegrees(target - current);
        float rotation = delta * factor;

        if (Math.abs(rotation) < minRotation && Math.abs(delta) > 0.1f) {
            rotation = Math.copySign(minRotation, rotation);
        }
        
        return current + rotation;
    }

    public float getAngleDistance(float angle1, float angle2) {
        return Math.abs(MathHelper.wrapDegrees(angle2 - angle1));
    }

    public boolean isOnTarget(float threshold) {
        float yawDiff = getAngleDistance(currentYaw, targetYaw);
        float pitchDiff = Math.abs(currentPitch - targetPitch);
        return yawDiff <= threshold && pitchDiff <= threshold;
    }

    
    public float getCurrentYaw() {
        return currentYaw;
    }
    
    public float getCurrentPitch() {
        return currentPitch;
    }
    
    public float getTargetYaw() {
        return targetYaw;
    }
    
    public float getTargetPitch() {
        return targetPitch;
    }
    
    public void setSmoothness(float smoothness) {
        this.smoothness = MathHelper.clamp(smoothness, 0.05f, 1.0f);
    }
    
    public void setAcceleration(float acceleration) {
        this.acceleration = MathHelper.clamp(acceleration, 1.0f, 2.0f);
    }
    
    public void setMinRotation(float minRotation) {
        this.minRotation = minRotation;
    }
    
    public void reset() {
        this.currentYaw = 0;
        this.currentPitch = 0;
        this.targetYaw = 0;
        this.targetPitch = 0;
    }
}
