package fun.eversense.api.utils.rpc.callbacks;

import com.sun.jna.Callback;
import fun.eversense.api.utils.rpc.utils.DiscordUser;

public interface JoinRequestCallback extends Callback {
    void apply(DiscordUser var1);
}
