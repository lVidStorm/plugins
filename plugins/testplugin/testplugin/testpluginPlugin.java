package net.runelite.client.plugins.microbot.storm.plugins.testplugin.testplugin;

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
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

import static net.runelite.client.plugins.microbot.util.Global.sleep;

@PluginDescriptor(
        name = PluginDescriptor.Default + "testplugin",
        description = "Storm's test plugin",
        tags = {"testing", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class testpluginPlugin extends Plugin {
    @Inject
    private testpluginConfig config;
    @Provides
    testpluginConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(testpluginConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private testpluginOverlay TestPluginOverlay;

    @Inject
    testpluginScript TestPluginScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(TestPluginOverlay);
        }
        TestPluginScript.run(config);
    }
    @Subscribe
    public void onItemSpawned(ItemSpawned itemSpawned)
    {

    }
    @Subscribe
    public void onStatChanged(StatChanged event) {
        /*
        System.out.println("Skill : "+event.getSkill().getName() + " Changed!");
        System.out.println(event.getSkill().getName() + " : " + event.getBoostedLevel() + "/" + event.getLevel());
        System.out.println(event.getSkill().getName() + " EXP :  " + event.getXp());

        if(testpluginScript.timeBetweenXPDrops==0 || (System.currentTimeMillis()-testpluginScript.timeBetweenXPDrops)>6000){
            testpluginScript.timeBetweenXPDrops=System.currentTimeMillis();
            sleep(10);
        }
        System.out.println("Time since previous exp : "+(System.currentTimeMillis()-testpluginScript.timeBetweenXPDrops));
        testpluginScript.timeBetweenXPDrops=System.currentTimeMillis();
        */
    }
    @Subscribe
    public void onHitsplatApplied(HitsplatApplied event) {
        if (event.getHitsplat().isMine())
        {
            /*
            System.out.println("hitsplat detected");
            testpluginScript.playerHit++;
             */
        }
    }
    @Subscribe
    public void onChatMessage(ChatMessage chatMessage) {
        if (chatMessage.getType() == ChatMessageType.GAMEMESSAGE) {
            if(chatMessage.getMessage().contains("The coal bag is now empty.")){
                System.out.println("Detected coal bag emptied in : "+(System.currentTimeMillis()-testpluginScript.startc)+"ms");
                //if(!blastoisefurnaceScript.coalBagEmpty) blastoisefurnaceScript.coalBagEmpty=true;
            }
            if(chatMessage.getMessage().contains("The coal bag contains")){
                //if(blastoisefurnaceScript.coalBagEmpty) blastoisefurnaceScript.coalBagEmpty=false;
            }
        }
    }
    @Subscribe
    public void onWidgetClosed(WidgetClosed widget){
        //OR 12?
        if(widget.getGroupId()==InterfaceID.BANK_INVENTORY){
            System.out.println("Detected bank closed");
        }
    }
    @Subscribe
    public void onGameTick(GameTick gameTick){
    }
    @Subscribe
    public void onWidgetLoaded(WidgetLoaded widget){
        //OR 12?
        if(widget.getGroupId()==InterfaceID.BANK_INVENTORY){
            System.out.println("Detected bank opened");
        }
    }
    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged inventory){
        if(inventory.getItemContainer().getId()!=93){System.out.println(inventory.getItemContainer().getId()+" changed.");}
        if(inventory.getItemContainer().getId()==93 && !inventory.getItemContainer().contains(ItemID.COAL)) {

        }
    }
    @Subscribe
    public void onVarbitChanged(VarbitChanged event)
    {
        if(event.getVarbitId()== Varbits.BLAST_FURNACE_COAL){
            System.out.println("Detected coal varbit changed to : "+event.getValue());
        }
    }
    @Subscribe
    public void onVarClientIntChanged(VarClientIntChanged test){


    }
    protected void shutDown() {
        TestPluginScript.shutdown();
        overlayManager.remove(TestPluginOverlay);
    }
}
