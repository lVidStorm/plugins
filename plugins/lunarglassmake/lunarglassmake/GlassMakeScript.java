package net.runelite.client.plugins.microbot.storm.plugins.lunarglassmake.lunarglassmake;

import java.util.concurrent.TimeUnit;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;

import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;
import static net.runelite.client.plugins.microbot.util.math.Random.random;

public class GlassMakeScript extends Script {

    public static String version = "1.0.0";
    public static String combinedMessage = "";
    long bankOpenStart;
    public static int tickCounter = 0;
    public static long glassMade = 0;
    private long startTime;

    // State management
    private enum State {
        GLASSMAKE,
        BANKING
    }

    private State currentState = State.GLASSMAKE;

    public boolean run(GlassMakeConfig config) {
        startTime = System.currentTimeMillis();
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!super.run() || !Microbot.isLoggedIn()) return;
                if (hasRequiredItems()) {
                    tickCounter=0;
                    Rs2Magic.cast(MagicAction.SUPERGLASS_MAKE);
                    //TODO sleep for 6 ticks, plus miniscule time extra(random)
                    sleepUntil(() -> tickCounter>=6);
                    sleep(5,17);
                    //boolean b = sleepUntilTrue(() ->(!hasRequiredItems()) && !Rs2Player.isAnimating(), random(17,43), 3000);
                    glassMade += 27;
                } else {
                    bank();
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 50, TimeUnit.MILLISECONDS);
        return true;
    }

    // Bank the finished hide
    private void bank() {
        if (currentState != State.BANKING) {
            currentState = State.BANKING;
            bankOpenStart = System.currentTimeMillis();
            if (!Rs2Bank.openBank()) return;
            if(withdrawRequiredItems()){

            } else {
                shutdown();
                return;
            }
            Rs2Bank.closeBank();
            boolean a = sleepUntilTrue(() ->(!Rs2Bank.isOpen()), random(17,43), 3000);
            currentState = State.GLASSMAKE;
            //calculateProfitAndDisplay(config);
        }
    }
    private boolean hasRequiredItems() {
        return Rs2Inventory.hasItemAmount(21504, 3)
               && Rs2Inventory.hasItem(9075)
               && Rs2Inventory.hasItemAmount(1783, 18);
    }
    private boolean withdrawRequiredItems() {
        sleepUntil(() -> Rs2Bank.isOpen());
        System.out.println("Time to detect bank open : "+(System.currentTimeMillis()-bankOpenStart));
        if(!Rs2Bank.hasItem(21504)
                || !Rs2Bank.hasItem(1783)
                || !Rs2Bank.hasItem(9075)){
            Microbot.showMessage("Insufficient materials");
            return false;
        }
        Rs2Bank.depositAllExcept(9075);
        sleepUntil(() -> !Rs2Inventory.hasItem(1775));
        //boolean b = sleepUntilTrue(!Rs2Inventory.hasItem(1775), random(13,26), 3000);
        int i = 0;
        while(i<3){
            Rs2Bank.withdrawOne(21504);
            sleep(60,120);
            i++;
        }
        sleepUntil(() -> Rs2Inventory.hasItemAmount(21504,3));
        Rs2Bank.withdrawX(1783, 18);
        sleepUntil(() -> Rs2Inventory.hasItem(1783));
        sleep(20,80);
        if(!Rs2Inventory.hasItem(9075)) {
            Rs2Bank.withdrawAll(9075);
            sleepUntil(() -> Rs2Inventory.hasItem(9075));
            sleep(20, 80);
        }
        return true;
    }
    @Override
    public void shutdown() {
        super.shutdown();
        glassMade = 0; // Reset the count of tanned hides
        combinedMessage = ""; // Reset the combined message
        currentState = State.GLASSMAKE; // Reset the current state
    }
}