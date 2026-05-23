package fun.eversense.client.modules.impl.render.base.implement;

import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.MathHelper;
import fun.eversense.eversense;
import fun.eversense.api.events.implement.EventRender;
import fun.eversense.api.storages.implement.helpertstorages.enumvar.ModuleClass;
import fun.eversense.api.utils.animation.AnimationUtils;
import fun.eversense.api.utils.animation.Easings;
import fun.eversense.api.utils.color.ColorUtils;
import fun.eversense.api.utils.draggable.Draggable;
import fun.eversense.api.utils.render.RenderUtils;
import fun.eversense.api.utils.render.fonts.msdf.Font;
import fun.eversense.api.utils.render.fonts.msdf.Fonts;
import fun.eversense.api.utils.scissor.ScissorUtils;
import fun.eversense.client.modules.impl.render.base.InterfaceProcessing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Potions extends InterfaceProcessing {
    private static final class PotionSnapshot {
        RegistryEntry<StatusEffect> entry;
        String baseName;
        int amplifier;
        int duration;
        boolean infinite;
    }

    private final Map<StatusEffect, AnimationUtils> animations = new LinkedHashMap<>();
    private final Map<StatusEffect, PotionSnapshot> snapshots = new HashMap<>();
    private final Map<StatusEffect, Integer> maxDurations = new HashMap<>();
    private final Set<StatusEffect> renderOrderSeen = new HashSet<>();
    private final AnimationUtils widthAnimation = new AnimationUtils(70, 10.5f, Easings.QUAD_OUT);

    public Potions(Draggable draggable) {
        super(draggable);
    }

    private Font issue(int size) { return Fonts.getFont("suisse", size); }
    private Font icon(int size) { return Fonts.getFont("icon", size); }

    private AnimationUtils getAnimation(StatusEffect effect) {
        return animations.computeIfAbsent(effect, e -> new AnimationUtils(0, 10.5f, Easings.QUAD_OUT));
    }

    private static String getLevelSuffix(int level) {
        return String.valueOf(Math.max(1, level));
    }

    private static String formatDuration(StatusEffectInstance effect) {
        return formatDuration(effect.getDuration(), effect.isInfinite());
    }

    private static String formatDuration(int duration, boolean infinite) {
        if (infinite) {
            return "inf";
        }
        int seconds = Math.max(0, duration / 20);
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return minutes + ":" + (secs < 10 ? "0" + secs : String.valueOf(secs));
    }

    private void updateSnapshot(StatusEffectInstance effect) {
        StatusEffect type = effect.getEffectType().value();
        PotionSnapshot snapshot = snapshots.computeIfAbsent(type, e -> new PotionSnapshot());
        snapshot.entry = effect.getEffectType();
        snapshot.baseName = I18n.translate(effect.getTranslationKey());
        snapshot.amplifier = effect.getAmplifier() + 1;
        snapshot.duration = effect.getDuration();
        snapshot.infinite = effect.isInfinite();
    }

    private List<StatusEffect> buildRenderOrder(Collection<StatusEffectInstance> effects, Set<StatusEffect> active) {
        List<StatusEffect> order = new ArrayList<>();
        renderOrderSeen.clear();
        for (StatusEffectInstance effect : effects) {
            StatusEffect type = effect.getEffectType().value();
            if (renderOrderSeen.add(type)) {
                order.add(type);
            }
        }
        for (StatusEffect type : animations.keySet()) {
            if (!active.contains(type)) {
                order.add(type);
            }
        }
        return order;
    }

    private void drawEffectIcon(EventRender.Default eventRender, RegistryEntry<StatusEffect> effect, float x, float y, int size, int alpha) {
        Sprite sprite = mc.getStatusEffectSpriteManager().getSprite(effect);
        int color = ColorUtils.rgba(255, 255, 255, alpha);
        RenderUtils.drawSprite(eventRender.getContext().getMatrices(), sprite, x, y, size, color);
    }

    private void drawTextWithShadow(EventRender.Default eventRender, Font font, String text, float x, float y, int color) {
        int shadow = ColorUtils.rgba(20, 20, 20, 145);
        font.draw(eventRender.getContext().getMatrices(), text, x + 0.8f, y + 0.8f, shadow);
        font.draw(eventRender.getContext().getMatrices(), text, x, y, color);
    }

    @Override
    public void onRender(EventRender.Default eventRender) {
        if (ModuleClass.interfaceModule.style.is("New")) {
            NewStyle(eventRender);
        } else {
            WaveStyle(eventRender);
        }
        super.onRender(eventRender);
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
        
        Collection<StatusEffectInstance> effects = mc != null && mc.player != null
                ? mc.player.getStatusEffects()
                : java.util.List.of();
        
        Set<StatusEffect> active = new HashSet<>();
        for (StatusEffectInstance effect : effects) {
            StatusEffect type = effect.getEffectType().value();
            active.add(type);
            getAnimation(type).update(1);
            updateSnapshot(effect);
        }
        
        for (Map.Entry<StatusEffect, AnimationUtils> entry : animations.entrySet()) {
            if (!active.contains(entry.getKey())) {
                entry.getValue().update(0);
            }
        }
        
        List<StatusEffect> renderOrder = buildRenderOrder(effects, active);
        
        // Подсчитываем видимые эффекты и максимальную ширину
        float maxWidth = 80f;
        int visibleCount = 0;
        
        for (StatusEffect type : renderOrder) {
            AnimationUtils anim = getAnimation(type);
            float animValue = anim.getValue();
            PotionSnapshot snapshot = snapshots.get(type);
            
            if (animValue > 0.01f && snapshot != null) {
                visibleCount++;
                String baseName = snapshot.baseName != null ? snapshot.baseName : I18n.translate(type.getTranslationKey());
                String levelSuffix = snapshot.amplifier > 1 ? String.valueOf(snapshot.amplifier) : "";
                String time = formatDuration(snapshot.duration, snapshot.infinite);
                
                float nameWidth = textFont.getStringWidth(baseName);
                if (!levelSuffix.isEmpty()) {
                    nameWidth += textFont.getStringWidth(" " + levelSuffix);
                }
                float timeWidth = textFont.getStringWidth(time);
                float lineWidth = nameWidth + timeWidth + 15f; // отступы без иконки
                if (lineWidth > maxWidth) maxWidth = lineWidth;
            }
        }
        
        // Размеры
        float headerHeight = 18f;
        float itemHeight = 14f;
        float totalHeight = headerHeight + (visibleCount * itemHeight);
        float width = maxWidth + 10f;
        
        // Рисуем общий фон (черный) - с закруглениями
        RenderUtils.drawRoundedRect(matrices, baseX, y, width, totalHeight, 3f, blackBgColor);
        
        // Рисуем заголовок (серый фон) - с закруглениями сверху
        RenderUtils.drawRoundedRect(matrices, baseX, y, width, headerHeight, 3f, grayBgColor);
        
        // Текст "Potions" и иконка в заголовке
        String headerText = "Potions";
        textFont.drawString(matrices, headerText, baseX + 6f, y + 7f, whiteColor);
        
        // Иконка справа в заголовке (используем иконку зелья из icon font)
        String headerIconGlyph = "d"; // иконка зелья
        float headerIconX = baseX + width - iconFont.getStringWidth(headerIconGlyph) - 6f;
        iconFont.drawString(matrices, headerIconGlyph, headerIconX, y + 7.5f, themeColor);
        
        // Рисуем список эффектов
        float offsetY = headerHeight + 4f;
        
        for (StatusEffect type : renderOrder) {
            AnimationUtils anim = getAnimation(type);
            float animValue = anim.getValue();
            PotionSnapshot snapshot = snapshots.get(type);
            
            if (animValue > 0.01f && snapshot != null) {
                ScissorUtils.push();
                ScissorUtils.setFromComponentCoordinates(baseX, y, width, totalHeight);
                
                int alpha = (int) (255 * animValue);
                int textColor = ColorUtils.rgba(255, 255, 255, alpha);
                
                // Название эффекта
                String baseName = snapshot.baseName != null ? snapshot.baseName : I18n.translate(type.getTranslationKey());
                float textX = baseX + 6f;
                textFont.drawString(matrices, baseName, textX, y + offsetY + 1f, textColor);
                
                // Уровень (только если > 1)
                if (snapshot.amplifier > 1) {
                    float nameWidth = textFont.getStringWidth(baseName);
                    String levelText = " " + snapshot.amplifier;
                    int levelColor = ColorUtils.setAlphaColor(themeColor, alpha);
                    textFont.drawString(matrices, levelText, textX + nameWidth, y + offsetY + 1f, levelColor);
                }
                
                // Время справа
                String time = formatDuration(snapshot.duration, snapshot.infinite);
                float timeX = baseX + width - textFont.getStringWidth(time) - 6f;
                textFont.drawString(matrices, time, timeX, y + offsetY + 1f, textColor);
                
                offsetY += itemHeight * animValue;
                
                ScissorUtils.pop();
                ScissorUtils.unset();
            }
        }
        
        // Очистка неактивных анимаций
        animations.entrySet().removeIf(entry -> !active.contains(entry.getKey()) && entry.getValue().getValue() <= 0.01f);
        snapshots.keySet().removeIf(type -> !animations.containsKey(type));
        maxDurations.keySet().removeIf(type -> !animations.containsKey(type));
        
        draggable.setWidth(width);
        draggable.setHeight(totalHeight);
    }

    public void WaveStyle(EventRender.Default eventRender) {
        float x = draggable.getX();
        float y = draggable.getY();

        int time = (int) ((System.currentTimeMillis() % 2000) / 2000f * 360f);

        int leftTop = ColorUtils.getThemeColor(time);
        int leftBottom = ColorUtils.getThemeColor(time + 30);
        int centerTop = ColorUtils.getThemeColor(time + 90);
        int centerBottom = ColorUtils.getThemeColor(time + 120);
        int rightTop = ColorUtils.getThemeColor(time + 180);
        int rightBottom = ColorUtils.getThemeColor(time + 210);

        Collection<StatusEffectInstance> effects = mc != null && mc.player != null
                ? mc.player.getStatusEffects()
                : java.util.List.of();

        Set<StatusEffect> active = new HashSet<>();
        for (StatusEffectInstance effect : effects) {
            StatusEffect type = effect.getEffectType().value();
            active.add(type);
            getAnimation(type).update(1);
        }
        for (Map.Entry<StatusEffect, AnimationUtils> entry : animations.entrySet()) {
            if (!active.contains(entry.getKey())) {
                entry.getValue().update(0);
            }
        }

        float width = 84f;
        float height = 18;
        int visibleEffects = 0;

        for (StatusEffectInstance effect : effects) {
            AnimationUtils anim = getAnimation(effect.getEffectType().value());
            float animValue = anim.getValue();
            if (animValue <= 0.01f) continue;
            visibleEffects++;

            String baseName = I18n.translate(effect.getTranslationKey());
            String levelSuffix = getLevelSuffix(effect.getAmplifier() + 1);
            String line = baseName + (levelSuffix.isEmpty() ? "" : " > " + levelSuffix);
            width = Math.max(width, issue(16).getWidth(line) + 38f);
            width = Math.max(width, issue(15).getWidth(formatDuration(effect)) + 38f);
            height += 18f * animValue;
        }

        if (visibleEffects == 0) {
            float headerHeight = 18f;
            RenderUtils.drawWaveHudHeader(eventRender.getContext().getMatrices(), x, y, width, 15, 0,
                    10, 10, leftTop, leftBottom, centerTop, centerBottom, rightTop, rightBottom);
            String title = "potions";
            float titleX = x + (width - issue(16).getWidth(title)) / 2.0f;
            drawTextWithShadow(eventRender, issue(16), title, titleX, y + 5, -1);
            draggable.setWidth(width);
            draggable.setHeight(headerHeight);
            return;
        }

        RenderUtils.drawWaveHudPanel(eventRender.getContext().getMatrices(), x, y, width, height, ColorUtils.rgba(25, 25, 25, 150),
                15, 0, 10, 10,
                leftTop, leftBottom, centerTop, centerBottom, rightTop, rightBottom);

        String title = "potions";
        float titleX = x + (width - issue(16).getWidth(title)) / 1.9f;
        drawTextWithShadow(eventRender, issue(16), title, titleX, y + 5, -1);

        float yOffset = 20f;
        for (StatusEffectInstance effect : effects) {
            AnimationUtils anim = getAnimation(effect.getEffectType().value());
            float animValue = anim.getValue();
            if (animValue <= 0.01f) continue;

            ScissorUtils.push();
            ScissorUtils.setFromComponentCoordinates(x, y, width, height);

            int alpha = (int) (255 * animValue);
            int textColor = ColorUtils.rgba(255, 255, 255, alpha);
            int levelColor = ColorUtils.rgba(20, 185, 45, alpha);

            float iconX = x + 5f;
            float iconY = y + yOffset;
            drawEffectIcon(eventRender, effect.getEffectType(), iconX, iconY, 11, alpha);

            String baseName = I18n.translate(effect.getTranslationKey()).toLowerCase();
            String levelSuffix = getLevelSuffix(effect.getAmplifier() + 1);
            float textX = iconX + 14f;

            issue(15).draw(eventRender.getContext().getMatrices(), baseName + " >", textX, y + yOffset - 1, textColor);
            if (!levelSuffix.isEmpty()) {
                float nameW = issue(14).getWidth(baseName + " >");
                issue(14).draw(eventRender.getContext().getMatrices(), " " + levelSuffix, textX + nameW + 2, y + yOffset - 0.5, levelColor);
            }

            issue(14).draw(eventRender.getContext().getMatrices(), formatDuration(effect), textX, y + yOffset + 7.5, textColor);

            yOffset += 18f * animValue;
            ScissorUtils.pop();
            ScissorUtils.unset();
        }

        draggable.setWidth(width);
        draggable.setHeight(height);
    }
}
