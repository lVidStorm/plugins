//package net.runelite.client.plugins.microbot.storm.plugins.blastoisefurnace;

import com.google.inject.Provides;
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
import enums.State;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

import static net.runelite.client.plugins.microbot.util.Global.sleep;

@PluginDescriptor(
        name = "<html>[<font color=#ff00ff>§</font>] " + "Blastifurnace",
        description = "Storm's test plugin",
        tags = {"blast", "furnace", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class blastifurnacePlugin extends Plugin {
    @Inject
    private blastifurnaceConfig config;
    @Provides
    blastifurnaceConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(blastifurnaceConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private blastifurnaceOverlay blastoiseFurnaceOverlay;

    @Inject
    blastifurnaceScript blastoiseFurnaceScript;


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
            blastifurnaceScript.staminaTimer = event.getValue();
        }
    }
    @Subscribe
    public void onWidgetClosed(WidgetClosed widget){
        if(widget.getGroupId()== InterfaceID.BANK_INVENTORY){
            //System.out.println("bank should be set to closed @ "+System.currentTimeMillis());
            blastifurnaceScript.bankIsOpen=false;
        }
    }
    @Subscribe
    public void onWidgetLoaded(WidgetLoaded widget){
        System.out.println("Widget loaded : "+widget.getGroupId());
        if(widget.getGroupId()== InterfaceID.BANK_INVENTORY){
            blastifurnaceScript.bankIsOpen=true;
        }
    }
    @Subscribe
    public void onStatChanged(StatChanged event) {
        blastifurnaceScript.lastXpDrop = System.currentTimeMillis();
        if(event.getXp()> blastifurnaceScript.previousXP){
            if(blastifurnaceScript.waitingXpDrop) {
                blastifurnaceScript.waitingXpDrop = false;
            }
        }
    }
    @Subscribe
    public void onChatMessage(ChatMessage chatMessage) {
        if (chatMessage.getType() == ChatMessageType.GAMEMESSAGE) {
            if(chatMessage.getMessage().contains("The coal bag is now empty.")){
                if(!blastifurnaceScript.coalBagEmpty) blastifurnaceScript.coalBagEmpty=true;
            }

            if(chatMessage.getMessage().contains("The coal bag contains")){
                if(blastifurnaceScript.coalBagEmpty) blastifurnaceScript.coalBagEmpty=false;
            }
        }
    }
    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged inventory){
        if(inventory.getItemContainer().getId()==93) {
            if (!inventory.getItemContainer().contains(ItemID.COAL) && blastifurnaceScript.state != State.BANKING) {
                if (!blastifurnaceScript.coalBagEmpty) blastifurnaceScript.coalBagEmpty = true;//TODO this sets the bag to empty when we're smithing and coal is added to our inventory.
            }
            if (inventory.getItemContainer().contains(ItemID.COAL) && blastifurnaceScript.state != State.SMITHING) {
                if (blastifurnaceScript.coalBagEmpty) blastifurnaceScript.coalBagEmpty = false;
            }
            if (inventory.getItemContainer().contains(config.getBars().getPrimaryOre()) && blastifurnaceScript.state != State.SMITHING){
                if(blastifurnaceScript.primaryOreEmpty){ blastifurnaceScript.primaryOreEmpty=false; }
            }
            if (!inventory.getItemContainer().contains(config.getBars().getPrimaryOre()) && blastifurnaceScript.state != State.BANKING){
                if(!blastifurnaceScript.primaryOreEmpty){ blastifurnaceScript.primaryOreEmpty=true; }
            }
            if(config.getBars().getSecondaryOre()!=null) {
                if (inventory.getItemContainer().contains(config.getBars().getSecondaryOre()) && blastifurnaceScript.state != State.SMITHING) {
                    //TODO ffs for some reason the print fixes it when run from IDE, but compiled still bugs out...
                    if (blastifurnaceScript.secondaryOreEmpty) { System.out.println("secondary set to not empty"); blastifurnaceScript.secondaryOreEmpty = false; }
                }
                if (!inventory.getItemContainer().contains(config.getBars().getSecondaryOre()) && blastifurnaceScript.state != State.BANKING) {
                    if (!blastifurnaceScript.secondaryOreEmpty) { blastifurnaceScript.secondaryOreEmpty = true; }
                }
                //TODO added
                if (!inventory.getItemContainer().contains(config.getBars().getSecondaryOre()) && blastifurnaceScript.state != State.SMITHING) {
                    if (!blastifurnaceScript.secondaryOreEmpty) { blastifurnaceScript.secondaryOreEmpty = true; }
                }
                if (inventory.getItemContainer().contains(config.getBars().getSecondaryOre()) && blastifurnaceScript.state != State.BANKING) {
                    if (blastifurnaceScript.secondaryOreEmpty) { blastifurnaceScript.secondaryOreEmpty = false; }
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
