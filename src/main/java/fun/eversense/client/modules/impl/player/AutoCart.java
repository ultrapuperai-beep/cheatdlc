package fun.eversense.client.modules.impl.player;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import fun.eversense.api.events.EventLink;
import fun.eversense.api.events.implement.EventBinding;
import fun.eversense.api.events.implement.EventUpdate;
import fun.eversense.api.utils.player.InventoryUtils;
import fun.eversense.client.modules.Module;
import fun.eversense.client.modules.settings.implement.BindSetting;
import fun.eversense.client.modules.settings.implement.FloatSetting;

import java.util.HashSet;
import java.util.Set;

public class AutoCart extends Module {

    public static AutoCart INSTANCE = new AutoCart();

    private static final int MAX_PREDICTION_TICKS = 80;
    private static final int BOW_CHARGE_TICKS = 10;

    private final FloatSetting range = new FloatSetting("Дистанция", 5.0f, 1.0f, 8.0f, 0.1f);
    private final BindSetting bowShootKey = new BindSetting("Кнопка выстрела", -1);

    private final Set<Integer> knownArrows = new HashSet<>();
    private final Set<Integer> processedArrows = new HashSet<>();

    private boolean bowShotQueued;
    private boolean bowCharging;
    private int bowChargeTime;
    private int bowPrevSlot = -1;

    public AutoCart() {
        super("AutoCart", "Автоматически размещает вагонетки с TNT на траектории стрел", ModuleCategory.PLAYER);
        addSettings(range, bowShootKey);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        knownArrows.clear();
        processedArrows.clear();
        resetBowState();
        cacheExistingArrows();
    }

    @Override
    public void onDisable() {
        knownArrows.clear();
        processedArrows.clear();
        if (mc.options != null) {
            mc.options.useKey.setPressed(false);
        }
        resetBowState();
        super.onDisable();
    }

    @EventLink
    public void onEvent(final EventBinding event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        
        Integer key = bowShootKey.getKey();
        if (key == null || key == -1) return;
        
        if (event.getKey() == key) {
            bowShotQueued = true;
        }
    }

    @EventLink
    public void onEvent(final EventUpdate event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        handleBowShoot();

        Set<Integer> aliveArrows = new HashSet<>();
        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof PersistentProjectileEntity projectile) || projectile instanceof TridentEntity) {
                continue;
            }

            if (projectile.getOwner() != mc.player) continue;

            int id = projectile.getId();
            aliveArrows.add(id);
            knownArrows.add(id);

            if (processedArrows.contains(id)) continue;
            if (!hasRequiredItemsInHotbar()) continue;
            if (!shouldProcessArrow(projectile)) continue;

            LandingPrediction prediction = predictLanding(projectile);
            if (prediction == null) continue;

            double rangeSq = range.get() * range.get();
            if (mc.player.squaredDistanceTo(Vec3d.ofCenter(prediction.placePos())) > rangeSq) {
                processedArrows.add(id);
                continue;
            }

            if (placeCartAt(prediction.placePos()) || projectile.isOnGround()) {
                processedArrows.add(id);
            }
        }

        knownArrows.retainAll(aliveArrows);
        processedArrows.retainAll(aliveArrows);
    }

    private void handleBowShoot() {
        if (mc.player == null || mc.interactionManager == null || mc.options == null) return;

        if (bowCharging) {
            if (!mc.player.getMainHandStack().isOf(Items.BOW)) {
                finishBowShot(false);
                return;
            }

            bowChargeTime++;
            if (bowChargeTime >= BOW_CHARGE_TICKS) {
                finishBowShot(true);
            }
            return;
        }

        if (!bowShotQueued) return;
        bowShotQueued = false;

        int bowSlot = InventoryUtils.find(Items.BOW, 0, 9);
        if (bowSlot == -1) return;

        bowPrevSlot = mc.player.getInventory().selectedSlot;
        if (bowPrevSlot != bowSlot) {
            mc.player.getInventory().selectedSlot = bowSlot;
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(bowSlot));
        }

        mc.options.useKey.setPressed(true);
        bowCharging = true;
        bowChargeTime = 0;
    }

    private void finishBowShot(boolean releaseShot) {
        if (mc.player == null || mc.interactionManager == null || mc.options == null) {
            resetBowState();
            return;
        }

        if (releaseShot) {
            mc.options.useKey.setPressed(false);
            if (mc.player.isUsingItem()) {
                mc.interactionManager.stopUsingItem(mc.player);
            }
        }

        if (bowPrevSlot >= 0 && bowPrevSlot <= 8 && mc.player.getInventory().selectedSlot != bowPrevSlot) {
            mc.player.getInventory().selectedSlot = bowPrevSlot;
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(bowPrevSlot));
        }

        resetBowState();
    }

    private void resetBowState() {
        bowShotQueued = false;
        bowCharging = false;
        bowChargeTime = 0;
        bowPrevSlot = -1;
    }

    private void cacheExistingArrows() {
        if (mc.player == null || mc.world == null) return;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof PersistentProjectileEntity projectile) || projectile instanceof TridentEntity) {
                continue;
            }
            if (projectile.getOwner() == mc.player) {
                knownArrows.add(projectile.getId());
            }
        }
    }

    private boolean hasRequiredItemsInHotbar() {
        return InventoryUtils.find(Items.RAIL, 0, 9) != -1
                && InventoryUtils.find(Items.TNT_MINECART, 0, 9) != -1;
    }

    private boolean shouldProcessArrow(PersistentProjectileEntity projectile) {
        return !projectile.isOnGround() && projectile.getVelocity().lengthSquared() > 1.0E-4;
    }

    private LandingPrediction predictLanding(PersistentProjectileEntity arrow) {
        if (mc.world == null) return null;

        Vec3d pos = arrow.getPos();
        Vec3d motion = arrow.getVelocity();

        for (int tick = 1; tick <= MAX_PREDICTION_TICKS; tick++) {
            Vec3d prevPos = pos;
            pos = pos.add(motion);
            motion = updateMotion(arrow, prevPos, motion);

            BlockHitResult hit = mc.world.raycast(new RaycastContext(
                    prevPos,
                    pos,
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    arrow
            ));
            if (hit.getType() != HitResult.Type.MISS) {
                BlockPos placePos = resolvePlacePos(hit);
                if (placePos != null) {
                    return new LandingPrediction(placePos, tick);
                }
                return null;
            }

            if (pos.y < mc.world.getBottomY() - 2) break;
        }

        return null;
    }

    private Vec3d updateMotion(PersistentProjectileEntity arrow, Vec3d pos, Vec3d motion) {
        if (mc.world == null) return motion;

        boolean inWater = mc.world.getBlockState(BlockPos.ofFloored(pos)).getFluidState().isIn(FluidTags.WATER);
        double drag = inWater ? 0.6 : 0.99;
        return motion.multiply(drag).add(0.0, -arrow.getFinalGravity(), 0.0);
    }

    private BlockPos resolvePlacePos(BlockHitResult hit) {
        if (mc.world == null) return null;

        BlockPos basePos = hit.getBlockPos();
        if (isRail(basePos)) return basePos;

        BlockPos preferred = hit.getSide() == Direction.UP ? basePos.up() : basePos;
        if (canPlaceRailAt(preferred)) return preferred;

        BlockPos up = basePos.up();
        if (canPlaceRailAt(up)) return up;

        BlockPos down = basePos.down();
        if (canPlaceRailAt(down)) return down;

        return null;
    }

    private boolean placeCartAt(BlockPos placePos) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return false;

        int railSlot = InventoryUtils.find(Items.RAIL, 0, 9);
        int cartSlot = InventoryUtils.find(Items.TNT_MINECART, 0, 9);

        if (railSlot == -1 || cartSlot == -1) return false;

        int prevSlot = mc.player.getInventory().selectedSlot;
        boolean placed = false;

        try {
            if (!isRail(placePos) && !placeRail(placePos, railSlot)) return false;
            if (hasTntMinecart(placePos)) return true;

            placeTntMinecart(placePos, cartSlot);
            restockMinecartSlot(cartSlot);
            placed = hasTntMinecart(placePos);
        } finally {
            mc.player.getInventory().selectedSlot = prevSlot;
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(prevSlot));
        }

        return placed;
    }

    private boolean isRail(BlockPos pos) {
        return mc.world != null && mc.world.getBlockState(pos).isIn(BlockTags.RAILS);
    }

    private boolean canPlaceRailAt(BlockPos pos) {
        if (mc.world == null) return false;
        if (isRail(pos)) return true;
        if (!mc.world.getBlockState(pos).isReplaceable()) return false;

        BlockPos support = pos.down();
        return mc.world.getBlockState(support).isSideSolidFullSquare(mc.world, support, Direction.UP);
    }

    private boolean placeRail(BlockPos placePos, int railSlot) {
        if (mc.player == null || mc.interactionManager == null) return false;

        mc.player.getInventory().selectedSlot = railSlot;
        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(railSlot));

        BlockPos support = placePos.down();
        BlockHitResult hitResult = new BlockHitResult(
                new Vec3d(placePos.getX() + 0.5, placePos.getY(), placePos.getZ() + 0.5),
                Direction.UP,
                support,
                false
        );

        ActionResult result = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
        if (result.isAccepted()) {
            mc.player.swingHand(Hand.MAIN_HAND);
            return true;
        }

        return isRail(placePos);
    }

    private void placeTntMinecart(BlockPos railPos, int cartSlot) {
        if (mc.player == null || mc.interactionManager == null) return;

        mc.player.getInventory().selectedSlot = cartSlot;
        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(cartSlot));

        BlockHitResult hitResult = new BlockHitResult(
                new Vec3d(railPos.getX() + 0.5, railPos.getY() + 0.15, railPos.getZ() + 0.5),
                Direction.UP,
                railPos,
                false
        );

        ActionResult result = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
        if (result.isAccepted()) {
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

    private void restockMinecartSlot(int hotbarSlot) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (hotbarSlot < 0 || hotbarSlot > 8) return;
        if (mc.player.getInventory().getStack(hotbarSlot).isOf(Items.TNT_MINECART)) return;

        int invSlot = InventoryUtils.find(Items.TNT_MINECART, 9, 36);
        if (invSlot == -1) return;

        mc.interactionManager.clickSlot(0, invSlot, hotbarSlot, SlotActionType.SWAP, mc.player);
    }

    private boolean hasTntMinecart(BlockPos railPos) {
        if (mc.world == null) return false;

        Box checkBox = new Box(railPos).expand(0.2, 1.0, 0.2);
        for (Entity entity : mc.world.getOtherEntities(null, checkBox)) {
            if (entity instanceof TntMinecartEntity) {
                return true;
            }
        }
        return false;
    }

    private record LandingPrediction(BlockPos placePos, int ticksToImpact) {
    }
}
