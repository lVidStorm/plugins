package net.runelite.client.plugins.microbot.storm.plugins.lunarglassmake.lunarglassmake;

import java.awt.AWTException;

import javax.inject.Inject;

import com.google.inject.Provides;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;


@PluginDescriptor(
        name = "<html>[<font color=#ff00ff>§</font>] " + "Lunar GlassMake",
        description = "Makes molten glass on the lunar spellbook",
        tags = {"magic", "moneymaking"},
        enabledByDefault = false
)
@Slf4j
public class GlassMakePlugin extends Plugin {
    @Inject
    private GlassMakeConfig config;
    @Provides
    GlassMakeConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(GlassMakeConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private GlassMakeOverlay tanLeatherOverlay;

    @Inject
    GlassMakeScript glassMakeScript;


    @Override
    protected void startUp() throws AWTException {
        log.info("Starting up GlassMakePlugin");
        glassMakeScript.run(config);
    }
    @Subscribe
    public void onGameTick(GameTick gameTick){
        GlassMakeScript.tickCounter++;
    }
    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged inventory){
        //System.out.println("Container event for container ID : " + inventory.getContainerId());
    }

    @Override
    protected void shutDown() {
        log.info("Shutting down GlassMakePlugin");
        glassMakeScript.shutdown();
    }
}