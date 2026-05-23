package fun.eversense.client.modules.impl.misc;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import fun.eversense.eversense;
import fun.eversense.api.events.EventLink;
import fun.eversense.api.events.implement.EventPacket;
import fun.eversense.client.modules.Module;
import fun.eversense.client.modules.settings.implement.BooleanSetting;

import java.util.Locale;

public class AutoAccept extends Module {

    public static AutoAccept INSTANCE = new AutoAccept();

    private final BooleanSetting onlyFriend = new BooleanSetting("Только друзья", false);

    public AutoAccept() {
        super("AutoAccept", "Автоматически принимает телепорт", ModuleCategory.MISC);
        addSettings(onlyFriend);
    }

    @EventLink
    public void onEvent(final EventPacket event) {
        if (mc.player == null || mc.world == null) return;
        if (event.getType() != EventPacket.Type.RECEIVE) return;

        Packet<?> packet = event.getPacket();
        if (packet instanceof GameMessageS2CPacket messagePacket) {
            String raw = messagePacket.content().getString().toLowerCase(Locale.ROOT);

            if (raw.contains("телепортироваться") || raw.contains("has requested teleport") || raw.contains("просит к вам телепортироваться")) {
                if (onlyFriend.isState()) {
                    boolean isFriend = false;

                    if (eversense.INSTANCE.friendStorage != null) {
                        for (String friend : eversense.INSTANCE.friendStorage.getFriends()) {
                            if (raw.contains(friend.toLowerCase(Locale.ROOT))) {
                                isFriend = true;
                                break;
                            }
                        }
                    }

                    if (!isFriend) {
                        return;
                    }
                }

                mc.player.networkHandler.sendChatCommand("tpaccept");
            }
        }
    }
}
