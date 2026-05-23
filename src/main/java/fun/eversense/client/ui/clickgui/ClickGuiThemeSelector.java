package fun.eversense.client.ui.clickgui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;
import fun.eversense.eversense;
import fun.eversense.api.utils.color.ColorUtils;
import fun.eversense.api.utils.math.HoveringUtils;
import fun.eversense.api.utils.render.RenderUtils;

import java.util.List;

public class ClickGuiThemeSelector {
    public void render(DrawContext context, Window window, float offsetY, float alphaMul, int shadeColor) {
        if (context == null || window == null) {
            return;
        }

        List<fun.eversense.api.storages.implement.ThemeStorage.Themes> themes = eversense.INSTANCE.themeStorage.getThemeList();
        if (themes == null || themes.isEmpty()) {
            return;
        }

        float totalWidth = themes.size() * ClickGuiLayout.THEME_BOX_SIZE + (themes.size() - 1) * ClickGuiLayout.THEME_BOX_GAP;
        float panelWidth = totalWidth + ClickGuiLayout.THEME_SIDE_PADDING * 2f;
        float panelX = getThemePanelX(window, panelWidth);
        float panelY = ClickGuiLayout.THEME_PANEL_Y + offsetY;
        float startX = panelX + ClickGuiLayout.THEME_SIDE_PADDING;
        float startY = panelY + (ClickGuiLayout.THEME_PANEL_H - ClickGuiLayout.THEME_BOX_SIZE) / 2f;

        // Новый стиль: стеклянный эффект для панели тем
        // Внешнее свечение
        RenderUtils.drawRoundedRect(context.getMatrices(), panelX - 1f, panelY - 1f, panelWidth + 2f, ClickGuiLayout.THEME_PANEL_H + 2f, 4.5f,
                ColorUtils.applyAlpha(ColorUtils.getThemeColor(), 0.2f * alphaMul));
        
        // Основной фон с градиентом
        RenderUtils.drawGradientRect(
                context.getMatrices(),
                panelX,
                panelY,
                panelWidth,
                ClickGuiLayout.THEME_PANEL_H,
                3.5f,
                ColorUtils.rgba(22, 22, 28, 235),
                ColorUtils.rgba(18, 18, 24, 235),
                false
        );
        
        if (((shadeColor >> 24) & 0xFF) > 0) {
            RenderUtils.drawRoundedRect(context.getMatrices(), panelX, panelY, panelWidth, ClickGuiLayout.THEME_PANEL_H, 3.5f, shadeColor);
        }

        fun.eversense.api.storages.implement.ThemeStorage.Themes selected = eversense.INSTANCE.themeStorage.getThemes();
        for (int i = 0; i < themes.size(); i++) {
            fun.eversense.api.storages.implement.ThemeStorage.Themes theme = themes.get(i);
            float boxX = startX + i * (ClickGuiLayout.THEME_BOX_SIZE + ClickGuiLayout.THEME_BOX_GAP);
            float boxY = startY;
            
            // Новый стиль: многослойное свечение для выбранной темы
            if (theme == selected) {
                // Внешнее свечение
                RenderUtils.drawRoundedRect(
                        context.getMatrices(),
                        boxX - 1.5f,
                        boxY - 1.5f,
                        ClickGuiLayout.THEME_BOX_SIZE + 3f,
                        ClickGuiLayout.THEME_BOX_SIZE + 3f,
                        ClickGuiLayout.THEME_BOX_RADIUS + 1.5f,
                        ColorUtils.setAlphaColor(-1, Math.max(1, (int) (120 * alphaMul)))
                );
                // Среднее свечение
                RenderUtils.drawRoundedRect(
                        context.getMatrices(),
                        boxX - 1f,
                        boxY - 1f,
                        ClickGuiLayout.THEME_BOX_SIZE + 2f,
                        ClickGuiLayout.THEME_BOX_SIZE + 2f,
                        ClickGuiLayout.THEME_BOX_RADIUS + 1f,
                        ColorUtils.setAlphaColor(-1, Math.max(1, (int) (180 * alphaMul)))
                );
                // Внутренняя граница
                RenderUtils.drawRoundedRect(
                        context.getMatrices(),
                        boxX - 0.5f,
                        boxY - 0.5f,
                        ClickGuiLayout.THEME_BOX_SIZE + 1,
                        ClickGuiLayout.THEME_BOX_SIZE + 1,
                        ClickGuiLayout.THEME_BOX_RADIUS + 0.5f,
                        ColorUtils.setAlphaColor(-1, Math.max(1, (int) (220 * alphaMul)))
                );
            }
            
            // Специальная визуализация для черно-белой темы
            if (theme.name().equals("Monochrome")) {
                // Левая половина - белая с градиентом
                RenderUtils.drawGradientRect(
                        context.getMatrices(),
                        boxX,
                        boxY,
                        ClickGuiLayout.THEME_BOX_SIZE / 2f,
                        ClickGuiLayout.THEME_BOX_SIZE,
                        ClickGuiLayout.THEME_BOX_RADIUS,
                        ColorUtils.applyAlpha(ColorUtils.rgba(240, 240, 240, 255), Math.max(0.55f, alphaMul)),
                        ColorUtils.applyAlpha(ColorUtils.rgba(200, 200, 200, 255), Math.max(0.55f, alphaMul)),
                        true
                );
                // Правая половина - черная с градиентом
                RenderUtils.drawGradientRect(
                        context.getMatrices(),
                        boxX + ClickGuiLayout.THEME_BOX_SIZE / 2f,
                        boxY,
                        ClickGuiLayout.THEME_BOX_SIZE / 2f,
                        ClickGuiLayout.THEME_BOX_SIZE,
                        ClickGuiLayout.THEME_BOX_RADIUS,
                        ColorUtils.applyAlpha(ColorUtils.rgba(80, 80, 80, 255), Math.max(0.55f, alphaMul)),
                        ColorUtils.applyAlpha(ColorUtils.rgba(40, 40, 40, 255), Math.max(0.55f, alphaMul)),
                        true
                );
            } else {
                // Обычные темы с градиентом и подсветкой
                int themeColor = getThemeDisplayColor(theme);
                int lighterTheme = ColorUtils.interpolateColor(themeColor, ColorUtils.rgba(255, 255, 255, 255), 0.1f);
                int darkerTheme = ColorUtils.darken(themeColor, 0.9f);
                RenderUtils.drawGradientRect(
                        context.getMatrices(),
                        boxX,
                        boxY,
                        ClickGuiLayout.THEME_BOX_SIZE,
                        ClickGuiLayout.THEME_BOX_SIZE,
                        ClickGuiLayout.THEME_BOX_RADIUS,
                        ColorUtils.applyAlpha(lighterTheme, Math.max(0.55f, alphaMul)),
                        ColorUtils.applyAlpha(darkerTheme, Math.max(0.55f, alphaMul)),
                        false
                );
                // Верхняя подсветка
                int brightHighlight = ColorUtils.interpolateColor(themeColor, ColorUtils.rgba(255, 255, 255, 255), 0.3f);
                RenderUtils.drawGradientRect(
                        context.getMatrices(),
                        boxX + 1f,
                        boxY + 1f,
                        ClickGuiLayout.THEME_BOX_SIZE - 2f,
                        ClickGuiLayout.THEME_BOX_SIZE * 0.3f,
                        ClickGuiLayout.THEME_BOX_RADIUS - 0.5f,
                        ColorUtils.applyAlpha(brightHighlight, 0.3f * alphaMul),
                        ColorUtils.applyAlpha(themeColor, 0.0f),
                        false
                );
            }
        }
    }

    public boolean handleClick(Window window, double mouseX, double mouseY, int button, float offsetY) {
        if (window == null || button != 0) {
            return false;
        }

        List<fun.eversense.api.storages.implement.ThemeStorage.Themes> themes = eversense.INSTANCE.themeStorage.getThemeList();
        if (themes == null || themes.isEmpty()) {
            return false;
        }

        float totalWidth = themes.size() * ClickGuiLayout.THEME_BOX_SIZE + (themes.size() - 1) * ClickGuiLayout.THEME_BOX_GAP;
        float panelWidth = totalWidth + ClickGuiLayout.THEME_SIDE_PADDING * 2f;
        float panelX = getThemePanelX(window, panelWidth);
        float panelY = ClickGuiLayout.THEME_PANEL_Y + offsetY;
        float startX = panelX + ClickGuiLayout.THEME_SIDE_PADDING;
        float startY = panelY + (ClickGuiLayout.THEME_PANEL_H - ClickGuiLayout.THEME_BOX_SIZE) / 2f;

        if (!HoveringUtils.isHovered(mouseX, mouseY, panelX, panelY, panelWidth, ClickGuiLayout.THEME_PANEL_H)) {
            return false;
        }

        for (int i = 0; i < themes.size(); i++) {
            float boxX = startX + i * (ClickGuiLayout.THEME_BOX_SIZE + ClickGuiLayout.THEME_BOX_GAP);
            float boxY = startY;
            if (HoveringUtils.isHovered(mouseX, mouseY, boxX, boxY, ClickGuiLayout.THEME_BOX_SIZE, ClickGuiLayout.THEME_BOX_SIZE)) {
                eversense.INSTANCE.themeStorage.setThemes(themes.get(i));
                return true;
            }
        }
        return false;
    }

    private int getThemeDisplayColor(fun.eversense.api.storages.implement.ThemeStorage.Themes theme) {
        int color = theme.getTheme().getColor(0);
        
        // Специальная визуализация для черно-белой темы (градиент)
        if (theme.name().equals("Monochrome")) {
            return ColorUtils.rgba(150, 150, 150, 255);
        }
        
        // Для белой темы показываем светло-серый чтобы было видно на фоне
        if (theme.name().equals("White")) {
            return ColorUtils.rgba(240, 240, 240, 255);
        }
        
        // Для черной темы
        if (theme.name().equals("Black")) {
            return ColorUtils.rgba(50, 50, 50, 255);
        }
        
        if (ColorUtils.alpha(color) == 0) {
            return ColorUtils.rgba(220, 220, 220, 180);
        }
        return color;
    }

    private float getThemePanelX(Window window, float panelWidth) {
        return (window.getScaledWidth() / 2F) - (panelWidth / 2F);
    }
}
