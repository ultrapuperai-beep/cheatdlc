package fun.eversense.api.utils.rpc;

import lombok.Getter;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.network.ServerInfo;
import fun.eversense.api.QClient;
import fun.eversense.api.utils.rpc.utils.DiscordEventHandlers;
import fun.eversense.api.utils.rpc.utils.DiscordRPC;
import fun.eversense.api.utils.rpc.utils.DiscordRichPresence;
import fun.eversense.client.modules.impl.render.base.implement.WaterMark;

@Getter
public class DiscordManager implements QClient {

    private DiscordDaemonThread discordDaemonThread;
    private long APPLICATION_ID;

    private boolean running;

    private String image;
    private String site;
    private String telegram;

    private void cppInit() {
        discordDaemonThread = new DiscordDaemonThread();
        APPLICATION_ID = 1466765806120472650L;
        running = true;
        image = "logo";
        site = "https://eversenseclient.fun/";
        telegram = "https://t.me/eversenseclient";
    }

    String state = "";

    public static DiscordRichPresence discordRichPresence = new DiscordRichPresence();
    public static DiscordRPC discordRPC = DiscordRPC.INSTANCE;

    public void init() {
        cppInit();
        DiscordEventHandlers handlers = new DiscordEventHandlers.Builder().build();

        DiscordRPC.INSTANCE.Discord_Initialize(String.valueOf(APPLICATION_ID), handlers, true, "");
        discordRichPresence.startTimestamp = System.currentTimeMillis() / 1000L;
        discordRichPresence.largeImageKey = image;
        discordRichPresence.largeImageText = "eversense client";
        discordRPC.Discord_UpdatePresence(discordRichPresence);

        new Thread(() -> {
            while (running) {
                try {
                    String username = WaterMark.getUsername();
                    String uid = WaterMark.getUID();
                    
                    discordRichPresence.details = "Name » " + username;
                    
                    // Получаем информацию о сервере
                    String serverInfo = getServerInfo();
                    if (serverInfo != null && !serverInfo.isEmpty()) {
                        discordRichPresence.state = serverInfo;
                    } else {
                        discordRichPresence.state = "UID » " + uid;
                    }
                    
                    discordRichPresence.largeImageKey = image;
                    discordRichPresence.largeImageText = "eversense client";
                    discordRichPresence.button_label_1 = "Купить";
                    discordRichPresence.button_url_1 = site;
                    discordRichPresence.button_label_2 = "Телеграмм";
                    discordRichPresence.button_url_2 = telegram;
                    
                    DiscordRPC.INSTANCE.Discord_UpdatePresence(discordRichPresence);
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (Exception e) {
                    // Игнорируем ошибки, чтобы поток продолжал работать
                }
            }
        }, "Discord-RPC-Updater").start();

        discordDaemonThread.start();
    }
    
    private String getServerInfo() {
        try {
            if (mc == null || mc.player == null) {
                if (mc != null && mc.currentScreen instanceof TitleScreen) {
                    return "В главном меню";
                } else if (mc != null && mc.currentScreen instanceof MultiplayerScreen) {
                    return "Выбор сервера";
                } else if (mc != null && mc.currentScreen instanceof SelectWorldScreen) {
                    return "Выбор мира";
                }
                return null;
            }
            
            ServerInfo serverInfo = mc.getCurrentServerEntry();
            if (serverInfo != null && serverInfo.address != null && !serverInfo.address.isEmpty()) {
                return "Играет на " + serverInfo.address;
            }
            
            return "В одиночной игре";
        } catch (Exception e) {
            return null;
        }
    }

    public DiscordManager start() {
        init();
        return this;
    }

    public void stopRPC() {
        running = false;
        DiscordRPC.INSTANCE.Discord_Shutdown();
        if (discordDaemonThread != null) {
            discordDaemonThread.interrupt();
        }
    }

    private class DiscordDaemonThread extends Thread {
        @Override
        public void run() {
            this.setName("Discord-RPC");

            try {
                while (running) {
                    DiscordRPC.INSTANCE.Discord_RunCallbacks();
                    Thread.sleep(15 * 1000);
                }
            } catch (Exception exception) {
                stopRPC();
            }

            super.run();
        }
    }
}
