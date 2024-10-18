package net.runelite.client.plugins.microbot.storm.modified.thieving;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.plugins.timersandbuffs.TimersAndBuffsPlugin;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Default + "iThieving",
        description = "Microbot thieving plugin",
        tags = {"thieving", "microbot", "skilling"},
        enabledByDefault = false
)
@Slf4j
public class iThievingPlugin extends Plugin {
    @Inject
    private iThievingConfig config;

    @Provides
    iThievingConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(iThievingConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private iThievingOverlay iThievingOverlay;

    @Inject
    iThievingScript iThievingScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(iThievingOverlay);
        }
        iThievingScript.run(config);
    }

    protected void shutDown() {
        iThievingScript.shutdown();
        overlayManager.remove(iThievingOverlay);
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage)
    {
        if (chatMessage.getType() == ChatMessageType.GAMEMESSAGE && chatMessage.getMessage().contains("protects you")) {
            TimersAndBuffsPlugin.t = null;
        }
    }
}
