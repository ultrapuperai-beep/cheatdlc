package fun.eversense.client.modules.impl.combat.components.rotations;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import fun.eversense.api.QClient;
import fun.eversense.api.storages.implement.RotationStorage;
import fun.eversense.api.utils.rotate.Rotation;
import fun.eversense.api.utils.rotate.RotationUtils;
import fun.eversense.client.modules.impl.combat.Aura;
import fun.eversense.client.modules.impl.combat.components.RotationsSystem;

import java.security.SecureRandom;

public class FunTimeRotation extends RotationsSystem implements QClient {
    
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final long LOOK_DELAY_MS = 3_500L;
    private static final int AIR_MISS_INTERVAL = 31;
    private static final long AIR_MISS_LOOK_MS = 250L;
    private static final long AIR_MISS_SWING_MS = 238L;

    private int lastAirMissSwingCount = -1;
    private int attackCount = 0;
    private long lastAttackTime = 0;
    private float currentYaw;
    private float currentPitch;

    public FunTimeRotation() {
        if (mc.player != null) {
            this.currentYaw = mc.player.getYaw();
            this.currentPitch = mc.player.getPitch();
        }
        this.attackCount = 0;
        this.lastAttackTime = System.currentTimeMillis();
    }

    public void reset() {
        if (mc.player != null) {
            this.currentYaw = mc.player.getYaw();
            this.currentPitch = mc.player.getPitch();
        }
        this.attackCount = 0;
        this.lastAttackTime = System.currentTimeMillis();
        this.lastAirMissSwingCount = -1;
    }

    public void onAttack() {
        this.attackCount++;
        this.lastAttackTime = System.currentTimeMillis();
    }

    @Override
    public void updateRotations(LivingEntity target) {
        if (target == null || mc.player == null) return;

        long elapsed = System.currentTimeMillis() - lastAttackTime;
        
        // Air miss механика
        boolean airMiss = attackCount > 0 && attackCount % AIR_MISS_INTERVAL == 0 && elapsed < AIR_MISS_LOOK_MS;
        if (airMiss) {
            if (elapsed >= AIR_MISS_SWING_MS && lastAirMissSwingCount != attackCount) {
                mc.player.swingHand(Hand.MAIN_HAND);
                lastAirMissSwingCount = attackCount;
            }
            
            Vec3d targetPoint = getTargetPoint(target);
            Vec2f targetRot = RotationUtils.getRotations(targetPoint);
            float yawDelta = MathHelper.wrapDegrees(targetRot.x - currentYaw);
            float missYaw = currentYaw + MathHelper.clamp(yawDelta, -22.0F, 22.0F);
            
            Rotation rotation = new Rotation(missYaw, -85.0F);
            RotationStorage.update(rotation, 360, 360, 360, 360, 1, 1, Aura.clientLook.isState());
            
            currentYaw = missYaw;
            currentPitch = -85.0F;
            return;
        }

        // Основная логика ротации
        Vec3d targetPoint = getTargetPoint(target);
        Vec2f targetRot = RotationUtils.getRotations(targetPoint);
        
        float yawDelta = MathHelper.wrapDegrees(targetRot.x - currentYaw);
        float pitchDelta = targetRot.y - currentPitch;
        float rotationDifference = (float) Math.hypot(Math.abs(yawDelta), Math.abs(pitchDelta));
        if (rotationDifference < 1.0E-4F) {
            rotationDifference = 1.0E-4F;
        }

        Rotation finalRotation;
        
        // Проверяем дистанцию до цели (в зоне атаки или нет)
        double distanceToTarget = mc.player.distanceTo(target);
        boolean inAttackRange = distanceToTarget <= 6.0; // Максимальная дистанция атаки
        
        if (inAttackRange) {
            finalRotation = applyTrackingRotation(yawDelta, pitchDelta, rotationDifference, target, elapsed);
        } else {
            finalRotation = applyDelayRotation(yawDelta, pitchDelta, rotationDifference, elapsed);
        }

        float rotationSpeed = 180.0F;
        RotationStorage.update(finalRotation, rotationSpeed, rotationSpeed, rotationSpeed, rotationSpeed, 1, 1, Aura.clientLook.isState());
        
        currentYaw = finalRotation.getYaw();
        currentPitch = finalRotation.getPitch();
    }

    private Rotation applyTrackingRotation(float yawDelta, float pitchDelta, float rotationDifference, LivingEntity target, long elapsed) {
        boolean attackNow = mc.player.getAttackCooldownProgress(1.0F) > 0.95F;
        boolean attackSoon = mc.player.getAttackCooldownProgress(1.0F) > 0.75F;
        boolean recentAttack = elapsed < 180;
        boolean attackZone = mc.player.distanceTo(target) <= 4.0F;

        float yawBudget = rand(18.0F, 28.0F);
        float pitchBudget = rand(2.8F, 6.2F);

        if (attackSoon) {
            yawBudget = Math.max(yawBudget, rand(34.0F, 52.0F));
            pitchBudget = Math.max(pitchBudget, rand(4.2F, 7.8F));
        }

        if (recentAttack) {
            yawBudget = Math.max(yawBudget, rand(44.0F, 72.0F));
            pitchBudget = Math.max(pitchBudget, rand(5.4F, 10.0F));
        }

        if (Math.abs(yawDelta) > 40.0F) {
            yawBudget += rand(10.0F, 18.0F);
        }
        if (Math.abs(yawDelta) > 75.0F) {
            yawBudget += rand(12.0F, 24.0F);
        }
        if (Math.abs(pitchDelta) > 20.0F) {
            pitchBudget += rand(1.4F, 3.2F);
        }
        if (Math.abs(pitchDelta) > 35.0F) {
            pitchBudget += rand(1.6F, 3.8F);
        }

        float moveYaw = MathHelper.clamp(yawDelta, -axisBudget(yawDelta, rotationDifference, yawBudget), axisBudget(yawDelta, rotationDifference, yawBudget));
        float movePitch = MathHelper.clamp(pitchDelta, -axisBudget(pitchDelta, rotationDifference, pitchBudget), axisBudget(pitchDelta, rotationDifference, pitchBudget));

        float blend = attackNow
                ? 1.0F
                : attackSoon
                ? rand(0.88F, 0.97F)
                : recentAttack
                ? rand(0.74F, 0.88F)
                : rand(0.56F, 0.74F);

        if (attackZone && !attackSoon && !recentAttack) {
            blend = Math.max(blend, rand(0.68F, 0.82F));
        }

        float shakeScale = attackZone ? 1.25F : 0.9F;
        if (attackSoon) {
            shakeScale = Math.max(shakeScale, 1.4F);
        }
        if (recentAttack) {
            shakeScale = Math.max(shakeScale, 1.55F);
        }
        
        float shakeYaw = trackShakeYaw(elapsed, attackCount, shakeScale, Math.abs(yawDelta));
        float shakePitch = trackShakePitch(elapsed, attackCount, shakeScale, Math.abs(pitchDelta));

        if (Math.abs(yawDelta) < 4.0F) {
            shakeYaw *= 0.35F;
        }
        if (Math.abs(pitchDelta) < 2.5F) {
            shakePitch *= 0.25F;
        }

        float newYaw = MathHelper.lerp(blend, currentYaw, currentYaw + moveYaw) + shakeYaw;
        float newPitch = MathHelper.lerp(blend, currentPitch, currentPitch + movePitch) + shakePitch;
        
        return new Rotation(newYaw, MathHelper.clamp(newPitch, -90.0F, 90.0F));
    }

    private Rotation applyDelayRotation(float yawDelta, float pitchDelta, float rotationDifference, long elapsed) {
        Rotation oscillation = switch (attackCount % 4) {
            case 0 -> new Rotation(
                    (float) Math.cos(elapsed / 40.0F + (attackCount % 6)),
                    (float) Math.sin(elapsed / 40.0F + (attackCount % 6))
            );
            case 1 -> new Rotation(
                    (float) Math.sin(elapsed / 40.0F + (attackCount % 6)),
                    (float) Math.cos(elapsed / 40.0F + (attackCount % 6))
            );
            case 2 -> new Rotation(
                    (float) Math.sin(elapsed / 40.0F + (attackCount % 6)),
                    (float) -Math.cos(elapsed / 40.0F + (attackCount % 6))
            );
            default -> new Rotation(
                    (float) -Math.cos(elapsed / 40.0F + (attackCount % 6)),
                    (float) Math.sin(elapsed / 40.0F + (attackCount % 6))
            );
        };

        float holdProgress = MathHelper.clamp(elapsed / (float) LOOK_DELAY_MS, 0.0F, 1.0F);
        float holdScale = elapsed >= LOOK_DELAY_MS ? 0.0F : 1.0F - holdProgress * 0.55F;

        float idleYaw = holdScale > 0.0F ? rand(12.0F, 22.0F) * oscillation.getYaw() * holdScale : 0.0F;
        float pitchWave = rand(0.35F, 1.35F) * (float) Math.cos((double) System.currentTimeMillis() / 420.0 + attackCount);
        float idlePitch = holdScale > 0.0F
                ? (rand(2.2F, 5.8F) * oscillation.getPitch() + pitchWave) * holdScale
                : 0.0F;

        float yawBudget = elapsed < 180
                ? rand(0.0F, 3.5F)
                : elapsed < 600
                ? rand(4.0F, 10.0F)
                : elapsed >= LOOK_DELAY_MS
                ? rand(12.0F, 28.0F)
                : rand(6.0F, 14.0F);

        float pitchBudget = elapsed < 180
                ? rand(0.0F, 1.0F)
                : elapsed < 600
                ? rand(1.2F, 3.0F)
                : elapsed >= LOOK_DELAY_MS
                ? rand(3.0F, 6.8F)
                : rand(1.5F, 4.2F);

        float moveYaw = MathHelper.clamp(yawDelta, -axisBudget(yawDelta, rotationDifference, yawBudget), axisBudget(yawDelta, rotationDifference, yawBudget));
        float movePitch = MathHelper.clamp(pitchDelta, -axisBudget(pitchDelta, rotationDifference, pitchBudget), axisBudget(pitchDelta, rotationDifference, pitchBudget));

        float returnBlend = elapsed < 180
                ? 0.0F
                : elapsed < 600
                ? rand(0.08F, 0.22F)
                : elapsed >= LOOK_DELAY_MS
                ? rand(0.54F, 0.78F)
                : rand(0.20F, 0.42F);

        float newYaw = MathHelper.lerp(returnBlend, currentYaw, currentYaw + moveYaw) + idleYaw;
        float newPitch = MathHelper.lerp(returnBlend, currentPitch, currentPitch + movePitch) + idlePitch;
        
        return new Rotation(newYaw, MathHelper.clamp(newPitch, -90.0F, 90.0F));
    }

    private float trackShakeYaw(long elapsed, int count, float scale, float absYawDelta) {
        float shake = (float) Math.sin(elapsed / 38.0F + count * 0.37F) * rand(0.45F, 1.25F)
                + (float) Math.cos(elapsed / 71.0F + count * 0.18F) * rand(0.18F, 0.55F);

        if (chance(absYawDelta > 24.0F ? 0.22F : 0.08F)) {
            shake += rand(-1.55F, 1.55F);
        }

        return shake * scale;
    }

    private float trackShakePitch(long elapsed, int count, float scale, float absPitchDelta) {
        float shake = (float) Math.sin(elapsed / 52.0F + count * 0.21F) * rand(0.10F, 0.42F)
                + (float) Math.cos(elapsed / 93.0F + count * 0.11F) * rand(0.08F, 0.28F);

        if (chance(absPitchDelta > 8.0F ? 0.18F : 0.06F)) {
            shake += rand(-0.55F, 0.55F);
        }

        return shake * scale;
    }

    private float axisBudget(float axisDelta, float rotationDifference, float budget) {
        return Math.abs(axisDelta / rotationDifference) * budget;
    }

    private boolean chance(float probability) {
        return RANDOM.nextFloat() < probability;
    }

    private float rand(float min, float max) {
        return MathHelper.lerp(RANDOM.nextFloat(), min, max);
    }

    private Vec3d getTargetPoint(LivingEntity target) {
        Box box = target.getBoundingBox();
        Vec3d center = box.getCenter();
        
        // Добавляем небольшую рандомизацию точки прицеливания
        double offsetX = (RANDOM.nextDouble() - 0.5) * 0.1;
        double offsetY = (RANDOM.nextDouble() - 0.5) * 0.1;
        double offsetZ = (RANDOM.nextDouble() - 0.5) * 0.1;
        
        return new Vec3d(
            center.x + offsetX,
            center.y + target.getHeight() * 0.5 + offsetY,
            center.z + offsetZ
        );
    }
}
