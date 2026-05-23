package fun.eversense.mixin;

import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import fun.eversense.client.modules.impl.render.SeeInvisiblesRenderState;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements SeeInvisiblesRenderState {

    @Unique
    private boolean eversense$seeInvisiblesTarget;

    @Override
    public boolean eversense$isSeeInvisiblesTarget() {
        return eversense$seeInvisiblesTarget;
    }

    @Override
    public void eversense$setSeeInvisiblesTarget(boolean value) {
        eversense$seeInvisiblesTarget = value;
    }
}
