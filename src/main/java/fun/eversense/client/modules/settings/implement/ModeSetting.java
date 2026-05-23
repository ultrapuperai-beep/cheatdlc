package fun.eversense.client.modules.settings.implement;

import lombok.Getter;
import lombok.Setter;
import fun.eversense.eversense;
import fun.eversense.client.modules.settings.Setting;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

@Getter
@Setter
public class ModeSetting extends Setting {

    private List<String> mods;

    private String current;

    private int index;

    public ModeSetting(String name, String current, String... modes) {
        super(name);
        this.mods = Arrays.asList(modes);
        this.index = this.mods.indexOf(current);
        if (this.index < 0) {
            this.index = 0;
        }
        this.current = this.mods.get(this.index);
    }

    public void set(String selected) {
        int newIndex = this.mods.indexOf(selected);
        if (newIndex < 0) {
            return;
        }
        this.current = selected;
        this.index = newIndex;
    }

    public boolean is(String mode) {
        return current.equals(mode);
    }

    public String displayMode(String mode) {
        return eversense.INSTANCE.localizationStorage == null ? mode : eversense.INSTANCE.localizationStorage.translate(mode);
    }

    public String displayCurrent() {
        return displayMode(current);
    }

    public ModeSetting visible(Supplier<Boolean> state) {
        this.visible = state;
        return this;
    }
}
