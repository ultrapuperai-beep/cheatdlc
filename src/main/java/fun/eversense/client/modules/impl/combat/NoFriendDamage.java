package fun.eversense.client.modules.impl.combat;

import fun.eversense.api.events.EventLink;
import fun.eversense.api.events.implement.EventAttackEntity;
import fun.eversense.client.modules.Module;
import fun.eversense.eversense;
import net.minecraft.entity.player.PlayerEntity;

public class NoFriendDamage extends Module {

    public static NoFriendDamage INSTANCE = new NoFriendDamage();

    public NoFriendDamage() {
        super("NoFriendDamage", "Не позволяет атаковать друзей", ModuleCategory.COMBAT);
    }

    @EventLink
    public void onAttack(EventAttackEntity event) {
        if (mc.player == null || mc.world == null) {
            return;
        }

        // Проверяем, что цель - игрок
        if (!(event.getTarget() instanceof PlayerEntity targetPlayer)) {
            return;
        }

        // Получаем имя игрока
        String playerName = targetPlayer.getName().getString();

        // Проверяем, является ли игрок другом
        if (eversense.INSTANCE.friendStorage != null && eversense.INSTANCE.friendStorage.isFriend(playerName)) {
            // Отменяем атаку
            event.cancel();
        }
    }
}
