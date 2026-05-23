package fun.eversense.client.modules.impl.movement;

import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.TridentItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import fun.eversense.api.events.EventLink;
import fun.eversense.api.events.implement.EventUpdate;
import fun.eversense.api.storages.implement.RotationStorage;
import fun.eversense.api.utils.rotate.Rotation;
import fun.eversense.client.modules.Module;
import fun.eversense.client.modules.settings.implement.BooleanSetting;
import fun.eversense.client.modules.settings.implement.ModeSetting;

public class Spider extends Module {

    public static Spider INSTANCE = new Spider();

    private final ModeSetting mode = new ModeSetting("Мод", "Вода", "Вода", "SpookyTime", "Bow");
    private final BooleanSetting legit = new BooleanSetting("Легит", false);

    private int lastSlot = -1;
    private boolean isClimbing = false;
    private int swapBackSlot = -1;
    private int spookyTicks;
    private int chargeSlot = -1;
    private boolean charging;
    private int bowChargeTicks = 0;
    private boolean isShooting = false;
    private int bowCooldown = 0;

    public Spider() {
        super("Spider", "Позволяет взбираться по стенам", ModuleCategory.MOVEMENT);
        addSettings(mode, legit);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.player == null) return;

        if (lastSlot != -1 && legit.isState()) {
            mc.player.getInventory().selectedSlot = lastSlot;
        }

        lastSlot = -1;
        swapBackSlot = -1;
        isClimbing = false;
        spookyTicks = 0;
        chargeSlot = -1;
        charging = false;
        bowChargeTicks = 0;
        isShooting = false;
        bowCooldown = 0;
    }

    @EventLink
    public void onUpdate(EventUpdate event) {
        if (mc.player == null || mc.world == null) return;

        if (!mc.player.horizontalCollision) {
            stopClimbing();
            return;
        }

        isClimbing = true;
        
        // Для режима Bow не устанавливаем горизонтальную ротацию
        if (!mode.is("Bow")) {
            RotationStorage.update(new Rotation(mc.player.getYaw(), 0), 360, 360, 360, 360, 1, 1, false);
        }

        if (mode.is("Bow")) {
            processBowMode();
            return;
        }

        if (mode.is("SpookyTime")) {
            processSpookyTime();
            return;
        }

        int bucketSlot = getBucketSlot(false);
        if (bucketSlot == -1) return;

        useBucket(bucketSlot, legit.isState());
        mc.player.setVelocity(mc.player.getVelocity().x, 0.36, mc.player.getVelocity().z);
    }

    private void stopClimbing() {
        if (lastSlot != -1 && legit.isState()) {
            mc.player.getInventory().selectedSlot = lastSlot;
            lastSlot = -1;
        }

        if (swapBackSlot != -1) {
            mc.interactionManager.clickSlot(0, swapBackSlot, 0, SlotActionType.QUICK_MOVE, mc.player);
            swapBackSlot = -1;
        }

        // Отпускаем клавишу использования если стреляли
        if (isShooting) {
            mc.options.useKey.setPressed(false);
        }

        isClimbing = false;
        spookyTicks = 0;
        chargeSlot = -1;
        charging = false;
        bowChargeTicks = 0;
        isShooting = false;
        bowCooldown = 0;
    }

    private void processBowMode() {
        // Ищем лук и стрелы
        int bowSlot = findBowSlot();
        if (bowSlot == -1 || !hasArrows()) {
            return;
        }

        // Уменьшаем кулдаун
        if (bowCooldown > 0) {
            bowCooldown--;
            // Во время кулдауна двигаемся к стене и немного вверх
            if (mc.player.horizontalCollision) {
                mc.player.setVelocity(mc.player.getVelocity().x, 0.25, mc.player.getVelocity().z);
            }
            // Держим ротацию вверх даже во время кулдауна
            RotationStorage.update(new Rotation(mc.player.getYaw(), -80), 360, 360, 360, 360, 1, 1, false);
            return;
        }

        // Устанавливаем ротацию почти вверх (80 градусов)
        RotationStorage.update(new Rotation(mc.player.getYaw(), -80), 360, 360, 360, 360, 1, 1, false);

        // Свапаем на лук если нужно
        if (mc.player.getInventory().selectedSlot != bowSlot) {
            if (lastSlot == -1) {
                lastSlot = mc.player.getInventory().selectedSlot;
            }
            mc.player.getInventory().selectedSlot = bowSlot;
            return; // Даем тик на своп
        }

        // Начинаем заряжать лук
        if (!isShooting) {
            mc.options.useKey.setPressed(true);
            isShooting = true;
            bowChargeTicks = 0;
        }

        bowChargeTicks++;

        // Стреляем когда лук заряжен
        if (bowChargeTicks >= 6) {
            // Отпускаем лук (стреляем)
            mc.options.useKey.setPressed(false);
            mc.interactionManager.stopUsingItem(mc.player);
            isShooting = false;
            bowChargeTicks = 0;
            bowCooldown = 5;
            
            // Даем импульс вверх сразу после выстрела
            mc.player.setVelocity(mc.player.getVelocity().x, 0.5, mc.player.getVelocity().z);
        }
    }

    private void processSpookyTime() {
        int bucketSlot = getBucketSlot(true);
        boolean bucketPulse = spookyTicks % 5 == 0;
        boolean boostPulse = spookyTicks % 4 != 3;

        keepChargeHeld();

        if (bucketSlot != -1 && bucketPulse) {
            useBucket(bucketSlot, false);
            keepChargeHeld();
        }

        double y = boostPulse ? 0.18 : 0.03;
        mc.player.setVelocity(mc.player.getVelocity().x, y, mc.player.getVelocity().z);
        spookyTicks++;
    }

    private void useBucket(int bucketSlot, boolean legitMode) {
        if (!legitMode) {
            int currentSlot = mc.player.getInventory().selectedSlot;
            boolean isInventorySwap = bucketSlot >= 9 && bucketSlot <= 35;

            if (isInventorySwap) {
                mc.interactionManager.clickSlot(0, bucketSlot, currentSlot, SlotActionType.SWAP, mc.player);
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                mc.interactionManager.clickSlot(0, bucketSlot, currentSlot, SlotActionType.SWAP, mc.player);
            } else {
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(bucketSlot));
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(currentSlot));
            }
            return;
        }

        boolean isInventorySwap = bucketSlot >= 9 && bucketSlot <= 35;

        if (isInventorySwap) {
            mc.interactionManager.clickSlot(0, bucketSlot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
            swapBackSlot = bucketSlot;
        } else if (mc.player.getInventory().selectedSlot != bucketSlot) {
            if (lastSlot == -1) {
                lastSlot = mc.player.getInventory().selectedSlot;
            }
            mc.player.getInventory().selectedSlot = bucketSlot;
        }

        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
    }

    private void keepChargeHeld() {
        if (isChargeItem(mc.player.getOffHandStack())) {
            if (!charging || spookyTicks % 12 == 0) {
                sendChargeUsePacket(Hand.OFF_HAND);
            }
            charging = true;
            return;
        }

        if (chargeSlot == -1 || !isChargeItem(mc.player.getInventory().getStack(chargeSlot))) {
            chargeSlot = getChargeHotbarSlot();
            charging = false;
        }
        if (chargeSlot == -1) return;

        if (mc.player.getInventory().selectedSlot != chargeSlot) {
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(chargeSlot));
            mc.player.getInventory().selectedSlot = chargeSlot;
            charging = false;
        }

        if (!charging || spookyTicks % 12 == 0) {
            sendChargeUsePacket(Hand.MAIN_HAND);
        }
        charging = true;
    }

    private void sendChargeUsePacket(Hand hand) {
        mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(hand, 0, mc.player.getYaw(), mc.player.getPitch()));
    }

    private int getBucketSlot(boolean allowLava) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (isBucket(stack, allowLava)) {
                return i;
            }
        }

        if (!legit.isState() || mode.is("SpookyTime")) {
            for (int i = 9; i < 36; i++) {
                ItemStack stack = mc.player.getInventory().getStack(i);
                if (isBucket(stack, allowLava)) {
                    return i;
                }
            }
        }

        return -1;
    }

    private int getChargeHotbarSlot() {
        for (int i = 0; i < 9; i++) {
            if (isChargeItem(mc.player.getInventory().getStack(i))) {
                return i;
            }
        }
        return -1;
    }

    private boolean isBucket(ItemStack stack, boolean allowLava) {
        return stack.getItem() == Items.WATER_BUCKET || allowLava && stack.getItem() == Items.LAVA_BUCKET;
    }

    private boolean isChargeItem(ItemStack stack) {
        return stack.getItem() instanceof BowItem || stack.getItem() instanceof TridentItem;
    }

    private int findBowSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof BowItem) {
                return i;
            }
        }
        return -1;
    }

    private boolean hasArrows() {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.ARROW || stack.getItem() == Items.SPECTRAL_ARROW || stack.getItem() == Items.TIPPED_ARROW) {
                return true;
            }
        }
        return false;
    }
}
