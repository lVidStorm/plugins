package net.runelite.client.plugins.microbot.storm.plugins.blastofurnace;

import com.google.inject.Provides;
import net.runelite.client.plugins.microbot.storm.plugins.blastofurnace.enums.State;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.ItemID;
import net.runelite.api.Varbits;
import net.runelite.api.events.*;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.eXioStorm + "Blastofurnace",
        description = "Storm's test plugin",
        tags = {"blast", "furnace", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class blastofurnacePlugin extends Plugin {
    @Inject
    private blastofurnaceConfig config;
    @Provides
    blastofurnaceConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(blastofurnaceConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private blastofurnaceOverlay blastoiseFurnaceOverlay;

    @Inject
    blastofurnaceScript blastoiseFurnaceScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(blastoiseFurnaceOverlay);
        }
        blastoiseFurnaceScript.run(config);
    }
    @Subscribe
    public void onVarbitChanged(VarbitChanged event) {
        if (event.getVarbitId() == Varbits.STAMINA_EFFECT) {
            blastofurnaceScript.staminaTimer = event.getValue();
        }
    }
    @Subscribe
    public void onWidgetClosed(WidgetClosed widget){
        if(widget.getGroupId()== InterfaceID.BANK_INVENTORY){
            //System.out.println("bank should be set to closed @ "+System.currentTimeMillis());
            blastofurnaceScript.bankIsOpen=false;
        }
    }
    @Subscribe
    public void onWidgetLoaded(WidgetLoaded widget){
        System.out.println("Widget loaded : "+widget.getGroupId());
        if(widget.getGroupId()== InterfaceID.BANK_INVENTORY){
            blastofurnaceScript.bankIsOpen=true;
        }
    }
    @Subscribe
    public void onStatChanged(StatChanged event) {
        blastofurnaceScript.lastXpDrop = System.currentTimeMillis();
        if(event.getXp()> blastofurnaceScript.previousXP){
            if(blastofurnaceScript.waitingXpDrop) {
                blastofurnaceScript.waitingXpDrop = false;
            }
        }
    }
    @Subscribe
    public void onChatMessage(ChatMessage chatMessage) {
        if (chatMessage.getType() == ChatMessageType.GAMEMESSAGE) {
            if(chatMessage.getMessage().contains("The coal bag is now empty.")){
                if(!blastofurnaceScript.coalBagEmpty) blastofurnaceScript.coalBagEmpty=true;
            }

            if(chatMessage.getMessage().contains("The coal bag contains")){
                if(blastofurnaceScript.coalBagEmpty) blastofurnaceScript.coalBagEmpty=false;
            }
        }
    }
    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged inventory){
        if(inventory.getItemContainer().getId()==93) {
            if (!inventory.getItemContainer().contains(ItemID.COAL) && blastofurnaceScript.state != State.BANKING) {
                if (!blastofurnaceScript.coalBagEmpty) blastofurnaceScript.coalBagEmpty = true;//TODO this sets the bag to empty when we're smithing and coal is added to our inventory.
            }
            if (inventory.getItemContainer().contains(ItemID.COAL) && blastofurnaceScript.state != State.SMITHING) {
                if (blastofurnaceScript.coalBagEmpty) blastofurnaceScript.coalBagEmpty = false;
            }
            if (inventory.getItemContainer().contains(config.getBars().getPrimaryOre()) && blastofurnaceScript.state != State.SMITHING){
                if(blastofurnaceScript.primaryOreEmpty){ blastofurnaceScript.primaryOreEmpty=false; }
            }
            if (!inventory.getItemContainer().contains(config.getBars().getPrimaryOre()) && blastofurnaceScript.state != State.BANKING){
                if(!blastofurnaceScript.primaryOreEmpty){ blastofurnaceScript.primaryOreEmpty=true; }
            }
            if(config.getBars().getSecondaryOre()!=null) {
                if (inventory.getItemContainer().contains(config.getBars().getSecondaryOre()) && blastofurnaceScript.state != State.SMITHING) {
                    //TODO ffs for some reason the print fixes it when run from IDE, but compiled still bugs out...
                    if (blastofurnaceScript.secondaryOreEmpty) { System.out.println("secondary set to not empty"); blastofurnaceScript.secondaryOreEmpty = false; }
                }
                if (!inventory.getItemContainer().contains(config.getBars().getSecondaryOre()) && blastofurnaceScript.state != State.BANKING) {
                    if (!blastofurnaceScript.secondaryOreEmpty) { blastofurnaceScript.secondaryOreEmpty = true; }
                }
                //TODO added
                if (!inventory.getItemContainer().contains(config.getBars().getSecondaryOre()) && blastofurnaceScript.state != State.SMITHING) {
                    if (!blastofurnaceScript.secondaryOreEmpty) { blastofurnaceScript.secondaryOreEmpty = true; }
                }
                if (inventory.getItemContainer().contains(config.getBars().getSecondaryOre()) && blastofurnaceScript.state != State.BANKING) {
                    if (blastofurnaceScript.secondaryOreEmpty) { blastofurnaceScript.secondaryOreEmpty = false; }
                }
            }
        }
    }
    @Subscribe
    public void onClientTick(ClientTick clientTick) {

    }
    protected void shutDown() {
        blastoiseFurnaceScript.shutdown();
        overlayManager.remove(blastoiseFurnaceOverlay);
    }
}
