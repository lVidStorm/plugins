package net.runelite.client.plugins.microbot.storm.modified.nmz;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.nmz.NmzConfig;
import net.runelite.client.plugins.microbot.playerassist.combat.PrayerPotionScript;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Default + "iNmz",
        description = "Microbot iNMZ",
        tags = {"nmz", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class iNmzPlugin extends Plugin {
    @Inject
    private iNmzConfig config;

    @Provides
    iNmzConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(iNmzConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private iNmzOverlay iNmzOverlay;

    @Inject
    iNmzScript iNmzScript;
    @Inject
    PrayerPotionScript prayerPotionScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(iNmzOverlay);
        }
        iNmzScript.run(config);
        if (config.togglePrayerPotions()) {
            prayerPotionScript.run((NmzConfig) config);
        }
    }

    protected void shutDown() {
        iNmzScript.shutdown();
        overlayManager.remove(iNmzOverlay);
        iNmzScript.setHasSurge(false);
    }

    @Subscribe
    public void onActorDeath(ActorDeath actorDeath) {
        if (config.stopAfterDeath() && actorDeath.getActor() == Microbot.getClient().getLocalPlayer()) {
            Rs2Player.logout();
            shutDown();
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (event.getType() == ChatMessageType.GAMEMESSAGE) {
            if (event.getMessage().equalsIgnoreCase("you feel a surge of special attack power!")) {
                iNmzScript.setHasSurge(true);
            } else if (event.getMessage().equalsIgnoreCase("your surge of special attack power has ended.")) {
                iNmzScript.setHasSurge(false);
            }
        }
    }
}
