package fun.eversense.client.ui.clickgui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;
import net.minecraft.util.math.MathHelper;
import fun.eversense.eversense;
import fun.eversense.api.utils.color.ColorUtils;
import fun.eversense.api.utils.input.KeyBoardUtils;
import fun.eversense.api.utils.math.HoveringUtils;
import fun.eversense.api.utils.render.RenderUtils;
import fun.eversense.api.utils.render.fonts.msdf.Font;
import fun.eversense.api.utils.render.fonts.msdf.Fonts;
import fun.eversense.api.utils.scissor.ScissorUtils;
import fun.eversense.client.modules.Module;
import fun.eversense.client.modules.settings.Setting;

import java.util.ArrayList;
import java.util.List;

public class ClickGuiRenderer {
    private final ClickGuiState state;
    private final ClickGuiSettingRenderer settingRenderer;
    private final ClickGuiThemeSelector themeSelector;

    public ClickGuiRenderer(ClickGuiState state, ClickGuiSettingRenderer settingRenderer, ClickGuiThemeSelector themeSelector) {
        this.state = state;
        this.settingRenderer = settingRenderer;
        this.themeSelector = themeSelector;
    }

    public void render(DrawContext context, int mouseX, int mouseY, Window window, float animationProgress) {
        if (window == null) {
            return;
        }

        float alphaMul = MathHelper.clamp(animationProgress, 0.0f, 1.0f);
        int shadeColor = getFadeShadeColor(alphaMul, 120);
        int colorTheme = getThemeColor();
        Module hoveredModule = null;

        Module.ModuleCategory[] categories = Module.ModuleCategory.values();
        for (int i = 0; i < categories.length; i++) {
            Module.ModuleCategory category = categories[i];
            float panelX = ClickGuiLayout.getCategoryPanelX(state.getX(), i);
            Module categoryHoveredModule = renderCategoryPanel(context, mouseX, mouseY, panelX, category, colorTheme, alphaMul, shadeColor);
            if (categoryHoveredModule != null) {
                hoveredModule = categoryHoveredModule;
            }
        }

        renderSearch(context, categories.length, colorTheme, alphaMul, getFadeShadeColor(alphaMul, 95));
        themeSelector.render(context, window, state.getRenderOffsetY(), alphaMul, getFadeShadeColor(alphaMul, 95));
        renderDescription(context, window, hoveredModule, colorTheme, animationProgress);
    }

    private Module renderCategoryPanel(DrawContext context, int mouseX, int mouseY, float panelX, Module.ModuleCategory category, int colorTheme, float alphaMul, int shadeColor) {
        float panelY = state.getY() + state.getRenderOffsetY();
        
        // Новый стиль: стеклянный эффект с градиентом
        // Внешнее свечение панели
        RenderUtils.drawRoundedRect(context.getMatrices(), panelX - 1f, panelY - 1f, ClickGuiLayout.WIDTH + 2f, ClickGuiLayout.HEIGHT + 2f, 9f, ColorUtils.applyAlpha(colorTheme, 0.15f * alphaMul));
        
        // Основной фон панели с градиентом
        RenderUtils.drawGradientRect(context.getMatrices(), panelX, panelY, ClickGuiLayout.WIDTH, ClickGuiLayout.HEIGHT, 8f,
                ColorUtils.rgba(18, 18, 24, 245),
                ColorUtils.rgba(12, 12, 16, 245), false);
        
        // Верхняя полоса с градиентом темы
        RenderUtils.drawGradientRect(context.getMatrices(), panelX, panelY, ClickGuiLayout.WIDTH, 23f, 8f,
                ColorUtils.applyAlpha(ColorUtils.darken(colorTheme, 0.25f), 0.4f),
                ColorUtils.applyAlpha(ColorUtils.darken(colorTheme, 0.35f), 0.3f), false);
        
        // Разделительная линия с свечением
        RenderUtils.drawRoundedRect(context.getMatrices(), panelX + 5, panelY + 23, ClickGuiLayout.WIDTH - 10, 1.5f, 0.75f, ColorUtils.applyAlpha(colorTheme, 0.6f));
        
        if (((shadeColor >> 24) & 0xFF) > 0) {
            RenderUtils.drawRoundedRect(context.getMatrices(), panelX, panelY, ClickGuiLayout.WIDTH, ClickGuiLayout.HEIGHT, 8, shadeColor);
        }

        // Иконка категории с свечением
        float iconX = panelX + 48 - (issue(15).getWidth(category.getName()) / 2F) - 4; // Сдвинуто левее с 50 на 48
        float iconY = panelY + 10;
        int iconHighlight = ColorUtils.interpolateColor(colorTheme, ColorUtils.rgba(255, 255, 255, 255), 0.3f);
        icons(14).drawGradientStringHorizontal(context.getMatrices(), category.getIcons(), iconX, iconY, 
                alpha(colorTheme, alphaMul), 
                alpha(iconHighlight, alphaMul));
        
        // Название категории
        issue(15).drawCenteredString(context.getMatrices(), category.getName(), panelX + 52, panelY + 9, alpha(-1, alphaMul));

        float contentY = ClickGuiLayout.getContentY(panelY);
        float contentHeight = ClickGuiLayout.getContentHeight();
        state.clampScroll(category, contentHeight);
        float moduleY = contentY + state.getScroll(category);
        Module hoveredModule = null;

        ScissorUtils.push();
        ScissorUtils.setFromComponentCoordinates(panelX, contentY, ClickGuiLayout.WIDTH, contentHeight);

        for (Module module : state.getModules(category)) {
            float openProgress = state.getOpenProgress(module);
            float moduleHeight = ClickGuiLayout.getModuleHeight(module, openProgress);

            if (moduleY + moduleHeight + ClickGuiLayout.MODULE_GAP >= contentY && moduleY <= contentY + contentHeight) {
                Module moduleHover = renderModule(context, mouseX, mouseY, panelX, moduleY, module, openProgress, moduleHeight, colorTheme, alphaMul, shadeColor);
                if (moduleHover != null) {
                    hoveredModule = moduleHover;
                }
            }

            moduleY += ClickGuiLayout.MODULE_GAP + moduleHeight;
        }

        ScissorUtils.pop();
        return hoveredModule;
    }

    private Module renderModule(DrawContext context, int mouseX, int mouseY, float panelX, float moduleY, Module module, float openProgress, float moduleHeight, int colorTheme, float alphaMul, int shadeColor) {
        List<Setting> settings = module.getSettings();
        renderModuleBackground(context, panelX, moduleY, moduleHeight, module.isEnable(), colorTheme, shadeColor);

        String moduleName = module.getName();
        String bindText = "";
        if (state.getBindingModule() == module) {
            bindText = " [...]";
        } else if (module.getKey() != -1) {
            bindText = " [" + state.toEnglish(KeyBoardUtils.getBindName(module.getKey())) + "]";
        }

        int nameColor = module.isEnable() ? alpha(-1, alphaMul) : alpha(ColorUtils.rgba(255, 255, 255, 170), alphaMul);
        int bindColor = module.isEnable() ? alpha(ColorUtils.rgba(255, 255, 255, 150), alphaMul) : alpha(ColorUtils.rgba(255, 255, 255, 100), alphaMul);

        issue(14).draw(context.getMatrices(), moduleName, panelX + ClickGuiLayout.SETTING_LEFT, moduleY + 8, nameColor);
        if (!bindText.isEmpty()) {
            float nameWidth = issue(14).getWidth(moduleName);
            issue(11).draw(context.getMatrices(), bindText, panelX + ClickGuiLayout.SETTING_LEFT + nameWidth, moduleY + 9, bindColor);
        }

        if (settings != null && !settings.isEmpty() && ClickGuiLayout.hasVisibleSettings(settings)) {
            renderModuleDots(context, panelX, moduleY, module, module.isEnable(), alphaMul);
        }

        if (settings != null && !settings.isEmpty()) {
            settingRenderer.render(context, module, panelX, moduleY, openProgress, colorTheme, mouseX, mouseY, state);
        }

        if (HoveringUtils.isHovered(mouseX, mouseY, panelX + ClickGuiLayout.MODULE_PADDING, moduleY, ClickGuiLayout.MODULE_INNER_WIDTH, moduleHeight)) {
            return module;
        }
        return null;
    }

    private void renderModuleBackground(DrawContext context, float panelX, float moduleY, float moduleHeight, boolean enabled, int colorTheme, int shadeColor) {
        if (enabled) {
            // Новый стиль для включенных модулей: яркое свечение + градиент
            // Внешнее свечение (более яркое)
            RenderUtils.drawRoundedRect(context.getMatrices(), 
                    panelX + ClickGuiLayout.MODULE_PADDING - 1.5f, 
                    moduleY - 1.5f, 
                    ClickGuiLayout.MODULE_INNER_WIDTH + 3f, 
                    moduleHeight + 3f, 
                    6.5f, 
                    ColorUtils.applyAlpha(colorTheme, 0.35f));
            
            // Средний слой свечения
            RenderUtils.drawRoundedRect(context.getMatrices(), 
                    panelX + ClickGuiLayout.MODULE_PADDING - 0.8f, 
                    moduleY - 0.8f, 
                    ClickGuiLayout.MODULE_INNER_WIDTH + 1.6f, 
                    moduleHeight + 1.6f, 
                    5.8f, 
                    ColorUtils.applyAlpha(colorTheme, 0.25f));
            
            // Основной фон с градиентом
            RenderUtils.drawGradientRect(context.getMatrices(), 
                    panelX + ClickGuiLayout.MODULE_PADDING, 
                    moduleY, 
                    ClickGuiLayout.MODULE_INNER_WIDTH, 
                    moduleHeight, 
                    5f,
                    ColorUtils.applyAlpha(ColorUtils.darken(colorTheme, 0.12f), 0.9f),
                    ColorUtils.applyAlpha(ColorUtils.darken(colorTheme, 0.18f), 0.85f), 
                    false);
            
            if (((shadeColor >> 24) & 0xFF) > 0) {
                RenderUtils.drawRoundedRect(context.getMatrices(), 
                        panelX + ClickGuiLayout.MODULE_PADDING, 
                        moduleY, 
                        ClickGuiLayout.MODULE_INNER_WIDTH, 
                        moduleHeight, 
                        5f, 
                        shadeColor);
            }
            return;
        }

        // Новый стиль для выключенных модулей: тонкое свечение + градиент
        // Легкое внешнее свечение
        RenderUtils.drawRoundedRect(context.getMatrices(), 
                panelX + ClickGuiLayout.MODULE_PADDING - 0.5f, 
                moduleY - 0.5f, 
                ClickGuiLayout.MODULE_INNER_WIDTH + 1f, 
                moduleHeight + 1f, 
                5.5f, 
                ColorUtils.applyAlpha(colorTheme, 0.08f));
        
        // Основной фон с градиентом
        RenderUtils.drawGradientRect(context.getMatrices(), 
                panelX + ClickGuiLayout.MODULE_PADDING, 
                moduleY, 
                ClickGuiLayout.MODULE_INNER_WIDTH, 
                moduleHeight, 
                5f,
                ColorUtils.rgba(28, 28, 35, 200),
                ColorUtils.rgba(22, 22, 28, 200), 
                false);
        
        if (((shadeColor >> 24) & 0xFF) > 0) {
            RenderUtils.drawRoundedRect(context.getMatrices(), 
                    panelX + ClickGuiLayout.MODULE_PADDING, 
                    moduleY, 
                    ClickGuiLayout.MODULE_INNER_WIDTH, 
                    moduleHeight, 
                    5f, 
                    shadeColor);
        }
    }

    private void renderModuleDots(DrawContext context, float panelX, float moduleY, Module module, boolean enabled, float alphaMul) {
        int dotsColor = enabled ? alpha(ColorUtils.rgba(255, 255, 255, 240), alphaMul) : alpha(ColorUtils.rgba(255, 255, 255, 120), alphaMul);
        float dotsX = panelX + 84f; // Сдвинуто левее с 87.5f на 84f
        float baseY = moduleY + 10f;
        float spacing = 2f;
        float radius = 2.2f;
        float bottomXOffset = 2.1f;
        float angle = state.updateDotsRotation(module, module.isOpen() ? (float) (Math.PI / 2f) : 0f);
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        float[][] offsets = {
                {0f, -spacing},
                {-bottomXOffset, spacing},
                {bottomXOffset, spacing}
        };

        // Новый стиль: яркое свечение для точек
        if (enabled) {
            for (float[] offset : offsets) {
                float rx = offset[0] * cos - offset[1] * sin;
                float ry = offset[0] * sin + offset[1] * cos;
                // Внешнее свечение
                RenderUtils.drawRoundCircle(context.getMatrices(), dotsX + rx, baseY + ry, radius + 1.5f, alpha(ColorUtils.applyAlpha(dotsColor, 0.25f), alphaMul));
                // Среднее свечение
                RenderUtils.drawRoundCircle(context.getMatrices(), dotsX + rx, baseY + ry, radius + 0.8f, alpha(ColorUtils.applyAlpha(dotsColor, 0.4f), alphaMul));
            }
        }

        // Основные точки
        for (float[] offset : offsets) {
            float rx = offset[0] * cos - offset[1] * sin;
            float ry = offset[0] * sin + offset[1] * cos;
            RenderUtils.drawRoundCircle(context.getMatrices(), dotsX + rx, baseY + ry, radius, dotsColor);
        }
    }

    private int getThemeColor() {
        if (!eversense.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")) {
            return eversense.INSTANCE.themeStorage.getThemes().getTheme().color[0];
        }
        return ColorUtils.getThemeColor();
    }

    private void renderSearch(DrawContext context, int categoryCount, int colorTheme, float alphaMul, int shadeColor) {
        float searchY = ClickGuiLayout.getSearchY(state.getY() + state.getRenderOffsetY());
        float searchW = getSearchWidth();
        float searchX = ClickGuiLayout.getSearchX(state.getX(), categoryCount, searchW);
        float searchH = ClickGuiLayout.SEARCH_HEIGHT;
        float selectionPaddingLeft = 3.0f;
        float selectionPaddingRight = 1.5f;

        // Новый стиль: стеклянный эффект с градиентом и свечением
        // Внешнее свечение
        RenderUtils.drawRoundedRect(context.getMatrices(), searchX - 1f, searchY - 1f, searchW + 2f, searchH + 2f, 6.5f, 
                ColorUtils.applyAlpha(colorTheme, 0.2f));
        
        // Основной фон с градиентом
        RenderUtils.drawGradientRect(context.getMatrices(), searchX, searchY, searchW, searchH, 6f,
                ColorUtils.rgba(25, 25, 32, 230),
                ColorUtils.rgba(18, 18, 24, 230), false);
        
        // Верхняя подсветка
        int searchHighlight = ColorUtils.interpolateColor(colorTheme, ColorUtils.rgba(255, 255, 255, 255), 0.15f);
        RenderUtils.drawGradientRect(context.getMatrices(), searchX + 1f, searchY + 1f, searchW - 2f, searchH * 0.4f, 5f,
                ColorUtils.applyAlpha(searchHighlight, 0.1f),
                ColorUtils.applyAlpha(colorTheme, 0.0f), false);
        
        // Граница с градиентом
        RenderUtils.drawRoundedRect(context.getMatrices(), searchX - 0.5f, searchY - 0.5f, searchW + 1f, searchH + 1f, 6.5f, 
                ColorUtils.applyAlpha(colorTheme, 0.3f));
        
        if (((shadeColor >> 24) & 0xFF) > 0) {
            RenderUtils.drawRoundedRect(context.getMatrices(), searchX, searchY, searchW, searchH, 6f, shadeColor);
        }

        String query = state.getSearchText();
        String text = query.isEmpty() ? "Search..." : query;
        int textColor = query.isEmpty()
                ? alpha(ColorUtils.rgba(255, 255, 255, 130), alphaMul)
                : alpha(ColorUtils.rgba(255, 255, 255, 245), alphaMul);

        float iconX = searchX + ClickGuiLayout.SEARCH_ICON_X;
        float textX = searchX + ClickGuiLayout.SEARCH_TEXT_X;
        float textY = searchY + 6.2f;
        
        // Иконка поиска с градиентом
        int searchIconHighlight = ColorUtils.interpolateColor(colorTheme, ColorUtils.rgba(255, 255, 255, 255), 0.3f);
        iconsNew(18).drawGradientStringHorizontal(context.getMatrices(), "l", iconX + 2, searchY + 6.5f, 
                alpha(colorTheme, alphaMul), 
                alpha(searchIconHighlight, alphaMul));

        ScissorUtils.push();
        ScissorUtils.setFromComponentCoordinates(
                textX - selectionPaddingLeft,
                searchY,
                searchW - ClickGuiLayout.SEARCH_TEXT_X - ClickGuiLayout.SEARCH_RIGHT_PADDING + selectionPaddingLeft,
                searchH
        );
        
        // Выделение текста с новым стилем
        if (!query.isEmpty() && state.hasSearchSelection()) {
            int selectionStart = state.getSearchSelectionStart();
            int selectionEnd = state.getSearchSelectionEnd();
            float selectedX = textX + issue(14).getWidth(query.substring(0, selectionStart)) - selectionPaddingLeft;
            float selectedW = issue(14).getWidth(query.substring(selectionStart, selectionEnd)) + selectionPaddingLeft + selectionPaddingRight;
            
            // Свечение выделения
            RenderUtils.drawRoundedRect(context.getMatrices(), selectedX - 0.5f, searchY + 3.3f, selectedW + 1f, 11.5f, 2f, 
                    alpha(ColorUtils.applyAlpha(colorTheme, 0.2f), alphaMul));
            // Основное выделение
            RenderUtils.drawRoundedRect(context.getMatrices(), selectedX, searchY + 3.8f, selectedW, 10.5f, 1.5f, 
                    alpha(ColorUtils.applyAlpha(colorTheme, 0.5f), alphaMul));
        }

        issue(14).draw(context.getMatrices(), text, textX, textY + 1, textColor);
        
        // Курсор с пульсацией
        if (state.isSearchActive() && (System.currentTimeMillis() / 500L) % 2L == 0L) {
            float cursorX = textX + issue(14).getWidth(query.substring(0, Math.min(state.getSearchCursor(), query.length())));
            // Свечение курсора
            RenderUtils.drawRoundedRect(context.getMatrices(), cursorX + 0.5f, searchY + 4f, 1.3f, 9.5f, 0.5f, 
                    alpha(ColorUtils.applyAlpha(colorTheme, 0.4f), alphaMul));
            // Основной курсор
            RenderUtils.drawRoundedRect(context.getMatrices(), cursorX + 1f, searchY + 4.5f, 0.8f, 9f, 0.4f, 
                    alpha(colorTheme, alphaMul));
        }
        ScissorUtils.pop();

        // Счетчик результатов с новым стилем
        if (!query.isEmpty()) {
            int totalResults = state.getTotalSearchResults();
            if (totalResults > 0) {
                String counterText = totalResults + " found";
                float counterX = searchX + searchW + 10f;
                float counterY = searchY + 6.5f;
                
                // Фон для счетчика
                float counterWidth = issue(12).getWidth(counterText) + 8f;
                RenderUtils.drawRoundedRect(context.getMatrices(), counterX - 4f, searchY + 3f, counterWidth, searchH - 6f, 3f,
                        alpha(ColorUtils.applyAlpha(ColorUtils.rgba(50, 200, 100, 200), 0.3f), alphaMul));
                
                issue(12).draw(context.getMatrices(), counterText, counterX, counterY, 
                        alpha(ColorUtils.rgba(150, 255, 150, 255), alphaMul));
            }
        }
    }

    private void renderDescription(DrawContext context, Window window, Module hoveredModule, int colorTheme, float alphaMul) {
        if (hoveredModule == null) {
            return;
        }

        String description = hoveredModule.getDisplayDescription();
        if (description == null || description.isBlank() || "NULLABLE".equalsIgnoreCase(description) || "desc".equalsIgnoreCase(description)) {
            return;
        }

        Font descriptionFont = issue(16);
        float maxWidth = window.getScaledWidth() - 40.0f;
        List<String> lines = wrapDescription(descriptionFont, description, maxWidth);
        if (lines.isEmpty()) {
            return;
        }

        float lineHeight = descriptionFont.getHeight() - 2.0f;
        float boxHeight = lines.size() * lineHeight;
        float centerX = window.getScaledWidth() * 0.5f;
        float startY = ClickGuiLayout.THEME_PANEL_Y - boxHeight - 6.0f;

        for (int i = 0; i < lines.size(); i++) {
            descriptionFont.drawCenteredString(context.getMatrices(), lines.get(i), centerX, startY + i * lineHeight, ColorUtils.applyAlpha(-1, alphaMul));
        }
    }

    private List<String> wrapDescription(Font font, String text, float maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.trim().split("\\s+");
        if (words.length == 0) {
            return lines;
        }

        StringBuilder currentLine = new StringBuilder();
        for (String word : words) {
            String candidate = currentLine.isEmpty() ? word : currentLine + " " + word;
            if (font.getWidth(candidate) <= maxWidth || currentLine.isEmpty()) {
                currentLine.setLength(0);
                currentLine.append(candidate);
                continue;
            }

            lines.add(currentLine.toString());
            currentLine.setLength(0);
            currentLine.append(word);
        }

        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    private float getSearchWidth() {
        String query = state.getSearchText();
        String text = query.isEmpty() ? "Search..." : query;
        float contentWidth = ClickGuiLayout.SEARCH_TEXT_X + issue(14).getWidth(text) + ClickGuiLayout.SEARCH_RIGHT_PADDING;
        return Math.max(ClickGuiLayout.SEARCH_WIDTH, contentWidth);
    }

    private Font issue(int size) {
        return Fonts.getFont("suisse", size);
    }

    private Font icons(int size) {
        return Fonts.getFont("icon", size);
    }

    private Font iconsNew(int size) {
        return Fonts.getFont("icon1", size);
    }

    private int alpha(int color, float alphaMul) {
        return ColorUtils.applyAlpha(color, alphaMul);
    }

    private int getFadeShadeColor(float alphaMul, int maxAlpha) {
        int alpha = MathHelper.clamp((int) ((1.0f - alphaMul) * maxAlpha), 0, 255);
        return ColorUtils.rgba(0, 0, 0, alpha);
    }
}
