package fun.eversense.client.modules.impl.render;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.AllayEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PigEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.AllayEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.entity.state.PigEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import fun.eversense.api.storages.implement.helpertstorages.enumvar.ModuleClass;

public class SatelliteFeatureRenderer extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {

    private static final Identifier ALLAY_TEXTURE = Identifier.ofVanilla("textures/entity/allay/allay.png");
    private static final Identifier PIG_TEXTURE = Identifier.ofVanilla("textures/entity/pig/pig.png");

    private final AllayEntityModel model;
    private final PigEntityModel pigModel;
    private final AllayEntityRenderState allayState = new AllayEntityRenderState();
    private final PigEntityRenderState pigState = new PigEntityRenderState();

    public SatelliteFeatureRenderer(
            FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> context,
            EntityRendererFactory.Context rendererContext
    ) {
        super(context);
        this.model = new AllayEntityModel(rendererContext.getPart(EntityModelLayers.ALLAY));
        this.pigModel = new PigEntityModel(rendererContext.getPart(EntityModelLayers.PIG));
    }

    @Override
    public void render(
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            PlayerEntityRenderState playerState,
            float yawDegrees,
            float pitch
    ) {
        Satellite sattelite = ModuleClass.sattelite;
        if (sattelite == null || !sattelite.shouldRender(playerState)) {
            return;
        }

        matrices.push();

        float baseX = sattelite.isLeftShoulder() ? 0.4f : -0.4f;
        float baseY = playerState.isInSneakingPose ? -1.3f : -1.5f;

        float idleBob = 0.0f;
        float idleYaw = 0.0f;
        float idleRoll = 0.0f;
        float idlePitch = 0.0f;
        float animationAge = playerState.age;
        if (sattelite.idleAnimation.isState()) {
            float speed = sattelite.idleSpeed.get();
            float strength = sattelite.idleStrength.get();
            float time = playerState.age * (0.7f + speed * 0.65f);

            idleBob = MathHelper.sin(time * 0.42f) * 0.06f * strength;
            idleYaw = MathHelper.sin(time * 0.16f) * 9.0f * strength;
            idleRoll = MathHelper.cos(time * 0.24f) * 7.0f * strength;
            idlePitch = MathHelper.sin(time * 0.31f) * 5.0f * strength;
            animationAge = playerState.age * (0.85f + speed * 0.45f);
        }

        matrices.translate(
                baseX + sattelite.offsetX.get(),
                baseY + sattelite.offsetY.get() + idleBob,
                sattelite.offsetZ.get()
        );

        float scale = sattelite.scale.get();
        matrices.scale(scale, scale, scale);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sattelite.rotateX.get() + idlePitch));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(sattelite.rotateY.get() + idleYaw));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(sattelite.rotateZ.get() + idleRoll));

        if (sattelite.petType.is("Pig")) {
            pigState.age = animationAge;
            pigState.limbFrequency = playerState.limbFrequency;
            pigState.limbAmplitudeMultiplier = playerState.limbAmplitudeMultiplier;
            pigState.yawDegrees = yawDegrees;
            pigState.pitch = pitch;
            pigState.invisible = playerState.invisible;
            pigState.invisibleToPlayer = playerState.invisibleToPlayer;
            pigState.hasOutline = playerState.hasOutline;
            pigState.shaking = playerState.shaking;
            pigState.baby = true; // Маленькая свинка
            pigState.touchingWater = playerState.touchingWater;
            pigState.bodyYaw = playerState.bodyYaw;
            pigState.baseScale = 0.5f; // Уменьшаем размер
            pigState.ageScale = 0.5f;
            pigState.pose = playerState.pose;
            pigState.deathTime = 0.0f;
            pigState.hurt = playerState.hurt;
            pigState.saddled = false;

            pigModel.setAngles(pigState);
            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(pigModel.getLayer(PIG_TEXTURE));
            pigModel.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV);
        } else {
            allayState.age = animationAge;
            allayState.limbFrequency = playerState.limbFrequency;
            allayState.limbAmplitudeMultiplier = playerState.limbAmplitudeMultiplier;
            allayState.yawDegrees = yawDegrees;
            allayState.pitch = pitch;
            allayState.invisible = playerState.invisible;
            allayState.invisibleToPlayer = playerState.invisibleToPlayer;
            allayState.hasOutline = playerState.hasOutline;
            allayState.shaking = playerState.shaking;
            allayState.baby = false;
            allayState.touchingWater = playerState.touchingWater;
            allayState.bodyYaw = playerState.bodyYaw;
            allayState.baseScale = 1.0f;
            allayState.ageScale = 1.0f;
            allayState.pose = playerState.pose;
            allayState.deathTime = 0.0f;
            allayState.hurt = playerState.hurt;
            allayState.dancing = false;
            allayState.spinning = false;
            allayState.spinningAnimationTicks = 0.0f;
            allayState.itemHoldAnimationTicks = 0.0f;

            model.setAngles(allayState);
            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(model.getLayer(ALLAY_TEXTURE));
            model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV);
        }

        matrices.pop();
    }
}
