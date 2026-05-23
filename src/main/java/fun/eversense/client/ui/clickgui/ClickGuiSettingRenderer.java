package fun.eversense.client.ui.clickgui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import fun.eversense.api.utils.animation.AnimationUtils;
import fun.eversense.api.utils.color.ColorUtils;
import fun.eversense.api.utils.input.KeyBoardUtils;
import fun.eversense.api.utils.render.RenderUtils;
import fun.eversense.api.utils.render.fonts.msdf.Font;
import fun.eversense.api.utils.render.fonts.msdf.Fonts;
import fun.eversense.api.utils.scissor.ScissorUtils;
import fun.eversense.client.modules.Module;
import fun.eversense.client.modules.settings.Setting;
import fun.eversense.client.modules.settings.implement.BindSetting;
import fun.eversense.client.modules.settings.implement.BooleanSetting;
import fun.eversense.client.modules.settings.implement.FloatSetting;
import fun.eversense.client.modules.settings.implement.ListSetting;
import fun.eversense.client.modules.settings.implement.ModeSetting;
import fun.eversense.client.modules.settings.implement.TextSetting;

import java.util.List;

public class ClickGuiSettingRenderer {
    private static final float HOVER_SCROLL_OVERFLOW_THRESHOLD = 6.0f;

    public void render(DrawContext context, Module module, float panelX, float moduleY, float openProgress, int colorTheme, double mouseX, double mouseY, ClickGuiState state) {
        List<Setting> settings = module.getSettings();
        if (settings == null || settings.isEmpty() || openProgress <= 0.01f) {
            return;
        }

        float maxSettingHeight = ClickGuiLayout.calculateSettingsHeight(module);
        float settingsClipY = moduleY + ClickGuiLayout.SETTING_START_Y;
        float settingsClipHeight = maxSettingHeight * openProgress;

        ScissorUtils.push();
        ScissorUtils.setFromComponentCoordinates(panelX + ClickGuiLayout.MODULE_PADDING, settingsClipY, ClickGuiLayout.MODULE_INNER_WIDTH, settingsClipHeight);

        float settingYoffset = ClickGuiLayout.SETTING_START_Y;
        for (Setting setting : settings) {
            if (setting == null || !setting.visible()) {
                continue;
            }

            float settingY = moduleY + settingYoffset + ClickGuiLayout.SETTING_PADDING;
            int alpha = (int) (255 * openProgress);

            if (setting instanceof BooleanSetting booleanSetting) {
                renderBooleanSetting(context, panelX, settingY, alpha, colorTheme, mouseX, mouseY, booleanSetting, state);
                settingYoffset += 12f;
            } else if (setting instanceof TextSetting textSetting) {
                renderTextSetting(context, panelX, settingY, alpha, colorTheme, mouseX, mouseY, textSetting, state);
                settingYoffset += 22f;
            } else if (setting instanceof FloatSetting floatSetting) {
                renderFloatSetting(context, panelX, settingY, alpha, colorTheme, mouseX, mouseY, floatSetting, state);
                settingYoffset += 22f;
            } else if (setting instanceof ModeSetting modeSetting) {
                renderModeSetting(context, panelX, settingY, alpha, colorTheme, mouseX, mouseY, modeSetting, state);
                settingYoffset += ClickGuiLayout.calculateModeSettingHeight(modeSetting);
            } else if (setting instanceof ListSetting listSetting) {
                renderListSetting(context, panelX, settingY, alpha, colorTheme, mouseX, mouseY, listSetting, state);
                settingYoffset += ClickGuiLayout.calculateListSettingHeight(listSetting);
            } else if (setting instanceof BindSetting bindSetting) {
                renderBindSetting(context, panelX, settingY, alpha, colorTheme, mouseX, mouseY, bindSetting, state);
                settingYoffset += 12f;
            }
        }

        ScissorUtils.pop();
    }

    private void renderBooleanSetting(DrawContext context, float panelX, float settingY, int alpha, int colorTheme, double mouseX, double mouseY, BooleanSetting booleanSetting, ClickGuiState state) {
        AnimationUtils backgroundAnimation = state.getBooleanBackgroundAnimation(booleanSetting);
        AnimationUtils circleAnimation = state.getBooleanCircleAnimation(booleanSetting);
        backgroundAnimation.update(booleanSetting.isState() ? 1f : 0f);
        circleAnimation.update(booleanSetting.isState() ? 1f : 0f);

        float backgroundProgress = backgroundAnimation.getValue();
        float circleProgress = circleAnimation.getValue();

        int offColor = ColorUtils.darken(colorTheme, 0.05f);
        int onColor = colorTheme;

        int r = (int) ((offColor >> 16 & 255) + ((onColor >> 16 & 255) - (offColor >> 16 & 255)) * backgroundProgress);
        int g = (int) ((offColor >> 8 & 255) + ((onColor >> 8 & 255) - (offColor >> 8 & 255)) * backgroundProgress);
        int b = (int) ((offColor & 255) + ((onColor & 255) - (offColor & 255)) * backgroundProgress);
        int a = (int) ((offColor >> 24 & 255) + ((onColor >> 24 & 255) - (offColor >> 24 & 255)) * backgroundProgress);
        int interpolatedColor = (a << 24) | (r << 16) | (g << 8) | b;

        float maxWidth = (panelX + 73f) - (panelX + ClickGuiLayout.SETTING_LEFT);
        drawStringWithHoverScroll(
                issue(13),
                context.getMatrices(),
                booleanSetting.name(),
                panelX + ClickGuiLayout.SETTING_LEFT,
                settingY,
                maxWidth,
                getPrimarySettingColor(alpha),
                mouseX,
                mouseY,
                state,
                getSettingTextKey(booleanSetting)
        );

        // Новый стиль: многослойное свечение для переключателя
        // Внешнее свечение
        RenderUtils.drawRoundedRect(
                context.getMatrices(),
                panelX + 74f,
                settingY - 3f,
                18f,
                11f,
                4.5f,
                ColorUtils.rgba((interpolatedColor >> 16) & 255, (interpolatedColor >> 8) & 255, interpolatedColor & 255, (int)(alpha * 0.25f * backgroundProgress))
        );
        
        // Среднее свечение
        RenderUtils.drawRoundedRect(
                context.getMatrices(),
                panelX + 74.5f,
                settingY - 2.5f,
                17f,
                10f,
                4f,
                ColorUtils.rgba((interpolatedColor >> 16) & 255, (interpolatedColor >> 8) & 255, interpolatedColor & 255, (int)(alpha * 0.35f * backgroundProgress))
        );

        // Основной фон с градиентом
        RenderUtils.drawGradientRect(
                context.getMatrices(),
                panelX + 75,
                settingY - 2,
                16,
                9,
                3.5f,
                ColorUtils.rgba((interpolatedColor >> 16) & 255, (interpolatedColor >> 8) & 255, interpolatedColor & 255, alpha),
                ColorUtils.darken(ColorUtils.rgba((interpolatedColor >> 16) & 255, (interpolatedColor >> 8) & 255, interpolatedColor & 255, alpha), 0.15f),
                false
        );

        float circleX = panelX + 79.5f + (circleProgress * 6.2f);
        // Внешнее свечение круга
        RenderUtils.drawRoundCircle(context.getMatrices(), circleX + 0.5f, settingY + 2.5f, 8.5f, ColorUtils.rgba(255, 255, 255, (int)(alpha * 0.2f * backgroundProgress)));
        // Среднее свечение
        RenderUtils.drawRoundCircle(context.getMatrices(), circleX + 0.5f, settingY + 2.5f, 7.8f, ColorUtils.rgba(255, 255, 255, (int)(alpha * 0.35f * backgroundProgress)));
        // Основной круг с градиентом
        RenderUtils.drawRoundCircle(context.getMatrices(), circleX + 0.5f, settingY + 2.5f, 7, ColorUtils.rgba(255, 255, 255, alpha));
        // Внутренняя подсветка
        RenderUtils.drawRoundCircle(context.getMatrices(), circleX + 0.5f, settingY + 1.8f, 4f, ColorUtils.rgba(255, 255, 255, (int)(alpha * 0.4f)));
    }

    private void renderFloatSetting(DrawContext context, float panelX, float settingY, int alpha, int colorTheme, double mouseX, double mouseY, FloatSetting floatSetting, ClickGuiState state) {
        if (floatSetting.isActive()) {
            floatSetting.setValue(state.updateActiveSliderValue(floatSetting, mouseX));
        }

        AnimationUtils sliderAnimation = state.getSliderAnimation(floatSetting);
        sliderAnimation.update(state.getSliderPos(floatSetting));
        float animatedPos = sliderAnimation.getValue();

        String valueString = formatSliderValue(floatSetting);
        float valueX = panelX + ClickGuiLayout.SETTING_RIGHT - issue(12).getWidth(valueString);
        float nameMaxWidth = (valueX - 4f) - (panelX + ClickGuiLayout.SETTING_LEFT);

        drawStringWithHoverScroll(
                issue(12),
                context.getMatrices(),
                floatSetting.name(),
                panelX + ClickGuiLayout.SETTING_LEFT,
                settingY + 1,
                nameMaxWidth,
                getPrimarySettingColor(alpha),
                mouseX,
                mouseY,
                state,
                getSettingTextKey(floatSetting)
        );

        issue(12).drawString(context.getMatrices(), valueString, valueX, settingY + 1, ColorUtils.setAlphaColor(colorTheme, alpha));

        // Новый стиль: трек слайдера с градиентом и свечением
        float trackX = panelX + ClickGuiLayout.SETTING_LEFT;
        float trackY = settingY + 9;
        
        // Фон трека с градиентом
        RenderUtils.drawGradientRect(context.getMatrices(), trackX, trackY, ClickGuiLayout.SLIDER_WIDTH, 4.5f, 1.25f,
                ColorUtils.setAlphaColor(ColorUtils.darken(colorTheme, 0.25f), alpha),
                ColorUtils.setAlphaColor(ColorUtils.darken(colorTheme, 0.3f), alpha), true);

        // Заполненная часть с градиентом и свечением
        if (animatedPos > 0.01f) {
            float fillWidth = animatedPos * ClickGuiLayout.SLIDER_WIDTH;
            // Внешнее свечение заполнения
            RenderUtils.drawRoundedRect(context.getMatrices(), trackX - 0.5f, trackY - 0.5f, fillWidth + 1f, 5.5f, 1.75f,
                    ColorUtils.setAlphaColor(colorTheme, (int)(alpha * 0.3f)));
            // Основное заполнение с градиентом
            int fillHighlight = ColorUtils.interpolateColor(colorTheme, ColorUtils.rgba(255, 255, 255, 255), 0.15f);
            RenderUtils.drawGradientRect(context.getMatrices(), trackX, trackY, fillWidth, 4.5f, 1.25f,
                    ColorUtils.setAlphaColor(fillHighlight, alpha),
                    ColorUtils.setAlphaColor(colorTheme, alpha), true);
            // Верхняя подсветка
            int topHighlight = ColorUtils.interpolateColor(colorTheme, ColorUtils.rgba(255, 255, 255, 255), 0.3f);
            RenderUtils.drawGradientRect(context.getMatrices(), trackX, trackY, fillWidth, 2f, 1.25f,
                    ColorUtils.setAlphaColor(topHighlight, (int)(alpha * 0.4f)),
                    ColorUtils.setAlphaColor(colorTheme, 0), false);
        }
        
        // Ручка слайдера с многослойным свечением
        float handleX = trackX + animatedPos * ClickGuiLayout.SLIDER_WIDTH;
        float handleY = settingY + 11.25f;
        
        // Внешнее свечение
        RenderUtils.drawRoundCircle(context.getMatrices(), handleX, handleY, 9f, ColorUtils.setAlphaColor(colorTheme, (int)(alpha * 0.25f)));
        // Среднее свечение
        RenderUtils.drawRoundCircle(context.getMatrices(), handleX, handleY, 7.5f, ColorUtils.setAlphaColor(colorTheme, (int)(alpha * 0.4f)));
        // Основной круг с градиентом (имитация через два круга)
        RenderUtils.drawRoundCircle(context.getMatrices(), handleX, handleY, 6.5f, ColorUtils.setAlphaColor(-1, alpha));
        // Внутренняя подсветка
        RenderUtils.drawRoundCircle(context.getMatrices(), handleX, handleY - 1f, 3.5f, ColorUtils.setAlphaColor(-1, (int)(alpha * 0.5f)));
    }

    private void renderTextSetting(DrawContext context, float panelX, float settingY, int alpha, int colorTheme, double mouseX, double mouseY, TextSetting textSetting, ClickGuiState state) {
        String value = textSetting.get();
        boolean editing = state.getEditingTextSetting() == textSetting;
        String preview = value == null || value.isEmpty() ? "..." : value;
        String boxText = editing ? preview + "_" : preview;
        float boxWidth = ClickGuiLayout.TEXT_SETTING_WIDTH;
        float boxX = panelX + 49f;

        drawStringWithHoverScroll(
                issue(13),
                context.getMatrices(),
                textSetting.name(),
                panelX + ClickGuiLayout.SETTING_LEFT,
                settingY,
                (boxX - 1f) - (panelX + ClickGuiLayout.SETTING_LEFT),
                getPrimarySettingColor(alpha),
                mouseX,
                mouseY,
                state,
                getSettingTextKey(textSetting)
        );

        float boxY = settingY - 2.5f;
        
        // Новый стиль: стеклянный эффект для текстового поля
        if (editing) {
            // Внешнее свечение при редактировании
            RenderUtils.drawRoundedRect(context.getMatrices(), boxX - 1f, boxY - 1f, boxWidth + 2f, 11f, 2.5f,
                    ColorUtils.setAlphaColor(colorTheme, (int)(alpha * 0.3f)));
            // Среднее свечение
            RenderUtils.drawRoundedRect(context.getMatrices(), boxX - 0.5f, boxY - 0.5f, boxWidth + 1f, 10f, 2f,
                    ColorUtils.setAlphaColor(colorTheme, (int)(alpha * 0.4f)));
        }
        
        // Основной фон с градиентом
        int bgColor1 = editing ? ColorUtils.darken(colorTheme, 0.1f) : ColorUtils.darken(colorTheme, 0.2f);
        int bgColor2 = editing ? ColorUtils.darken(colorTheme, 0.15f) : ColorUtils.darken(colorTheme, 0.25f);
        RenderUtils.drawGradientRect(context.getMatrices(), boxX, boxY, boxWidth, 9f, 1.5f,
                ColorUtils.setAlphaColor(bgColor1, alpha),
                ColorUtils.setAlphaColor(bgColor2, alpha), false);
        
        // Верхняя подсветка
        if (editing) {
            int textHighlight = ColorUtils.interpolateColor(colorTheme, ColorUtils.rgba(255, 255, 255, 255), 0.2f);
            RenderUtils.drawGradientRect(context.getMatrices(), boxX + 1f, boxY + 1f, boxWidth - 2f, 3f, 1f,
                    ColorUtils.setAlphaColor(textHighlight, (int)(alpha * 0.2f)),
                    ColorUtils.setAlphaColor(colorTheme, 0), false);
        }
        
        int textColor = ColorUtils.setAlphaColor(-1, alpha);
        ScissorUtils.push();
        ScissorUtils.setFromComponentCoordinates(boxX + 2f, boxY + 1f, boxWidth - 4f, 7f);
        issue(12).drawString(context.getMatrices(), boxText, boxX + 3f, settingY + 1f, textColor);
        ScissorUtils.pop();
    }

    private void renderModeSetting(DrawContext context, float panelX, float settingY, int alpha, int colorTheme, double mouseX, double mouseY, ModeSetting modeSetting, ClickGuiState state) {
        drawStringWithHoverScroll(
                issue(12),
                context.getMatrices(),
                modeSetting.name(),
                panelX + ClickGuiLayout.SETTING_LEFT,
                settingY + 1,
                ClickGuiLayout.SETTING_RIGHT - ClickGuiLayout.SETTING_LEFT,
                getPrimarySettingColor(alpha),
                mouseX,
                mouseY,
                state,
                getSettingTextKey(modeSetting)
        );

        float modeY = settingY + 10;
        for (String mode : modeSetting.getMods()) {
            boolean selected = modeSetting.getCurrent().equals(mode);
            AnimationUtils animation = state.getModeAnimation(getModeKey(modeSetting, mode), selected);
            animation.update(selected ? 1f : 0f);
            float progress = animation.getValue();

            // Новый стиль: радио-кнопки с многослойным свечением
            float radioX = panelX + 86;
            float radioY = modeY + 2;
            
            if (selected) {
                // Внешнее свечение для выбранного
                RenderUtils.drawRoundCircle(context.getMatrices(), radioX, radioY, 10.5f, 
                        ColorUtils.setAlphaColor(colorTheme, (int)(alpha * 0.2f * progress)));
                // Среднее свечение
                RenderUtils.drawRoundCircle(context.getMatrices(), radioX, radioY, 9.8f, 
                        ColorUtils.setAlphaColor(colorTheme, (int)(alpha * 0.35f * progress)));
            }
            
            // Внешний круг с градиентом
            int outerColor = ColorUtils.setAlphaColor(colorTheme, (int) (alpha * (0.3f + 0.7f * progress)));
            RenderUtils.drawRoundCircle(context.getMatrices(), radioX, radioY, 9f, outerColor);
            
            // Внутренний круг
            int innerColor = selected ? ColorUtils.setAlphaColor(ColorUtils.darken(colorTheme, 0.35f), alpha) : ColorUtils.rgba(255, 255, 255, alpha);
            float innerRadius = (6f - (progress * 2f)) + 3f;
            RenderUtils.drawRoundCircle(context.getMatrices(), radioX, radioY, innerRadius, innerColor);
            
            // Центральная точка для выбранного
            if (selected && progress > 0.5f) {
                int centerHighlight = ColorUtils.interpolateColor(colorTheme, ColorUtils.rgba(255, 255, 255, 255), 0.2f);
                RenderUtils.drawRoundCircle(context.getMatrices(), radioX, radioY, 3f * progress, 
                        ColorUtils.setAlphaColor(centerHighlight, (int)(alpha * progress)));
            }

            issue(13).draw(context.getMatrices(), mode, panelX + ClickGuiLayout.SETTING_LEFT, modeY, getSecondarySettingColor(alpha));
            modeY += 10f;
        }
    }

    private void renderListSetting(DrawContext context, float panelX, float settingY, int alpha, int colorTheme, double mouseX, double mouseY, ListSetting listSetting, ClickGuiState state) {
        drawStringWithHoverScroll(
                issue(12),
                context.getMatrices(),
                listSetting.name(),
                panelX + ClickGuiLayout.SETTING_LEFT,
                settingY + 1,
                ClickGuiLayout.SETTING_RIGHT - ClickGuiLayout.SETTING_LEFT,
                getPrimarySettingColor(alpha),
                mouseX,
                mouseY,
                state,
                getSettingTextKey(listSetting)
        );

        float listY = settingY + 10;
        for (BooleanSetting entry : listSetting.getSettings()) {
            if (!entry.visible()) {
                continue;
            }

            boolean selected = entry.isState();
            AnimationUtils animation = state.getListAnimation(getListKey(listSetting, entry), selected);
            animation.update(selected ? 1f : 0f);
            float progress = animation.getValue();

            // Новый стиль: чекбоксы с многослойным свечением
            float checkX = panelX + 86;
            float checkY = listY + 2;
            
            if (selected) {
                // Внешнее свечение для выбранного
                RenderUtils.drawRoundCircle(context.getMatrices(), checkX, checkY, 10.5f, 
                        ColorUtils.setAlphaColor(colorTheme, (int)(alpha * 0.2f * progress)));
                // Среднее свечение
                RenderUtils.drawRoundCircle(context.getMatrices(), checkX, checkY, 9.8f, 
                        ColorUtils.setAlphaColor(colorTheme, (int)(alpha * 0.35f * progress)));
            }
            
            // Внешний круг с градиентом
            int outerColor = ColorUtils.setAlphaColor(colorTheme, (int) (alpha * (0.3f + 0.7f * progress)));
            RenderUtils.drawRoundCircle(context.getMatrices(), checkX, checkY, 9, outerColor);
            
            // Внутренний круг
            int innerColor = selected ? ColorUtils.setAlphaColor(ColorUtils.darken(colorTheme, 0.35f), alpha) : ColorUtils.rgba(255, 255, 255, alpha);
            float innerRadius = (6f - (progress * 2f)) + 3f;
            RenderUtils.drawRoundCircle(context.getMatrices(), checkX, checkY, innerRadius, innerColor);
            
            // Галочка для выбранного
            if (selected && progress > 0.6f) {
                int checkHighlight = ColorUtils.interpolateColor(colorTheme, ColorUtils.rgba(255, 255, 255, 255), 0.25f);
                RenderUtils.drawRoundCircle(context.getMatrices(), checkX, checkY, 3.5f * progress, 
                        ColorUtils.setAlphaColor(checkHighlight, (int)(alpha * progress)));
            }

            drawStringWithHoverScroll(
                    issue(13),
                    context.getMatrices(),
                    entry.name(),
                    panelX + ClickGuiLayout.SETTING_LEFT,
                    listY,
                    (panelX + 73f) - (panelX + ClickGuiLayout.SETTING_LEFT),
                    getSecondarySettingColor(alpha),
                    mouseX,
                    mouseY,
                    state,
                    getListKey(listSetting, entry) + "_text"
            );

            listY += 10f;
        }
    }

    private void renderBindSetting(DrawContext context, float panelX, float settingY, int alpha, int colorTheme, double mouseX, double mouseY, BindSetting bindSetting, ClickGuiState state) {
        boolean binding = state.getBindingSetting() == bindSetting;
        AnimationUtils bindAnimation = state.getBindAnimation(getBindKey(bindSetting), binding);
        bindAnimation.update(binding ? 1f : 0f);
        float progress = bindAnimation.getValue();

        String bindString = binding ? "..." : state.toEnglish(KeyBoardUtils.getBindName(bindSetting.getKey()));
        float bindTextWidth = issue(12).getWidth(bindString);
        float bindWidth = bindTextWidth + 6f;
        float bindX = panelX + ClickGuiLayout.SETTING_RIGHT - bindWidth;
        float bindY = settingY - 2.5f;

        // Новый стиль: кнопка бинда с градиентом и свечением
        int baseColor = ColorUtils.interpolateColor(ColorUtils.darken(colorTheme, 0.2f), colorTheme, progress);
        
        if (binding) {
            // Внешнее свечение при привязке
            RenderUtils.drawRoundedRect(context.getMatrices(), bindX - 1f, bindY - 1f, bindWidth + 2f, 11f, 2.5f,
                    ColorUtils.setAlphaColor(colorTheme, (int)(alpha * 0.3f * progress)));
            // Среднее свечение
            RenderUtils.drawRoundedRect(context.getMatrices(), bindX - 0.5f, bindY - 0.5f, bindWidth + 1f, 10f, 2f,
                    ColorUtils.setAlphaColor(colorTheme, (int)(alpha * 0.4f * progress)));
        }
        
        // Основной фон с градиентом
        RenderUtils.drawGradientRect(context.getMatrices(), bindX, bindY, bindWidth, 9, 1.5f,
                ColorUtils.setAlphaColor(baseColor, alpha),
                ColorUtils.setAlphaColor(ColorUtils.darken(baseColor, 0.15f), alpha), false);
        
        // Верхняя подсветка
        if (binding) {
            int bindHighlight = ColorUtils.interpolateColor(colorTheme, ColorUtils.rgba(255, 255, 255, 255), 0.2f);
            RenderUtils.drawGradientRect(context.getMatrices(), bindX + 1f, bindY + 1f, bindWidth - 2f, 3f, 1f,
                    ColorUtils.setAlphaColor(bindHighlight, (int)(alpha * 0.3f * progress)),
                    ColorUtils.setAlphaColor(colorTheme, 0), false);
        }
        
        int bindTextColor = ColorUtils.setAlphaColor(ColorUtils.interpolateColor(ColorUtils.rgb(140, 139, 145), -1, progress), alpha);
        issue(12).drawString(context.getMatrices(), bindString, bindX + 3, settingY + 1, bindTextColor);
        
        drawStringWithHoverScroll(
                issue(12),
                context.getMatrices(),
                bindSetting.name(),
                panelX + ClickGuiLayout.SETTING_LEFT,
                settingY + 1,
                (bindX - 4f) - (panelX + ClickGuiLayout.SETTING_LEFT),
                getPrimarySettingColor(alpha),
                mouseX,
                mouseY,
                state,
                getSettingTextKey(bindSetting)
        );
    }

    private String getModeKey(ModeSetting setting, String mode) {
        return System.identityHashCode(setting) + "_mode_" + mode;
    }

    private String getListKey(ListSetting setting, BooleanSetting entry) {
        return setting.hashCode() + "_list_" + entry.name();
    }

    private String getBindKey(BindSetting setting) {
        return setting.hashCode() + "_bind";
    }

    private String formatSliderValue(FloatSetting setting) {
        float value = setting.get();
        float increment = setting.getIncrement();
        if (increment >= 1f) {
            return String.valueOf((int) value);
        }
        if (increment >= 0.1f) {
            return String.format("%.1f", value);
        }
        return String.format("%.2f", value);
    }

    private void drawStringWithHoverScroll(Font font, MatrixStack matrix, String text, float x, float y, float maxWidth, int color, double mouseX, double mouseY, ClickGuiState state, String animationKey) {
        if (text == null || text.isEmpty() || maxWidth <= 0f) {
            return;
        }

        float totalWidth = font.getWidth(text);
        float overflow = totalWidth - maxWidth;
        if (overflow <= HOVER_SCROLL_OVERFLOW_THRESHOLD) {
            font.draw(matrix, text, x, y, color);
            return;
        }

        boolean hovered = isTextHovered(x, y, maxWidth, font.getHeight(), mouseX, mouseY);
        float scrollPhase = state.advanceTextScrollPhase(animationKey, hovered);
        boolean scrollActive = state.isTextScrollActive(animationKey, hovered);
        AnimationUtils hoverAnimation = state.getTextHoverAnimation(animationKey, scrollActive);
        hoverAnimation.update(scrollActive ? 1f : 0f);
        float hoverProgress = hoverAnimation.getValue();
        float scrollOffset = getHoverScrollOffset(overflow, scrollPhase) * hoverProgress;

        ScissorUtils.push();
        ScissorUtils.setFromComponentCoordinates(x, y - 2.0f, maxWidth, font.getHeight() + 4.0f);
        font.draw(matrix, text, x - scrollOffset, y, color);
        ScissorUtils.pop();
    }

    private int getPrimarySettingColor(int alpha) {
        return ColorUtils.rgba(245, 245, 248, alpha);
    }

    private int getSecondarySettingColor(int alpha) {
        return ColorUtils.rgba(186, 186, 194, alpha);
    }

    private boolean isTextHovered(float x, float y, float width, float height, double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y - 2.0f && mouseY <= y + height + 2.0f;
    }

    private float getHoverScrollOffset(float maxOffset, float phase) {
        if (maxOffset <= 0.0f) {
            return 0.0f;
        }

        float pingPong = phase < 0.5f ? (phase * 2.0f) : (2.0f - phase * 2.0f);
        float eased = pingPong * pingPong * (3.0f - 2.0f * pingPong);
        return maxOffset * eased;
    }

    private String getSettingTextKey(Setting setting) {
        return "setting_text_" + System.identityHashCode(setting);
    }

    private Font issue(int size) {
        return Fonts.getFont("suisse", size);
    }
}
