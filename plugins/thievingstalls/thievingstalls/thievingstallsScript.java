package net.runelite.client.plugins.microbot.storm.plugins.thievingstalls.thievingstalls;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Item;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;
import static net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory.items;

//TODO add states for banking?
public class thievingstallsScript extends Script {
    public static double version = 1.0;
    @Inject
    private thievingstallsConfig config;
    boolean initScript = false;
    private static int sleepMin;
    private static int sleepMax;
    private static int sleepTarget;
    static boolean needToDrop;
    static long timeSinceSteal;
    public boolean run(thievingstallsConfig config) {
        initScript = true;
        Microbot.enableAutoRunOn = false;
        sleepMin = config.sleepMin();
        sleepMax = config.sleepMax();
        sleepTarget = config.sleepTarget();
        needToDrop=false;
        this.config = config;
        timeSinceSteal=System.currentTimeMillis();
        java.util.List<String> doNotDropItemList = Arrays.stream(config.DoNotDropItemList().split(",")).collect(Collectors.toList());
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                //long timeTest = System.currentTimeMillis();
                //sleep(calculateSleepDuration());
                //System.out.println("calculateSleepDuration time : "+(System.currentTimeMillis()-timeTest));
                if(timeSinceSteal<System.currentTimeMillis()-4000 && !needToDrop){
                    if(Rs2GameObject.findObjectByIdAndDistance(Integer.parseInt(config.gameObjectID()), 2)!=null){
                        if(config.turbo()){
                            timeSinceSteal=System.currentTimeMillis();
                            Rs2GameObject.interact(Integer.parseInt(config.gameObjectID()), "Steal-from");
                        } else {
                            calculateSleepDuration();
                            timeSinceSteal=System.currentTimeMillis();
                            Rs2GameObject.interact(Integer.parseInt(config.gameObjectID()), "Steal-from");
                        }
                    }
                }
                sleepUntil(() ->!Rs2Player.isAnimating(),400);
                if(Rs2Inventory.isFull() || (config.dropEa() && !onlyContainsPlus(config.keepItemsAboveValue(),doNotDropItemList.toArray(new String[0])))){
                    needToDrop=true;
                }
                if(needToDrop){
                    calculateSleepDuration();
                    dropItems();
                    needToDrop = false;
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 10, TimeUnit.MILLISECONDS);
        return true;
    }
    static int calculateSleepDuration() {
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
    private void dropItems() {
        java.util.List<String> doNotDropItemList = Arrays.stream(config.DoNotDropItemList().split(",")).collect(Collectors.toList());
        Rs2Inventory.dropAllExcept(config.keepItemsAboveValue(), doNotDropItemList);
        sleepUntil(() ->onlyContains(doNotDropItemList.toArray(new String[0])),300);
    }
    public static boolean onlyContains(String... names) {
        return items().stream().allMatch(x -> Arrays.stream(names).anyMatch(name -> x.name.equalsIgnoreCase(name)));
    }
    public static boolean onlyContainsPlus(int gpValue, String... names) {
        java.util.List<String> valuedItems = new ArrayList<>(List.of(names));
        for (Rs2Item item :
                new ArrayList<>(items())) {
            if (item == null) continue;
            long totalPrice = (long) Microbot.getClientThread().runOnClientThread(() ->
                    Microbot.getItemManager().getItemPrice(item.id) * item.quantity);
            if (totalPrice <= gpValue) continue;
            valuedItems.add(item.getName());
        }

        return items().stream().allMatch(x -> Arrays.stream(valuedItems.toArray(new String[0])).anyMatch(name -> x.name.equalsIgnoreCase(name)));
    }
    @Override
    public void shutdown() {
        super.shutdown();
    }
}
