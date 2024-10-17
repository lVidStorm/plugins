package net.runelite.client.plugins.microbot.storm.plugins.thievingstalls.thievingstalls;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

import static net.runelite.client.plugins.microbot.util.Global.sleep;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;

@PluginDescriptor(
        name = PluginDescriptor.Default + "thievingstalls",
        description = "Storm's test plugin",
        tags = {"testing", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class thievingstallsPlugin extends Plugin {
    @Inject
    private thievingstallsConfig config;
    @Provides
    thievingstallsConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(thievingstallsConfig.class);
    }

    @Inject
    private Client client;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private thievingstallsOverlay ThievingStallsOverlay;

    @Inject
    thievingstallsScript ThievingStallsScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(ThievingStallsOverlay);
        }
        ThievingStallsScript.run(config);
    }
    //TODO on item added to inventory, wait for animation to end, then drop individual item if not matching any from do not drop
    @Subscribe
    private void onItemContainerChanged(ItemContainerChanged event)
    {
        if (event.getItemContainer().equals(client.getItemContainer(InventoryID.INVENTORY))){
            //System.out.println("Picked up Item : "+ event.getItemContainer()+"/"+event.getContainerId());
            //if(event.getItemContainer().getId()==0){

            //}
        }
    }
    @Subscribe
    public void onItemSpawned(ItemSpawned itemSpawned)
    {

    }
    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned gameObject){
        if(gameObject.getGameObject().getId()==Integer.parseInt(config.gameObjectID()) && gameObject.getGameObject().getWorldLocation().distanceTo(Rs2Player.getWorldLocation())<2){
            //System.out.println("Time since previous steal : "+(System.currentTimeMillis()-thievingstallsScript.timeSinceSteal));
            if(!Rs2Inventory.isFull() && !thievingstallsScript.needToDrop) {
                if (config.turbo()) {
                    Rs2GameObject.interact(Integer.parseInt(config.gameObjectID()), "Steal-from");
                    thievingstallsScript.timeSinceSteal=System.currentTimeMillis();

                } else {
                    thievingstallsScript.calculateSleepDuration();
                    Rs2GameObject.interact(Integer.parseInt(config.gameObjectID()), "Steal-from");
                    thievingstallsScript.timeSinceSteal=System.currentTimeMillis();
                }
            } else {
                thievingstallsScript.needToDrop=true;
            }
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

    protected void shutDown() {
        ThievingStallsScript.shutdown();
        overlayManager.remove(ThievingStallsOverlay);
    }
}
