package fun.eversense.client.modules.impl.render.base;

import lombok.RequiredArgsConstructor;
import fun.eversense.api.QClient;
import fun.eversense.api.events.implement.EventRender;
import fun.eversense.api.events.implement.EventUpdate;
import fun.eversense.api.utils.draggable.Draggable;

@RequiredArgsConstructor
public class InterfaceProcessing implements QClient {

    public final Draggable draggable;
    private boolean unusualRectType = false;

    public boolean isUnusualRectType() {
        return unusualRectType;
    }

    public void setUnusualRectType(boolean unusualRectType) {
        this.unusualRectType = unusualRectType;
    }

    public void onUpdate(EventUpdate eventUpdate) {
    }

    public void onRender(EventRender.Default eventRender) {

    }
}
