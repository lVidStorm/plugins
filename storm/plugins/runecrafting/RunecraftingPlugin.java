package net.runelite.client.plugins.microbot.storm.plugins.runecrafting;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Varbits;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = "<html>[<font color=#ff00ff>§</font>] " + "Runecrafting",
        description = "Lava runecrafting plugin",
        tags = {"runecrafting", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class RunecraftingPlugin extends Plugin {
    @Inject
    private RunecraftingConfig config;
    @Provides
    RunecraftingConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(RunecraftingConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private RunecraftingOverlay runecraftingOverlay;

    @Inject
    RunecraftingScript runecraftingScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(runecraftingOverlay);
        }
        runecraftingScript.run(config);
    }
    @Subscribe
    public void onWidgetClosed(WidgetClosed widget){
        if(widget.getGroupId()== InterfaceID.BANK_INVENTORY){
            //System.out.println("bank should be set to closed @ "+System.currentTimeMillis());
            RunecraftingScript.bankIsOpen=false;
        }
    }
    @Subscribe
    public void onWidgetLoaded(WidgetLoaded widget){
        if(widget.getGroupId()== InterfaceID.BANK_INVENTORY){
            RunecraftingScript.bankIsOpen=true;
        }
    }
    @Subscribe
    public void onVarbitChanged(VarbitChanged event) {
        if (event.getVarbitId() == Varbits.STAMINA_EFFECT) {
            RunecraftingScript.staminaTimer = event.getValue();
        }
        if (event.getVarbitId() == Varbits.MAGIC_IMBUE) {
            RunecraftingScript.imbueTimer = event.getValue();
        }
    }
    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (event.getType() == ChatMessageType.PUBLICCHAT) {
            RunecraftingScript.hopscheduled = true;
        }
    }
    protected void shutDown() {
        runecraftingScript.shutdown();
        overlayManager.remove(runecraftingOverlay);
    }

}
