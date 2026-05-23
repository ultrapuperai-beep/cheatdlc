package fun.eversense.api.events.implement;

import lombok.AllArgsConstructor;
import fun.eversense.api.events.Event;

@AllArgsConstructor
public class EventCloseInv extends Event {
    public int windowId;
}

