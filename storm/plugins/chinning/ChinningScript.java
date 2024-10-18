package net.runelite.client.plugins.microbot.storm.plugins.chinning;

import net.runelite.api.NPC;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;

import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;
import static net.runelite.client.plugins.microbot.util.math.Random.random;


public class ChinningScript extends Script {
    public static double version = 1.0;
    public static boolean actionsScheduled = false;
    public static int lowPrioritydelay = 100;
    public static int highPrioritydelay = 60;

    private static boolean outOfPrayerPotions = false;
    public static boolean outOfRangePotions = false;
    public static boolean restorePrayer = false;
    public static boolean restoreRange = false;
    public static boolean hitpointDamage = false;

    public static boolean moveHit = false;
    public static boolean stacking = false;
    static int previousXP;
    NPC npc;

    public static long previousAction = System.currentTimeMillis();
    public static long calcSleep = 59;

    private static int sleepMin;
    private static int sleepMax;
    private static int sleepTarget;

    public boolean run(ChinningConfig config) {
        previousXP = Microbot.getClient().getSkillExperience(Skill.RANGED);
        Microbot.enableAutoRunOn = false;
        String[] tileOne = config.tileOne().split(",");
        String[] tileTwo = config.tileTwo().split(",");
        String[] npcTile = config.tileTwo().split(",");
        sleepMin = config.sleepMin();
        sleepMax = config.sleepMax();
        sleepTarget = config.sleepTarget();
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                //long startTime = System.currentTimeMillis();
                if(actionsScheduled) {
                    calcSleep = System.currentTimeMillis() - previousAction;
                    actionsScheduled = false;
                    if(!outOfPrayerPotions) {
                        if (restorePrayer && Rs2Inventory.hasItem(config.prayeritem())) {
                            //TODO change this from 60 to calculated sleep time? maybe, maybe not for emergency?
                            int g = random(61, 121);
                            if (calcSleep < g) {
                                sleep((int) (g - calcSleep));
                            }
                            if (!outOfPrayerPotions) {
                                if (config.restoreamount() != 0) {
                                    if (Rs2Inventory.hasItem(config.prayeritem())) {
                                        if (outOfPrayerPotions) {
                                            outOfPrayerPotions = false;
                                        }
                                        restorePrayer = false;
                                        Rs2Inventory.interact(config.prayeritem(), "drink");
                                        refreshActionTimer();
                                        int e = calculateSleepDuration();
                                        if (calcSleep < e) {
                                            sleep((int) (e - calcSleep));
                                        }
                                        npc = Microbot.getClientThread().runOnClientThread(() -> Microbot.getClient().getNpcs().stream()
                                                .filter(x -> x != null && x.getName() != null && !x.isDead())
                                                .sorted(Comparator.comparingInt(value -> value.getWorldLocation()
                                                        .distanceTo(new WorldPoint(Integer.parseInt(npcTile[0]), Integer.parseInt(npcTile[1]), Rs2Player.getWorldLocation().getPlane()))))).findFirst().get();
                                        Rs2Npc.attack(npc);
                                        refreshActionTimer();
                                        //System.out.println("restore prayer");
                                    }
                                    if (!Rs2Inventory.hasItem(config.prayeritem())) {
                                        outOfPrayerPotions = true;
                                    }
                                }
                            }
                        }
                        if (restoreRange && Rs2Inventory.hasItem(config.combatitem())) {
                            int k = calculateSleepDuration();
                            if (calcSleep < k) {
                                sleep((int) (k - calcSleep));
                            }
                                if (config.restoreamount() != 0) {
                                    if (Rs2Inventory.hasItem(config.combatitem())) {
                                        if (outOfRangePotions) {
                                            outOfRangePotions = false;
                                        }
                                        restoreRange = false;
                                        Rs2Inventory.interact(config.combatitem(), "drink");
                                        refreshActionTimer();
                                        int e = calculateSleepDuration();
                                        if (calcSleep < e) {
                                            sleep((int) (e - calcSleep));
                                        }
                                        npc = Microbot.getClientThread().runOnClientThread(() -> Microbot.getClient().getNpcs().stream()
                                                .filter(x -> x != null && x.getName() != null && !x.isDead())
                                                .sorted(Comparator.comparingInt(value -> value.getWorldLocation()
                                                        .distanceTo(new WorldPoint(Integer.parseInt(npcTile[0]), Integer.parseInt(npcTile[1]), Rs2Player.getWorldLocation().getPlane()))))).findFirst().get();
                                        Rs2Npc.attack(npc);
                                        refreshActionTimer();
                                    }
                                    if (!Rs2Inventory.hasItem(config.combatitem())) {
                                        outOfRangePotions = true;
                                    }
                                }

                        }
                        if ((Microbot.getClient().getEnergy() / 100) < 50) {
                            if (Rs2Inventory.hasItem("Stamina Potion")) {
                                int k = calculateSleepDuration();
                                if (calcSleep < k) {
                                    sleep((int) (k - calcSleep));
                                }
                                Rs2Inventory.interact("Stamina Potion", "drink");
                                refreshActionTimer();
                                int e = calculateSleepDuration();
                                if (calcSleep < e) {
                                    sleep((int) (e - calcSleep));
                                }
                                npc = Microbot.getClientThread().runOnClientThread(() -> Microbot.getClient().getNpcs().stream()
                                        .filter(x -> x != null && x.getName() != null && !x.isDead())
                                        .sorted(Comparator.comparingInt(value -> value.getWorldLocation()
                                                .distanceTo(new WorldPoint(Integer.parseInt(npcTile[0]), Integer.parseInt(npcTile[1]), Rs2Player.getWorldLocation().getPlane()))))).findFirst().get();
                                Rs2Npc.attack(npc);
                                refreshActionTimer();
                                //TODO have it attack the npc again~
                            }
                        }
                        if (moveHit) {
                            //TODO change this from 60 to calculated sleep time?
                            int k = (calculateSleepDuration()*2);
                            if (calcSleep < k) {
                                sleep((int) (k - calcSleep));
                            }
                            if (stacking) {
                                //TODO move to tile x need to test that this works~
                                Rs2Walker.walkFastCanvas(new WorldPoint(Integer.parseInt(tileOne[0]), Integer.parseInt(tileOne[1]), Rs2Player.getWorldLocation().getPlane()));
                                stacking=false;
                                //TODO move to tile x
                            } else {
                                //TODO move to tile y need to test that this works~
                                Rs2Walker.walkFastCanvas(new WorldPoint(Integer.parseInt(tileTwo[0]), Integer.parseInt(tileTwo[1]), Rs2Player.getWorldLocation().getPlane()));
                                stacking=true;
                                //TODO move to tile y
                            }
                            //TODO change this to just check the player's tile location? if it doesn't work well to wait for walking.
                            Rs2Player.waitForWalking(200);
                            refreshActionTimer();
                            int y = calculateSleepDuration();
                            if (calcSleep < y) {
                                sleep((int) (y - calcSleep));
                            }
                            //TODO attack npc on x tile
                            //TODO maybe change it to sort by distance to tile instead?
                        /*
                        npc = Microbot.getClientThread().runOnClientThread(() -> Microbot.getClient().getNpcs().stream()
                                .filter(x -> x != null && x.getName() != null && !x.isDead())
                                .filter(x -> x.getWorldLocation().getX()==Integer.parseInt(npcTile[0]) && x.getWorldLocation().getY()==Integer.parseInt(npcTile[1])))
                                .findFirst().get();*/
                            npc = Microbot.getClientThread().runOnClientThread(() -> Microbot.getClient().getNpcs().stream()
                                    .filter(x -> x != null && x.getName() != null && !x.isDead())
                                    .sorted(Comparator.comparingInt(value -> value.getWorldLocation()
                                            .distanceTo(new WorldPoint(Integer.parseInt(npcTile[0]), Integer.parseInt(npcTile[1]), Rs2Player.getWorldLocation().getPlane()))))).findFirst().get();
                            Rs2Npc.attack(npc);
                            //TODO attack npc on x tile
                            refreshActionTimer();
                            moveHit = false;
                        }
                    }
                    if (outOfPrayerPotions) {
                        int g = random(61, 121);
                        if (calcSleep < g) {
                            sleep((int) (g - calcSleep));
                        }
                        Rs2Inventory.interact(config.teleportitem(), config.teleportAction());
                        while (this.isRunning()) {
                            boolean a = sleepUntilTrue(() -> !this.isRunning(), 6000, 600000);
                        }
                    }
                    //System.out.println("Run energy @ "+Microbot.getClient().getEnergy()/100);
                    //long endTime = System.currentTimeMillis();
                    //long totalTime = endTime - startTime;
                    //System.out.println("Total time for loop " + totalTime);
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 20, TimeUnit.MILLISECONDS);
        return true;
    }
    public static void refreshActionTimer (){
        previousAction = System.currentTimeMillis();
        calcSleep = System.currentTimeMillis()-previousAction;
    }
    public static int calculateSleepDuration() {
        // Create a Random object
        Random random = new Random();

        // Calculate the mean (average) of sleepMin and sleepMax, adjusted by sleepTarget
        double mean = (sleepMin + sleepMax + sleepTarget) / 3.0;

        // Calculate the standard deviation with added noise
        double noiseFactor = 0.2; // Adjust the noise factor as needed (0.0 to 1.0)
        double stdDeviation = Math.abs(sleepTarget - mean) / 3.0 * (1 + noiseFactor * (random.nextDouble() - 0.5) * 2);

        // Generate a random number following a normal distribution
        int sleepDuration;
        do {
            // Generate a random number using nextGaussian method, scaled by standard deviation
            sleepDuration = (int) Math.round(mean + random.nextGaussian() * stdDeviation);
        } while (sleepDuration < sleepMin || sleepDuration > sleepMax); // Ensure the duration is within the specified range

        return sleepDuration;
    }
    @Override
    public void shutdown() {
        super.shutdown();
    }
}
