package fun.eversense.client.modules.impl.render.base.implement;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import fun.eversense.eversense;
import fun.eversense.api.events.implement.EventRender;
import fun.eversense.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.eversense.api.utils.animation.AnimationUtils;
import fun.eversense.api.utils.animation.Easings;
import fun.eversense.api.utils.color.ColorUtils;
import fun.eversense.api.utils.draggable.Draggable;
import fun.eversense.api.utils.input.KeyBoardUtils;
import fun.eversense.api.utils.render.RenderUtils;
import fun.eversense.api.utils.render.fonts.msdf.Font;
import fun.eversense.api.utils.render.fonts.msdf.Fonts;
import fun.eversense.client.modules.impl.misc.ServerHelper;
import fun.eversense.client.modules.impl.render.base.InterfaceProcessing;

import java.util.ArrayList;
import java.util.List;

public class HelperBinds extends InterfaceProcessing {
    private final AnimationUtils widthAnimation = new AnimationUtils(80.0f, 10.5f, Easings.QUAD_OUT);

    public HelperBinds(Draggable draggable) {
        super(draggable);
    }

    private Font issue(int size) {
        return Fonts.getFont("suisse", size);
    }

    @Override
    public void onRender(EventRender.Default eventRender) {
        List<ServerHelper.HelperBind> binds = getVisibleBinds();
        if (binds.isEmpty()) {
            widthAnimation.update(0.0f);
            draggable.setWidth(0.0f);
            draggable.setHeight(0.0f);
            return;
        }

        if (ModuleClass.interfaceModule.style.is("New")) {
            NewStyle(eventRender, binds);
        } else {
            WaveStyle(eventRender, binds);
        }

        super.onRender(eventRender);
    }

    private List<ServerHelper.HelperBind> getVisibleBinds() {
        ServerHelper serverHelper = ServerHelper.INSTANCE;
        List<ServerHelper.HelperBind> binds = new ArrayList<>();
        if (serverHelper == null) return binds;

        List<ServerHelper.HelperBind> helperBinds = serverHelper.isLonyMode()
                ? serverHelper.getLonyHelperBinds()
                : serverHelper.getSpookyHelperBinds();

        for (ServerHelper.HelperBind bind : helperBinds) {
            if (bind.bind().getKey() != -1) {
                binds.add(bind);
            }
        }

        return binds;
    }

    private void NewStyle(EventRender.Default eventRender, List<ServerHelper.HelperBind> binds) {
        MatrixStack matrices = eventRender.getContext().getMatrices();
        float x = draggable.getX();
        float y = draggable.getY();
        
        // Получаем цвет темы
        int themeColor;
        if (!eversense.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")) {
            themeColor = eversense.INSTANCE.themeStorage.getThemes().getTheme().color[0];
        } else {
            themeColor = ColorUtils.getThemeColor();
        }
        
        int whiteColor = ColorUtils.rgba(255, 255, 255, 255);
        int grayBgColor = ColorUtils.rgba(35, 37, 40, 100);
        int blackBgColor = ColorUtils.rgba(0, 0, 0, 180);
        
        Font keyFont = issue(13);
        float height = 20.0f;
        float itemSize = 10.0f;
        float itemScale = 0.63f;
        float fontGap = 3.0f;
        float cellGap = 5.5f;
        float sidePadding = 7.0f;
        float width = getCompactWidth(binds, keyFont, itemSize, fontGap, cellGap, sidePadding, 70.0f);

        widthAnimation.update(width);
        float animatedWidth = widthAnimation.getValue();

        // Рисуем общий фон (черный) - с закруглениями
        RenderUtils.drawRoundedRect(matrices, x, y, animatedWidth, height, 3f, blackBgColor);
        
        // Рисуем заголовок (серый фон) - с закруглениями сверху
        RenderUtils.drawRoundedRect(matrices, x, y, animatedWidth, height, 3f, grayBgColor);

        if (binds.isEmpty()) {
            issue(13).draw(matrices, "Helper", x + 6.0f, y + 7.0f, whiteColor);
            draggable.setWidth(animatedWidth);
            draggable.setHeight(height);
            return;
        }

        drawCompactBinds(eventRender.getContext(), binds, keyFont, x, y, height, itemSize, itemScale, fontGap, cellGap, sidePadding, 7.5f);

        draggable.setWidth(animatedWidth);
        draggable.setHeight(height);
    }

    private void WaveStyle(EventRender.Default eventRender, List<ServerHelper.HelperBind> binds) {
        MatrixStack matrices = eventRender.getContext().getMatrices();
        float x = draggable.getX();
        float y = draggable.getY();

        int time = (int) ((System.currentTimeMillis() % 2000) / 2000f * 360f);
        int leftTop = ColorUtils.getThemeColor(time);
        int leftBottom = ColorUtils.getThemeColor(time + 30);
        int centerTop = ColorUtils.getThemeColor(time + 90);
        int centerBottom = ColorUtils.getThemeColor(time + 120);
        int rightTop = ColorUtils.getThemeColor(time + 180);
        int rightBottom = ColorUtils.getThemeColor(time + 210);

        Font keyFont = issue(14);
        float height = 22.0f;
        float itemSize = 11.0f;
        float itemScale = 0.69f;
        float fontGap = 3.5f;
        float cellGap = 6.0f;
        float sidePadding = 7.0f;
        float width = getCompactWidth(binds, keyFont, itemSize, fontGap, cellGap, sidePadding, 72.0f);

        widthAnimation.update(width);
        float animatedWidth = widthAnimation.getValue();

        if (binds.isEmpty()) {
            RenderUtils.drawWaveHudHeader(matrices, x, y, animatedWidth, 15, 0,
                    10, 10, leftTop, leftBottom, centerTop, centerBottom, rightTop, rightBottom);
            String title = "helper";
            float titleX = x + (animatedWidth - issue(15).getWidth(title)) / 2.0f;
            issue(15).drawStringWithShadow(matrices, title, titleX, y + 5.0f, -1);
            draggable.setWidth(animatedWidth);
            draggable.setHeight(18.0f);
            return;
        }

        RenderUtils.drawWaveHudPanel(matrices, x, y, animatedWidth, height, ColorUtils.rgba(25, 25, 25, 150),
                3.5f, 0, 10, 10,
                leftTop, leftBottom, centerTop, centerBottom, rightTop, rightBottom);

        drawCompactBinds(eventRender.getContext(), binds, keyFont, x, y, height, itemSize, itemScale, fontGap, cellGap, sidePadding, 9.5f);

        draggable.setWidth(animatedWidth);
        draggable.setHeight(height);
    }

    private float getCompactWidth(List<ServerHelper.HelperBind> binds, Font keyFont, float itemSize, float fontGap, float cellGap, float sidePadding, float emptyWidth) {
        if (binds.isEmpty()) {
            return emptyWidth;
        }

        float width = sidePadding * 2.0f;
        for (int i = 0; i < binds.size(); i++) {
            String keyName = KeyBoardUtils.getBindName(binds.get(i).bind().getKey());
            width += itemSize + fontGap + keyFont.getWidth(keyName);
            if (i < binds.size() - 1) {
                width += cellGap;
            }
        }
        return width;
    }

    private void drawCompactBinds(DrawContext context, List<ServerHelper.HelperBind> binds, Font keyFont, float x, float y, float height,
                                  float itemSize, float itemScale, float fontGap, float cellGap, float sidePadding, float textOffsetY) {
        MatrixStack matrices = context.getMatrices();
        float offsetX = x + sidePadding;
        float itemY = y + (height - itemSize) * 0.5f;
        float textY = y + textOffsetY;

        for (int i = 0; i < binds.size(); i++) {
            ServerHelper.HelperBind bind = binds.get(i);
            String keyName = KeyBoardUtils.getBindName(bind.bind().getKey());
            drawItemIcon(context, new ItemStack(bind.item()), offsetX, itemY, itemScale);
            keyFont.draw(matrices, keyName, offsetX + itemSize + fontGap, textY, ColorUtils.rgba(255, 255, 255, 240));

            offsetX += itemSize + fontGap + keyFont.getWidth(keyName);
            if (i < binds.size() - 1) {
                offsetX += cellGap;
            }
        }
    }

    private void drawItemIcon(DrawContext context, ItemStack stack, float x, float y, float scale) {
        MatrixStack matrices = context.getMatrices();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        matrices.push();
        matrices.translate(x, y, 0.0f);
        matrices.scale(scale, scale, 1.0f);
        context.drawItem(stack, 0, 0);
        matrices.pop();
        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();
    }

    private int getThemeColor() {
        if (!eversense.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")) {
            return eversense.INSTANCE.themeStorage.getThemes().getTheme().color[0];
        }
        return ColorUtils.getThemeColor();
    }
}
