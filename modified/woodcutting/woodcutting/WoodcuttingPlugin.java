package net.runelite.client.plugins.microbot.storm.modified.woodcutting.woodcutting;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.mouse.VirtualMouse;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Default + "Woodcutting",
        description = "Microbot woodcutting plugin",
        tags = {"Woodcutting", "microbot", "skilling"},
        enabledByDefault = false
)
@Slf4j
public class WoodcuttingPlugin extends Plugin {
    @Inject
    private WoodcuttingConfig config;
    @Inject
    private Client client;
    @Inject
    private ClientThread clientThread;
    @Inject
    Notifier notifier;

    @Provides
    WoodcuttingConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(WoodcuttingConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private WoodcuttingOverlay woodcuttingOverlay;

    @Inject
    WoodcuttingScript woodcuttingScript;


    @Override
    protected void startUp() throws AWTException {
        Microbot.pauseAllScripts = false;
        Microbot.setClient(client);
        Microbot.setClientThread(clientThread);
        Microbot.setNotifier(notifier);
        Microbot.setMouse(new VirtualMouse());
        if (overlayManager != null) {
            overlayManager.add(woodcuttingOverlay);
        }
        woodcuttingScript.run(config);
    }

    protected void shutDown() {
        woodcuttingScript.shutdown();
        overlayManager.remove(woodcuttingOverlay);
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage) {
        if (chatMessage.getType() == ChatMessageType.GAMEMESSAGE && chatMessage.getMessage().contains("you can't light a fire here.")) {
            woodcuttingScript.cannotLightFire = true;
        }
    }
}
