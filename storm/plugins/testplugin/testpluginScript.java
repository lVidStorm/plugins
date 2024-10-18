package net.runelite.client.plugins.microbot.storm.plugins.testplugin;

import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import net.runelite.api.*;
import net.runelite.client.plugins.microbot.globval.enums.InterfaceTab;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;

import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;
import static net.runelite.client.plugins.microbot.util.bank.Rs2Bank.openBank;
import static net.runelite.client.plugins.microbot.util.camera.Rs2Camera.isTileOnScreen;
import static net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory.items;
import static net.runelite.client.plugins.microbot.util.npc.Rs2Npc.getNpc;


public class testpluginScript extends Script {
    public static double version = 1.0;
    private testpluginConfig config;
    String item = "Gold coffin";
    int oldXP = 0;
    static long previousItemChange = System.currentTimeMillis();
    static int playerHit=0;
    int newXP = 0;
    long firstxpdrop;
    long secondxpdrop;
    static long timeBetweenXPDrops=0;
    boolean initScript = false;
    public int varbitTest;
    NPC npc;
    public static long startc;
    public boolean run(testpluginConfig config) {
        startc=System.currentTimeMillis();
        varbitTest=Microbot.getVarbitValue(Varbits.BLAST_FURNACE_COAL);
        initScript = true;
        this.config = config;
        String[] runToTile = config.walktile().split(",");
        Microbot.enableAutoRunOn = false;
        //oldXP = Microbot.getClient().getSkillExperience(Skill.THIEVING);
        java.util.List<String> doNotDropItemList = Arrays.stream(config.DoNotDropItemList().split(",")).collect(Collectors.toList());
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                /*
                if(this.isRunning()){ Rs2GameObject.interact(9100); }
                int fullInventory=Rs2Inventory.size();
                long start = System.currentTimeMillis();
                if(this.isRunning()){ sleepUntil(()->fullInventory!=Rs2Inventory.size()); }
                System.out.println("Detected inventory space in : "+(System.currentTimeMillis()-start)+"ms");
                if(this.isRunning()){ sleep(100); }
                if(this.isRunning()){ Rs2Inventory.interact(COAL_BAG_12019, "empty"); }
                if(this.isRunning()){ sleep(45); }
                */
                /*
                int oldVarbit=varbitTest;
                System.out.println("varbit value at startup : "+varbitTest);
                long starta = System.currentTimeMillis();
                if(this.isRunning()){ Rs2GameObject.interact(9100); }
                if(this.isRunning()){ sleepUntil(()->!Rs2Inventory.isFull()); }
                if(this.isRunning()){ System.out.println("Detected varbit change in : "+(System.currentTimeMillis()-starta)+"ms"); }
                if(this.isRunning()){ sleep(200); }
                if(this.isRunning()){ Rs2Inventory.interact(COAL_BAG_12019, "empty"); }
                if(this.isRunning()){ sleep(200); }
                if(this.isRunning()){ Rs2GameObject.interact(9100); }
                long start = System.currentTimeMillis();
                if(this.isRunning()){ sleepUntil(()->varbitTest!=oldVarbit); }
                if(this.isRunning()){ System.out.println("Detected varbit change in : "+(System.currentTimeMillis()-start)+"ms"); }
                */
                //Rs2Bank.openBank(Rs2GameObject.findObjectById(26707));
                /*
                if(Rs2Widget.hasWidget("bar") && !Rs2Widget.hasWidget("How many would you like to take?")){
                    System.out.println("Detecting bar widget!");
                } else if(Rs2Widget.hasWidget("How many would you like to take?")) {
                    System.out.println("Detected first widget window");
                } else {
                    System.out.println("No widgets detected");
                }*/
                //System.out.println(Microbot.getClient().getVarbitValue(BLAST_FURNACE_COFFER));//TODO I'm looking at how the default blast furnace plugin gets their coffer value and keeps track of it
                //TODO that's what I'm looking at
                //TODO so with what we found before, the script will run until the coffer is completely empty.
                //TODO I'll move on
                //System.out.println(Rs2GameObject.convertGameObjectToObjectComposition(29330).getImpostor().getId());//TODO IMPORTANT! NEED FOR LATER!
                //Rs2Walker.walkFastCanvas(new WorldPoint(3364,2998,Rs2Player.getWorldLocation().getPlane()));
                /*
                System.out.println("test : "+ Arrays.toString(doNotDropItemList.toArray(new String[0])));
                if(!onlyContains(doNotDropItemList.toArray(new String[0]))){
                    System.out.println("detected inventory has unwanted items");
                } else {
                    System.out.println("You may have fixed it!");
                }*/
                //Rs2Inventory.hasItemAmount()
                //System.out.println("pure essence count : "+Rs2Inventory.get(7936).quantity);
                //System.out.println("Distance to altar teleport : "+Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(new WorldPoint(3312, 3253, 0)));
                //if(Rs2Player.getWorldLocation().getY() > 3244) { System.out.println("should return true"); }
                /*
                while(this.isRunning()){
                    System.out.println("Distance to teleport : "+Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(new WorldPoint(3312, 3253, 0)));
                    sleep(800);
                }*/
                //System.out.println("You crossed the finish line!");
                //if(this.isRunning()){ Rs2Magic.cast(MagicAction.MAGIC_IMBUE); }
                //repairPouches();
                //Rs2Inventory.interact("crafting cape", "teleport");
                //Rs2Walker.walkFastCanvas(new WorldPoint(Integer.parseInt(runToTile[0]), Integer.parseInt(runToTile[1]), Rs2Player.getWorldLocation().getPlane()));
                //System.out.println("cape exists : "+Rs2GameObject.exists(31986));
                //System.out.println("empty exists : "+Rs2GameObject.exists(15394));
                //Rs2GameObject.getAll().stream().filter(x -> x.getLocalLocation().distanceTo(Rs2Player.getLocalLocation())<20).forEach(tileObject -> System.out.println(tileObject.getId()));
                //System.out.println(Rs2GameObject.findGameObjectByLocation(new WorldPoint(Rs2Player.getWorldLocation().getX(), Rs2Player.getWorldLocation().getY()-1, Rs2Player.getWorldLocation().getPlane())));
                /*
                //Rs2GameObject.getGameObjects(41006);
                TileObject test = Rs2GameObject.findObjectById(41006);
                Collections.singletonList(test).stream().filter(x ->
                        x.getWorldLocation().distanceTo(Objects.requireNonNull(Rs2GameObject.findObjectById(41006)).getWorldLocation()) > 2).collect(Collectors.toList());
                 */
                //System.out.println("test : "+test);
                /*
                if (initScript) {
                    initScript = false;
                    Microbot.getClientThread().runOnSeperateThread(() -> {
                        while (isRunning()) {
                            System.out.println("testing separate thread");
                            sleep(1000);
                        }
                        return true;
                    });
                }
                System.out.println("Testing reachable code");
                */
                //NPC doubleFishingSpot = getNpc(10569);
                /*
                NPC doubleFishingSpot = getNpc(10635);
                if(doubleFishingSpot!=null){
                    System.out.println("Test : "+ Objects.requireNonNull(Rs2GameObject.getObjectComposition(4447)));
                    //System.out.println("Imposter ID is : "+Objects.requireNonNull(Rs2GameObject.getObjectComposition(10569)).getImpostor().getId());
                    System.out.println(doubleFishingSpot.getComposition().toString());
                    //System.out.println("test X : "+doubleFishingSpot.getMinimapLocation().getX()+ " Y : "+ doubleFishingSpot.getMinimapLocation().getY());
                    //System.out.println(doubleFishingSpot.getWorldLocation().getRegionX());
                    //System.out.println("test X : "+doubleFishingSpot.getWorldLocation().getRegionX()+ " Y : "+ doubleFishingSpot.getWorldLocation().getRegionY());
                    //System.out.println("Found double fishing spot at X : "+doubleFishingSpot.getWorldLocation().getX()+ " Y : "+ doubleFishingSpot.getWorldLocation().getY());
                    System.out.println("Player position is : X : "+Rs2Player.getWorldLocation().getX()+" Y : "+Rs2Player.getWorldLocation().getY());
                    //System.out.println("Found double fishing spot at X : "+doubleFishingSpot.getLocalLocation().getX()+ " Y : "+ doubleFishingSpot.getLocalLocation().getY());
                    //System.out.println("Player local position is : X : "+Rs2Player.getLocalLocation().getX()+" Y : "+Rs2Player.getLocalLocation().getY());
                } else {
                    System.out.println("Unable to find double spot");
                }*/
                //System.out.println("Player location is X: "+ Rs2Player.getWorldLocation().getX() +" Y: "+ Rs2Player.getWorldLocation().getY());
                //List<GameObject> lstrikeshadows = Rs2GameObject.getGameObjects(26185).stream().filter(x -> x.getWorldLocation().getX()==Rs2Player.getWorldLocation().getX() && x.getWorldLocation().getY()==Rs2Player.getWorldLocation().getY()).collect(Collectors.toList());
                //TODO filter is ADDING elements to the list, not removing.
                //List<GameObject> lstrikeshadows = Rs2GameObject.getGameObjects(26185).stream().filter(x -> x.getWorldLocation().distanceTo(Rs2Player.getWorldLocation())>2).collect(Collectors.toList());
                //System.out.println(!lstrikeshadows.isEmpty());
                //System.out.println(lstrikeshadows);
                //List<GameObject> iloomingFire = Rs2GameObject.getGameObjects(41006);
                //iloomingFire.forEach(x -> System.out.println("Lightning location is X: "+x.getWorldLocation().getX()+" Y: "+x.getWorldLocation().getY()));
                //TODO maybe have it check for fishng spots distance to lightning strike zone
                //System.out.println(Microbot.getClient().get);
                //TileObject loomingFire = Rs2GameObject.findObjectById(41006);
                /*
                System.out.println("Cloud X: "+
                Rs2GameObject.findObjectById(41006).getLocalLocation().getX()+" Y: "+
                Rs2GameObject.findObjectById(41006).getLocalLocation().getY());*/
                //System.out.println(loomingFire);
                //System.out.println("Player location is X: "+ Rs2Player.getWorldLocation().getX() +" Y: "+ Rs2Player.getWorldLocation().getY());
                //Rs2Bank.openBank();
                //System.out.println("800ms frequency");

                //System.out.println("Times hit : "+playerHit);
                //TODO check fill coffin, and burgh teleport.
                //Rs2Npc.interact(Rs2Npc.getNpc("Menaphite Thug"), "Lure");
                //lure_NPC(Rs2Npc.getNpc("Menaphite Thug"));
                //MenuEntryImpl(getOption=Use, getTarget=null, getIdentifier=1415, getType=GAME_OBJECT_FIRST_OPTION, getParam0=65, getParam1=56, getItemId=-1, isForceLeftClick=true, getWorldViewId=-1, isDeprioritized=false)
                //MenuEntryImpl(getOption=Climb-up, getTarget=<col=ffff>Staircase, getIdentifier=6242, getType=GAME_OBJECT_FIRST_OPTION, getParam0=65, getParam1=54, getItemId=-1, isForceLeftClick=false, getWorldViewId=-1, isDeprioritized=false)
                //Rs2GameObject.interact(new WorldPoint(3353, 2960, 0), "Climb-up");
                /*
                List<NPC> npcsInArea = Microbot.getClient().getNpcs().stream().filter(x ->
                                x.getWorldLocation().getX() >= 3340).filter(x -> x.getWorldLocation().getY() <= 2956).filter(x ->
                                x.getWorldLocation().getX() <= 3344).filter(x -> x.getWorldLocation().getY() >= 2953)
                        .collect(Collectors.toList());
                System.out.println("Number of NPCs in bjArea == "+npcsInArea.size());
                */
                /*
                if (inArea(Rs2Player.getWorldLocation(), 3340,2956,3344,2953)) {
                    System.out.println("Player is in the bj area.");
                } else {
                    System.out.println("Player is NOT the bj area.");
                }
                */
                /*
                if(Microbot.getClient().getLocalPlayer().getWorldLocation().getPlane()==0) {
                    Rs2GameObject.interact(6242, true);
                } else {
                    Rs2GameObject.interact(6243, true);
                }
                sleep(1000);
                */

                //long startTime = System.currentTimeMillis();
                //System.out.println(Rs2Npc.getNpc("Small Lizard"));
                //System.out.println(Rs2Npc.getNpc("Desert Lizard").getComposition().toString());
                /*
                Rs2Inventory.interact("coffin", "fill");
                sleep(1000,5000);
                Rs2Inventory.interact("Morytania legs", "Burgh Teleport");
                sleep(1000,5000);
                */
                /*
                Rs2Item rs2Item = items().stream().filter(x -> x.name.equalsIgnoreCase(item.toLowerCase())).findFirst().orElse(null);
                String[] actions = rs2Item.getInventoryActions();
                for (int i = 0; i < actions.length; i++) {
                    System.out.println(i+" : "+actions[i]);
                }*/

                /*
                if(oldXP<Microbot.getClient().getSkillExperience(Skill.THIEVING)){
                    firstxpdrop=System.currentTimeMillis();
                    oldXP = Microbot.getClient().getSkillExperience(Skill.THIEVING);
                    while(oldXP==Microbot.getClient().getSkillExperience(Skill.THIEVING)){
                        secondxpdrop=System.currentTimeMillis();
                        sleep(20);
                    }
                    timeBetweenXPDrops=secondxpdrop-firstxpdrop;
                    System.out.println("Time between knock-out and first pickpocket : "+ timeBetweenXPDrops);
                    firstxpdrop=System.currentTimeMillis();
                    oldXP = Microbot.getClient().getSkillExperience(Skill.THIEVING);
                    while(oldXP==Microbot.getClient().getSkillExperience(Skill.THIEVING)){
                        secondxpdrop=System.currentTimeMillis();
                        sleep(20);
                    }
                    System.out.println("Time between first pickpocket and second pickpocket : "+ timeBetweenXPDrops);
                }*/
                //sleepUntil(() -> oldXP < Microbot.getClient().getSkillExperience(Skill.THIEVING), 1000);

                //System.out.println("Experience after action : "+ oldXP);

                //long endTime = System.currentTimeMillis();
                //long totalTime = endTime - startTime;
                //System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 10000, TimeUnit.MILLISECONDS);
        return true;
    }
    public boolean lure_NPC(NPC npc){
        //TODO toggle run off,
        //TODO walk to 3346, 2955
        //TODO wait for NPC to be next to player
        //TODO check which side of the player the npc is on
        //TODO if npc on wrong side, toggle run ON and attempt to reposition by running away from the NPC, and then back to the correct tile.
        //TODO remember to make sure run is ON.
        Rs2Player.toggleRunEnergy(false);
        sleep(200,260);
        Rs2Npc.interact(npc, "Lure");
        boolean lureStarted = sleepUntilTrue(() -> Rs2Widget.hasWidget("Psst. Come here, I want to show you something."), 100, 5000);
        if(lureStarted){
            sleep(120,160);
            Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
            sleepUntilTrue(() -> !Rs2Widget.hasWidget("Psst. Come here, I want to show you something."), 100, 3000);
            sleep(120,160);
        } else {
            return false;
        }
        boolean lureResult = Rs2Widget.hasWidget("What is it?");
        if(lureResult){
            Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
            sleepUntilTrue(() -> !Rs2Widget.hasWidget("What is it?"), 100, 3000);
            sleep(120,160);
            Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
            //TODO add here~
            Rs2Walker.walkTo(new WorldPoint(3346,2955,0), 0);
            sleepUntil(() -> Rs2Player.getWorldLocation().getX()==3346 && Rs2Player.getWorldLocation().getY()==2955);
            sleep(120,160);
            waitForNPC(npc);
            //TODO end of walk to~
            if(npc.getWorldLocation().getY()<Rs2Player.getWorldLocation().getY()){
                Rs2Walker.walkTo(new WorldPoint(3346,2959,0), 0);
                sleepUntil(() -> Rs2Player.getWorldLocation().getY()>=2958, 3000);
                waitForNPC(npc);
                Rs2Walker.walkTo(new WorldPoint(3346,2955,0), 0);
                sleepUntil(() -> Rs2Player.getWorldLocation().getX()==3346 && Rs2Player.getWorldLocation().getY()==2955);
                waitForNPC(npc);
                //TODO above is for if the npc is south of the player.
            } else {
                Rs2Walker.walkTo(new WorldPoint(3343,2954,0), 0);
                sleepUntil(() -> Rs2Player.getWorldLocation().getX()==3343 && Rs2Player.getWorldLocation().getY()==2954, 3000);
                waitForNPC(npc);
                Rs2Player.toggleRunEnergy(true);
            }
            //sleepUntil(() -> npc.getWorldLocation().distanceTo(Rs2Player.getWorldLocation())==1, 3000);
            return true;
        } else {
            return false;
        }
    }
    public static boolean inArea(WorldPoint entity, int ax, int ay, int bx, int by){
        return (entity.getX() >= ax && entity.getY() <= ay) && (entity.getX() <= bx && entity.getY() >= by);
    }
    public void waitForNPC(NPC npc){
        long movingStart = System.currentTimeMillis();
        while(npc.getWorldLocation().distanceTo(Rs2Player.getWorldLocation())!=1){
            WorldPoint isMoving = npc.getWorldLocation();
            sleep(700);
            if(npc.getWorldLocation()==isMoving){
                break;
            }
            if((System.currentTimeMillis()-movingStart)>=5000){
                break;
            }
        }
    }
    private void customSleep(String priority){
        //TODO making this a method so that somebody can come along later, or we can modify this later, to be more data-driven / realistic
        if(this.isRunning()) {
            if (priority.equalsIgnoreCase("low")) {
                sleep(600, 1800);
            }
            if (priority.equalsIgnoreCase("moderate")) {
                sleep(213, 431);
            }
            if (priority.equalsIgnoreCase("high")) {
                sleep(61, 97);
            }
        }
    }
    private void repairPouches() {
        if(Rs2Tab.getCurrentTab() != InterfaceTab.MAGIC){
            if(this.isRunning()){ Rs2Tab.switchToMagicTab(); sleepUntil(()-> Rs2Tab.getCurrentTab() == InterfaceTab.MAGIC);} }
        if(this.isRunning()){ Rs2Magic.cast(MagicAction.NPC_CONTACT); }
        //if(this.isRunning()){ Rs2Widget.clickWidget(14286953); }
        if(this.isRunning()){ sleepUntil(() -> Rs2Widget.getWidget(4915214) != null,10000); }
        if(this.isRunning()){ Rs2Widget.clickWidget(4915214); }
        if(this.isRunning()){ sleepUntil(()-> Rs2Widget.findWidget("Click here to continue") != null); }
        if(this.isRunning()){ sleep(60,100); }
        //VirtualKeyboard.keyPress(KeyEvent.VK_SPACE);
        if(this.isRunning()){ Rs2Widget.clickWidget("Click here to continue"); }
        if(this.isRunning()){ sleepUntil(()-> Rs2Widget.findWidget("Can you repair my pouches?") != null); }
        if(this.isRunning()){ sleep(60,100); }
        //VirtualKeyboard.keyPress(KeyEvent.VK_1);
        if(this.isRunning()){ Rs2Widget.clickWidget("Can you repair my pouches?"); }
        if(this.isRunning()){ sleepUntil(()-> Rs2Widget.findWidget("Click here to continue") != null); }
        if(this.isRunning()){ sleep(60,100); }
        //VirtualKeyboard.keyPress(KeyEvent.VK_SPACE);
        if(this.isRunning()){ Rs2Widget.clickWidget("Click here to continue"); }
        if(this.isRunning()){ sleep(60,100); }
        //VirtualKeyboard.keyPress(KeyEvent.VK_SPACE);
        if(this.isRunning()){ Rs2Widget.clickWidget("Click here to continue"); }
        if(Rs2Tab.getCurrentTab() != InterfaceTab.INVENTORY){
            if(this.isRunning()){ Rs2Tab.switchToInventoryTab(); sleepUntil(()-> Rs2Tab.getCurrentTab() == InterfaceTab.INVENTORY);} }
        if(this.isRunning()){ sleep(60,100); }
    }
    public static boolean onlyContains(String... names) {
        return items().stream().allMatch(x -> Arrays.stream(names).anyMatch(name -> x.name.equalsIgnoreCase(name)));
    }
    @Override
    public void shutdown() {
        super.shutdown();
    }
}
