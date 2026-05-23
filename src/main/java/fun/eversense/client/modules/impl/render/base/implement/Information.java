package fun.eversense.client.modules.impl.render.base.implement;

import fun.eversense.eversense;
import fun.eversense.api.events.implement.EventRender;
import fun.eversense.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.eversense.api.utils.color.ColorUtils;
import fun.eversense.api.utils.draggable.Draggable;
import fun.eversense.api.utils.math.MathUtils;
import fun.eversense.api.utils.render.RenderUtils;
import fun.eversense.api.utils.render.fonts.msdf.Font;
import fun.eversense.api.utils.render.fonts.msdf.Fonts;
import fun.eversense.client.modules.impl.render.base.InterfaceProcessing;

public class Information extends InterfaceProcessing {

    public Information(Draggable draggable) {
        super(draggable);
    }

    @Override
    public void onRender(EventRender.Default eventRender) {
        if (ModuleClass.interfaceModule.style.is("New")) NewStyle(eventRender);
        else if (ModuleClass.interfaceModule.style.is("Wave")) WaveStyle(eventRender);
        else DefaultStyle(eventRender);
        super.onRender(eventRender);
    }

    public void DefaultStyle(EventRender.Default eventRender) {
        float x = draggable.getX(), y = draggable.getY();
        Font font = Fonts.getFont("suisse", 13);
        Font iconFont = Fonts.getFont("icon", 16);
        Font smallIconFont = Fonts.getFont("icon", 15);

        int colorTheme;
        if (!eversense.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")) {
            colorTheme = eversense.INSTANCE.themeStorage.getThemes().getTheme().color[0];
        } else {
            colorTheme = ColorUtils.getThemeColor();
        }
        boolean drawSquares = isUnusualRectType();

        int px = (int)Math.floor(mc.player.getX());
        int py = (int)Math.floor(mc.player.getY());
        int pz = (int)Math.floor(mc.player.getZ());

        float height = 20f;
        double bps = MathUtils.calculateBPS();
        String xValue = String.valueOf(px);
        String yValue = String.valueOf(py);
        String zValue = String.valueOf(pz);
        String coordsText = xValue + "x " + yValue + "y " + zValue + "z";
        String bpsValue = formatTwoDecimals(bps);
        String bpsSuffix = " b/s";
        float widthbps = font.getWidth(bpsValue + bpsSuffix);
        float xbps = x + 17 + widthbps;
        float widthCords = font.getWidth(coordsText);
        float totalWidth = 13 + widthCords + widthbps + 2 + 13.8f;

        // Рисуем панель с эффектом свечения (как в WaterMark)
        int glowColor = ColorUtils.applyAlpha(colorTheme, 0.25f);
        RenderUtils.drawShadow(eventRender.getContext().getMatrices(), x, y, totalWidth, height, 8, 12f, glowColor);
        RenderUtils.drawDefaultHudThemedPanel(eventRender.getContext().getMatrices(), x, y, totalWidth, height, 3.5f, 4f, colorTheme);
        
        if (drawSquares) {
            RenderUtils.drawHudSquarePattern(eventRender.getContext().getMatrices(), x, y, totalWidth, height, colorTheme);
        }

        float speedTextX = x + 13.5f;
        float bpsValueWidth = font.getWidth(bpsValue);
        font.draw(eventRender.getContext().getMatrices(), bpsValue, speedTextX, y + 8.5, -1);
        font.draw(eventRender.getContext().getMatrices(), bpsSuffix, speedTextX + bpsValueWidth - 2, y + 8.5, colorTheme);
        float coordsX = xbps + 9f;
        font.draw(eventRender.getContext().getMatrices(), xValue, coordsX, y + 8.5, -1);
        coordsX += font.getWidth(xValue);
        font.draw(eventRender.getContext().getMatrices(), "x", coordsX - 1, y + 8.5, colorTheme);
        coordsX += font.getWidth("x ");
        font.draw(eventRender.getContext().getMatrices(), yValue, coordsX, y + 8.5, -1);
        coordsX += font.getWidth(yValue);
        font.draw(eventRender.getContext().getMatrices(), "y", coordsX - 1, y + 8.5, colorTheme);
        coordsX += font.getWidth("y ");
        font.draw(eventRender.getContext().getMatrices(), zValue, coordsX, y + 8.5, -1);
        coordsX += font.getWidth(zValue);
        font.draw(eventRender.getContext().getMatrices(), "z", coordsX - 1, y + 8.5, colorTheme);
        iconFont.draw(eventRender.getContext().getMatrices(), "c", x + 3.25, y + 8.5, colorTheme);
        smallIconFont.draw(eventRender.getContext().getMatrices(), "x", xbps - 1, y + 8.75, colorTheme);


        draggable.setHeight(height);
        draggable.setWidth(totalWidth);
    }

    public void NewStyle(EventRender.Default eventRender) {
        float baseX = draggable.getX(), y = draggable.getY();
        var matrices = eventRender.getContext().getMatrices();
        
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
        
        var textFont = Fonts.getFont("suisse", 13);
        var iconFont = Fonts.getFont("icon", 14);
        
        if (textFont == null) textFont = Fonts.getFont("suisse", 12);
        if (iconFont == null) iconFont = Fonts.getFont("icon", 14);
        
        // Получаем данные
        int px = (int)Math.floor(mc.player.getX());
        int py = (int)Math.floor(mc.player.getY());
        int pz = (int)Math.floor(mc.player.getZ());
        double bps = MathUtils.calculateBPS();
        
        String bpsValue = formatTwoDecimals(bps);
        String bpsText = bpsValue + " b/s";
        String coordsText = px + "x " + py + "y " + pz + "z";
        
        // Подсчитываем максимальную ширину
        float bpsWidth = textFont.getStringWidth(bpsText);
        float coordsWidth = textFont.getStringWidth(coordsText);
        float maxWidth = Math.max(bpsWidth, coordsWidth) + 30f; // отступы + иконки
        
        // Размеры
        float headerHeight = 18f;
        float itemHeight = 14f;
        float totalHeight = headerHeight + (2 * itemHeight); // 2 строки: bps и coords
        float width = maxWidth + 10f;
        
        // Рисуем общий фон (черный) - с закруглениями
        RenderUtils.drawRoundedRect(matrices, baseX, y, width, totalHeight, 3f, blackBgColor);
        
        // Рисуем заголовок (серый фон) - с закруглениями сверху
        RenderUtils.drawRoundedRect(matrices, baseX, y, width, headerHeight, 3f, grayBgColor);
        
        // Текст "Information" и иконка в заголовке
        String headerText = "Information";
        textFont.drawString(matrices, headerText, baseX + 6f, y + 7f, whiteColor);
        
        // Иконка справа в заголовке
        String headerIconGlyph = "x"; // иконка координат
        float headerIconX = baseX + width - iconFont.getStringWidth(headerIconGlyph) - 6f;
        iconFont.drawString(matrices, headerIconGlyph, headerIconX, y + 7.5f, themeColor);
        
        // Рисуем содержимое
        float offsetY = headerHeight + 4f;
        
        // Строка BPS
        textFont.drawString(matrices, bpsValue, baseX + 6f, y + offsetY + 1f, whiteColor);
        float bpsValueWidth = textFont.getStringWidth(bpsValue);
        textFont.drawString(matrices, " b/s", baseX + 6f + bpsValueWidth, y + offsetY + 1f, themeColor);
        
        offsetY += itemHeight;
        
        // Строка координат
        String xValue = String.valueOf(px);
        String yValue = String.valueOf(py);
        String zValue = String.valueOf(pz);
        
        float coordsX = baseX + 6f;
        textFont.drawString(matrices, xValue, coordsX, y + offsetY + 1f, whiteColor);
        coordsX += textFont.getStringWidth(xValue);
        textFont.drawString(matrices, "x ", coordsX, y + offsetY + 1f, themeColor);
        coordsX += textFont.getStringWidth("x ");
        
        textFont.drawString(matrices, yValue, coordsX, y + offsetY + 1f, whiteColor);
        coordsX += textFont.getStringWidth(yValue);
        textFont.drawString(matrices, "y ", coordsX, y + offsetY + 1f, themeColor);
        coordsX += textFont.getStringWidth("y ");
        
        textFont.drawString(matrices, zValue, coordsX, y + offsetY + 1f, whiteColor);
        coordsX += textFont.getStringWidth(zValue);
        textFont.drawString(matrices, "z", coordsX, y + offsetY + 1f, themeColor);
        
        draggable.setWidth(width);
        draggable.setHeight(totalHeight);
    }

    public void WaveStyle(EventRender.Default eventRender) {
        float x = draggable.getX(), y = draggable.getY();

        float time = (System.currentTimeMillis() % 2000) / 2000f * 360f;

        int leftTop1 = ColorUtils.getThemeColor((int) time);
        int leftBottom1 = ColorUtils.getThemeColor((int) (time + 30));
        int centerTop1 = ColorUtils.getThemeColor((int) (time + 90));
        int centerBottom1 = ColorUtils.getThemeColor((int) (time + 120));
        int rightTop1 = ColorUtils.getThemeColor((int) (time + 180));
        int rightBottom1 = ColorUtils.getThemeColor((int) (time + 210));

        String title = "coords";
        String xText = "x: " + (int) mc.player.getPos().getX();
        String yText = "y: " + (int) mc.player.getPos().getY();
        String zText = "z: " + (int) mc.player.getPos().getZ();

        var font = Fonts.getFont("suisse", 15);

        float xWidth = font.getWidth(xText);
        float yWidth = font.getWidth(yText);
        float zWidth = font.getWidth(zText);
        float titleWidth = font.getWidth(title);

        float maxCoordWidth = Math.max(xWidth, Math.max(yWidth, zWidth));

        float padding = 9f;
        float rectWidth = maxCoordWidth + padding;
        float rectHeight = 40;

        rectWidth = Math.max(rectWidth, 35);

        float centerX = x + rectWidth / 2;

        RenderUtils.drawWaveHudPanel(eventRender.getContext().getMatrices(), x, y, rectWidth, rectHeight, ColorUtils.rgba(25, 25, 25, 150),
                3.5f, 0, 10, 10,
                leftTop1, leftBottom1, centerTop1, centerBottom1, rightTop1, rightBottom1);

        float barPadding = 5f;
        RenderUtils.drawWaveHudHeader(eventRender.getContext().getMatrices(), x + barPadding, y + 12, rectWidth - barPadding * 2, 2.5f, 0,
                10, 10, leftTop1, leftBottom1, centerTop1, centerBottom1, rightTop1, rightBottom1);

        font.drawStringWithShadow(eventRender.getContext().getMatrices(), title, centerX - titleWidth / 2, y + 5, -1);
        font.drawStringWithShadow(eventRender.getContext().getMatrices(), xText, x + 4.5f, y + 17f, -1);
        font.drawStringWithShadow(eventRender.getContext().getMatrices(), yText, x + 4.5f, y + 24f, -1);
        font.drawStringWithShadow(eventRender.getContext().getMatrices(), zText, x + 4.5f, y + 31f, -1);

        float bpsX = x + rectWidth + 5;
        float bpsY = y;

        double bps = MathUtils.calculateBPS();

        String bpsTitle = "bps";
        String bpsText = String.valueOf((int) bps);

        float bpsTitleWidth = font.getWidth(bpsTitle);
        float bpsTextWidth = font.getWidth(bpsText);

        float bpsRectWidth = Math.max(bpsTitleWidth, bpsTextWidth) + 10;
        float bpsRectHeight = 25;

        bpsRectWidth = Math.max(bpsRectWidth, 30);

        float bpsCenterX = bpsX + bpsRectWidth / 2;

        RenderUtils.drawWaveHudPanel(eventRender.getContext().getMatrices(), bpsX, bpsY, bpsRectWidth, bpsRectHeight, ColorUtils.rgba(25, 25, 25, 150),
                3.5f, 0, 10, 10,
                leftTop1, leftBottom1, centerTop1, centerBottom1, rightTop1, rightBottom1);

        RenderUtils.drawWaveHudHeader(eventRender.getContext().getMatrices(), bpsX + barPadding, bpsY + 12, bpsRectWidth - barPadding * 2, 2.5f, 0,
                10, 10, leftTop1, leftBottom1, centerTop1, centerBottom1, rightTop1, rightBottom1);

        font.drawStringWithShadow(eventRender.getContext().getMatrices(), bpsTitle, bpsCenterX - bpsTitleWidth / 2, bpsY + 5, -1);
        font.drawStringWithShadow(eventRender.getContext().getMatrices(), bpsText, bpsCenterX - bpsTextWidth / 2, bpsY + 17f, -1);

        float totalWidth = rectWidth + 5 + bpsRectWidth;
        draggable.setWidth(totalWidth);
        draggable.setHeight(rectHeight);
    }

    private String formatTwoDecimals(double value) {
        int scaled = (int) Math.round(value * 100.0D);
        int fraction = Math.abs(scaled % 100);
        return (scaled / 100) + "." + (fraction < 10 ? "0" : "") + fraction;
    }
}
