package net.runelite.client.plugins.microbot.storm.plugins.ModifBankStander;

import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Item;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import javax.inject.Inject;
import java.awt.event.KeyEvent;
import java.util.concurrent.TimeUnit;
import java.util.Random;

import static net.runelite.client.plugins.microbot.util.Global.*;
import static net.runelite.client.plugins.microbot.util.Global.sleep;
import static net.runelite.client.plugins.microbot.util.math.Random.random;

public class zBanksBankStanderScript extends Script {
    public static long previousItemChange;
    @Inject
    private zBanksBankStanderConfig config;

    public static double version = 1.1;
    private zCurrentStatus currentStatus = zCurrentStatus.FETCH_SUPPLIES;

    String firstItemIdentifier;
    private int firstItemQuantity;
    private Integer firstItemId;
    String secondItemIdentifier;
    private int secondItemQuantity;
    public static Integer secondItemId;
    String thirdItemIdentifier;
    private int thirdItemQuantity;
    private Integer thirdItemId;
    String fourthItemIdentifier;
    private int fourthItemQuantity;
    public static Integer fourthItemId;
    private int sleepMin;
    private int sleepMax;
    private int sleepTarget;
    public static boolean bankIsOpen=false;
    public static boolean isWaitingForPrompt=false;

    public boolean run(zBanksBankStanderConfig config) {
        this.config = config; // Initialize the config object before accessing its parameters
        bankIsOpen=Rs2Bank.isOpen();
        // Initialize other variables
        firstItemIdentifier = config.firstItemIdentifier();
        firstItemQuantity = config.firstItemQuantity();
        secondItemIdentifier = config.secondItemIdentifier();
        secondItemQuantity = config.secondItemQuantity();
        thirdItemIdentifier = config.thirdItemIdentifier();
        thirdItemQuantity = config.thirdItemQuantity();
        fourthItemIdentifier = config.fourthItemIdentifier();
        fourthItemQuantity = config.fourthItemQuantity();
        sleepMin = config.sleepMin();
        sleepMax = config.sleepMax();
        sleepTarget = config.sleepTarget();

        // Determine whether the first & second item is the ID or Name.
        firstItemId = TryParseInt(config.firstItemIdentifier());
        secondItemId = TryParseInt(config.secondItemIdentifier());
        thirdItemId = TryParseInt(config.thirdItemIdentifier());
        fourthItemId = TryParseInt(config.fourthItemIdentifier());

        // Print the types of firstItemIdentifier and firstItemId
        System.out.println("Type of firstItemIdentifier: " + firstItemIdentifier.getClass().getSimpleName());
        System.out.println("Type of firstItemId: " + (firstItemId != null ? firstItemId.getClass().getSimpleName() : "null"));
        // Print the types of secondItemIdentifier and secondItemId
        System.out.println("Type of secondItemIdentifier: " + secondItemIdentifier.getClass().getSimpleName());
        System.out.println("Type of secondItemId: " + (secondItemId != null ? secondItemId.getClass().getSimpleName() : "null"));

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!Microbot.isLoggedIn()) return;
            try {
                //start
                combineItems();

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 10, TimeUnit.MILLISECONDS);
        return true;
    }

    private boolean hasItems() {
        // Check if the player has the required quantity of both items using the configuration
        if (firstItemId != null && secondItemId != null) {
            // User has inputted the item id for both items.
            //System.out.println("Checking for items by ID...");
            return Rs2Inventory.hasItem(firstItemId) &&
                    Rs2Inventory.hasItem(secondItemId);
        } else if (firstItemId != null) {
            // User has inputted the item id for the first item and item identifier for the second item.
            System.out.println("Checking for first item by ID and second item by identifier...");
            return Rs2Inventory.hasItem(firstItemId) &&
                    Rs2Inventory.hasItem(secondItemIdentifier);
        } else if (secondItemId != null) {
            // User has inputted the item id for the second item and item identifier for the first item.
            System.out.println("Checking for second item by ID and first item by identifier...");
            return Rs2Inventory.hasItem(firstItemIdentifier) &&
                    Rs2Inventory.hasItem(secondItemId);
        } else {
            // User has inputted the item identifier for both items.
            System.out.println("Checking for items by identifier...");
            return Rs2Inventory.hasItem(firstItemIdentifier) &&
                    Rs2Inventory.hasItem(secondItemIdentifier);
        }
    }


    private boolean fetchItems() {
        sleep(calculateSleepDuration());
        // Check if we have supplies already by calling hasItems
        if (!hasItems()) {
            if (!bankIsOpen) {
                // Open Bank
                openBank();
                //Rs2Bank.useBank();
                //sleep(calculateSleepDuration());
            }
            boolean i = sleepUntilTrue(() -> bankIsOpen, random(67,97), 18000);
            sleep(61,97);
            if (firstItemId != null && secondItemId != null && !config.supercombat()) {
                Rs2Bank.depositAllExcept(firstItemId, secondItemId, thirdItemId, fourthItemId);
            } else if (firstItemId == null && secondItemId == null && !config.supercombat()){
                Rs2Bank.depositAllExcept(firstItemIdentifier, secondItemIdentifier, thirdItemIdentifier, fourthItemIdentifier);
            } else {
                Rs2Bank.depositAll();
            }
            boolean b = sleepUntilTrue(() -> !Rs2Inventory.isFull(), 100, 6000);
            sleep(100,300);
            if (!checkBankCount()) { return false; }
            // Check the type of first item identifier
            if (firstItemId != null) {
                // User has inputted the item id for the first item.
                if (Rs2Bank.hasItem(firstItemId) && !Rs2Inventory.hasItem(firstItemId)) {
                    // Withdraw Item 1 Qty
                    Rs2Bank.withdrawX(true, firstItemId, firstItemQuantity);
                    sleep(100,300);
                }
            } else {
                // User has inputted the item identifier for the first item.
                if (Rs2Bank.hasItem(firstItemIdentifier) && !Rs2Inventory.hasItem(firstItemIdentifier)) {
                    // Withdraw Item 1 Qty
                    Rs2Bank.withdrawX(true, firstItemIdentifier, firstItemQuantity);
                    sleep(100,300);
                }
            }
            // Check the type of second item identifier
            if (secondItemId != null) {
                // User has inputted the item id for the second item.
                if (Rs2Bank.hasItem(secondItemId)) {
                    if(!config.withdrawAll()) {
                        // Withdraw Item 2 Qty
                        Rs2Bank.withdrawX(true, secondItemId, secondItemQuantity);
                    } else {
                        Rs2Bank.withdrawAll(secondItemId);
                    }
                }
            } else {
                // User has inputted the item identifier for the second item.
                if (Rs2Bank.hasItem(secondItemIdentifier)) {
                    if(!config.withdrawAll()) {
                        // Withdraw Item 2 Qty
                        Rs2Bank.withdrawX(true, secondItemIdentifier, secondItemQuantity);
                    } else {
                        Rs2Bank.withdrawAll(true, secondItemIdentifier);
                    }
                }
            }

            if(config.supercombat()){
                if (thirdItemId != null) {
                    //TODO check item quantities as well~
                    // User has inputted the item id for the first item.
                    if (Rs2Bank.hasItem(thirdItemId) && !Rs2Inventory.hasItem(thirdItemId)) {
                        // Withdraw Item 1 Qty
                        Rs2Bank.withdrawX(true, thirdItemId, thirdItemQuantity);
                        sleep(100,300);
                    }
                } else {
                    // User has inputted the item identifier for the first item.
                    if (Rs2Bank.hasItem(thirdItemIdentifier)&& !Rs2Inventory.hasItem(thirdItemIdentifier)) {
                        // Withdraw Item 1 Qty
                        Rs2Bank.withdrawX(true, thirdItemIdentifier, thirdItemQuantity);
                        sleep(100,300);
                    }
                }
                // Check the type of second item identifier
                if (fourthItemId != null) {
                    // User has inputted the item id for the fourth item.
                    if (Rs2Bank.hasItem(fourthItemId)) {
                        if(!config.withdrawAll()) {
                            // Withdraw Item 2 Qty
                            Rs2Bank.withdrawX(true, fourthItemId, fourthItemQuantity);
                        } else {
                            Rs2Bank.withdrawAll(fourthItemId);
                        }
                    }
                } else {
                    // User has inputted the item identifier for the fourth item.
                    if (Rs2Bank.hasItem(fourthItemIdentifier)) {
                        if(!config.withdrawAll()) {
                            // Withdraw Item 2 Qty
                            Rs2Bank.withdrawX(true, fourthItemIdentifier, fourthItemQuantity);
                        } else {
                            Rs2Bank.withdrawAll(true, fourthItemIdentifier);
                        }
                    }
                }
            }
            //TODO make this do something after some time has elapsed~(such as logout, switch items, etc.)
            long startTime = System.currentTimeMillis();
            do {
                if (hasItems() || !this.isRunning()) {
                    break;
                }
                sleep(10);
            } while (System.currentTimeMillis() - startTime < 10000);
            System.out.println("Time to detect items in inventory : "+(System.currentTimeMillis()-startTime));
            /*
            while(!hasItems() && this.isRunning()) {
                boolean k = sleepUntilTrue(() ->hasItems(), 10, 10000);
                //sleepUntil(() -> hasItems());
            }*/
            //TODO instead of indefinite loop, have it check that item exists in bank, and loop if not.
/*
            if(!hasItems()){
                if((firstItemId != null)) { if (Rs2Bank.hasItem(firstItemId)) { if(!Rs2Inventory.hasItem(firstItemId)){

                        } }
                } else { if (Rs2Bank.hasItem(firstItemIdentifier)) { if(!Rs2Inventory.hasItem(firstItemIdentifier)){

                        } }
                }
                if((secondItemId != null)) { if (Rs2Bank.hasItem(secondItemId)) { if(!Rs2Inventory.hasItem(firstItemId)){

                        } }
                } else { if (Rs2Bank.hasItem(secondItemIdentifier)) { if(!Rs2Inventory.hasItem(secondItemIdentifier)){

                        } }
                } }*/

            if (hasItems()) {
                previousItemChange=(System.currentTimeMillis()-2500);
                // Close Bank
                long bankCloseTime = System.currentTimeMillis();
                while(this.isRunning() && bankIsOpen && (System.currentTimeMillis()-bankCloseTime<32000)) {
                    closeBank();
                    //System.out.println("Sending close bank click @ "+System.currentTimeMillis());
                    boolean z = sleepUntilTrue(() -> !bankIsOpen, random(60, 97), 5000);
                    //System.out.println("Time to detect bank closed : "+(System.currentTimeMillis()-bankCloseTime));
                    sleep(calculateSleepDuration() - 10);
                }
                if(bankIsOpen){
                    sleep(calculateSleepDuration());
                    Rs2Player.logout();
                    sleep(calculateSleepDuration());
                }
                currentStatus = zCurrentStatus.COMBINE_ITEMS; // Set status to COMBINE_ITEMS after fetching items
                return true;
            }
        }

        return true;
    }

    private boolean combineItems() {
        //System.out.println("Combine items started @ "+System.currentTimeMillis());
        // Check if we have the items, if not, fetch them
        if (!hasItems()) {
            boolean fetchedItems = fetchItems();
            if (!fetchedItems) {
                Microbot.showMessage("Unsufficient items found.");
                while(this.isRunning()){
                    sleep(300,3000);
                }
            }
            return false; // Return false to indicate that items are being fetched
        }
        if(config.waitForAnimation()) {
            if (Rs2Player.isAnimating() /*|| Microbot.isGainingExp*/ || (System.currentTimeMillis()-previousItemChange)<2400) { return false; }
        }

        // Combine items based on the type of identifiers
        if (firstItemId != null && secondItemId != null) {
            //TODO change how this works, maybe last item slot, or each slot?
            // If both IDs are not null, use IDs for both items
            System.out.println("first item click @ "+System.currentTimeMillis());
            Rs2Inventory.use(firstItemId);
            sleep(calculateSleepDuration());
            Rs2Inventory.use(secondItemId);
        } else if (firstItemId != null) {
            // If only firstItemId is not null, use it and secondItemIdentifier
            System.out.println("first item click @ "+System.currentTimeMillis());
            Rs2Inventory.use(firstItemId);
            sleep(calculateSleepDuration());
            Rs2Inventory.use(secondItemIdentifier);
        } else if (secondItemId != null) {
            // If only secondItemId is not null, use it and firstItemIdentifier
            System.out.println("first item click @ "+System.currentTimeMillis());
            Rs2Inventory.use(firstItemIdentifier);
            sleep(calculateSleepDuration());
            Rs2Inventory.use(secondItemId);
        } else {
            // If both IDs are null, use identifiers for both items
            System.out.println("first item click @ "+System.currentTimeMillis());
            Rs2Inventory.use(firstItemIdentifier);
            sleep(calculateSleepDuration());
            Rs2Inventory.use(secondItemIdentifier);
        }

        if (config.needMenuEntry()) {
            sleep(calculateSleepDuration());
            isWaitingForPrompt=true;
            // Simulate a key press (e.g., pressing SPACE)
            sleepUntil(() -> !isWaitingForPrompt, random(800,1200));
            Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
            previousItemChange=System.currentTimeMillis();
        // Sleep until animation is finished or item is no longer in inventory

            //TODO expensive loop, change to something else EDIT: not sure if even working?
            boolean i = sleepUntilTrue(() -> !Rs2Inventory.hasItem(secondItemIdentifier != null ? String.valueOf(secondItemId) : secondItemIdentifier),10, 40000);
        //sleepUntil(() -> !Rs2Inventory.hasItem(secondItemIdentifier != null ? String.valueOf(secondItemId) : secondItemIdentifier), 40000);
        }
        sleep(calculateSleepDuration());
        // Update current status to indicate fetching supplies next
        currentStatus = zCurrentStatus.FETCH_SUPPLIES;

        return true;
    }


    private int calculateSleepDuration() {
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
    public void openBank() {
        Microbot.status = "Opening bank";
        if (Microbot.getClient().isWidgetSelected()) {
            Microbot.getMouse().click();
        }
        if (!bankIsOpen) {
            boolean action = false;
            GameObject bank = Rs2GameObject.findBank();
            if (bank == null) {
                GameObject chest = Rs2GameObject.findChest();
                if (chest == null) {
                    NPC npc = Rs2Npc.getNpc("banker");
                    if (npc != null) {
                        action = Rs2Npc.interact(npc, "bank");
                    }
                } else {
                    action = Rs2GameObject.interact(chest, "use");
                }
            } else {
                action = Rs2GameObject.interact(bank, "bank");
            }
            if (action) {
                sleepUntil(() -> bankIsOpen || Rs2Widget.hasWidget("Please enter your PIN"), 2500);
            }
        }
    }
    public void closeBank() {
        if (bankIsOpen) {
            Rs2Widget.clickChildWidget(786434, 11);
        }
    }
    public boolean checkBankCount(){
        if(!Rs2Bank.isOpen()){
            openBank();
            boolean i = sleepUntilTrue(() -> bankIsOpen, random(67,97), 18000);
            sleep(200,600);
        }
        System.out.println("Attempting to check first item");
        System.out.println("First item quantitiy == "+(Rs2Bank.bankItems.stream().filter(item -> item.id == firstItemId).mapToInt(item -> item.quantity).sum())+"/"+config.firstItemQuantity());
        if (firstItemId != null && (Rs2Bank.bankItems.stream().filter(item -> item.id == firstItemId).mapToInt(item -> item.quantity).sum())<config.firstItemQuantity()) {
            System.out.println("Failing here for some reason");
            return false;
        } else if (firstItemId == null && Rs2Bank.count(firstItemIdentifier)<config.firstItemQuantity()) {
            System.out.println("Oh. You dumbass.");
            return false;
        }
        System.out.println("Attempting to check second item");
        System.out.println("Second item quantitiy == "+(Rs2Bank.bankItems.stream().filter(item -> item.id == secondItemId).mapToInt(item -> item.quantity).sum())+"/"+config.secondItemQuantity());
        if (secondItemId != null && (Rs2Bank.bankItems.stream().filter(item -> item.id == secondItemId).mapToInt(item -> item.quantity).sum())<config.secondItemQuantity()) {
            return false;
        } else if (secondItemId == null && Rs2Bank.count(secondItemIdentifier)<config.secondItemQuantity()) {
            return false;
        }
        if(config.supercombat()) {
            System.out.println("Attempting to check third item");
            System.out.println("Third item quantitiy == "+(Rs2Bank.bankItems.stream().filter(item -> item.id == thirdItemId).mapToInt(item -> item.quantity).sum())+"/"+config.thirdItemQuantity());
            if (thirdItemId != null && (Rs2Bank.bankItems.stream().filter(item -> item.id == thirdItemId).mapToInt(item -> item.quantity).sum())<config.thirdItemQuantity()) {
                return false;
            } else if (thirdItemId == null && Rs2Bank.count(thirdItemIdentifier) < config.thirdItemQuantity()) {
                return false;
            }
            System.out.println("Attempting to check fourth item");
            System.out.println("Fourth item quantitiy == "+(Rs2Bank.bankItems.stream().filter(item -> item.id == fourthItemId).mapToInt(item -> item.quantity).sum())+"/"+config.fourthItemQuantity());
            if (fourthItemId != null && (Rs2Bank.bankItems.stream().filter(item -> item.id == fourthItemId).mapToInt(item -> item.quantity).sum())<config.fourthItemQuantity()) {
                return false;
            } else if (fourthItemId == null && Rs2Bank.count(fourthItemIdentifier) < config.fourthItemQuantity()) {
                return false;
            }
        }
        return true;
    }
    // method to parse string to integer, returns null if parsing fails
    public static Integer TryParseInt(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            System.out.println("Could not Parse Int from Item, using Name Instead");
            return null;
        }
    }
}
