package fun.eversense.client.modules.impl.combat.components.rotations;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import fun.eversense.api.QClient;
import fun.eversense.api.storages.implement.RotationStorage;
import fun.eversense.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.eversense.api.utils.combat.PredictUtils;
import fun.eversense.api.utils.rotate.MultipointUtils;
import fun.eversense.api.utils.rotate.Rotation;
import fun.eversense.api.utils.rotate.RotationUtils;
import fun.eversense.client.modules.impl.combat.Aura;
import fun.eversense.client.modules.impl.combat.components.RotationsSystem;
import fun.eversense.client.modules.impl.combat.components.gcd.GCDUtil;
import fun.eversense.client.modules.impl.combat.components.interpolation.BestPoint;

public class GazanRotation extends RotationsSystem implements QClient {

    private float lastYaw;
    private float lastPitch;
    private float speedAcceleration;
    private boolean back;
    private int ticksToAttack;
    private boolean isTurnaroundActive;

    private Vec3d currentMultipoint;
    private long lastMultipointSwitchTime;
    private static final long MULTIPOINT_SWITCH_INTERVAL = 150;
    private int multipointIndex;

    public GazanRotation() {
        if (mc.player != null) {
            this.lastYaw = mc.player.getYaw();
            this.lastPitch = mc.player.getPitch();
        }
        this.speedAcceleration = 0.0F;
        this.back = false;
        this.ticksToAttack = 0;
        this.isTurnaroundActive = false;
        this.currentMultipoint = null;
        this.lastMultipointSwitchTime = 0;
        this.multipointIndex = 0;
    }

    public void reset() {
        if (mc.player != null) {
            this.lastYaw = mc.player.getYaw();
            this.lastPitch = mc.player.getPitch();
        }
        this.speedAcceleration = 0.0F;
        this.back = false;
        this.ticksToAttack = 0;
        this.isTurnaroundActive = false;
        this.currentMultipoint = null;
        this.lastMultipointSwitchTime = 0;
        this.multipointIndex = 0;
    }
    
    public void onAttack() {
        this.lastMultipointSwitchTime = System.currentTimeMillis();
        this.multipointIndex++;
    }

    private Vec3d[] generateMultipoints(LivingEntity target) {
        Box box = target.getBoundingBox();
        double centerX = (box.minX + box.maxX) / 2.0;
        double centerY = (box.minY + box.maxY) / 2.0;
        double centerZ = (box.minZ + box.maxZ) / 2.0;
        
        double width = box.maxX - box.minX;
        double height = box.maxY - box.minY;
        double depth = box.maxZ - box.minZ;

        return new Vec3d[] {
            new Vec3d(centerX, centerY + height * 0.3, centerZ), // центр верх
            new Vec3d(box.minX + width * 0.2, centerY, centerZ), // лево
            new Vec3d(box.maxX - width * 0.2, centerY, centerZ), // право
            new Vec3d(centerX, centerY, box.minZ + depth * 0.2), // перед
            new Vec3d(centerX, centerY, box.maxZ - depth * 0.2), // зад
            new Vec3d(centerX, centerY - height * 0.2, centerZ), // центр низ
            new Vec3d(box.minX + width * 0.3, centerY + height * 0.2, box.minZ + depth * 0.3), // диагональ 1
            new Vec3d(box.maxX - width * 0.3, centerY + height * 0.2, box.maxZ - depth * 0.3)  // диагональ 2
        };
    }

    @Override
    public void updateRotations(LivingEntity target) {
        if (target != null) {
            long currentTime = System.currentTimeMillis();
            double randomFactor = Math.random();
            double timeSineFactor = Math.sin((double)currentTime / 1000.0) * Math.cos((double)currentTime / 450.0);
            Vec3d playerVelocity = this.mc.player.getVelocity();
            boolean isMoving = this.mc.player.sidewaysSpeed != 0.0F || this.mc.player.forwardSpeed != 0.0F;

            Vec3d targetPoint;
            
            if (this.mc.player.isGliding() && target.isGliding()) {
                Vec3d interpolatedRotation = Vec3d.fromPolar(target.getLerpTargetPitch(), target.getLerpTargetYaw());
                Vec3d rotationVector = target.getRotationVector();
                Vec3d relativePos = target.getPos().add(0, target.getHeight() * 0.6f, 0).subtract(mc.player.getEyePos());
                Vec3d blendedDirection = interpolatedRotation.normalize().lerp(rotationVector, interpolatedRotation.length());

                if (ModuleClass.forward != null && ModuleClass.forward.isEnable()) {
                    relativePos = relativePos.add(blendedDirection.normalize().multiply(ModuleClass.forward.forward.getValue().floatValue()));
                }

                targetPoint = mc.player.getEyePos().add(relativePos);
            } else {

                if (this.currentMultipoint == null || 
                    (currentTime - this.lastMultipointSwitchTime) > MULTIPOINT_SWITCH_INTERVAL) {
                    
                    Vec3d[] multipoints = generateMultipoints(target);
                    this.currentMultipoint = multipoints[this.multipointIndex % multipoints.length];
                    this.lastMultipointSwitchTime = currentTime;
                }
                
                targetPoint = this.currentMultipoint;

                double noiseScale = 0.08 + randomFactor * 0.05;
                targetPoint = targetPoint.add(
                    Math.sin(timeSineFactor * 2.5) * noiseScale,
                    Math.cos(timeSineFactor * 2.0) * noiseScale * 0.6,
                    Math.cos(timeSineFactor * 2.8) * noiseScale
                );
                
                if (isMoving) {
                    double velocityInfluence = 0.045 + randomFactor * 0.055;
                    targetPoint = targetPoint.add(
                        playerVelocity.x * (0.3 + timeSineFactor * 0.2) + (randomFactor - 0.5) * velocityInfluence, 
                        (randomFactor - 0.7) * 0.04, 
                        playerVelocity.z * (0.35 + timeSineFactor * 0.2) + (randomFactor - 0.6) * velocityInfluence
                    );
                }
                
                if (shouldUseElytraPredict(target) && !this.isTurnaroundActive) {
                    targetPoint = getPredictedPoint(target, targetPoint);
                } else if (this.isTurnaroundActive) {
                    targetPoint = target.getBoundingBox().getCenter();
                }
            }
            
            Vec2f targetRotationVec = RotationUtils.getRotations(targetPoint);
            Rotation targetRotation = new Rotation(targetRotationVec.x, targetRotationVec.y);
            float targetYaw = targetRotation.getYaw();
            float targetPitch = targetRotation.getPitch();
            float yawDifference = Math.abs(MathHelper.wrapDegrees(targetYaw - this.lastYaw));
            boolean isReadyToAttack = this.mc.player.getAttackCooldownProgress(1.0F) > 0.93F && this.ticksToAttack <= 1;
            
            if (!this.back) {
                float accelerationRate = 0.0048F + (float)(randomFactor * 0.0055);
                if (yawDifference > 30.0F) {
                    accelerationRate += 0.01F;
                }
                
                if (yawDifference > 10.0F && isReadyToAttack) {
                    accelerationRate += 0.015F;
                }
                
                if (isMoving) {
                    accelerationRate *= 0.92F + (float)randomFactor * 0.15F;
                }
                
                if (isReadyToAttack) {
                    accelerationRate += 0.018F * (float)randomFactor;
                }
                
                this.speedAcceleration += accelerationRate * (0.94F + (float)timeSineFactor * 0.12F);
                float maxAcceleration = isMoving ? 0.21F + (float)randomFactor * 0.04F : 0.24F + (float)randomFactor * 0.03F;
                if (this.speedAcceleration >= maxAcceleration) {
                    this.back = true;
                }
            } else {
                this.speedAcceleration -= (isReadyToAttack ? 0.024F : 0.011F) * (1.0F + (float)randomFactor * 0.5F);
                if (this.speedAcceleration <= -0.042F) {
                    this.back = false;
                }
            }
            
            float maxSpeed = this.mc.player.isGliding() ? 0.55F : (isMoving ? 0.38F : 0.28F);
            float currentSpeed = MathHelper.clamp(this.speedAcceleration, 0.026F, maxSpeed);
            if (isReadyToAttack && randomFactor > 0.75) {
                currentSpeed = Math.min(currentSpeed + (float)(randomFactor * 0.06), maxSpeed + 0.04F);
            }
            
            float yawChange = MathHelper.wrapDegrees(targetYaw - this.lastYaw);
            float pitchChange = targetPitch - this.lastPitch;
            float maxYawChange = (this.mc.player.isGliding() ? 72.0F : (isReadyToAttack ? 38.0F : 24.0F)) + (float)(randomFactor * 10.0);
            float maxPitchChange = (this.mc.player.isGliding() ? 38.0F : (isReadyToAttack ? 22.0F : 15.0F)) + (float)(randomFactor * 8.0);
            
            if (isMoving && !isReadyToAttack) {
                maxYawChange *= 0.85F;
                maxPitchChange *= 0.85F;
            }
            
            yawChange = MathHelper.clamp(yawChange, -maxYawChange, maxYawChange);
            pitchChange = MathHelper.clamp(pitchChange, -maxPitchChange, maxPitchChange);
            float newYaw = this.lastYaw + yawChange * currentSpeed;
            float newPitch = MathHelper.clamp(
                this.lastPitch + pitchChange * currentSpeed * (isReadyToAttack ? 0.92F : 0.82F) * (float)(0.88 + randomFactor * 0.28) * (0.7F + (float)randomFactor * 0.15F), 
                -89.2F, 
                89.2F
            );
            
            float gcdValue = GCDUtil.getGCDValue();
            if (gcdValue > 0.0F) {
                newYaw = this.lastYaw + (float)Math.round((newYaw - this.lastYaw) / gcdValue) * gcdValue;
                newPitch = this.lastPitch + (float)Math.round((newPitch - this.lastPitch) / gcdValue) * gcdValue;
            }
            
            Rotation finalRotation = new Rotation(newYaw, newPitch);
            float rotationSpeed = this.mc.player.isGliding() && target.isGliding() ? 360.0F : (isMoving ? 64.0F + (float)randomFactor * 28.0F : 75.0F + (float)randomFactor * 35.0F);
            RotationStorage.update(finalRotation, rotationSpeed, rotationSpeed, rotationSpeed, rotationSpeed, 0, 1, Aura.clientLook.isState());
            this.lastYaw = finalRotation.getYaw();
            this.lastPitch = finalRotation.getPitch();
        }
    }
}
