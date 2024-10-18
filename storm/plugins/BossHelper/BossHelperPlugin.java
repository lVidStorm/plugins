package net.runelite.client.plugins.microbot.storm.plugins.BossHelper;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        //name = PluginDescriptor.Default + "BossHelper",
        name = "<html>[<font color=#ff00ff>§</font>] " + "BossHelper",
        description = "Storm's BossHelper plugin",
        tags = {"bossing", "StormScript"},
        enabledByDefault = false
)
@Slf4j
public class BossHelperPlugin extends Plugin {
    @Inject
    private BossHelperConfig config;
    @Provides
    BossHelperConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(BossHelperConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private BossHelperOverlay exampleOverlay;

    @Inject
    BossHelperScript BossHelperScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(exampleOverlay);
        }
        BossHelperScript.run(config);
    }

    protected void shutDown() {
        BossHelperScript.shutdown();
        overlayManager.remove(exampleOverlay);
    }
}
