package fun.eversense.client.modules.impl.misc;

import fun.eversense.api.events.EventLink;
import fun.eversense.api.events.implement.EventBinding;
import fun.eversense.api.utils.chat.ChatUtils;
import fun.eversense.client.modules.Module;
import fun.eversense.client.modules.settings.implement.BindSetting;
import fun.eversense.eversense;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;

public class ClickFriend extends Module {

    public static ClickFriend INSTANCE = new ClickFriend();

    private final BindSetting bind = new BindSetting("Бинд", -1);

    public ClickFriend() {
        super("ClickFriend", "Добавляет игрока в друзья по нажатию кнопки", ModuleCategory.MISC);
        addSettings(bind);
    }

    @EventLink
    public void onBinding(EventBinding event) {
        if (mc.player == null || mc.world == null || mc.currentScreen != null) {
            return;
        }

        if (event.getKey() != bind.getKey()) {
            return;
        }

        // Получаем игрока под прицелом в радиусе 5 блоков
        PlayerEntity targetPlayer = getTargetPlayer(5.0f);

        if (targetPlayer == null) {
            ChatUtils.sendMessage("§cНе найден игрок под прицелом в радиусе 5 блоков!");
            return;
        }

        String playerName = targetPlayer.getName().getString();

        // Проверяем, не является ли игрок уже другом
        if (eversense.INSTANCE.friendStorage.isFriend(playerName)) {
            ChatUtils.sendMessage("§eИгрок §f" + playerName + "§e уже в списке друзей!");
            return;
        }

        // Добавляем игрока в друзья
        eversense.INSTANCE.friendStorage.add(playerName);
        ChatUtils.sendMessage("§aИгрок §f" + playerName + "§a добавлен в друзья!");
    }

    private PlayerEntity getTargetPlayer(float range) {
        Vec3d eyePos = mc.player.getCameraPosVec(1.0F);
        Vec3d lookVec = mc.player.getRotationVec(1.0F);
        Vec3d reachVec = eyePos.add(lookVec.multiply(range));

        EntityHitResult result = ProjectileUtil.raycast(
                mc.player,
                eyePos,
                reachVec,
                mc.player.getBoundingBox().expand(range),
                entity -> entity != mc.player && entity instanceof PlayerEntity,
                range * range
        );

        if (result != null) {
            Entity entity = result.getEntity();
            if (entity instanceof PlayerEntity player) {
                return player;
            }
        }

        return null;
    }
}
