package fun.eversense.client.modules.impl.render.base.implement;

import net.minecraft.client.util.math.MatrixStack;
import fun.eversense.eversense;
import fun.eversense.api.events.implement.EventRender;
import fun.eversense.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.eversense.api.utils.color.ColorUtils;
import fun.eversense.api.utils.draggable.Draggable;
import fun.eversense.api.utils.render.RenderUtils;
import fun.eversense.api.utils.render.fonts.msdf.Fonts;
import fun.eversense.client.modules.impl.render.base.InterfaceProcessing;

import java.awt.*;

public class WaterMark extends InterfaceProcessing {
    private boolean showFps = true;
    private boolean showMs = true;
    private boolean showServer = true;
    private boolean showTps = true;

    public static String getUsername() {
        return "Admen";
    }

    public static String getUID() {
        return "1";
    }

    public WaterMark(Draggable draggable) {
        super(draggable);
    }

    public boolean isShowFps() {
        return showFps;
    }

    public void setShowFps(boolean showFps) {
        this.showFps = showFps;
    }

    public boolean isShowMs() {
        return showMs;
    }

    public void setShowMs(boolean showMs) {
        this.showMs = showMs;
    }

    public boolean isShowServer() {
        return showServer;
    }

    public void setShowServer(boolean showServer) {
        this.showServer = showServer;
    }

    public boolean isShowTps() {
        return showTps;
    }

    public void setShowTps(boolean showTps) {
        this.showTps = showTps;
    }

    @Override
    public void onRender(EventRender.Default eventRender) {
        if (ModuleClass.interfaceModule.style.is("Wave")) WaveStyle(eventRender);
        else if (ModuleClass.interfaceModule.style.is("New")) NewStyle(eventRender);
        else DefaultStyle(eventRender);
        super.onRender(eventRender);
    }

    public void DefaultStyle(EventRender.Default eventRender) {
        var matrices = eventRender.getContext().getMatrices();
        float x = draggable.getX();
        float y = draggable.getY();
        var logoFont = Fonts.getFont("logo", 20);
        var iconNew14 = Fonts.getFont("iconnew", 14);
        var iconNew15 = Fonts.getFont("iconnew", 15);
        var icon14 = Fonts.getFont("icon", 14);
        var statsIconFont = Fonts.getFont("eversense", 14);
        if (statsIconFont == null) statsIconFont = iconNew14 != null ? iconNew14 : icon14;
        var suisse13 = Fonts.getFont("suisse", 13);
        var suisse15 = Fonts.getFont("suisse", 15);

        float mainRectH = 20;
        int themeColor;
        if (!eversense.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")) {
            themeColor = eversense.INSTANCE.themeStorage.getThemes().getTheme().color[0];
        } else {
            themeColor = ColorUtils.getThemeColor();
        }
        
        boolean drawSquares = isUnusualRectType();
        float rect2Pad = 4;
        String username = getUsername();
        int whiteColor = new Color(255, 255, 255, 255).getRGB();
        float textY = y + 8.5f;

        // Крутой текст бренда с градиентом
        String brandText = "eversense";
        float brandTextX = x + 6f;
        float brandTextW = suisse15.getStringWidth(brandText);

        float mainRectX = x;
        float mainRectY = y;
        float mainRectW = brandTextX + brandTextW + 8f - x;

        // Рисуем главную панель с улучшенным дизайном
        int glowColor = ColorUtils.applyAlpha(themeColor, 0.25f);
        RenderUtils.drawShadow(matrices, mainRectX, mainRectY, mainRectW, mainRectH, 8, 12f, glowColor);
        RenderUtils.drawDefaultHudThemedPanel(matrices, mainRectX, mainRectY, mainRectW, mainRectH, 3.5f, 4f, themeColor);
        
        if (drawSquares) {
            RenderUtils.drawHudSquarePattern(matrices, mainRectX, mainRectY, mainRectW, mainRectH, themeColor);
        }

        // Рисуем текст бренда с градиентом
        int gradientStart = themeColor;
        int gradientEnd = ColorUtils.interpolateColor(themeColor, ColorUtils.rgba(255, 255, 255, 255), 0.3f);
        suisse15.drawGradientStringHorizontal(matrices, brandText, brandTextX, y + 7.8f, gradientStart, gradientEnd);

        // Вторая панель со статистикой
        float rect2X = mainRectX + mainRectW + 3f;
        float rect2H = 19.5f;
        String userIconGlyph = "e";
        float icon2Y = y + 8.7f;

        String fpsIconGlyph = "j";
        String pingIconGlyph = "f";
        float icon3Y = y + 8.5f;

        int fps = mc != null ? mc.getCurrentFps() : 0;
        String fpsValue = String.valueOf(fps);
        String fpsSuffix = "fps";
        String fpsText = fpsValue + fpsSuffix;

        int ping = 0;
        if (mc != null && mc.player != null && mc.getNetworkHandler() != null) {
            var entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            if (entry != null) ping = entry.getLatency();
        }
        String pingValue = String.valueOf(ping);
        String pingSuffix = "ms";
        String pingText = pingValue + pingSuffix;

        float contentW = rect2Pad;
        contentW += iconNew14.getStringWidth(userIconGlyph) + 2f;
        if (!username.isEmpty()) {
            contentW += suisse13.getStringWidth(username) + 3f;
        }
        if (showFps) {
            contentW += statsIconFont != null ? statsIconFont.getStringWidth(fpsIconGlyph) + 2f : 0f;
            contentW += suisse13.getStringWidth(fpsText) + 3f;
        }
        if (showMs) {
            contentW += statsIconFont != null ? statsIconFont.getStringWidth(pingIconGlyph) + 2f : 0f;
            contentW += suisse13.getStringWidth(pingText) + 3f;
        }
        contentW += rect2Pad;

        float rect2W = contentW;
        
        // Рисуем вторую панель с эффектом свечения
        RenderUtils.drawShadow(matrices, rect2X, mainRectY, rect2W, rect2H, 8, 12f, glowColor);
        RenderUtils.drawDefaultHudThemedPanel(matrices, rect2X, mainRectY, rect2W, rect2H, 3.5f, 4f, themeColor);
        
        if (drawSquares) {
            RenderUtils.drawHudSquarePattern(matrices, rect2X, mainRectY, rect2W, rect2H, themeColor);
        }

        float drawX = rect2X + rect2Pad + 2f;

        iconNew14.drawGradientStringHorizontal(matrices, userIconGlyph, drawX - 1f, icon2Y, gradientStart, gradientEnd);
        drawX += iconNew14.getStringWidth(userIconGlyph) + 2f;

        if (!username.isEmpty()) {
            suisse13.drawString(matrices, username, drawX, textY, whiteColor);
            drawX += suisse13.getStringWidth(username) + 3f;
        }

        if (showFps) {
            if (statsIconFont != null) {
                statsIconFont.drawGradientStringHorizontal(matrices, fpsIconGlyph, drawX, icon3Y, gradientStart, gradientEnd);
                drawX += statsIconFont.getStringWidth(fpsIconGlyph) + 2f;
            }
            suisse13.drawString(matrices, fpsValue, drawX, textY, whiteColor);
            suisse13.drawString(matrices, fpsSuffix, drawX + suisse13.getStringWidth(fpsValue) - 1, textY, themeColor);
            drawX += suisse13.getStringWidth(fpsText) + 3f;
        }

        if (showMs) {
            if (statsIconFont != null) {
                statsIconFont.drawGradientStringHorizontal(matrices, pingIconGlyph, drawX, icon3Y, gradientStart, gradientEnd);
                drawX += statsIconFont.getStringWidth(pingIconGlyph) + 2f;
            }
            suisse13.drawString(matrices, pingValue, drawX, textY, whiteColor);
            suisse13.drawString(matrices, pingSuffix, drawX + suisse13.getStringWidth(pingValue) - 0.5, textY, themeColor);
        }

        String serverName = "Singleplayer";
        if (mc != null) {
            var info = mc.getCurrentServerEntry();
            if (info != null && info.address != null && !info.address.isEmpty()) {
                serverName = info.address;
            }
        }

        boolean showBottom = showServer || showTps;
        float rectBtmY = mainRectY + mainRectH + 3f;
        float rectBtmH = 19.5f;

        int iconSmallSize = 15;
        float iconSmallW = iconNew15.getStringWidth("n");
        float iconSmallY = rectBtmY + 8.3f;
        float serverTextY = rectBtmY + 8.5f;
        String serverDisplayName = formatServerNameForDisplay(serverName);
        float serverTextW = suisse13.getStringWidth(serverDisplayName);
        String extraIconGlyph = "y";
        float extraIconW = iconNew15.getStringWidth(extraIconGlyph);
        float extraIconY = rectBtmY + 8.3f;
        String tpsValue = formatOneDecimal(getServerTps());
        String tpsSuffix = "tps";
        String tpsText = tpsValue + tpsSuffix;
        float tpsTextW = suisse13.getStringWidth(tpsText);
        float rectBtmW = 0f;
        
        if (showBottom) {
            float bottomX = x + rect2Pad + 10f;
            if (showServer) {
                bottomX += iconSmallW + 3f + serverTextW;
            }
            if (showTps) {
                if (showServer) bottomX += 4f;
                bottomX += extraIconW + 3f + tpsTextW;
            }
            rectBtmW = Math.max(50f, (bottomX + rect2Pad) - x);

            // Рисуем нижнюю панель с эффектом свечения
            RenderUtils.drawShadow(matrices, x, rectBtmY, rectBtmW, rectBtmH, 8, 12f, glowColor);
            RenderUtils.drawDefaultHudThemedPanel(matrices, x, rectBtmY, rectBtmW, rectBtmH, 3.5f, 4f, themeColor);
            
            if (drawSquares) {
                RenderUtils.drawHudSquarePattern(matrices, x, rectBtmY, rectBtmW, rectBtmH, themeColor);
            }

            float drawBottomX = x + rect2Pad + 8f;
            if (showServer) {
                iconNew15.drawGradientStringHorizontal(matrices, "n", drawBottomX - 6f, iconSmallY, gradientStart, gradientEnd);
                drawBottomX += iconSmallW + 3f;
                drawServerNameWithThemeParts(matrices, serverDisplayName, drawBottomX, serverTextY, themeColor, whiteColor);
                drawBottomX += serverTextW;
            }
            if (showTps) {
                if (showServer) drawBottomX += 4f;
                iconNew15.drawGradientStringHorizontal(matrices, extraIconGlyph, drawBottomX - 1.5f, extraIconY, gradientStart, gradientEnd);
                drawBottomX += extraIconW + 3f;
                suisse13.drawString(matrices, tpsValue, drawBottomX - 1.75f, serverTextY, whiteColor);
                suisse13.drawString(matrices, tpsSuffix, drawBottomX + suisse13.getStringWidth(tpsValue) - 2.5f, serverTextY, themeColor);
            }
        }

        float totalW = Math.max(mainRectW + 3f + rect2W, rectBtmW);
        draggable.setWidth(totalW);
        draggable.setHeight(showBottom ? (mainRectH + 3f + rectBtmH) : mainRectH);
    }

    public void WaveStyle(EventRender.Default eventRender) {
        float x = draggable.getX(), y = draggable.getY();
        var matrices = eventRender.getContext().getMatrices();
        
        // Шрифты - увеличенные размеры
        var logoFont = Fonts.getFont("museosanscyrl", 26);
        var textFont = Fonts.getFont("museosanscyrl", 18);
        
        if (logoFont == null) logoFont = Fonts.getFont("suisse", 18);
        if (textFont == null) textFont = Fonts.getFont("suisse", 17);
        
        String username = getUsername();
        
        // Получаем информацию
        int fps = mc != null ? mc.getCurrentFps() : 0;
        
        // Получаем пинг
        int ping = 0;
        if (mc != null && mc.player != null && mc.getNetworkHandler() != null) {
            var entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            if (entry != null) ping = entry.getLatency();
        }
        String fpsText = fps + "fps";
        
        // Получаем сервер
        String serverName = "offline";
        if (mc != null) {
            var info = mc.getCurrentServerEntry();
            if (info != null && info.address != null && !info.address.isEmpty()) {
                serverName = info.address;
                int portIndex = serverName.indexOf(':');
                if (portIndex > 0) {
                    serverName = serverName.substring(0, portIndex);
                }
            }
        }
        
        // Получаем время

        
        // Формируем текст
        String text = "|  " + username + "  |  " + fpsText + "  |  " + serverName ;
        
        // Вычисляем размеры
        String logo = "EVERSENSE";
        float logoW = logoFont.getStringWidth(logo);
        float textW = textFont.getStringWidth(text);
        
        float totalWidth = logoW + textW + 5f;
        float totalHeight = 20f;

        // Цвета
        int bgColor = ColorUtils.rgba(11, 11, 20, 255);
        int whiteColor = ColorUtils.rgba(255, 255, 255, 255);
        int cyanColor = ColorUtils.rgba(34, 179, 246, 255);
        
        // Рисуем фон (один прямоугольник)
        RenderUtils.drawRoundedRect(matrices, x, y, totalWidth, totalHeight, 1f, bgColor);
        
        // Рисуем логотип "EVERSENSE" с эффектом тени
        float logoX = x + 5f;
        float logoY = y + 5f;
        
        // Тень логотипа (голубая, смещенная вниз)
        logoFont.drawString(matrices, logo, logoX, logoY - 1f, cyanColor);
        // Основной логотип (белый, смещенный вправо)
        logoFont.drawString(matrices, logo, logoX + 1f, logoY, whiteColor);
        
        // Рисуем текст
        float textX = x + logoW + 5f;
        float textY = logoY + 2f;
        textFont.drawString(matrices, text, textX, textY, whiteColor);

        draggable.setWidth(totalWidth);
        draggable.setHeight(totalHeight);
    }

    public void NewStyle(EventRender.Default eventRender) {
        float x = draggable.getX(), y = draggable.getY();
        var matrices = eventRender.getContext().getMatrices();
        
        // Шрифты
        var textFont = Fonts.getFont("suisse", 15);
        var iconFont = Fonts.getFont("iconnew", 14);
        var iconFontOld = Fonts.getFont("icon", 14); // добавил старый шрифт icon
        
        if (textFont == null) textFont = Fonts.getFont("suisse", 14);
        if (iconFont == null) iconFont = Fonts.getFont("icon", 14);
        
        String username = getUsername();
        
        // Получаем FPS
        int fps = mc != null ? mc.getCurrentFps() : 0;
        String fpsText = fps + " fps";
        
        // Получаем цвет темы
        int themeColor;
        if (!eversense.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")) {
            themeColor = eversense.INSTANCE.themeStorage.getThemes().getTheme().color[0];
        } else {
            themeColor = ColorUtils.getThemeColor();
        }
        
        int whiteColor = ColorUtils.rgba(255, 255, 255, 255);
        
        // Иконки из iconnew
        String userIconGlyph = "e"; // иконка пользователя
        // ШЕСТЕРЕНКА FPS: попробуйте эти символы по очереди:
        // a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z
        String fpsIconGlyph = "i"; // иконка FPS - ИЗМЕНИТЕ ЭТОТ СИМВОЛ
        String dotGlyph = "•"; // белая точка
        
        // Формируем текст: "[logo.png] eversense • [icon] username • [icon] 165 fps"
        String brandText = "eversense";
        
        // Размеры логотипа PNG
        float logoSize = 14f; // размер логотипа
        
        // Вычисляем размеры
        float brandW = textFont.getStringWidth(brandText); // убрал пробел перед текстом
        float dotW = textFont.getStringWidth(" " + dotGlyph + " ");
        float userIconW = iconFont.getStringWidth(userIconGlyph);
        float usernameW = textFont.getStringWidth(" " + username);
        float fpsIconW = iconFont.getStringWidth(fpsIconGlyph);
        float fpsTextW = textFont.getStringWidth(" " + fpsText);
        
        float totalWidth = logoSize + brandW + dotW + userIconW + usernameW + dotW + fpsIconW + fpsTextW + 8f; // уменьшил отступы с 10f до 8f
        float totalHeight = 20f; // уменьшил высоту фона с 22f до 20f
        
        // Рисуем фон
        int bgColor = ColorUtils.rgba(0, 0, 0, 150);
        RenderUtils.drawRoundedRect(matrices, x, y, totalWidth, totalHeight, 3f, bgColor);
        
        // Позиция для рисования
        float drawX = x + 3f; // уменьшил левый отступ с 5f до 3f
        float textY = y + 7.5f; // опустил текст на 1px ниже (было 6.5f)
        float iconY = y + 8.5f; // опустил иконки на 1px ниже (было 7.5f)
        float logoY = y + 3f; // поднял логотип выше (было 4f)
        
        // Рисуем PNG логотип
        net.minecraft.util.Identifier logoTexture = net.minecraft.util.Identifier.of("eversense", "fonts/msdf/icon/eversense.png");
        RenderUtils.drawImage(matrices, logoTexture, drawX, logoY, logoSize, logoSize, themeColor);
        drawX += logoSize + 1f; // уменьшил отступ после логотипа с 2f до 1f
        
        // Рисуем "eversense"
        textFont.drawString(matrices, brandText, drawX, textY, whiteColor); // убрал пробел перед текстом
        drawX += brandW;
        
        // Рисуем первую точку (белая)
        textFont.drawString(matrices, " " + dotGlyph + " ", drawX, textY, whiteColor);
        drawX += dotW;
        
        // Рисуем иконку пользователя (цвет темы)
        iconFont.drawString(matrices, userIconGlyph, drawX, iconY, themeColor);
        drawX += userIconW;
        
        // Рисуем имя пользователя
        textFont.drawString(matrices, " " + username, drawX, textY, whiteColor);
        drawX += usernameW;
        
        // Рисуем вторую точку (белая) - поднята чуть выше для выравнивания
        textFont.drawString(matrices, " " + dotGlyph + " ", drawX, textY - 0.25f, whiteColor);
        drawX += dotW;
        
        // Рисуем иконку FPS (цвет темы)
        iconFont.drawString(matrices, fpsIconGlyph, drawX, iconY, themeColor);
        drawX += iconFont.getStringWidth(fpsIconGlyph);
        
        // Рисуем FPS текст
        textFont.drawString(matrices, " " + fpsText, drawX, textY, whiteColor);
        
        draggable.setWidth(totalWidth);
        draggable.setHeight(totalHeight);
    }

    private void drawServerNameWithThemeParts(MatrixStack matrices, String serverName, float x, float y, int themeColor, int whiteColor) {
        var font = Fonts.getFont("suisse", 13);
        String[] parts = serverName.split("\\.");
        if (parts.length < 2) {
            font.drawString(matrices, serverName, x, y, whiteColor);
            return;
        }

        String mainPart = String.join(".", java.util.Arrays.copyOf(parts, parts.length - 1));
        String suffixPart = "." + parts[parts.length - 1];

        font.drawString(matrices, mainPart, x, y, whiteColor);
        float suffixX = x + font.getStringWidth(mainPart) - 2f;
        font.drawString(matrices, suffixPart, suffixX, y, themeColor);
    }

    private String formatServerNameForDisplay(String serverName) {
        if (serverName == null || serverName.isEmpty()) {
            return "";
        }

        String host = serverName;
        int portIndex = host.indexOf(':');
        if (portIndex > 0) {
            host = host.substring(0, portIndex);
        }

        String[] parts = host.split("\\.");
        if (parts.length >= 3) {
            return String.join(".", java.util.Arrays.copyOfRange(parts, 1, parts.length));
        }
        return host;
    }

    private float getServerTps() {
        if (eversense.INSTANCE == null || eversense.INSTANCE.tpsCalc == null) {
            return 20.0f;
        }
        return Math.max(0.0f, Math.min(20.0f, eversense.INSTANCE.tpsCalc.getTPS()));
    }

    private String formatOneDecimal(float value) {
        int scaled = Math.round(value * 10.0f);
        return (scaled / 10) + "." + Math.abs(scaled % 10);
    }
}