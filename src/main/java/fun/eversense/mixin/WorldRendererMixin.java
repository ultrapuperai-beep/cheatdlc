package fun.eversense.mixin;

import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Fog;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profilers;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import fun.eversense.api.QClient;
import fun.eversense.api.events.EventInvoker;
import fun.eversense.api.events.implement.Event3DRender;
import fun.eversense.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.eversense.client.modules.impl.render.Removals;
import fun.eversense.client.modules.impl.render.ShaderEsp;
import fun.eversense.client.modules.impl.render.Sonar;
import fun.eversense.client.modules.impl.render.HitSonar;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin implements QClient {

    @Inject(method = "renderParticles", at = @At("HEAD"), cancellable = true)
    private void eversense$renderParticles(FrameGraphBuilder frameGraphBuilder, Camera camera, float tickDelta, Fog fog, CallbackInfo ci) {
        if (ModuleClass.INSTANCE == null) return;

        Removals removals = ModuleClass.removals;
        if (removals != null && removals.isEnabled("Частицы")) {
            ci.cancel();
        }
    }

    @Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
    private void eversense$renderWeather(FrameGraphBuilder frameGraphBuilder, Vec3d pos, float tickDelta, Fog fog, CallbackInfo ci) {
        if (ModuleClass.INSTANCE == null) return;

        Removals removals = ModuleClass.removals;
        if (removals != null && removals.isEnabled("Погода")) {
            ci.cancel();
        }
    }

    @Inject(method = "addWeatherParticlesAndSound", at = @At("HEAD"), cancellable = true)
    private void eversense$addWeatherParticlesAndSound(Camera camera, CallbackInfo ci) {
        if (ModuleClass.INSTANCE == null) return;

        Removals removals = ModuleClass.removals;
        if (removals != null && removals.isEnabled("Погода")) {
            ci.cancel();
        }
    }

    @Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
    private void eversense$renderClouds(FrameGraphBuilder frameGraphBuilder, Matrix4f positionMatrix, Matrix4f projectionMatrix, CloudRenderMode renderMode, Vec3d cameraPos, float ticks, int color, float cloudHeight, CallbackInfo ci) {
        if (ModuleClass.INSTANCE == null) return;

        Removals removals = ModuleClass.removals;
        if (removals != null && removals.isEnabled("Облака")) {
            ci.cancel();
        }
    }

    @Inject(method = "renderBlockEntities", at = @At("HEAD"), cancellable = true)
    private void eversense$renderBlockEntities(MatrixStack matrices, VertexConsumerProvider.Immediate mainConsumers, VertexConsumerProvider.Immediate translucentConsumers, Camera camera, float tickDelta, CallbackInfo ci) {
        if (ModuleClass.INSTANCE == null) return;

        Removals removals = ModuleClass.removals;
        if (removals != null && removals.isEnabled("Блок-сущности")) {
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void render(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, Matrix4f positionMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        Sonar sonar = ModuleClass.INSTANCE != null ? ModuleClass.INSTANCE.sonar : null;
        HitSonar hitSonar = ModuleClass.INSTANCE != null ? ModuleClass.INSTANCE.hitSonar : null;
        boolean has3DListeners = EventInvoker.hasListeners(Event3DRender.class);
        boolean renderSonar = sonar != null && sonar.isEnable();
        boolean renderHitSonar = hitSonar != null && hitSonar.isEnable();
        if (!has3DListeners && !renderSonar && !renderHitSonar) {
            return;
        }

        Profilers.get().swap("eversense_renderWorld");
        MatrixStack matrices = new MatrixStack();
        matrices.multiplyPositionMatrix(positionMatrix);
        if (has3DListeners) {
            new Event3DRender(matrices, positionMatrix, projectionMatrix, camera, tickCounter.getTickDelta(false)).call();
        }
        if (renderSonar) {
            sonar.renderFromMixin(positionMatrix, projectionMatrix, camera.getPos());
        }
        if (renderHitSonar) {
            hitSonar.renderFromMixin(positionMatrix, projectionMatrix, camera.getPos());
        }
    }

    @Inject(method = "drawEntityOutlinesFramebuffer", at = @At("HEAD"), cancellable = true)
    private void eversense$drawEntityOutlinesFramebuffer(CallbackInfo ci) {
        if (ModuleClass.INSTANCE == null) return;
        ShaderEsp shaderEsp = ModuleClass.INSTANCE.shaderEsp;
        if (shaderEsp != null && shaderEsp.isEnable()) {
            ci.cancel();
            return;
        }

    }

    @Inject(method = "drawBlockOutline", at = @At("HEAD"), cancellable = true)
    public void onDrawBlockOutline(CallbackInfo ci) {
        if (ModuleClass.INSTANCE.blockOverlay.isEnable()) ci.cancel();
    }
}
