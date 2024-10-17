package net.runelite.client.plugins.microbot.storm.modified.zpestcontrol;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.pestcontrol.Portal;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@PluginDescriptor(
        name = PluginDescriptor.Mocrosoft + "zPest Control",
        description = "Microbot Pest Control plugin, this only supports the combat 100+ boat. Start at the front of the boat",
        tags = {"pest control", "microbot", "minigames"},
        enabledByDefault = false
)
@Slf4j
public class zPestControlPlugin extends Plugin {
    @Inject
    private zPestControlConfig config;

    @Provides
    zPestControlConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(zPestControlConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private zPestControlOverlay pestControlOverlay;

    @Inject
    zPestControlScript pestControlScript;

    private final Pattern SHIELD_DROP = Pattern.compile("The ([a-z]+), [^ ]+ portal shield has dropped!", Pattern.CASE_INSENSITIVE);


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(pestControlOverlay);
        }
        pestControlScript.run(config);
    }

    protected void shutDown() {
        pestControlScript.shutdown();
        overlayManager.remove(pestControlOverlay);
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage) {
        if (chatMessage.getType() == ChatMessageType.GAMEMESSAGE) {
            Matcher matcher = SHIELD_DROP.matcher(chatMessage.getMessage());
            if (matcher.lookingAt()) {
                switch (matcher.group(1)) {
                    case "purple":
                        zPestControlScript.portals.stream().filter(x -> x == Portal.PURPLE).findFirst().get().setHasShield(false);
                        break;
                    case "blue":
                        zPestControlScript.portals.stream().filter(x -> x == Portal.BLUE).findFirst().get().setHasShield(false);
                        break;
                    case "red":
                        zPestControlScript.portals.stream().filter(x -> x == Portal.RED).findFirst().get().setHasShield(false);
                        break;
                    case "yellow":
                        zPestControlScript.portals.stream().filter(x -> x == Portal.YELLOW).findFirst().get().setHasShield(false);
                        break;
                }
            }
        }
    }
}
