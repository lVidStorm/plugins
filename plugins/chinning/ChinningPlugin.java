package net.runelite.client.plugins.microbot.storm.plugins.chinning;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Skill;
import net.runelite.api.events.PlayerSpawned;
import net.runelite.api.events.StatChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = "<html>[<font color=#ff00ff>§</font>] " + "Chinning",
        description = "Microbot chinning plugin",
        tags = {"chinning", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class ChinningPlugin extends Plugin {
    @Inject
    private ChinningConfig config;
    @Provides
    ChinningConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(ChinningConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private ChinningOverlay chinningOverlay;

    @Inject
    ChinningScript chinningScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(chinningOverlay);
        }
        chinningScript.run(config);
    }

    protected void shutDown() {
        chinningScript.shutdown();
        overlayManager.remove(chinningOverlay);
    }
    @Subscribe
    public void onPlayerSpawned(PlayerSpawned player) {
        System.out.println("detected player spawn?");
    }
    @Subscribe
    public void onStatChanged(StatChanged event) {
        if(event.getSkill().getName().matches(Skill.HITPOINTS.getName())){
            if(!ChinningScript.hitpointDamage) {
                if(!ChinningScript.actionsScheduled) { ChinningScript.actionsScheduled = true; }
                //ChinningScript.highPrioritydelay=random(61,121);
                ChinningScript.hitpointDamage = true;
            }
            //TODO run away?
        }
        if(event.getSkill().getName().matches(Skill.PRAYER.getName())){
            if((Microbot.getClient().getRealSkillLevel(Skill.PRAYER) - event.getBoostedLevel()) < config.restoreamount()){
                if(!ChinningScript.restorePrayer) {
                    if(!ChinningScript.actionsScheduled) { ChinningScript.actionsScheduled = true; }
                    //ChinningScript.highPrioritydelay=random(61,121);
                    ChinningScript.restorePrayer = true;
                }
            }
        }
        if(event.getSkill().getName().matches(Skill.RANGED.getName())){
            if((Microbot.getClient().getRealSkillLevel(Skill.RANGED) - event.getBoostedLevel()) < config.restorecbat()){
                if(!ChinningScript.restoreRange && !ChinningScript.outOfRangePotions) {
                    if(!ChinningScript.actionsScheduled) { ChinningScript.actionsScheduled = true; }
                    //ChinningScript.lowPrioritydelay=ChinningScript.calculateSleepDuration();
                    ChinningScript.restoreRange = true;
                }
            }
            if(event.getXp()>ChinningScript.previousXP){
                if(!ChinningScript.actionsScheduled) { ChinningScript.actionsScheduled = true; }
                ChinningScript.previousXP=event.getXp();
                //ChinningScript.lowPrioritydelay=ChinningScript.calculateSleepDuration();
                ChinningScript.moveHit = true;
            }
        }
        //event.getBoostedLevel();
        //event.getSkill().getName();
        System.out.println("gained experience");
    }
}
