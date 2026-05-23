package fun.eversense.api.storages.implement;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import fun.eversense.eversense;
import fun.eversense.api.storages.implement.helpertstorages.Theme;
import fun.eversense.api.utils.color.ColorUtils;

import java.awt.*;
import java.util.Arrays;

@Getter
@Setter
public class ThemeStorage {

    private ObjectArrayList<Themes> themeList = new ObjectArrayList<>();
    private Themes themes;

    public ThemeStorage() {
        this.onInitialize();
    }

    private void onInitialize() {
        this.themeList.addAll(Arrays.asList(
                Themes.Custom,
                Themes.Purple,
                Themes.Red,
                Themes.Blue,
                Themes.Green,
                Themes.Pink,
                Themes.Orange,
                Themes.Blues,
                Themes.Yellows,
                Themes.Monochrome,
                Themes.White,
                Themes.Black
        ));
        this.themes = this.themeList.get(1);
    }

    @RequiredArgsConstructor @Getter
    public enum Themes {
        Custom(new Theme("Rainbow", ColorUtils.rgba(255, 255, 255, 0))),
        Purple(new Theme("Lavender", ColorUtils.rgba(190, 143, 255, 255), ColorUtils.darken(ColorUtils.rgba(190, 143, 255, 255), 0.35F))),
        Red(new Theme("Blood", ColorUtils.rgba(230, 50, 57, 255), ColorUtils.darken(ColorUtils.rgba(230, 50, 57, 255), 0.35f))),
        Blue(new Theme("Ocean", ColorUtils.rgba(95, 113, 191, 255), ColorUtils.darken(ColorUtils.rgba(95, 113, 191, 255), 0.35f))),
        Green(new Theme("Emerald", ColorUtils.rgba(60, 220, 140, 255), ColorUtils.darken(ColorUtils.rgba(60, 220, 140, 255), 0.35f))),
        Pink(new Theme("Rose", ColorUtils.rgba(255, 120, 190, 255), ColorUtils.darken(ColorUtils.rgba(255, 120, 190, 255), 0.35f))),
        Orange(new Theme("Gold", ColorUtils.rgba(252, 192, 88, 255), ColorUtils.darken(ColorUtils.rgba(252, 192, 88, 255), 0.35f))),
        Blues(new Theme("Diamond", ColorUtils.rgba(125, 217, 250, 255), ColorUtils.darken(ColorUtils.rgba(125, 217, 250, 255), 0.35f))),
        Yellows(new Theme("Sun", ColorUtils.rgba(252, 231, 88, 255), ColorUtils.darken(ColorUtils.rgba(252, 231, 88, 255), 0.35f))),
        Monochrome(new Theme("Monochrome", ColorUtils.rgba(180, 180, 180, 255), ColorUtils.rgba(100, 100, 100, 255))),
        White(new Theme("Pure White", ColorUtils.rgba(255, 255, 255, 255), ColorUtils.rgba(220, 220, 220, 255))),
        Black(new Theme("Pure Black", ColorUtils.rgba(60, 60, 60, 255), ColorUtils.rgba(30, 30, 30, 255)));

        final Theme theme;
    }
}
