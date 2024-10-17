package net.runelite.client.plugins.microbot.storm.plugins.idler;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Default + "Idler",
        description = "Microbot idler plugin",
        tags = {"idler", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class IdlerPlugin extends Plugin {
    @Inject
    private IdlerConfig config;
    @Provides
    IdlerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(IdlerConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private IdlerOverlay idlerOverlay;

    @Inject
    IdlerScript idlerScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(idlerOverlay);
        }
        idlerScript.run(config);
    }

    protected void shutDown() {
        idlerScript.shutdown();
        overlayManager.remove(idlerOverlay);
    }
    int ticks = 10;
    @Subscribe
    public void onGameTick(GameTick tick)
    {
        //System.out.println(getName().chars().mapToObj(i -> (char)(i + 3)).map(String::valueOf).collect(Collectors.joining()));

        if (ticks > 0) {
            ticks--;
        } else {
            ticks = 10;
        }

    }

}
