import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Item;


public class useonobjectScript extends Script {
    private static final int MAX_TRIES = 3;
    private Set<Integer> recentItems = new HashSet<>();
    public boolean key1isdown;
    public boolean key2isdown;
    public boolean bones;
    public boolean run(useonobjectConfig config) {
        bones=false;
        key1isdown =false;
        key2isdown =false;
        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                //long startTime = System.currentTimeMillis();
                if(key1isdown) {
                    while (key1isdown && this.isRunning()) {
                        System.out.println("Script is detecting key down");
                        if (!Rs2Bank.isOpen()) {
                            if (!bones) {
                                //11943 lava bone id
                                if (Rs2Inventory.hasItem(config.itemID())) {
                                    if (!config.lastSlot()) {
                                        Rs2Inventory.use(getRandomItemWithLimit(config.itemID()));
                                    } else {
                                        Rs2Inventory.useLast(config.itemID());
                                    }
                                }
                                sleep(config.sleepMin(), config.sleepMax());
                                bones = true;
                            } else {//411 is chaos altar
                                if (Rs2Inventory.hasItem(config.itemID())) {
                                    Rs2GameObject.interact(config.objectID(), "use");
                                    sleep(config.sleepMin(), config.sleepMax());
                                }
                                bones = false;
                            }
                        } else {
                            sleep(100, 300);
                        }
                    }
                } else if (key2isdown && Rs2Inventory.get(config.itemID()).name.contains("bone")){
                    while (key2isdown && this.isRunning()) {
                        Rs2Inventory.interact(getRandomItemWithLimit(config.itemID()), "bury");
                    }
                }
                    bones = false;
                    //long endTime = System.currentTimeMillis();
                    //long totalTime = endTime - startTime;
                    //System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 30, TimeUnit.MILLISECONDS);
        return true;
    }
    @Override
    public void shutdown() {
        super.shutdown();
    }
    public Rs2Item getRandomItemWithLimit(int itemId) {
        List<Rs2Item> matchingItems = Rs2Inventory.items().stream()
                .filter(item -> item.id == itemId)
                .collect(Collectors.toList());

        if (matchingItems.isEmpty()) {
            return null; // No items match the provided ID
        }

        Rs2Item selectedItem = null;
        int tries = 0;

        while (tries < MAX_TRIES) {
            int randomIndex = ThreadLocalRandom.current().nextInt(matchingItems.size());
            selectedItem = matchingItems.get(randomIndex);

            // Check if the item has been selected recently
            if (!recentItems.contains(selectedItem.getSlot())) {
                break;
            }

            tries++;
        }

        // Update recent items list
        recentItems.add(selectedItem.getSlot());
        if (recentItems.size() > MAX_TRIES) {
            Iterator<Integer> iterator = recentItems.iterator();
            iterator.next();
            iterator.remove(); // Remove the oldest item
        }

        return selectedItem;
    }
}
