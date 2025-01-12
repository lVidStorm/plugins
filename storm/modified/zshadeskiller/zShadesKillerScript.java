package net.runelite.client.plugins.microbot.storm.modified.zshadeskiller;

import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.NPC;
import net.runelite.api.Skill;
import net.runelite.api.World;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.storm.modified.zshadeskiller.enums.Area;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.models.RS2Item;
import net.runelite.client.plugins.microbot.storm.modified.zshadeskiller.enums.State;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.awt.event.KeyEvent;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;
import static net.runelite.client.plugins.microbot.util.math.Random.random;
import static net.runelite.client.plugins.microbot.storm.modified.zshadeskiller.enums.State.USE_TELEPORT_TO_BANK;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;
import static net.runelite.client.plugins.microbot.util.player.Rs2Player.eatAt;


public class zShadesKillerScript extends Script {
    //TODO make script stop when bank is missing food
    //TODO support ferox enclave / POH / teleports for restoring prayer as well as restore prayer items?
    public static double version = 1.2;
    public static State state = State.BANKING;
    static Instant lastAnimating = (Instant.now().plusMillis(random(120, 350)));
    static boolean teleportToShades = false;
    static boolean teleportToBank = false;
    static boolean coffinFull;
    int teleportItemAmount = 1;
    NPC npc = null;
    long combatStart = 0;
    int capeItem;
    static List<RS2Item> temp = new ArrayList<>();

    zShadesKillerConfig config;

    List<String> keyTier;
    List<String> keyDescription;

    boolean initScript = false;
    boolean resetActions = false;

    boolean coffinHasItems = true;


    private void withdrawCoffin() {
        if (config.useCoffin()) {
            Rs2Bank.withdrawOne("coffin");
            sleepUntil(() -> Rs2Inventory.hasItem("coffin"));
            sleep(120, 220);
        }
    }

    private String getKeyInBank() {
        for (String keytier : keyTier) {
            for (String keydescription : keyDescription) {
                if(keyTier.indexOf(config.SHADES().requiredKeys)<keyTier.indexOf(keytier)) return "";
                if (Rs2Bank.hasItem(keytier+keydescription))
                    return keytier+keydescription;
            }
        }
        return "";
    }
    private String getKeyInInventory() {
        for (String keytier : keyTier) {
            for (String wholekey : keyDescription) {
                if(keyTier.indexOf(config.SHADES().requiredKeys)<keyTier.indexOf(keytier)) return "";
                if (Rs2Inventory.hasItem(keytier+wholekey))
                    return keytier+wholekey;
            }
        }
        return "";
    }
    private boolean hasRequiredItemsToKillShades() {
        return Rs2Inventory.hasItem(getKeyInInventory())
                && Rs2Inventory.hasItem(config.teleportItemToShades())
                && Rs2Inventory.hasItem(config.teleportItemToBank())
                && Rs2Equipment.hasEquipped(capeItem)
                && Rs2Inventory.hasItemAmount(config.food().getName(), config.foodAmount());
    }

    private boolean withdrawRequiredItems() {
        sleepUntil(() -> Rs2Bank.isOpen());
        Rs2Bank.depositAll();
        sleep(400, 1000);
        String key = getKeyInBank();
        if (key.equals("")) {
            Microbot.showMessage("You are missing your key.");
            sleep(5000);
            return false;
        }
        if(!Rs2Equipment.hasEquipped(capeItem)){
            Rs2Bank.withdrawOne(capeItem);
            sleepUntil(() -> Rs2Inventory.hasItem(capeItem));
            sleep(120,220);
            Rs2Bank.wearItem(capeItem);
            sleepUntil(() -> !Rs2Inventory.hasItem(capeItem));
            sleep(120,220);
        }
        if(!Rs2Inventory.hasItem(key)) {
            Rs2Bank.withdrawOne(key, random(100, 600));
            sleepUntil(() -> Rs2Inventory.hasItem(key));
            sleep(120,220);
        }
        if(!Rs2Inventory.hasItem(config.teleportItemToShades())) {
            Rs2Bank.withdrawOne(config.teleportItemToShades(), random(100, 600));
            sleepUntil(() -> Rs2Inventory.hasItem(config.teleportItemToShades()));
            sleep(120,220);
        }
        if(!Rs2Inventory.hasItemAmount(config.teleportItemToBank(),teleportItemAmount)) {
            Rs2Bank.withdrawOne(config.teleportItemToBank(), random(100, 600));
            sleepUntil(() -> Rs2Inventory.hasItem(config.teleportItemToBank()));
        }
        if(Rs2Bank.hasItem(config.food().getName())) {
            Rs2Bank.withdrawX(config.food().getName(), config.foodAmount());
            sleepUntil(() -> Rs2Inventory.hasItem(config.food().getName()));
            sleep(120, 220);
        } else {
            while(this.isRunning() && !Rs2Bank.hasItem(config.food().getName())){
                sleep(5000);
            }
        }
        withdrawCoffin();
        sleep(800, 1200);
        return true;
    }

    private void emptyCoffin() {
        //TODO do something here to prioritize empty coffin
        if (!Rs2Inventory.hasItem("coffin")) {
            Rs2Bank.depositAll();
            sleep(400, 800);
            Rs2Bank.withdrawOne("coffin");
            sleepUntil(() -> Rs2Inventory.hasItem("coffin"));
            sleep(120, 220);
        }
        if(Rs2Inventory.hasItem(config.food().getName())){
            Rs2Bank.depositAll(config.food().getName());
        }
        Rs2Bank.closeBank();
        sleepUntil(() -> !Rs2Bank.isOpen());
        sleep(800, 1200);
        Rs2Inventory.interact("coffin", "configure");
        boolean isEmptyCoffin = sleepUntilTrue(() -> Rs2Widget.hasWidget("Empty Coffin."), 100, 3000);
        if (!isEmptyCoffin) return;
        Rs2Widget.clickWidget("Empty Coffin");
        boolean isMakeInterface = sleepUntilTrue(() -> Rs2Widget.hasWidget("What would you like to take?"), 100, 3000);
        if (!isMakeInterface) {
            boolean isCoffinEmpty = Rs2Widget.hasWidget("Your coffin is empty.");
            if (isCoffinEmpty) {
                coffinHasItems = false;
                coffinFull = false;
            }
            return;
        }
        Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
        sleepUntil(() -> Rs2Inventory.hasItem("remains"));
        sleep(200, 1250);
        if(Rs2Inventory.hasItem("remains") && !Rs2Inventory.isFull()){
            coffinFull = false;
            coffinHasItems = false;
        }
        boolean foundBank = Rs2Bank.openBank();
        if(!foundBank && Rs2Bank.walkToBank()){
            sleep(60, 120);
            Rs2GameObject.interact(12759, true);

        }
        sleepUntil(() -> Rs2Bank.isOpen());
        sleep(400, 1200);
        Rs2Bank.depositAll("remains");
        sleepUntil(() -> !Rs2Inventory.hasItem("remains"));
        sleep(400, 1200);
        if(!coffinHasItems){
            if(!Rs2Inventory.hasItemAmount(config.food().getName(), config.foodAmount()) && Rs2Inventory.hasItem(config.food().getName())) {
                Rs2Bank.depositAll(config.food().getName());
            }
            Rs2Bank.withdrawX(config.food().getName(), config.foodAmount());
            sleepUntil(() -> !Rs2Inventory.hasItem(config.food().getName()));
            sleep(120, 220);
            //TODO add something here to fix depositall/withdraw extra step
        }
    }

    public boolean run(zShadesKillerConfig config) {
        this.config = config;
        //TODO reduce these down to just "key crimson", "key red", etc.
        keyTier = Arrays.asList(
        "Gold",
        "Silver",
        "Black",
        "Steel",
        "Bronze");
        keyDescription = Arrays.asList(
        " key crimson",
        " key red",
        " key brown",
        " key black",
        " key purple");
        if(Objects.equals(config.teleportItemToBank(), config.teleportItemToShades())){
            teleportItemAmount=2;
        }
        capeItem = Rs2Equipment.get(EquipmentInventorySlot.CAPE).getId();
        initScript = true;
        combatStart = 0;
        //TODO use indexOf to determine world to hop to.
        //String[] Worlds = config.suitableWorlds().split(",");//TODO if(keyTier.indexOf(config.SHADES().requiredKeys)<keyTier.indexOf(keytier)) return "";
        List<Integer> Worlds = Arrays.stream(config.suitableWorlds().split(","))
                .map(String::trim)        // Trim each string
                .map(Integer::parseInt)   // Parse each string to an integer
                .collect(Collectors.toList());
        state = State.BANKING;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run()) return;
            if (!Microbot.isLoggedIn()) return;
            try {
                //long startTime = System.currentTimeMillis();
                if (initScript) {
                    if (Rs2Player.getWorldLocation().distanceTo(config.SHADES().location) < 30) {
                        state = State.WALK_TO_SHADES;
                    }
                    initScript = false;
                }

                boolean ate = eatAt(config.eatAt());
                if (ate) {
                    resetActions = true;
                }
                //TODO he did it again... set this when had 99 HP, so doesn't run at threshold, but exact hp?
                if (Microbot.getClient().getBoostedSkillLevel(Skill.HITPOINTS) <= config.hpTreshhold() && Rs2Inventory.hasItem(config.teleportItemToBank()) && teleportToBank) {
                    state = USE_TELEPORT_TO_BANK;
                }
                switch (state) {
                    case BANKING:
                        if (hasRequiredItemsToKillShades() && !coffinHasItems) {
                            if(Rs2Bank.isOpen()){
                                Rs2Bank.closeBank();
                                sleepUntil(() -> !Rs2Bank.isOpen());
                                sleep(800, 1200);
                            }

                            teleportToShades=true;
                            state = State.USE_TELEPORT_TO_SHADES;
                            return;
                        }
                        lastAnimating = (Instant.now().plusMillis(random(120, 350)));
                        if(!Rs2Bank.isOpen()) {
                            boolean foundBank = Rs2Bank.openBank();
                            if (!foundBank && !Rs2Bank.walkToBank()) {
                                Rs2Bank.walkToBank();
                                sleep(160, 420);
                                return;
                            }
                            //TODO might have this fixed via adding "NullObjectID.NULL_12759," to microbot\playerassist\constants\Constants.java "BANK_OBJECT_IDS".
                            if (!foundBank && Rs2Bank.walkToBank() && inArea(Rs2Player.getWorldLocation(), Area.Burgh_Bank)) {
                                sleep(160, 420);
                                Rs2GameObject.interact(12759, "bank");
                            }
                            sleepUntil(() -> Rs2Bank.isOpen(), 3000);
                            sleep(300, 420);
                        }
                        //TODO potentially problem here with check inventory for remains, need a new method to check if any names contain "remains"
                        if(Rs2Inventory.hasItem("remains") || Rs2Inventory.getEmptySlots()<2){
                            System.out.println("Attempting to deposit remains");
                            Rs2Bank.depositAll("remains");
                            sleepUntil(() -> !Rs2Inventory.hasItem("remains"));
                            sleep(220, 300);
                        }
                        sleep(180, 220);
                        if (coffinHasItems) {
                            emptyCoffin();
                            return;
                        }
                        if (!Rs2Player.isFullHealth()) {
                            Rs2Bank.withdrawX(config.food().getName(), config.foodAmount());
                            sleepUntil(() -> Rs2Inventory.hasItem(config.food().getName()));
                            sleep(120, 220);
                            Rs2Bank.closeBank();
                            sleepUntil(() -> !Rs2Bank.isOpen());
                            sleep(1200);
                            //TODO I don't think the bank has to be closed to eat food?
                            while (!Rs2Player.isFullHealth() && Rs2Inventory.hasItem(config.food().getName())) {
                                eatAt(99);
                                Rs2Player.waitForAnimation();
                                sleep(40, 250);
                            }
                            //coffinHasItems=true;
                            return;
                        }
                        //TODO issue here, needs to not withdraw items if has items.
                        if (Rs2Bank.isOpen() && !hasRequiredItemsToKillShades()) {
                            boolean result = withdrawRequiredItems();
                            if (!result) return;
                            Rs2Bank.closeBank();
                            sleepUntil(() -> !Rs2Bank.isOpen());
                        }
                        if (Rs2Bank.isOpen()) {
                        Rs2Bank.closeBank();
                        sleepUntil(() -> !Rs2Bank.isOpen());
                    }
                        break;
                    case USE_TELEPORT_TO_SHADES:
                        if (teleportToShades) {
                            if (Rs2Inventory.hasItem(config.teleportItemToShades())) {
                                WorldPoint previousLocation = new WorldPoint(Rs2Player.getWorldLocation().getX(), Rs2Player.getWorldLocation().getY(), Rs2Player.getWorldLocation().getPlane());
                                Rs2Inventory.interact(config.teleportItemToShades(), config.teleportActionToShades());
                                if (this.isRunning()) { sleepUntil(() -> Rs2Player.getWorldLocation().distanceTo(previousLocation) > 10); }
                                //Rs2Player.waitForAnimation();
                                teleportToShades = false;
                                teleportToBank = true;
                            }
                        } else {
                            state = State.WALK_TO_SHADES;
                        }
                        break;
                    case WALK_TO_SHADES:
                        if (inArea(Rs2Player.getWorldLocation(), config.SHADES().Area)) {
                            combatStart = System.currentTimeMillis();
                            state = State.FIGHT_SHADES;
                            return;
                        }
                        Rs2Walker.walkTo(config.SHADES().location, 1);
                        break;
                    case WORLD_HOP:

                        if (inArea(Rs2Player.getWorldLocation(), config.SHADES().Area)) {
                            sleep(61,97);
                            WorldPoint previousLocation = new WorldPoint(Rs2Player.getWorldLocation().getX(), Rs2Player.getWorldLocation().getY(), Rs2Player.getWorldLocation().getPlane());
                            Rs2Inventory.interact(config.teleportItemToBank(),config.teleportActionToBank());
                            if (this.isRunning()) { sleepUntil(() -> Rs2Player.getWorldLocation().distanceTo(previousLocation) > 10); }
                            //Rs2Player.waitForAnimation();
                            sleep(10000, 11200);
                        }
                        int nextWorld = Worlds.get((Worlds.indexOf(Microbot.getClient().getWorld())+1) % Worlds.size());
                        if (this.isRunning()) { Microbot.getClient().openWorldHopper(); }
                        if (this.isRunning()) { sleepUntil(() -> Rs2Widget.hasWidget("Current world - "+Rs2Player.getWorld())); }
                        sleep(61,97);
                        Microbot.hopToWorld(nextWorld);
                        if (this.isRunning()) { sleepUntil(() -> Rs2Widget.hasWidget("Current world - "+Rs2Player.getWorld())); }
                        sleep(61,97);
                        if(Microbot.getClient().getWorld()!=nextWorld) return;
                        teleportToBank = false;
                        teleportToShades = true;
                        state = State.BANKING;
                        break;
                    case USE_TELEPORT_TO_BANK:
                         if (teleportToBank){
                             WorldPoint previousLocation = new WorldPoint(Rs2Player.getWorldLocation().getX(), Rs2Player.getWorldLocation().getY(), Rs2Player.getWorldLocation().getPlane());
                             Rs2Inventory.interact(config.teleportItemToBank(),config.teleportActionToBank());
                             if (this.isRunning()) { sleepUntil(() -> Rs2Player.getWorldLocation().distanceTo(previousLocation) > 10); }
                             //Rs2Player.waitForAnimation();
                             teleportToBank = false;
                             teleportToShades = true;
                        } else {
                            state = State.BANKING;
                        }
                        break;
                    case FIGHT_SHADES:
                        if(!teleportToBank){
                            teleportToBank = true;
                        }
                        //TODO add config option to bank / abort fight if another player appears/ maybe world hop?
                        boolean isLooting;
                        boolean firstClick = true;
                        long lootingstart = System.currentTimeMillis();
                        long lootattempt = System.currentTimeMillis();
                        if(!Rs2Inventory.isFull()){
                            isLooting = getItemsInArea(true);
                        } else {
                            isLooting=false;
                        }
                        if(isLooting) {
                            int i = 90;
                            while(getItemsInArea(false) && (System.currentTimeMillis()-lootingstart)<6000) {
                                if (firstClick) {
                                    if((System.currentTimeMillis()-lootattempt)<random((i * 8), (i * 16))) {
                                        firstClick=false;
                                        lootattempt = System.currentTimeMillis();
                                        i = (int) (i * 1.5);
                                        getItemsInArea(true);
                                    }
                                } else {
                                    if((System.currentTimeMillis()-lootattempt)<random((i * 2), (i * 4))){
                                        i=(int)(i*1.5);
                                        lootattempt = System.currentTimeMillis();
                                        getItemsInArea(true);
                                    }
                                }
                                sleep(17, 63);
                            }
                            while((System.currentTimeMillis()-lootattempt)<random(153, 301)) {
                                sleep(20);
                            }
                            isLooting=false;
                        }
                        if(npc != null){
                            if(npc.isDead()){
                                int f = random(1,3);
                                if(f>1) {
                                    System.out.println("Sleeping while we wait for items");
                                    npc=null;
                                    boolean b = sleepUntilTrue(() -> getItemsInArea(false),100, 8000);
                                    System.out.println(b);
                                    System.out.println("Done sleeping for items");
                                    return;
                                }
                            }
                        }
                        if(Instant.now().compareTo(lastAnimating) >= 0
                                && !Rs2Combat.inCombat()
                                && !getItemsInArea(false)) {
                            //TODO use cached NPC index to store the npc so you can save NPCs for later
                            if (fetchNpc()) {
                                Rs2Npc.interact(npc, "Attack");
                                sleepUntil(() -> Rs2Combat.inCombat(), 1000);
                                sleep(200, 600);
                                lastAnimating = (Instant.now().plusMillis(random(20, 400)));
                            }
                        }
                        Rs2Combat.setSpecState(true, config.specialAttack() * 10);
                        if (Rs2Inventory.isFull()) {
                            if(!coffinFull) {
                                //Rs2Inventory.combine("remains", "coffin");
                                Rs2Inventory.interact("coffin", "fill");
                                sleepUntil(() -> !Rs2Inventory.hasItem("remains"), 3000);
                            }
                            coffinHasItems = true;
                            boolean isInventoryNoLongerFull = sleepUntilTrue(() -> !Rs2Inventory.isFull(), 100, 2000);
                            if (!isInventoryNoLongerFull) {
                                coffinFull=true;
                                if(Rs2Inventory.hasItem("Rotten food")){
                                    Rs2Inventory.drop("Rotten food");
                                    sleepUntil(() -> !Rs2Inventory.hasItem("Rotten food"), 3000);
                                    sleep(90, 250);
                                }
                                //TODO BUG HERE! check if coffin is empty first!
                                if(Rs2Inventory.hasItem(config.food().getName())) {
                                    eatAt(100);
                                    Rs2Player.waitForAnimation();
                                    sleep(40, 250);
                                    isInventoryNoLongerFull = sleepUntilTrue(() -> !Rs2Inventory.isFull(), 100, 2000);
                                }
                                if (!isInventoryNoLongerFull) {
                                    teleportToBank=true;
                                    state = USE_TELEPORT_TO_BANK;
                                }
                            }
                        }

                        break;
                }
                //long endTime = System.currentTimeMillis();
                //long totalTime = endTime - startTime;
                //System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
        return true;

    }
    public static boolean inArea(WorldPoint entity, Area area){
        return (entity.getX() >= area.ax && entity.getY() >= area.ay) && (entity.getX() <= area.bx && entity.getY() <= area.by);
    }
    public boolean getItemsInArea(boolean loot){
        if(temp.isEmpty()){ return false; }
        temp = temp.stream().sorted(Comparator
                        .comparingInt(value -> value.getTile().getLocalLocation()
                                .distanceTo(Microbot.getClient().getLocalPlayer().getLocalLocation())))
                .collect(Collectors.toList());
        if(loot) {
            final int invSize = Rs2Inventory.size();
            boolean iteminteracted = Rs2GroundItem.interact(temp.stream().findFirst().get());
            if(iteminteracted){
                boolean i = sleepUntilTrue(() -> invSize != Rs2Inventory.size(), 100, 7000);
                System.out.println(i);
                temp.clear();
            }
            return iteminteracted;
        } else {
            return true;
        }
    }
    //TODO make sure it doesn't steal/try to steal NPCs from other players~
    private boolean fetchNpc(){
        Microbot.status = "Searching for Shades";
        // First, check if there's an NPC attacking my player
        if(Microbot.getClient().getLocalPlayer().getInteracting()!=null
            && Microbot.getClient().getLocalPlayer().getInteracting().getWorldLocation().distanceTo(Rs2Player.getWorldLocation())<2
            && Rs2Npc.hasLineOfSight((NPC)Microbot.getClient().getLocalPlayer().getInteracting())){
            npc = (NPC) Microbot.getClient().getLocalPlayer().getInteracting();
            return true;
        }
        NPC attackingNpc = Microbot.getClient().getNpcs().stream()
                .filter(x -> x.getInteracting() == Microbot.getClient().getLocalPlayer()
                        && Objects.requireNonNull(x.getName()).contains(config.SHADES().displayName)
                        && !x.isDead())
                .findFirst()
                .orElse(null);
        // If there's an NPC attacking, return it
        if (attackingNpc != null) {
            npc = attackingNpc;
            return true;
        }
        npc = Microbot.getClient().getNpcs().stream().filter(x ->
                x.getWorldLocation().getX() >= config.SHADES().Area.ax && x.getWorldLocation().getY() >= config.SHADES().Area.ay
                && x.getWorldLocation().getX() <= config.SHADES().Area.bx && x.getWorldLocation().getY() <= config.SHADES().Area.by
                && Objects.requireNonNull(x.getName()).contains(config.SHADES().displayName) && !x.isDead()).min(Comparator.comparingInt(value ->
                value.getWorldLocation().distanceTo(Microbot.getClient().getLocalPlayer().getWorldLocation()))).orElse(null);
        return npc!=null;
    }
    @Override
    public void shutdown() {
        super.shutdown();
    }
}
