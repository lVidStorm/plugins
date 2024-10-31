package net.runelite.client.plugins.microbot.storm.plugins.runecrafting;

import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.globval.enums.InterfaceTab;
import net.runelite.client.plugins.microbot.storm.plugins.runecrafting.enums.State;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Item;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;

import javax.inject.Inject;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static net.runelite.client.plugins.microbot.storm.plugins.runecrafting.enums.State.*;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;
import static net.runelite.client.plugins.microbot.util.equipment.JewelleryLocationEnum.PVP_ARENA;
import static net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory.get;
import static net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory.items;
import static net.runelite.client.plugins.microbot.util.math.Random.random;

//TODO use test script to check distances for WALKING
//TODO needs better detection of teleport finished
//TODO need to reset values for things when script runs out of resources, unable to resume after running out of stamina potions
//TODO @@@@@@@@@ when banking have it destroy binding necklace when less than 3 charges left and using colossal pouch.
public class RunecraftingScript extends Script {
    public static double version = 1.0;
    @Inject
    private RunecraftingConfig config;
    private int sleepMin;
    private int sleepMax;
    private int sleepTarget;
    List<Integer> inventoryItems = new ArrayList<>();
    ItemContainer inventory;
    public static boolean bankIsOpen = false;
    static int staminaTimer = 0;
    static int imbueTimer = 0;
    static boolean hopscheduled;
    static boolean shouldstopscript;
    int inventoryEmptySpace;
    boolean firstpouchemptied=false;
    boolean smallpouchemptied=false, mediumpouchemptied=false, largepouchemptied=false, giantpouchemptied=false, colossalpouchemptied=false;

    public static State state = State.BANKING;

    public boolean run(RunecraftingConfig config) {
        Microbot.enableAutoRunOn = false;
        hopscheduled = false;
        List<Integer> Worlds = Arrays.stream(config.suitableWorlds().split(","))
                .map(String::trim)        // Trim each string
                .map(Integer::parseInt)   // Parse each string to an integer
                .collect(Collectors.toList());
        shouldstopscript = false;
        this.config = config; // Initialize the config object before accessing its parameters
        inventory = Microbot.getClientThread().runOnClientThread(() ->Microbot.getClient().getItemContainer(InventoryID.INVENTORY));
        bankIsOpen = Rs2Bank.isOpen();
        sleepMin = config.sleepMin();
        sleepMax = config.sleepMax();
        sleepTarget = config.sleepTarget();
        inventoryEmptySpace=0;
        staminaTimer=0;
        shouldstopscript=false;
        firstpouchemptied=false;
        smallpouchemptied=false; mediumpouchemptied=false; largepouchemptied=false; giantpouchemptied=false; colossalpouchemptied=false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                if(this.isRunning() && hasItems() && isCloseToFireAltar() && !isCloseToBank() && state!=WALKING) { state=WALKING; }
                //TODO change this so has method "isCloseToRuneAltar" instead so it doesn't get set off by being anywhere.
                if(this.isRunning() && hasItems() && !isCloseToFireAltar() && !isCloseToBank() && state!=RUNECRAFTING) { state=RUNECRAFTING; }
                if(this.isRunning() && !hasItems() && isCloseToBank() && state!=BANKING) { state=BANKING; }
                switch (state) {
                    case BANKING:
                        //TODO change isNearBank to something else, freezes on first use
                        if(isCloseToBank()) {
                            /*
                                for (Rs2Item item : Rs2Inventory.items()) {
                                    // Print item name and ID to the console
                                    System.out.println("Item: " + item.getName() + " | ID: " + item.getId());
                                }*/
                              //fetchAndMemorizeInventory();
                            if(config.hopWhenChatMessage() && hopscheduled) {
                                hopscheduled = false;
                                int nextWorld = Worlds.get((Worlds.indexOf(Microbot.getClient().getWorld())+1) % Worlds.size());
                                sleep(1000, 3200);
                                Microbot.hopToWorld(nextWorld);
                                sleep(10000, 11200);
                            }
                            if((Rs2Inventory.hasItem(5515) && config.useGiantPouch()) ||
                                    (Rs2Inventory.hasItem(5513) && config.useLargePouch()) ||
                                    (Rs2Inventory.hasItem(6819) && config.useLargePouch()) ||
                                    (Rs2Inventory.hasItem(5511) && config.useMediumPouch()) ||
                                    (Rs2Inventory.hasItem(26786) && config.useColossalPouch()) ||
                                    (Rs2Inventory.hasItem(26906) && config.useColossalPouch())
                            ){
                                if(this.isRunning()){ sleep(100,120); }
                                repairPouches();
                                return;
                            }
                            if(config.useColossalPouch()) {
                                System.out.println("Varbit value is : "+Microbot.getVarbitPlayerValue(487));
                                if (Microbot.getVarbitPlayerValue(487) < 3) {
                                    if(this.isRunning() && !Rs2Inventory.hasItem(5521)){ Rs2Equipment.unEquip(5521); }
                                    if(this.isRunning()) { Rs2Inventory.waitForInventoryChanges(200); }
                                    if(this.isRunning()){ sleep(61, 97); }
                                    while(this.isRunning() && Rs2Inventory.hasItem(5521)) {
                                        if (!this.isRunning()){ return; }
                                        if (this.isRunning()) { Rs2Inventory.interact(5521, "destroy"); }
                                        if (this.isRunning()) { sleepUntil(() -> Rs2Widget.hasWidget("Yes")); }
                                        if (this.isRunning()) { sleep(61, 97); }
                                        if (this.isRunning()) { Rs2Keyboard.keyPress(KeyEvent.VK_1); }
                                        if (this.isRunning()) { Rs2Inventory.waitForInventoryChanges(200); }
                                        if (this.isRunning()) { sleep(61, 97); }
                                    }
                                }

                            }
                            if (!hasItems()) {
                                while (!bankIsOpen && this.isRunning()) {
                                    openChest();
                                    if(this.isRunning()){ sleepUntil(() -> bankIsOpen, 4000); }
                                    if(this.isRunning()){ sleep(61, 97); }
                                }
                                createItemList();
                                if(this.isRunning()){ Rs2Bank.depositAll(x -> inventoryItems.stream().noneMatch(id -> id == x.id)); }
                                withdrawMissingItems();
                                closeBank();
                                if(this.isRunning()){ sleepUntil(() -> !bankIsOpen); }
                                if(this.isRunning()){ sleep(calculateSleepDuration()); }
                            }
                            if((Rs2Inventory.hasItem(5515) && config.useGiantPouch()) ||
                                    (Rs2Inventory.hasItem(5513) && config.useLargePouch()) ||
                                    (Rs2Inventory.hasItem(6819) && config.useLargePouch()) ||
                                    (Rs2Inventory.hasItem(5511) && config.useMediumPouch()) ||
                                    (Rs2Inventory.hasItem(26786) && config.useColossalPouch()) ||
                                    (Rs2Inventory.hasItem(26906) && config.useColossalPouch())
                            ){ return; }
                            if(this.isRunning() && Rs2Inventory.isFull()){
                            if(this.isRunning()){ Rs2Tab.switchToEquipmentTab(); }
                            if(this.isRunning()){ sleepUntil(() -> Rs2Tab.getCurrentTab() == InterfaceTab.EQUIPMENT); }
                            if(this.isRunning()){ sleep(calculateSleepDuration()); }
                            if(this.isRunning()){ Rs2Equipment.useRingAction(PVP_ARENA); }
                            if(this.isRunning()){ sleepUntil(this::isCloseToFireAltar, 10000); }
                            if(this.isRunning()){ Rs2Tab.switchToInventoryTab(); }
                            if(this.isRunning()){ sleepUntil(() -> Rs2Tab.getCurrentTab() == InterfaceTab.INVENTORY); }
                            if(this.isRunning()){ sleep(calculateSleepDuration()); }
                            }
                            if (isCloseToFireAltar()) {
                                state = WALKING;
                            }
                        } else {
                            System.out.println("supposed to be banking, not detecting bank");
                            if (isCloseToFireAltar()) {
                                state = WALKING;
                            }
                            sleep(1000);
                        }
                        break;
                    case WALKING:
                        //TODO something here is breaking, needs to check area?
                        //TODO change which walker is used, don't want it continuously trying to pathfind
                        if(this.isRunning() && isCloseToFireAltar()) {
                            //if (this.isRunning() && Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(new WorldPoint(3312, 3253, 0)) > 3) {
                            //Rs2Walker.walkTo(3312, 3253, Rs2Player.getWorldLocation().getPlane(), 2);
                            //sleep(calculateSleepDuration()); }
                            //if (this.isRunning()) { sleepUntil(() -> Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(new WorldPoint(3312, 3253, 0)) < 8 || !Rs2Player.isMoving(), 3000); }
                            //if (this.isRunning()) { sleep(calculateSleepDuration()); }
                            //TODO change this to click on a tile somewhere since the altar might not be loaded on the player's screen
                            if(this.isRunning() && !Rs2Player.isMoving()) {
                                int i = 0;
                                while (this.isRunning() && !Rs2Player.isMoving() && i<2) {
                                    if (this.isRunning() && !Rs2Player.isMoving()) { Rs2GameObject.interact(34817, "Enter"); }
                                    if (this.isRunning() && !Rs2Player.isMoving()) { sleep(calculateSleepDuration()); }
                                    if (this.isRunning() && !Rs2Player.isMoving()) { sleep(calculateSleepDuration()); }
                                    if (this.isRunning() && !Rs2Player.isMoving()) { sleep(calculateSleepDuration()); }
                                    if (this.isRunning() && !Rs2Player.isMoving()) { sleep(calculateSleepDuration()); }
                                    i++;
                                }
                                if(this.isRunning() && !Rs2Player.isMoving()) {
                                    Rs2Walker.walkFastCanvas(new WorldPoint(3308,3244 , Rs2Player.getWorldLocation().getPlane()));
                                }
                            }

                            if (this.isRunning()) { sleepUntil(() -> Rs2Player.isMoving(), 600); }
                            //if(Rs2Player.isMoving()) { sleepUntil(() -> Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(new WorldPoint(3312, 3253, 0)) < 6, 8000); }
                            if(Rs2Player.isMoving()) { boolean i = sleepUntilTrue(() -> Rs2Player.getWorldLocation().getY() > 3244,100, 10000); }
                            if (this.isRunning()) { sleep(calculateSleepDuration()); }
                            while(this.isRunning() && isCloseToFireAltar()) {
                                if (this.isRunning()) { Rs2GameObject.interact(34817, "Enter"); }
                                if (this.isRunning()) { sleepUntil(() -> Rs2Player.isMoving(), 200); }
                                if(Rs2Player.isMoving()) { if (this.isRunning()) { sleepUntil(() -> !isCloseToFireAltar(), 10000); } }
                                else { if (this.isRunning()) { sleep(calculateSleepDuration()); } }
                            }
                            //myInteract();
                            if (this.isRunning()) { sleep(calculateSleepDuration()); }
                            //if (this.isRunning()) { sleepUntil(() -> !isCloseToFireAltar(), 3000); }
                            //TODO change this to IS in altar area
                        }
                        if(!isCloseToFireAltar()){
                            state = RUNECRAFTING;
                        }
                        break;
                    case RUNECRAFTING:
                        //TODO something breaking, we get stuck spamming altar
                        if(this.isRunning() && !isCloseToBank() && (Rs2Inventory.hasItem(7936) || Rs2Inventory.hasItem(24704))) {
                            if (this.isRunning()) { Rs2Inventory.useItemOnObject(config.selectRuneToMake().getPrimaryRequiredRune(), config.selectAlter().getAlterID()); }
                            if (this.isRunning()) { sleep(calculateSleepDuration()); }
                            if (Rs2Tab.getCurrentTab() != InterfaceTab.MAGIC) { if (this.isRunning()) {
                                    Rs2Tab.switchToMagicTab();
                                    sleepUntil(() -> Rs2Tab.getCurrentTab() == InterfaceTab.MAGIC);
                                    sleep(calculateSleepDuration()); }
                            }
                            if (this.isRunning()) { Rs2Magic.cast(MagicAction.MAGIC_IMBUE); }
                            if (this.isRunning()) { sleep(calculateSleepDuration()); }
                            if (Rs2Tab.getCurrentTab() != InterfaceTab.INVENTORY) { if (this.isRunning()) {
                                    Rs2Tab.switchToInventoryTab();
                                    sleepUntil(() -> Rs2Tab.getCurrentTab() == InterfaceTab.INVENTORY);
                                    sleep(calculateSleepDuration()); }
                            }
                            if (this.isRunning()) { sleepUntil(() -> !Rs2Inventory.hasItem(config.selectRuneToMake().getEssenceTypeRequired())); }
                            if (this.isRunning()) { sleep(calculateSleepDuration()); }
                            //TODO MOTHERFUCKER
                            //TODO emptyPouches() method is getting stuck?
                            while (this.isRunning() && emptyPouches()) {
                                if (this.isRunning()) { sleepUntil(() -> Rs2Inventory.hasItem(config.selectRuneToMake().getEssenceTypeRequired()), 600); }
                                if (this.isRunning() && imbueTimer == 0) { Rs2Magic.cast(MagicAction.MAGIC_IMBUE); sleep(calculateSleepDuration()); }
                                if (this.isRunning()) { sleep(calculateSleepDuration()); }
                                if (this.isRunning() && (!Rs2Inventory.hasItem(7936) && !Rs2Inventory.hasItem(24704))) {
                                    break;
                                }
                                if (this.isRunning()) { System.out.println("dat fucker"); }
                                if (this.isRunning()) { Rs2Inventory.useItemOnObject(config.selectRuneToMake().getPrimaryRequiredRune(), config.selectAlter().getAlterID()); }
                                if (this.isRunning()) { sleep(calculateSleepDuration()); }
                                if (this.isRunning()) { sleepUntil(() -> (!Rs2Inventory.hasItem(7936) && !Rs2Inventory.hasItem(24704)), 600); }
                                if (this.isRunning()) { sleep(5, 35); }
                            }
                            smallpouchemptied = false;
                            mediumpouchemptied = false;
                            largepouchemptied = false;
                            giantpouchemptied = false;
                            colossalpouchemptied = false;
                            if (this.isRunning() && (Rs2Inventory.hasItem(7936) || Rs2Inventory.hasItem(24704))) {
                                if (this.isRunning()) { System.out.println("dis fucker"); }
                                if (this.isRunning()) { Rs2Inventory.useItemOnObject(config.selectRuneToMake().getPrimaryRequiredRune(), config.selectAlter().getAlterID()); }
                                if (this.isRunning()) { sleep(calculateSleepDuration()); }
                                if (this.isRunning()) { sleepUntil(() -> (!Rs2Inventory.hasItem(7936) && !Rs2Inventory.hasItem(24704)), 600); }
                                if (this.isRunning()) { sleep(5, 35); }
                            }
                            //TODO motherfucker
                        }
                        if(this.isRunning() && !isCloseToBank()){
                            teletoCraftingGuild();
                        }
                        //if(this.isRunning()){ Rs2Player.waitForAnimation(); }
                        if(this.isRunning()){ sleepUntil(() -> isCloseToBank(), 10000); }
                        if(this.isRunning()){ sleep(calculateSleepDuration()); }
                        if(this.isRunning()){ Rs2Tab.switchToInventoryTab(); }
                        if(this.isRunning()){ sleep(calculateSleepDuration()); }
                        if(Rs2Bank.isNearBank(10)){
                            state = BANKING;
                        }
                        break;
                    case REPAIRING:

                        break;
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 10, TimeUnit.MILLISECONDS);
        return true;
    }

    //TODO methods past here ~
    private boolean hasItems() {
        // Check if the player has the required items
        return (
                ((config.useSmallPouch() && (Rs2Inventory.hasItem(5509))) || (!config.useSmallPouch() && (!Rs2Inventory.hasItem(5509)))) &&
                        ((config.useMediumPouch() && (Rs2Inventory.hasItem(5511) || Rs2Inventory.hasItem(5510))) || (!config.useMediumPouch() && (!Rs2Inventory.hasItem(5511) || !Rs2Inventory.hasItem(5510)))) &&
                        ((config.useLargePouch() && (Rs2Inventory.hasItem(5513) || Rs2Inventory.hasItem(6819))) || (!config.useLargePouch() && (!Rs2Inventory.hasItem(5513) || !Rs2Inventory.hasItem(6819)))) &&
                        ((config.useGiantPouch() && (Rs2Inventory.hasItem(5515) || Rs2Inventory.hasItem(5514))) || (!config.useGiantPouch() && (!Rs2Inventory.hasItem(5515) || !Rs2Inventory.hasItem(5514)))) &&
                        ((config.useColossalPouch() && (Rs2Inventory.hasItem(26784) || Rs2Inventory.hasItem(26786) || Rs2Inventory.hasItem(26906))) || (!config.useColossalPouch() && (!Rs2Inventory.hasItem(26786) || !Rs2Inventory.hasItem(26906)))) &&
                        //rune pouches
                        (Rs2Inventory.hasItem(12791) || Rs2Inventory.hasItem(23650) || Rs2Inventory.hasItem(24416)) &&
                        //pure/daeyalt essence
                        (Rs2Inventory.hasItem(7936) || Rs2Inventory.hasItem(24704)) &&
                        //earth runes
                        Rs2Inventory.hasItem(config.selectRuneToMake().getPrimaryRequiredRune()) &&
                        //crafting cape trimmed/untrimmed
                        (Rs2Equipment.hasEquipped(9780) || Rs2Equipment.hasEquipped(9781)) &&
                        //binding necklace
                        Rs2Equipment.hasEquipped(5521) &&
                        //ring of dueling
                        (Rs2Equipment.hasEquipped(2552) || Rs2Equipment.hasEquipped(2554) || Rs2Equipment.hasEquipped(2556) || Rs2Equipment.hasEquipped(2558) || Rs2Equipment.hasEquipped(2560) || Rs2Equipment.hasEquipped(2562) || Rs2Equipment.hasEquipped(2564) || Rs2Equipment.hasEquipped(2566)) &&
                        //fire tiara
                        Rs2Equipment.hasEquipped(5537) && Rs2Inventory.isFull()
        );
    }

    public void createItemList() {
        if(this.isRunning()) {
            if (!inventoryItems.isEmpty()) {
                inventoryItems.clear();
            }
            if (config.useSmallPouch()) {
                if (Rs2Inventory.hasItem(5509)) {
                    inventoryItems.add(5509);
                }
            }
            if (config.useMediumPouch()) {
                if (Rs2Inventory.hasItem(5511)) {
                    inventoryItems.add(5511);
                }
                if (Rs2Inventory.hasItem(5510)) {
                    inventoryItems.add(5510);
                }
            }
            if (config.useLargePouch()) {
                if (Rs2Inventory.hasItem(5512)) {
                    inventoryItems.add(5512);
                }
                if (Rs2Inventory.hasItem(5513)) {
                    inventoryItems.add(5513);
                }
                if (Rs2Inventory.hasItem(6819)) {
                    inventoryItems.add(6819);
                }
            }
            if (config.useGiantPouch()) {
                if (Rs2Inventory.hasItem(5515)) {
                    inventoryItems.add(5515);
                }
                if (Rs2Inventory.hasItem(5514)) {
                    inventoryItems.add(5514);
                }
            }
            if (config.useColossalPouch()) {
                if (Rs2Inventory.hasItem(26784)) {
                    inventoryItems.add(26784);
                }
                if (Rs2Inventory.hasItem(26786)) {
                    inventoryItems.add(26786);
                }
                if (Rs2Inventory.hasItem(26906)) {
                    inventoryItems.add(26906);
                }
            }
            //rune pouches
            if (Rs2Inventory.hasItem(12791)) {
                inventoryItems.add(12791);
            }
            if (Rs2Inventory.hasItem(23650)) {
                inventoryItems.add(23650);
            }
            if (Rs2Inventory.hasItem(24416)) {
                inventoryItems.add(24416);
            }
            //earth runes
            if (Rs2Inventory.hasItem(config.selectRuneToMake().getPrimaryRequiredRune())) {
                inventoryItems.add(config.selectRuneToMake().getPrimaryRequiredRune());
            }
            //pure essence
            if(Rs2Inventory.hasItem(7936)) { inventoryItems.add(7936); }
            //daeyalt essence
            if(Rs2Inventory.hasItem(24704)) { inventoryItems.add(24704); }
        }
    }

    public void withdrawMissingItems() {
        if(this.isRunning() && Rs2Bank.hasItem("Stamina potion")) {
            if (this.isRunning() && staminaTimer == 0 && Microbot.getClient().getEnergy() < 7500 && !Rs2Inventory.isFull()) {
                if (this.isRunning()) { Rs2Bank.withdrawOne("Stamina potion"); }
                if (this.isRunning()) { sleepUntil(() -> Rs2Inventory.hasItem("Stamina potion")); }
                if (this.isRunning()) { sleep(calculateSleepDuration()); }
                if (this.isRunning()) { Rs2Inventory.interact("Stamina potion", "drink"); }
                if (this.isRunning()) { sleepUntil(() -> staminaTimer > 0); }
                if (this.isRunning()) { sleep(calculateSleepDuration()); }
                if (this.isRunning() && (Rs2Inventory.hasItem("Stamina potion") || Rs2Inventory.hasItem(229))) {
                    if (this.isRunning() && Rs2Inventory.hasItem("Stamina potion")) {
                        Rs2Bank.depositOne("Stamina potion");
                        sleepUntil(() -> !Rs2Inventory.hasItem("Stamina"));
                    } else { Rs2Bank.depositOne(229); sleepUntil(() -> !Rs2Inventory.hasItem(229)); }
                    if (this.isRunning()) { sleep(calculateSleepDuration()); }
                }
            }
        } else { shouldstopscript=true; }
        if(this.isRunning() && shouldstopscript){ do{ Microbot.showMessage("You are missing items"); sleepUntil(() -> Rs2Inventory.isFull(), 99999); if (Rs2Inventory.isFull()) { shouldstopscript=false; } } while(this.isRunning() && shouldstopscript); }
        if (config.useSmallPouch() && !Rs2Inventory.isFull()) {
            if (!Rs2Inventory.hasItem(5509)) {
                if(this.isRunning()){ Rs2Bank.withdrawItem(5509); }
                if(this.isRunning()){ sleepUntil(() -> Rs2Inventory.hasItem(5509)); }
                if(this.isRunning()){ sleep(calculateSleepDuration()); }
            }
        }
        if (config.useMediumPouch() && !Rs2Inventory.isFull()) {
            if (Rs2Bank.hasItem(5511) && !Rs2Inventory.hasItem(5511)) {
                if(this.isRunning()){ Rs2Bank.withdrawItem(5511); }
                if(this.isRunning()){ sleepUntil(() -> Rs2Inventory.hasItem(5511)); }
                if(this.isRunning()){ sleep(calculateSleepDuration()); }
            }
            if (Rs2Bank.hasItem(5510) && !Rs2Inventory.hasItem(5510)) {
                if(this.isRunning()){ Rs2Bank.withdrawItem(5510); }
                if(this.isRunning()){ sleepUntil(() -> Rs2Inventory.hasItem(5510)); }
                if(this.isRunning()){ sleep(calculateSleepDuration()); }
            }
        }
        if (config.useLargePouch() && !Rs2Inventory.isFull()) {
            if (Rs2Bank.hasItem(5512) && !Rs2Inventory.hasItem(5512)) {
                if(this.isRunning()){ Rs2Bank.withdrawItem(5512); }
                if(this.isRunning()){ sleepUntil(() -> Rs2Inventory.hasItem(5512)); }
                if(this.isRunning()){ sleep(calculateSleepDuration()); }
            }
            if (Rs2Bank.hasItem(5513) && !Rs2Inventory.hasItem(5513)) {
                if(this.isRunning()){ Rs2Bank.withdrawItem(5513); }
                if(this.isRunning()){ sleepUntil(() -> Rs2Inventory.hasItem(5513)); }
                if(this.isRunning()){ sleep(calculateSleepDuration()); }
            }
            if (Rs2Bank.hasItem(6819) && !Rs2Inventory.hasItem(6819)) {
                if(this.isRunning()){ Rs2Bank.withdrawItem(6819); }
                if(this.isRunning()){ sleepUntil(() -> Rs2Inventory.hasItem(6819)); }
                if(this.isRunning()){ sleep(calculateSleepDuration()); }
            }
        }
        if (config.useGiantPouch() && !Rs2Inventory.isFull()) {
            if (Rs2Bank.hasItem(5515) && !Rs2Inventory.hasItem(5515)) {
                if(this.isRunning()){ Rs2Bank.withdrawItem(5515); }
                if(this.isRunning()){ sleepUntil(() -> Rs2Inventory.hasItem(5515)); }
                if(this.isRunning()){ sleep(calculateSleepDuration()); }
            }
            if (Rs2Bank.hasItem(5514) && !Rs2Inventory.hasItem(5514)) {
                if(this.isRunning()){ Rs2Bank.withdrawItem(5514); }
                if(this.isRunning()){ sleepUntil(() -> Rs2Inventory.hasItem(5514)); }
                if(this.isRunning()){ sleep(calculateSleepDuration()); }
            }
        }
        if (config.useColossalPouch() && !Rs2Inventory.isFull()) {
            if (Rs2Bank.hasItem(26784) && !Rs2Inventory.hasItem(26784)) {
                if(this.isRunning()){ Rs2Bank.withdrawItem(26784); }
                if(this.isRunning()){ sleepUntil(() -> Rs2Inventory.hasItem(26784)); }
                if(this.isRunning()){ sleep(calculateSleepDuration()); }
            }
            if (Rs2Bank.hasItem(26786) && !Rs2Inventory.hasItem(26786)) {
                if(this.isRunning()){ Rs2Bank.withdrawItem(26786); }
                if(this.isRunning()){ sleepUntil(() -> Rs2Inventory.hasItem(26786)); }
                if(this.isRunning()){ sleep(calculateSleepDuration()); }
            }
            if (Rs2Bank.hasItem(26906) && !Rs2Inventory.hasItem(26906)) {
                if(this.isRunning()){ Rs2Bank.withdrawItem(26906); }
                if(this.isRunning()){ sleepUntil(() -> Rs2Inventory.hasItem(26906)); }
                if(this.isRunning()){ sleep(calculateSleepDuration()); }
            }
        }
        //TODO make something to check rune counts in rune pouches? so spells dont fail to cast
        //rune pouches
        if (Rs2Bank.hasItem(12791) && !Rs2Inventory.hasItem(12791) && !Rs2Inventory.isFull()) {
            if(this.isRunning()){ Rs2Bank.withdrawItem(12791); }
            if(this.isRunning()){ sleepUntil(() -> Rs2Inventory.hasItem(12791)); }
            if(this.isRunning()){ sleep(calculateSleepDuration()); }
        }
        if (Rs2Bank.hasItem(23650) && !Rs2Inventory.hasItem(23650) && !Rs2Inventory.isFull()) {
            if(this.isRunning()){ Rs2Bank.withdrawItem(23650); }
            if(this.isRunning()){ sleepUntil(() -> Rs2Inventory.hasItem(23650)); }
            if(this.isRunning()){ sleep(calculateSleepDuration()); }
        }
        if (Rs2Bank.hasItem(24416) && !Rs2Inventory.hasItem(24416) && !Rs2Inventory.isFull()) {
            if(this.isRunning()){ Rs2Bank.withdrawItem(24416); }
            if(this.isRunning()){ sleepUntil(() -> Rs2Inventory.hasItem(24416)); }
            if(this.isRunning()){ sleep(calculateSleepDuration()); }
        }
        //TODO change above to check config for version / stop when one is in inventory.
        //crafting cape
        if (Rs2Bank.hasItem(9780) && !Rs2Equipment.hasEquipped(9780) && !Rs2Inventory.hasItem(9780) && !Rs2Inventory.isFull()) {
            if(this.isRunning()){ Rs2Bank.withdrawAllAndEquip(9780); }
            if(this.isRunning()){ sleepUntil(() -> Rs2Equipment.hasEquipped(9780)); }
            if(this.isRunning()){ sleep(calculateSleepDuration()); }
        }
        //crafting cape
        if (Rs2Bank.hasItem(9781) && !Rs2Equipment.hasEquipped(9781) && !Rs2Inventory.hasItem(9781) && !Rs2Inventory.isFull()) {
            if(this.isRunning()){ Rs2Bank.withdrawAllAndEquip(9781); }
            if(this.isRunning()){ sleepUntil(() -> Rs2Equipment.hasEquipped(9781)); }
            if(this.isRunning()){ sleep(calculateSleepDuration()); }
        }
        //binding necklace
        if (Rs2Bank.hasItem(5521) && !Rs2Equipment.hasEquipped(5521) && !Rs2Inventory.hasItem(5521) && !Rs2Inventory.isFull()) {
            if(this.isRunning()){ Rs2Bank.withdrawXAndEquip(5521, 1); }
            if(this.isRunning()){ sleepUntil(() -> Rs2Equipment.hasEquipped(5521)); }
            if(this.isRunning()){ sleep(calculateSleepDuration()); }
        } else if (!Rs2Bank.hasItem(5521) && !Rs2Equipment.hasEquipped(5521) && !Rs2Inventory.hasItem(5521)) { shouldstopscript=true; }
        if(this.isRunning() && shouldstopscript){ do{ Microbot.showMessage("You are missing resources"); sleepUntil(() -> Rs2Inventory.isFull(), 99999); if (Rs2Inventory.isFull()) { shouldstopscript=false; } } while(this.isRunning() && shouldstopscript); }
        //ring of dueling
        /*
        if (this.isRunning() && Rs2Bank.hasItem(2552) && (
                !Rs2Equipment.hasEquipped(2552) ||
                        !Rs2Equipment.hasEquipped(2554) ||
                        !Rs2Equipment.hasEquipped(2556) ||
                        !Rs2Equipment.hasEquipped(2558) ||
                        !Rs2Equipment.hasEquipped(2560) ||
                        !Rs2Equipment.hasEquipped(2562) ||
                        !Rs2Equipment.hasEquipped(2564)) && !Rs2Inventory.hasItem(2552)) {
            if(this.isRunning()){ Rs2Bank.withdrawXAndEquip(2552, 1); }
            if(this.isRunning()){ sleepUntil(() -> Rs2Equipment.hasEquipped(2552)); }
            if(this.isRunning()){ sleep(calculateSleepDuration()); }
        }*/
        if (this.isRunning() && Rs2Bank.hasItem(2552) && (
                !Rs2Equipment.hasEquippedContains("Ring of Dueling")) && !Rs2Inventory.hasItem(2552) && !Rs2Inventory.isFull()) {
            if(this.isRunning()){ Rs2Bank.withdrawXAndEquip(2552, 1); }
            if(this.isRunning()){ sleepUntil(() -> Rs2Equipment.hasEquipped(2552)); }
            if(this.isRunning()){ sleep(calculateSleepDuration()); }
        } else if (!Rs2Bank.hasItem("Ring of Dueling") && !Rs2Equipment.hasEquippedContains("Ring of Dueling") && !Rs2Inventory.hasItem("Ring of Dueling")) { shouldstopscript=true; }
        if(this.isRunning() && shouldstopscript){ do{ Microbot.showMessage("You are missing resources"); sleepUntil(() -> Rs2Inventory.isFull(), 99999); if (Rs2Inventory.isFull()) { shouldstopscript=false; } } while(this.isRunning() && shouldstopscript); }
        //fire tiara
        if (Rs2Bank.hasItem(5537) && !Rs2Equipment.hasEquipped(5537) && !Rs2Inventory.hasItem(5537) && !Rs2Inventory.isFull()) {
            if(this.isRunning()){ Rs2Bank.withdrawXAndEquip(5537, 1); }
            if(this.isRunning()){ sleepUntil(() -> Rs2Equipment.hasEquipped(5537)); }
            if(this.isRunning()){ sleep(calculateSleepDuration()); }
        } else if (!Rs2Bank.hasItem(5537) && !Rs2Equipment.hasEquipped(5537) && !Rs2Inventory.hasItem(5537)) { shouldstopscript=true; }
        if(this.isRunning() && shouldstopscript){ do{ Microbot.showMessage("You are missing resources"); sleepUntil(() -> Rs2Inventory.isFull(), 99999); if (Rs2Inventory.isFull()) { shouldstopscript=false; } } while(this.isRunning() && shouldstopscript); }
        //earth runes
        if (Rs2Bank.hasItem(config.selectRuneToMake().getPrimaryRequiredRune()) && !Rs2Inventory.hasItem(config.selectRuneToMake().getPrimaryRequiredRune()) && !Rs2Inventory.isFull()) {
            if(this.isRunning()){ Rs2Bank.withdrawAll(config.selectRuneToMake().getPrimaryRequiredRune()); }
            if(this.isRunning()){ sleepUntil(() -> Rs2Inventory.hasItem(config.selectRuneToMake().getPrimaryRequiredRune())); }
            if(this.isRunning()){ sleep(calculateSleepDuration()); }
            if(this.isRunning()){ Rs2Bank.depositOne(config.selectRuneToMake().getPrimaryRequiredRune()); }
            if(this.isRunning()){ sleep(calculateSleepDuration()); }
        } else if (!Rs2Bank.hasItem(config.selectRuneToMake().getPrimaryRequiredRune()) && !Rs2Equipment.hasEquipped(config.selectRuneToMake().getPrimaryRequiredRune()) && !Rs2Inventory.hasItem(config.selectRuneToMake().getPrimaryRequiredRune())) { shouldstopscript=true; }
        if(this.isRunning() && shouldstopscript){ do{ Microbot.showMessage("You are missing resources"); sleepUntil(() -> Rs2Inventory.isFull(), 99999); if (Rs2Inventory.isFull()) { shouldstopscript=false; } } while(this.isRunning() && shouldstopscript); }
        //pure essence
        if (Rs2Bank.hasItem(7936) && !Rs2Inventory.isFull()) {
            if (this.isRunning()) { Rs2Bank.withdrawAll(7936); }
            if (this.isRunning()) { sleepUntil(() -> Rs2Inventory.hasItem(7936) && Rs2Inventory.isFull()); }
            if (this.isRunning()) { sleep(calculateSleepDuration()); }
        }
        //daeyalt essence
        if (Rs2Bank.hasItem(24704) && !Rs2Inventory.isFull()) {
            if (this.isRunning()) { Rs2Bank.withdrawAll(24704); }
            if (this.isRunning()) { sleepUntil(() -> Rs2Inventory.hasItem(24704) && Rs2Inventory.isFull()); }
            if (this.isRunning()) { sleep(calculateSleepDuration()); }
        }
        if(this.isRunning() && !Rs2Inventory.isFull()) { shouldstopscript=true; }
        if(this.isRunning() && shouldstopscript){ do{ Microbot.showMessage("You are missing resources"); sleepUntil(() -> Rs2Inventory.isFull(), 99999); if (Rs2Inventory.isFull()) { shouldstopscript=false; } } while(this.isRunning() && shouldstopscript); }
        boolean pouchesfilled=false;
        boolean firstpouchfilled=false;
        boolean smallpouchfilled=false, mediumpouchfilled=false, largepouchfilled=false, giantpouchfilled=false, colossalpouchfilled=false;
        //TODO wait for empty inventory space to be less than it was, and have each pouch attempt to fill on first pass
        //TODO needs to check for rune amounts and only withdraw if not enough

        while(this.isRunning() && !pouchesfilled) {
            if(!Rs2Bank.isOpen()){ openChest(); sleep(calculateSleepDuration()); }
            if(this.isRunning() && (!Rs2Inventory.hasItem(7936) && !Rs2Inventory.hasItem(24704))) { if (Rs2Bank.hasItem(7936) && !Rs2Inventory.isFull()) { if (this.isRunning()) { Rs2Bank.withdrawAll(7936); } if (this.isRunning()) { sleepUntil(() -> Rs2Inventory.hasItem(7936) && Rs2Inventory.isFull()); } if (this.isRunning()) { sleep(calculateSleepDuration()); } } if (Rs2Bank.hasItem(24704) && !Rs2Inventory.isFull()) { if (this.isRunning()) { Rs2Bank.withdrawAll(24704); } if (this.isRunning()) { sleepUntil(() -> Rs2Inventory.hasItem(24704) && Rs2Inventory.isFull()); } if (this.isRunning()) { sleep(calculateSleepDuration()); } } }

            if(this.isRunning() && Rs2Inventory.hasItem(26784) && !colossalpouchfilled && (firstpouchfilled || Rs2Inventory.isFull()) && (Rs2Inventory.hasItem(7936) || Rs2Inventory.hasItem(24704))){
                if (this.isRunning()) { Rs2Inventory.interact(26784, "fill"); }
                if (this.isRunning() && !firstpouchfilled) { firstpouchfilled=true; }
                if(this.isRunning()) { Rs2Inventory.waitForInventoryChanges(200); }
                if (this.isRunning()) { sleep(calculateSleepDuration()); }
                if (this.isRunning() && (Rs2Inventory.hasItem(7936)||Rs2Inventory.hasItem(24704))) { colossalpouchfilled=true; }
            }
            if(this.isRunning() && Rs2Inventory.hasItem(26786) && !colossalpouchfilled && (firstpouchfilled || Rs2Inventory.isFull()) && (Rs2Inventory.hasItem(7936) || Rs2Inventory.hasItem(24704))){
                if (this.isRunning()) { Rs2Inventory.interact(26786, "fill"); }
                if (this.isRunning() && !firstpouchfilled) { firstpouchfilled=true; }
                if(this.isRunning()) { Rs2Inventory.waitForInventoryChanges(200); }
                if (this.isRunning()) { sleep(calculateSleepDuration()); }
                if (this.isRunning() && (Rs2Inventory.hasItem(7936)||Rs2Inventory.hasItem(24704))) { colossalpouchfilled=true; }
            }
            if(this.isRunning() && Rs2Inventory.hasItem(26906) && !colossalpouchfilled && (firstpouchfilled || Rs2Inventory.isFull()) && (Rs2Inventory.hasItem(7936) || Rs2Inventory.hasItem(24704))){
                if (this.isRunning()) { Rs2Inventory.interact(26906, "fill"); }
                if (this.isRunning() && !firstpouchfilled) { firstpouchfilled=true; }
                if(this.isRunning()) { Rs2Inventory.waitForInventoryChanges(200); }
                if (this.isRunning()) { sleep(calculateSleepDuration()); }
                if (this.isRunning() && (Rs2Inventory.hasItem(7936)||Rs2Inventory.hasItem(24704))) { colossalpouchfilled=true; }
            }
            if(this.isRunning() && Rs2Inventory.hasItem(5514) && !giantpouchfilled && (firstpouchfilled || Rs2Inventory.isFull()) && (Rs2Inventory.hasItem(7936) || Rs2Inventory.hasItem(24704))){
                checkRequiredEssence(12);
                if (this.isRunning()) { Rs2Inventory.interact(5514, "fill"); }
                if (this.isRunning() && !firstpouchfilled) { firstpouchfilled=true; }
                if(this.isRunning()) { Rs2Inventory.waitForInventoryChanges(200); }
                if (this.isRunning()) { sleep(calculateSleepDuration()); }
                if (this.isRunning() && (Rs2Inventory.hasItem(7936)||Rs2Inventory.hasItem(24704))) { giantpouchfilled=true; }
            }
            if(this.isRunning() && Rs2Inventory.hasItem(5515) && !giantpouchfilled && (firstpouchfilled || Rs2Inventory.isFull()) && (Rs2Inventory.hasItem(7936) || Rs2Inventory.hasItem(24704))){
                checkRequiredEssence(12);
                if (this.isRunning()) { Rs2Inventory.interact(5515, "fill"); }
                if (this.isRunning() && !firstpouchfilled) { firstpouchfilled=true; }
                if(this.isRunning()) { Rs2Inventory.waitForInventoryChanges(200); }
                if (this.isRunning()) { sleep(calculateSleepDuration()); }
                if (this.isRunning() && (Rs2Inventory.hasItem(7936)||Rs2Inventory.hasItem(24704))) { giantpouchfilled=true; }
            }
            if(this.isRunning() && Rs2Inventory.hasItem(5512) && !largepouchfilled && (firstpouchfilled || Rs2Inventory.isFull()) && (Rs2Inventory.hasItem(7936) || Rs2Inventory.hasItem(24704))){
                checkRequiredEssence(9);
                if (this.isRunning()) { Rs2Inventory.interact(5512, "fill"); }
                if (this.isRunning() && !firstpouchfilled) { firstpouchfilled=true; }
                if(this.isRunning()) { Rs2Inventory.waitForInventoryChanges(200); }
                if (this.isRunning()) { sleep(calculateSleepDuration()); }
                if (this.isRunning() && (Rs2Inventory.hasItem(7936)||Rs2Inventory.hasItem(24704))) { largepouchfilled=true; }
            }
            if(this.isRunning() && Rs2Inventory.hasItem(5513) && !largepouchfilled && (firstpouchfilled || Rs2Inventory.isFull()) && (Rs2Inventory.hasItem(7936) || Rs2Inventory.hasItem(24704))){
                checkRequiredEssence(9);
                if (this.isRunning()) { Rs2Inventory.interact(5513, "fill"); }
                if (this.isRunning() && !firstpouchfilled) { firstpouchfilled=true; }
                if(this.isRunning()) { Rs2Inventory.waitForInventoryChanges(200); }
                if (this.isRunning()) { sleep(calculateSleepDuration()); }
                if (this.isRunning() && (Rs2Inventory.hasItem(7936)||Rs2Inventory.hasItem(24704))) { largepouchfilled=true; }
            }
            if(this.isRunning() && Rs2Inventory.hasItem(6819) && !largepouchfilled && (firstpouchfilled || Rs2Inventory.isFull()) && (Rs2Inventory.hasItem(7936) || Rs2Inventory.hasItem(24704))){
                checkRequiredEssence(9);
                if (this.isRunning()) { Rs2Inventory.interact(6819, "fill"); }
                if (this.isRunning() && !firstpouchfilled) { firstpouchfilled=true; }
                if(this.isRunning()) { Rs2Inventory.waitForInventoryChanges(200); }
                if (this.isRunning()) { sleep(calculateSleepDuration()); }
                if (this.isRunning() && (Rs2Inventory.hasItem(7936)||Rs2Inventory.hasItem(24704))) { largepouchfilled=true; }
            }
            if(this.isRunning() && Rs2Inventory.hasItem(5510) && !mediumpouchfilled && (firstpouchfilled || Rs2Inventory.isFull()) && (Rs2Inventory.hasItem(7936) || Rs2Inventory.hasItem(24704))){
                checkRequiredEssence(6);
                if (this.isRunning()) { Rs2Inventory.interact(5510, "fill"); }
                if (this.isRunning() && !firstpouchfilled) { firstpouchfilled=true; }
                if(this.isRunning()) { Rs2Inventory.waitForInventoryChanges(200); }
                if (this.isRunning()) { sleep(calculateSleepDuration()); }
                if (this.isRunning() && (Rs2Inventory.hasItem(7936)||Rs2Inventory.hasItem(24704))) { mediumpouchfilled=true; }
            }
            if(this.isRunning() && Rs2Inventory.hasItem(5511) && !mediumpouchfilled && (firstpouchfilled || Rs2Inventory.isFull()) && (Rs2Inventory.hasItem(7936) || Rs2Inventory.hasItem(24704))){
                checkRequiredEssence(6);
                if (this.isRunning()) { Rs2Inventory.interact(5511, "fill"); }
                if (this.isRunning() && !firstpouchfilled) { firstpouchfilled=true; }
                if(this.isRunning()) { Rs2Inventory.waitForInventoryChanges(200); }
                if (this.isRunning()) { sleep(calculateSleepDuration()); }
                if (this.isRunning() && (Rs2Inventory.hasItem(7936)||Rs2Inventory.hasItem(24704))) { mediumpouchfilled=true; }
            }
            if(this.isRunning() && Rs2Inventory.hasItem(5509) && !smallpouchfilled && (firstpouchfilled || Rs2Inventory.isFull()) && (Rs2Inventory.hasItem(7936) || Rs2Inventory.hasItem(24704))){
                checkRequiredEssence(3);
                if (this.isRunning()) { Rs2Inventory.interact(5509, "fill"); }
                if (this.isRunning() && !firstpouchfilled) { firstpouchfilled=true; }
                if(this.isRunning()) { Rs2Inventory.waitForInventoryChanges(200); }
                if (this.isRunning()) { sleep(calculateSleepDuration()); }
                if (this.isRunning() && (Rs2Inventory.hasItem(7936)||Rs2Inventory.hasItem(24704))) { smallpouchfilled=true; }
            }
            if(this.isRunning()){ sleepUntil(() -> !Rs2Inventory.isFull(), random(200,600)); }
            if (this.isRunning()) { sleep(calculateSleepDuration()); }
            if(Rs2Inventory.isFull()){ pouchesfilled=true; }
            //pure essence
            if (Rs2Bank.hasItem(7936) && !Rs2Inventory.isFull()) {
                if (this.isRunning()) { Rs2Bank.withdrawAll(7936); }
                if (this.isRunning()) { sleepUntil(() -> Rs2Inventory.hasItem(7936) && Rs2Inventory.isFull()); }
                if (this.isRunning()) { sleep(calculateSleepDuration()); }
            }
            //daeyalt essence
            if (Rs2Bank.hasItem(24704) && !Rs2Inventory.isFull()) {
                if (this.isRunning()) { Rs2Bank.withdrawAll(24704); }
                if (this.isRunning()) { sleepUntil(() -> Rs2Inventory.hasItem(24704) && Rs2Inventory.isFull()); }
                if (this.isRunning()) { sleep(calculateSleepDuration()); }
            }
        }
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
        if (!bankIsOpen && this.isRunning()) {
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
        if (bankIsOpen && this.isRunning()) {
            if(this.isRunning()){ Rs2Widget.clickChildWidget(786434, 11); }
        }
    }
    private void openChest() {Rs2Bank.openBank(Rs2GameObject.findObjectById(config.selectBank().getBankID()));}
    private boolean isCloseToFireAltar() {
        if (Microbot.getClient().getLocalPlayer() == null) return false;
        return Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(new WorldPoint(3315, 3235, 0)) < 30;
    }
    private boolean isCloseToBank() {
        if (Microbot.getClient().getLocalPlayer() == null) return false;
        return Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(new WorldPoint(2933, 3284, 0)) < 30;
    }
    //TODO colossal pouch might change this~
    private void repairPouches() {
        boolean mediumpouchneedsrefill=false, largepouchneedsrefill=false, giantpouchneedsrefill=false, colossalpouchneedsrefill=false;
        if(Rs2Inventory.hasItem(5511)){ mediumpouchneedsrefill=true; }
        if(Rs2Inventory.hasItem(5513)){ largepouchneedsrefill=true; }
        if(Rs2Inventory.hasItem(6819)){ largepouchneedsrefill=true; }
        if(Rs2Inventory.hasItem(5515)){ giantpouchneedsrefill=true; }
        if(Rs2Inventory.hasItem(26786)){ colossalpouchneedsrefill=true; }
        if(Rs2Inventory.hasItem(26906)){ colossalpouchneedsrefill=true; }
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
        if(!config.useColossalPouch()) {
            if (this.isRunning()) { Rs2Widget.clickWidget("Click here to continue"); }
            if(this.isRunning()){ sleep(60,100); }
        } else {
            if(this.isRunning()){ Rs2Widget.clickWidget("Thanks."); }
            if(this.isRunning()){ sleep(60,100); }
        }
        if(Rs2Tab.getCurrentTab() != InterfaceTab.INVENTORY){
            //TODO some bug here?
            if(this.isRunning()){ Rs2Tab.switchToInventoryTab(); sleepUntil(()-> Rs2Tab.getCurrentTab() == InterfaceTab.INVENTORY, 1200);} }
        if(mediumpouchneedsrefill && !Rs2Inventory.hasItem(5510) && this.isRunning()) { sleepUntil(() -> Rs2Inventory.hasItem(5510), 2000); }
        if(largepouchneedsrefill && !Rs2Inventory.hasItem(5512) && this.isRunning()) { sleepUntil(() -> Rs2Inventory.hasItem(5512), 2000); }
        if(giantpouchneedsrefill && !Rs2Inventory.hasItem(5514) && this.isRunning()) { sleepUntil(() -> Rs2Inventory.hasItem(5514), 2000); }
        if(colossalpouchneedsrefill && !Rs2Inventory.hasItem(26784) && this.isRunning()) { sleepUntil(() -> Rs2Inventory.hasItem(26784), 2000); }
        if(this.isRunning()){ sleep(calculateSleepDuration()); }
        if(mediumpouchneedsrefill && Rs2Inventory.hasItem(5510) && this.isRunning()) { Rs2Inventory.interact(5510, "fill"); sleepUntil(() -> !Rs2Inventory.isFull(), 2000); sleep(calculateSleepDuration()); }
        if(largepouchneedsrefill && Rs2Inventory.hasItem(5512) && this.isRunning()) { Rs2Inventory.interact(5512, "fill"); sleepUntil(() -> !Rs2Inventory.isFull(), 2000); sleep(calculateSleepDuration()); }
        if(giantpouchneedsrefill && Rs2Inventory.hasItem(5514) && this.isRunning()) { Rs2Inventory.interact(5514, "fill"); sleepUntil(() -> !Rs2Inventory.isFull(), 2000); sleep(calculateSleepDuration()); }
        if(colossalpouchneedsrefill && Rs2Inventory.hasItem(26784) && this.isRunning()) { Rs2Inventory.interact(26784, "fill"); sleepUntil(() -> !Rs2Inventory.isFull(), 2000); sleep(calculateSleepDuration()); }
    }
    private void teletoCraftingGuild() {
        if(this.isRunning()){ Rs2Tab.switchToEquipmentTab(); }
        if(this.isRunning()){ sleepUntil(() -> Rs2Tab.getCurrentTab() == InterfaceTab.EQUIPMENT); }
        if(this.isRunning()){ sleep(100, 150); }
        if(this.isRunning()){ Rs2Widget.clickWidget(25362448); }
    }
    //TODO empty pouches doesn't work when inventory gets filled
    private boolean emptyPouches(){
        inventoryEmptySpace = Rs2Inventory.getEmptySlots();
        //TODO Empty order needs to attempt to empty evenly.
        //int invSize = Rs2Inventory.fullSlotCount();
        if(config.useColossalPouch() && !colossalpouchemptied && !Rs2Inventory.isFull() && this.isRunning()){
            Rs2Inventory.interact("Colossal pouch","Empty");
            //sleepUntil(() -> Rs2Inventory.getEmptySlots()<inventoryEmptySpace, 400);
            Rs2Inventory.waitForInventoryChanges(200);
            sleep(calculateSleepDuration());
            if(!Rs2Inventory.isFull()){ inventoryEmptySpace = Rs2Inventory.getEmptySlots(); colossalpouchemptied=true; }
        }
        //TODO from here empty evenly
        //TODO 12
        if(config.useGiantPouch() && !giantpouchemptied && !Rs2Inventory.isFull() && this.isRunning()){
            if(Rs2Inventory.getEmptySlots()<12) { return true; }
            Rs2Inventory.interact("Giant pouch","Empty");
            //sleepUntil(() -> Rs2Inventory.getEmptySlots()<inventoryEmptySpace, 400);
            Rs2Inventory.waitForInventoryChanges(200);
            sleep(calculateSleepDuration());
            if(!Rs2Inventory.isFull()){ inventoryEmptySpace = Rs2Inventory.getEmptySlots(); giantpouchemptied=true; }
        }
        //TODO 9
        if(config.useLargePouch() && !largepouchemptied && !Rs2Inventory.isFull() && this.isRunning()){
            if(Rs2Inventory.getEmptySlots()<9) { return true; }
            Rs2Inventory.interact("Large pouch","Empty");
            //sleepUntil(() -> Rs2Inventory.getEmptySlots()<inventoryEmptySpace, 400);
            Rs2Inventory.waitForInventoryChanges(200);
            sleep(calculateSleepDuration());
            if(!Rs2Inventory.isFull()){ inventoryEmptySpace = Rs2Inventory.getEmptySlots(); largepouchemptied=true; }
        }
        //TODO 6
        if(config.useMediumPouch() && !mediumpouchemptied && !Rs2Inventory.isFull() && this.isRunning()){
            if(Rs2Inventory.getEmptySlots()<6) { return true; }
            Rs2Inventory.interact("Medium pouch","Empty");
            //sleepUntil(() -> Rs2Inventory.getEmptySlots()<inventoryEmptySpace, 400);
            Rs2Inventory.waitForInventoryChanges(200);
            sleep(calculateSleepDuration());
            if(!Rs2Inventory.isFull()){ inventoryEmptySpace = Rs2Inventory.getEmptySlots(); mediumpouchemptied=true; }
        }
        //TODO 3
        if(config.useSmallPouch() && !smallpouchemptied && !Rs2Inventory.isFull() && this.isRunning()){
            if(Rs2Inventory.getEmptySlots()<3) { return true; }
            Rs2Inventory.interact("Small pouch","Empty");
            //sleepUntil(() -> Rs2Inventory.getEmptySlots()<inventoryEmptySpace, 400);
            Rs2Inventory.waitForInventoryChanges(200);
            sleep(calculateSleepDuration());
            if(!Rs2Inventory.isFull()){ inventoryEmptySpace = Rs2Inventory.getEmptySlots(); smallpouchemptied=true; }
        }

        return Rs2Inventory.isFull();
    }

    private void checkRequiredEssence(int essenceRequired) {
        if (this.isRunning()) {
            if (Rs2Inventory.hasItem(config.selectRuneToMake().getEssenceTypeRequired())) {
                if (items().stream().filter(x -> x.id == config.selectRuneToMake().getEssenceTypeRequired()).count() < essenceRequired) {
                    if (Rs2Bank.hasItem(config.selectRuneToMake().getEssenceTypeRequired()) && !Rs2Inventory.isFull()) {
                        if (this.isRunning()) {
                            if (this.isRunning()) {
                                Rs2Bank.withdrawAll(config.selectRuneToMake().getEssenceTypeRequired());
                            }
                        }
                        if (this.isRunning()) {
                            if (this.isRunning()) {
                                sleepUntil(() -> Rs2Inventory.hasItem(config.selectRuneToMake().getEssenceTypeRequired()) && Rs2Inventory.isFull());
                            }
                        }
                        if (this.isRunning()) {
                            if (this.isRunning()) {
                                sleep(calculateSleepDuration());
                            }
                        }
                    }
                }
            } else {
                if (Rs2Bank.hasItem(config.selectRuneToMake().getEssenceTypeRequired()) && !Rs2Inventory.isFull()) {
                    if (this.isRunning()) {
                        if (this.isRunning()) {
                            Rs2Bank.withdrawAll(config.selectRuneToMake().getEssenceTypeRequired());
                        }
                    }
                    if (this.isRunning()) {
                        if (this.isRunning()) {
                            sleepUntil(() -> Rs2Inventory.hasItem(config.selectRuneToMake().getEssenceTypeRequired()) && Rs2Inventory.isFull());
                        }
                    }
                    if (this.isRunning()) {
                        if (this.isRunning()) {
                            sleep(calculateSleepDuration());
                        }
                    }
                }
            }
        }
    }
    public void fetchAndMemorizeInventory() {
        inventory = Microbot.getClientThread().runOnClientThread(() -> Microbot.getClient().getItemContainer(InventoryID.INVENTORY));
        System.out.println("1");
        if (inventory != null) {
            Item[] items = inventory.getItems();
            List<Rs2Item> newInventoryItems = new ArrayList<>();

            // Use index as slot
            for (int slot = 0; slot < items.length; slot++) {
                Item item = items[slot];
                if (item.getId() != -1) {  // Valid item
                    System.out.println("4");
                    ItemComposition itemComp = Microbot.getClientThread().runOnClientThread(() -> Microbot.getClient().getItemDefinition(item.getId()));
                    System.out.println("5");
                    newInventoryItems.add(new Rs2Item(item, itemComp, slot));  // Use slot as index
                    System.out.println("6");
                }
            }
            System.out.println("7");
            // Update Rs2Inventory with new items
            updateRs2Inventory(newInventoryItems);
            System.out.println("8");
        } else {
            System.out.println("Inventory is empty or not available.");
        }
    }
    // Method to compare and update Rs2Inventory
    private void updateRs2Inventory(List<Rs2Item> newItems) {
        List<Rs2Item> currentItems = Rs2Inventory.items();

        if (!currentItems.equals(newItems)) {
            // If there is a difference, update the inventory
            Rs2Inventory.inventoryItems.clear();
            Rs2Inventory.inventoryItems.addAll(newItems);
            System.out.println("Rs2Inventory updated with new items.");
        } else {
            System.out.println("No discrepancies found. Rs2Inventory is up-to-date.");
        }
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
}
