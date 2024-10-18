package net.runelite.client.plugins.microbot.storm.plugins.ModifBankStander;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.ItemContainerChanged;
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
import java.util.Arrays;

@PluginDescriptor(
        name = "<html>[<font color=#ff00ff>§</font>] " + "BankStander",
        description = "Credit to original coder : Bank",
        tags = {"bankstander", "bank.js"},
        enabledByDefault = false
)
@Slf4j
public class zBanksBankStanderPlugin extends Plugin {
    @Inject
    private zBanksBankStanderConfig config;

    @Provides
    zBanksBankStanderConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(zBanksBankStanderConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private zBanksBankStanderOverlay banksBankStanderOverlay;

    @Inject
    zBanksBankStanderScript banksBankStanderScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(banksBankStanderOverlay);
        }
        banksBankStanderScript.run(config);
    }
    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged inventory){
        if(inventory.getContainerId()==93){
            if(Arrays.stream(inventory.getItemContainer().getItems()).anyMatch(x -> x.getId()==zBanksBankStanderScript.secondItemId)){
                // average is 1800, max is 2400~
                zBanksBankStanderScript.previousItemChange=System.currentTimeMillis();
                //System.out.println("still processing items");
            } else {
                //System.out.println("done processing items");
                zBanksBankStanderScript.previousItemChange=(System.currentTimeMillis()-2500);
            }
        }
    }
    @Subscribe
    public void onWidgetClosed(WidgetClosed widget){
        if(widget.getGroupId()== InterfaceID.BANK_INVENTORY){
            //System.out.println("bank should be set to closed @ "+System.currentTimeMillis());
            zBanksBankStanderScript.bankIsOpen=false;
        }
    }
    @Subscribe
    public void onWidgetLoaded(WidgetLoaded widget){
        if(widget.getGroupId()== InterfaceID.BANK_INVENTORY){
            zBanksBankStanderScript.bankIsOpen=true;
        }else if (widget.getGroupId()==270) {
            if(zBanksBankStanderScript.isWaitingForPrompt) {
                zBanksBankStanderScript.isWaitingForPrompt = false;
            }
        }
    }
    protected void shutDown() {
        banksBankStanderScript.shutdown();
        overlayManager.remove(banksBankStanderOverlay);
    }
}
