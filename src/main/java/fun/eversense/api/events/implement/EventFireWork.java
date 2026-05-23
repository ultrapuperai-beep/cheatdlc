package fun.eversense.api.events.implement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import fun.eversense.api.events.Event;
@Getter
@Setter
@AllArgsConstructor
public class EventFireWork extends Event {
    private final FireworkRocketEntity firework;
}