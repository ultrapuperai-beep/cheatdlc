package fun.eversense.client.modules.impl.player;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import fun.eversense.api.events.EventLink;
import fun.eversense.api.events.implement.EventUpdate;
import fun.eversense.client.modules.Module;
import fun.eversense.mixin.IMinecraftClientAccessor;

public class FastExp extends Module {

    public static FastExp INSTANCE = new FastExp();

    public FastExp() {
        super("FastExp", "Позволяет бросать пузырьки опыта без задержки", ModuleCategory.PLAYER);
    }

    @EventLink
    public void onUpdate(EventUpdate event) {
        if (mc.player == null) {
            return;
        }

        ItemStack stack = mc.player.getMainHandStack();
        if (stack.isOf(Items.EXPERIENCE_BOTTLE)) {
            ((IMinecraftClientAccessor) mc).setItemUseCooldown(0);
        }
    }
}
