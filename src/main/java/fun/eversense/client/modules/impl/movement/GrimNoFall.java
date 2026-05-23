package fun.eversense.client.modules.impl.movement;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import fun.eversense.api.events.EventLink;
import fun.eversense.api.events.implement.EventMoveInput;
import fun.eversense.api.events.implement.EventPacket;
import fun.eversense.api.events.implement.EventUpdate;
import fun.eversense.client.modules.Module;
import fun.eversense.client.modules.settings.implement.ModeSetting;

public class GrimNoFall extends Module {

    public static GrimNoFall INSTANCE = new GrimNoFall();
    
    private final ModeSetting mode = new ModeSetting("Режим", "Grim", "Grim", "Grim 2.3.73", "GrimV2");
    
    // Поля для режима Grim 2.3.73
    private int fallCounter = 0;
    private boolean shouldJump = false;
    
    // Поля для режима GrimV2
    private double prevFallDistance = 0.0;
    private boolean prevOnGround = false;
    private boolean flag = false;

    public GrimNoFall() {
        super("NoFall", "Убирает урон от падения", ModuleCategory.MOVEMENT);
        addSettings(mode);
    }

    @Override
    public void onDisable() {
        fallCounter = 0;
        shouldJump = false;
        prevFallDistance = 0.0;
        prevOnGround = false;
        flag = false;
        super.onDisable();
    }

    @EventLink
    public void onUpdate(final EventUpdate ignored) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        
        if (mode.is("Grim")) {
            // Оригинальный режим Grim
            if (!mc.player.isOnGround() && mc.player.fallDistance > 1f) {
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(
                    mc.player.getX(), 
                    mc.player.getY() + 0.000000001, 
                    mc.player.getZ(), 
                    mc.player.getYaw(), 
                    mc.player.getPitch(), 
                    true, 
                    false
                ));
                mc.player.onLanding();
            }
        } else if (mode.is("Grim 2.3.73")) {
            // Новый режим Grim 2.3.73
            if (mc.player.isOnGround() && mc.player.verticalCollision && fallCounter > 0) {
                Vec3d motion = mc.player.getVelocity();
                
                if (motion.x == 0.0 && motion.z == 0.0) {
                    // Если игрок не двигается - прыгаем
                    shouldJump = true;
                } else {
                    // Если игрок двигается - отправляем пакет START_FALL_FLYING
                    mc.getNetworkHandler().sendPacket(
                        new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING)
                    );
                }
            }
            
            // Считаем падения
            if (!mc.player.isOnGround() && mc.player.fallDistance > 0) {
                fallCounter++;
            } else if (mc.player.isOnGround()) {
                fallCounter = 0;
            }
        } else if (mode.is("GrimV2")) {
            // Режим GrimV2 - сбрасываем флаг когда игрок на земле
            if (flag && mc.player.isOnGround()) {
                flag = false;
            }
        }
    }
    
    @EventLink
    public void onPacket(EventPacket event) {
        if (mc.player == null || !mode.is("GrimV2")) return;
        
        Packet<?> packet = event.getPacket();
        
        // Обрабатываем исходящие пакеты движения
        if (event.getType() == EventPacket.Type.SEND && packet instanceof PlayerMoveC2SPacket movePacket) {
            // Проверяем условия для активации
            boolean currentOnGround = movePacket.isOnGround();
            
            // Уменьшаем порог до 2.0 чтобы срабатывало раньше
            if (currentOnGround && !prevOnGround && prevFallDistance >= 2.0) {
                flag = true;
                
                // Создаем новый пакет с измененными координатами
                if (movePacket instanceof PlayerMoveC2SPacket.Full) {
                    PlayerMoveC2SPacket.Full fullPacket = (PlayerMoveC2SPacket.Full) movePacket;
                    event.cancel();
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(
                        6767.0, // x
                        0.0,    // y
                        6767.0, // z
                        fullPacket.getYaw(0),
                        fullPacket.getPitch(0),
                        false,  // onGround = false
                        fullPacket.horizontalCollision()
                    ));
                } else if (movePacket instanceof PlayerMoveC2SPacket.PositionAndOnGround) {
                    event.cancel();
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                        6767.0, // x
                        0.0,    // y
                        6767.0, // z
                        false,  // onGround = false
                        false
                    ));
                }
                
                // Сбрасываем fallDistance
                mc.player.fallDistance = 0;
            }
            
            prevOnGround = currentOnGround;
            prevFallDistance = mc.player.fallDistance;
        }
    }
    
    @EventLink
    public void onMoveInput(EventMoveInput event) {
        if (mc.player == null) return;
        
        if (mode.is("Grim 2.3.73") && shouldJump) {
            event.setJump(true);
            shouldJump = false;
            fallCounter = 0;
        }
    }

}