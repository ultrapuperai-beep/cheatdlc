package fun.eversense.client.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.SwordItem;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import fun.eversense.api.events.EventLink;
import fun.eversense.api.events.implement.Event3DRender;
import fun.eversense.api.utils.color.ColorUtils;
import fun.eversense.client.modules.Module;
import fun.eversense.client.modules.settings.implement.BooleanSetting;
import fun.eversense.client.modules.settings.implement.FloatSetting;
import fun.eversense.client.modules.settings.implement.ModeSetting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BackSwords extends Module {

    public static BackSwords INSTANCE = new BackSwords();

    private final BooleanSetting self = new BooleanSetting("На себя", true);
    private final BooleanSetting players = new BooleanSetting("На игроков", false);
    private final BooleanSetting drawAnimation = new BooleanSetting("Анимация доставания", true);
    private final ModeSetting colorMode = new ModeSetting("Режим цвета", "Client Theme", "Client Theme", "Custom");
    private final FloatSetting customColorR = new FloatSetting("Красный", 232f, 0f, 255f, 1f)
            .visible(() -> colorMode.is("Custom"));
    private final FloatSetting customColorG = new FloatSetting("Зелёный", 28f, 0f, 255f, 1f)
            .visible(() -> colorMode.is("Custom"));
    private final FloatSetting customColorB = new FloatSetting("Синий", 28f, 0f, 255f, 1f)
            .visible(() -> colorMode.is("Custom"));
    private final FloatSetting scale = new FloatSetting("Масштаб", 1.0f, 0.5f, 1.5f, 0.05f);
    private final FloatSetting glowStrength = new FloatSetting("Сила свечения", 35.0f, 10.0f, 100.0f, 5.0f);
    private final FloatSetting opacity = new FloatSetting("Прозрачность", 200.0f, 50.0f, 255.0f, 5.0f);

    private final Map<UUID, PlayerAnimationState> stateMap = new HashMap<>();

    private static final int SEGMENTS = 12;
    private static final int SEGMENTS_FINE = 16;
    private static final int BLADE_SEGS = 10;
    private static final float TAPER_START = 0.82f;

    public BackSwords() {
        super("BackSwords", "Красивый меч за спиной с анимацией", ModuleCategory.RENDER);
        addSettings(self, players, drawAnimation, colorMode, customColorR, customColorG, customColorB, 
                    scale, glowStrength, opacity);
    }

    private static class PlayerAnimationState {
        boolean holdingSword;
        float animProgress;
        long lastUpdate;
    }

    private PlayerAnimationState getOrAddState(LivingEntity entity) {
        return stateMap.computeIfAbsent(entity.getUuid(), id -> {
            PlayerAnimationState state = new PlayerAnimationState();
            state.holdingSword = isSwordDrawn(entity);
            state.animProgress = state.holdingSword ? 1.0f : 0.0f;
            state.lastUpdate = System.currentTimeMillis();
            return state;
        });
    }

    private boolean isHoldingSword(LivingEntity entity) {
        if (entity == null) return false;
        return entity.getMainHandStack().getItem() instanceof SwordItem;
    }

    private boolean isSwordDrawn(LivingEntity entity) {
        return isHoldingSword(entity);
    }

    private void updateAnimationState(PlayerAnimationState state, LivingEntity entity) {
        long now = System.currentTimeMillis();
        long delta = now - state.lastUpdate;
        state.lastUpdate = now;

        if (delta < 0) delta = 0;
        if (delta > 100) delta = 100;

        boolean isCurrentlyHolding = isSwordDrawn(entity);
        state.holdingSword = isCurrentlyHolding;

        if (isCurrentlyHolding) {
            if (state.animProgress < 1.0f) {
                state.animProgress += delta / 400.0f;
                if (state.animProgress > 1.0f) {
                    state.animProgress = 1.0f;
                }
            }
        } else {
            if (state.animProgress > 0.0f) {
                state.animProgress -= delta / 400.0f;
                if (state.animProgress < 0.0f) {
                    state.animProgress = 0.0f;
                }
            }
        }
    }

    private boolean shouldRenderOn(PlayerEntity player) {
        boolean isLocalPlayer = player == mc.player;
        if (isLocalPlayer) {
            return self.isState();
        }
        return players.isState();
    }

    @EventLink
    public void onRender3D(Event3DRender event) {
        if (mc.player == null || mc.world == null) return;

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == null || !shouldRenderOn(player)) continue;

            PlayerAnimationState state = getOrAddState(player);
            updateAnimationState(state, player);

            float p = state.animProgress;
            if (p <= 0.0f) continue;

            MatrixStack matrixStack = event.getMatrices();
            matrixStack.push();

            // Позиционирование относительно игрока
            double x = MathHelper.lerp(event.getTickDelta(), player.prevX, player.getX());
            double y = MathHelper.lerp(event.getTickDelta(), player.prevY, player.getY());
            double z = MathHelper.lerp(event.getTickDelta(), player.prevZ, player.getZ());

            matrixStack.translate(x - mc.gameRenderer.getCamera().getPos().x,
                                y - mc.gameRenderer.getCamera().getPos().y,
                                z - mc.gameRenderer.getCamera().getPos().z);

            matrixStack.translate(0.0, player.getHeight() * 0.7, 0.0);
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-player.bodyYaw));
            matrixStack.translate(0.0, 0.0, 0.23);
            matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-45f));
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(15f));

            float scaleVal = scale.get();
            matrixStack.scale(scaleVal, scaleVal, scaleVal);

            int accentColor = getAccentColor();

            beginRenderState();
            renderScabbard(matrixStack, accentColor);

            boolean renderSheathedHilt = p < 0.5f;
            if (renderSheathedHilt) {
                renderHilt(matrixStack, 0.475F, 0.49F, 0.26F, 0.52F, 0.045F, 5, 0.74F, accentColor);
            }

            endRenderState();

            matrixStack.pop();
        }
    }

    private int getAccentColor() {
        if (colorMode.is("Client Theme")) {
            return ColorUtils.getThemeColor();
        } else {
            int r = (int) customColorR.get();
            int g = (int) customColorG.get();
            int b = (int) customColorB.get();
            return ColorUtils.rgba(r, g, b, 255);
        }
    }

    private void renderScabbard(MatrixStack ms, int accentColor) {
        drawChamsCylinder(ms, 0, -0.475F, 0, 0.020F, 0.013F, 0.95F, SEGMENTS, 0xFF1C1C1C, accentColor);
        drawChamsCylinder(ms, 0, -0.490F, 0, 0.021F, 0.014F, 0.035F, SEGMENTS, 0xFFD4AF37, 0xFFD4AF37);
        drawChamsCylinder(ms, 0, 0.12F, 0, 0.023F, 0.016F, 0.018F, SEGMENTS, accentColor, accentColor);
        drawChamsCylinder(ms, 0, 0.20F, 0, 0.023F, 0.016F, 0.018F, SEGMENTS, accentColor, accentColor);
        drawChamsCylinder(ms, 0, 0.26F, 0, 0.022F, 0.015F, 0.014F, SEGMENTS, accentColor, accentColor);
        drawChamsCylinder(ms, 0, 0.32F, 0, 0.022F, 0.015F, 0.014F, SEGMENTS, accentColor, accentColor);
    }

    private void renderHilt(MatrixStack ms, float tsubaY, float tsukaY, float tsukaH,
                            float wrapStart, float wrapStep, int wrapCount,
                            float kashiraY, int accentColor) {
        drawChamsCylinder(ms, 0, tsubaY, 0, 0.048F, 0.038F, 0.014F, SEGMENTS_FINE, 0xFFD4AF37, 0xFFD4AF37);
        drawChamsCylinder(ms, 0, tsubaY + 0.001F, 0, 0.042F, 0.032F, 0.012F, SEGMENTS_FINE, 0xFF1C1C1C, accentColor);
        drawChamsCylinder(ms, 0, tsukaY, 0, 0.015F, 0.011F, tsukaH, 10, 0xFF1C1C1C, accentColor);

        for (int i = 0; i < wrapCount; i++) {
            float wy = wrapStart + wrapStep * i;
            drawChamsCylinder(ms, 0, wy, 0, 0.019F, 0.015F, 0.016F, 8, accentColor, accentColor);
        }

        drawChamsCylinder(ms, 0, kashiraY, 0, 0.017F, 0.013F, 0.018F, 10, 0xFFD4AF37, 0xFFD4AF37);
    }

    private void beginRenderState() {
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.defaultBlendFunc();
        GL11.glShadeModel(GL11.GL_SMOOTH);
    }

    private void endRenderState() {
        RenderSystem.enableCull();
        GL11.glShadeModel(GL11.GL_FLAT);
        RenderSystem.disableBlend();
    }

    private void drawChamsCylinder(MatrixStack stack, float cx, float cy, float cz,
                                   float rx, float rz, float height, int segments,
                                   int baseColor, int accentColor) {
        int glowColor = ColorUtils.interpolateColor(accentColor, 0xFFFFFFFF, 0.35f);
        int glowAlpha = (int) glowStrength.get();
        int baseAlpha = (int) opacity.get();
        float e = 0.004F;

        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        drawCylinder(stack, cx, cy - e, cz, rx + e, rz + e, height + e * 2, segments,
                ColorUtils.applyOpacity(glowColor, glowAlpha / 255f));

        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        drawCylinder(stack, cx, cy, cz, rx, rz, height, segments,
                ColorUtils.applyOpacity(baseColor, baseAlpha / 255f));

        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        drawCylinderOutline(stack, cx, cy, cz, rx, rz, height, segments,
                ColorUtils.applyOpacity(accentColor, 180 / 255f));
    }

    private void drawCylinder(MatrixStack stack, float cx, float cy, float cz,
                              float rx, float rz, float height, int seg, int color) {
        Matrix4f m = stack.peek().getPositionMatrix();
        int cr = ColorUtils.red(color), cg = ColorUtils.green(color), 
            cb = ColorUtils.blue(color), ca = ColorUtils.alpha(color);

        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        
        for (int i = 0; i < seg; i++) {
            float a1 = (float) (2.0 * Math.PI * i / seg);
            float a2 = (float) (2.0 * Math.PI * (i + 1) / seg);
            float cos1 = (float) Math.cos(a1), sin1 = (float) Math.sin(a1);
            float cos2 = (float) Math.cos(a2), sin2 = (float) Math.sin(a2);

            float x1 = cx + rx * cos1, z1 = cz + rz * sin1;
            float x2 = cx + rx * cos2, z2 = cz + rz * sin2;

            buffer.vertex(m, x1, cy, z1).color(cr, cg, cb, ca);
            buffer.vertex(m, x2, cy, z2).color(cr, cg, cb, ca);
            buffer.vertex(m, x2, cy + height, z2).color(cr, cg, cb, ca);
            buffer.vertex(m, x1, cy + height, z1).color(cr, cg, cb, ca);
        }
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
        
        for (int i = 0; i < seg; i++) {
            float a1 = (float) (2.0 * Math.PI * i / seg);
            float a2 = (float) (2.0 * Math.PI * (i + 1) / seg);
            float cos1 = (float) Math.cos(a1), sin1 = (float) Math.sin(a1);
            float cos2 = (float) Math.cos(a2), sin2 = (float) Math.sin(a2);

            float x1 = cx + rx * cos1, z1 = cz + rz * sin1;
            float x2 = cx + rx * cos2, z2 = cz + rz * sin2;

            buffer.vertex(m, cx, cy + height, cz).color(cr, cg, cb, ca);
            buffer.vertex(m, x1, cy + height, z1).color(cr, cg, cb, ca);
            buffer.vertex(m, x2, cy + height, z2).color(cr, cg, cb, ca);

            buffer.vertex(m, cx, cy, cz).color(cr, cg, cb, ca);
            buffer.vertex(m, x2, cy, z2).color(cr, cg, cb, ca);
            buffer.vertex(m, x1, cy, z1).color(cr, cg, cb, ca);
        }
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    private void drawCylinderOutline(MatrixStack stack, float cx, float cy, float cz,
                                     float rx, float rz, float height, int seg, int color) {
        Matrix4f m = stack.peek().getPositionMatrix();
        int cr = ColorUtils.red(color), cg = ColorUtils.green(color), 
            cb = ColorUtils.blue(color), ca = ColorUtils.alpha(color);

        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        RenderSystem.lineWidth(1.2f);

        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        
        for (int i = 0; i < seg; i++) {
            float a1 = (float) (2.0 * Math.PI * i / seg);
            float a2 = (float) (2.0 * Math.PI * (i + 1) / seg);
            float cos1 = (float) Math.cos(a1), sin1 = (float) Math.sin(a1);
            float cos2 = (float) Math.cos(a2), sin2 = (float) Math.sin(a2);

            float x1 = cx + rx * cos1, z1 = cz + rz * sin1;
            float x2 = cx + rx * cos2, z2 = cz + rz * sin2;

            buffer.vertex(m, x1, cy, z1).color(cr, cg, cb, ca);
            buffer.vertex(m, x2, cy, z2).color(cr, cg, cb, ca);

            buffer.vertex(m, x1, cy + height, z1).color(cr, cg, cb, ca);
            buffer.vertex(m, x2, cy + height, z2).color(cr, cg, cb, ca);

            if (i % 3 == 0) {
                buffer.vertex(m, x1, cy, z1).color(cr, cg, cb, ca);
                buffer.vertex(m, x1, cy + height, z1).color(cr, cg, cb, ca);
            }
        }
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }

    @Override
    public void onDisable() {
        stateMap.clear();
        super.onDisable();
    }
}
