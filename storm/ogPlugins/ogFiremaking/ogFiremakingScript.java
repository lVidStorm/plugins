package net.runelite.client.plugins.microbot.storm.ogPlugins.ogFiremaking;

import net.runelite.api.NPC;
import net.runelite.api.ObjectID;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.storm.ogPlugins.ogFiremaking.enums.FiremakingStatus;
import net.runelite.client.plugins.microbot.storm.ogPlugins.ogFiremaking.enums.Logs;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.math.Random;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;

import java.util.concurrent.TimeUnit;
public class ogFiremakingScript extends Script {
    public static double version = 1.1;
    private boolean hasTinderBox() {return Rs2Inventory.hasItem("tinderbox");}
    private boolean doesNeedReturn(ogFiremakingConfig config){ if(config.selectedLocation().getReturnPoints() != null){return true;} return false;}
    private boolean hasLogs(){if (Rs2Inventory.hasItemAmount(calcedLogs.getItemID(), 27)) {return true;} else{return false;}}
    private boolean isClosetoBanker(NPC banker){if(Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(banker.getWorldLocation()) <= 6){return true;} else {return false;}}
    private WorldPoint lastSpot = null;
    private WorldPoint secondToLastSpot = null; //Really didn't want to rewrite code lmaoooo
    private WorldPoint startPoint;
    private FiremakingStatus firemakingStatus = FiremakingStatus.FETCH_SUPPLIES;
    private Logs calcedLogs = null;
    public boolean run(ogFiremakingConfig config) {
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run()) return;
            try {

                calcLogToUse(config);

                fetchSupplies(config);
                goToSpot(config);
                burnShit(calcedLogs);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 600, Random.random(500,700), TimeUnit.MILLISECONDS);
        return true;
    }
    private void fetchSupplies(ogFiremakingConfig config){
        //A lil bot like but will work on it. Will need to get Widget tabs and click on tab instead of scroll
        if(firemakingStatus == FiremakingStatus.FETCH_SUPPLIES){
            //Checks for logs
            if(!hasLogs()){
                int[] Bankers = config.selectedLocation().getBankers();
                NPC SelectedBanker = Rs2Npc.getNpc(Bankers[Random.random(0,Bankers.length)]);
                if(doesNeedReturn(config) && !isClosetoBanker(SelectedBanker)){
                    WorldPoint[] returnPoints = config.selectedLocation().getReturnPoints();
                    WorldPoint goBack = returnPoints[Random.random(0,returnPoints.length)];
                    if(this.isRunning()){ Rs2Walker.walkCanvas(goBack); }
                    if(this.isRunning()){ sleepUntil(()-> Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(goBack) <= 4); }
                    if(this.isRunning()){ sleep(1800,2500); }
                }
                if(!Rs2Bank.isOpen()){
                    if(this.isRunning()){ sleepUntil(() -> Rs2Bank.openBank(SelectedBanker)); }
                    if(this.isRunning()){ callAFK(27,50,5000); }
                    if(!hasTinderBox()){
                        if(!Rs2Bank.hasItem("tinderbox")){Microbot.getNotifier().notify("Get more tinderbox ya bum!");super.shutdown();}
                        if(this.isRunning()){ Rs2Bank.withdrawItem("tinderbox"); }
                        if(this.isRunning()){ sleepUntil(()-> Rs2Inventory.use("tinderbox")); }
                        if(this.isRunning()){ sleep(30,80); }
                    }
                    if(!hasLogs()){
                        if(!Rs2Bank.hasItem(calcedLogs.getName())){Microbot.getNotifier().notify("Get more "+calcedLogs.getName()+" ya bum!");super.shutdown();}
                        if(this.isRunning()){ Rs2Bank.withdrawAll(true, calcedLogs.getName()); }
                        if(this.isRunning()){ sleepUntil(this::hasLogs); }
                        if(this.isRunning()){ callAFK(67,50,786); }
                        if(this.isRunning()){ sleep(30,80); }
                    }
                }
                if(this.isRunning()){ Rs2Bank.closeBank(); }
            }
        }
        if(hasLogs() && hasTinderBox()){firemakingStatus = FiremakingStatus.FIND_EMPTY_SPOT;}
    }
    private void goToSpot(ogFiremakingConfig config){
        if(firemakingStatus == FiremakingStatus.FIND_EMPTY_SPOT){
            WorldPoint[] startPositions = config.selectedLocation().getFiremakingStartingSpots();
            while(this.isRunning() && startPoint == lastSpot || startPoint == secondToLastSpot){startPoint = startPositions[Random.random(0,startPositions.length)];}
            //Updated BEWARE!!!
            if(this.isRunning()){ Rs2Walker.walkMiniMap(startPoint, 1); }
            if(this.isRunning()){ sleepUntil(() -> Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(startPositions[0]) < 1,10000); }
            if (Rs2GameObject.findObject(ObjectID.FIRE_26185, startPoint) == null) {
                if(this.isRunning()){ Rs2Walker.walkCanvas(startPoint); }
                if(this.isRunning()){ sleepUntil(()->Microbot.getClient().getLocalPlayer().getWorldLocation().equals(startPoint),10000); }
                if(this.isRunning()){ firemakingStatus = FiremakingStatus.FIREMAKING; }
                if(this.isRunning() && lastSpot != null){secondToLastSpot = lastSpot;}
                if(this.isRunning()){ lastSpot = startPoint; }
            }
            if(this.isRunning()){ callAFK(19,50,97); }
        }
    }
    private void burnShit(Logs getLog){
        if (firemakingStatus == FiremakingStatus.FIREMAKING) {
            if (!Rs2Inventory.hasItem(getLog.getName())) {
                firemakingStatus = FiremakingStatus.FETCH_SUPPLIES;
            }
            if (Rs2GameObject.findObject(ObjectID.FIRE_26185, Microbot.getClient().getLocalPlayer().getWorldLocation()) != null) {
                firemakingStatus = FiremakingStatus.FIND_EMPTY_SPOT;
            }
            while (this.isRunning() && firemakingStatus == FiremakingStatus.FIREMAKING) {
                if (!Rs2Inventory.hasItem(getLog.getName()))
                    break;
                sleepUntil(()-> Microbot.getClient().getLocalPlayer().getPoseAnimation() != 733);
                if (Rs2GameObject.findObject(ObjectID.FIRE_26185, Microbot.getClient().getLocalPlayer().getWorldLocation()) != null) {
                    firemakingStatus = FiremakingStatus.FIND_EMPTY_SPOT;
                }
                if(this.isRunning()){ sleep(60,120); }
                if(this.isRunning()){ Rs2Inventory.use("tinderbox"); }
                if(this.isRunning()){ sleep(60,120); }
                if(this.isRunning()){ Rs2Inventory.use(getLog.getName()); }
                //TODO change this to better detect firemaking stuff~
                if(this.isRunning()){ sleepUntil(() -> Microbot.getClient().getLocalPlayer().getPoseAnimation() == 823, 20000); }
                if(this.isRunning()){ sleep(300); }
            }
            if(this.isRunning()){ callAFK(34,50,400); }
        }
    }
    private void calcLogToUse(ogFiremakingConfig config){
        if(config.getProgressionMode()){
            int firemakingLevel = Microbot.getClient().getBoostedSkillLevel(Skill.FIREMAKING);
            if(firemakingLevel >= 90){this.calcedLogs = Logs.REDWOOD;}
            else if(firemakingLevel >= 75){this.calcedLogs = Logs.MAGIC;}
            else if(firemakingLevel >= 60){this.calcedLogs = Logs.YEW;}
            else if(firemakingLevel >= 50){this.calcedLogs = Logs.MAHOGANY;}
            else if(firemakingLevel >= 45){this.calcedLogs = Logs.MAPLE;}
            else if(firemakingLevel >= 42){this.calcedLogs = Logs.ARCTIC_PINE;}
            else if(firemakingLevel >= 35){this.calcedLogs = Logs.TEAK;}
            else if(firemakingLevel >= 30){this.calcedLogs = Logs.WILLOW;}
            else if(firemakingLevel >= 15){this.calcedLogs = Logs.OAK;}
            else if(firemakingLevel >= 1){this.calcedLogs = Logs.RED_LOGS;}
        } else { this.calcedLogs =  config.selectedLogs(); }
    }
    private void callAFK(int chance, int min, int max){
        if(Random.random(1,chance) == 1){
            if(this.isRunning()){ sleep(min,max); }
        }
    }
}
