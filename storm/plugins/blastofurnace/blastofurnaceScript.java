package net.runelite.client.plugins.microbot.storm.plugins.blastofurnace;

import com.google.common.base.Stopwatch;
import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import net.runelite.api.Varbits;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.storm.plugins.blastofurnace.enums.Bars;
import net.runelite.client.plugins.microbot.storm.plugins.blastofurnace.enums.State;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Item;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static net.runelite.client.plugins.microbot.storm.plugins.blastofurnace.enums.State.*;
import static net.runelite.api.ItemID.*;
import static net.runelite.api.NullObjectID.NULL_29330;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;
import static net.runelite.client.plugins.microbot.util.math.Random.random;

//TODO add more isRunning checks for waits / sleeps
//TODO check inventory has items before leaving bank, restart if not.(when character levels it interrupts) / reopen bank
//TODO check for any ores present on conveyor belt?
//TODO CHECK THAT INVENTORY HAS SPACE WHEN GRABBING ORES, AND OPENING THE BANK!
public class blastofurnaceScript extends Script {
    static long timeoutMultiplier = 1;
    public static double version = 1.0;
    public static long lastXpDrop;
    private blastofurnaceConfig config;
    boolean initScript = false;
    public static State state = BANKING;
    static int staminaTimer;
    private boolean useSecondaryOre = false;
    private int cofferCoins;
    private int refillCofferAmmount;
    private boolean stopScript=false;
    private int secondaryTripCounter;
    public static boolean bankIsOpen=false;
    static int previousXP;
    static boolean waitingnexttick;
    static boolean waitingXpDrop;
    static boolean coalBagEmpty;
    static boolean primaryOreEmpty;
    static boolean secondaryOreEmpty;
    private boolean firstIteration;
    int bankCoal;
    private long lastPaymentTime = 0;  // To track when the last payment was made
    private static final long PAYMENT_INTERVAL = 580 * 1000; // 9 minutes 50 seconds in milliseconds
    private int getStamEffect() {return Microbot.getClientThread().runOnClientThread(() -> Microbot.getClient().getVarbitValue(Varbits.STAMINA_EFFECT));}
    private void iceGlovesEquip(){if(Rs2Equipment.hasEquipped(ItemID.ICE_GLOVES)){return;} if(this.isRunning()) { Rs2Inventory.wield(ItemID.ICE_GLOVES); } sleepUntil(() -> Rs2Equipment.hasEquipped(ItemID.ICE_GLOVES));}
    private void goldGlovesEquip(){if(Rs2Equipment.hasEquipped(ItemID.GOLDSMITH_GAUNTLETS)){return;} Rs2Inventory.wield(ItemID.GOLDSMITH_GAUNTLETS); sleepUntil(() -> Rs2Equipment.hasEquipped(ItemID.GOLDSMITH_GAUNTLETS));}
    private int getBFDispenserState() {return Microbot.getClientThread().runOnClientThread(() -> Microbot.getClient().getVarbitValue(Varbits.BAR_DISPENSER));}
    private String openBank = "openBank", withdrawAll = "withdrawAll", depositAll="depositAll", interact = "interact", withdrawOne="withdrawOne", invInteract="invInteract";

    public boolean run(blastofurnaceConfig config) {
        //TODO check for out of resources,
        if(config.getBars()== Bars.RUNITE_BAR){timeoutMultiplier=4;}
        else if (config.getBars()==Bars.ADAMANTITE_BAR){timeoutMultiplier=3;}
        else if (config.getBars()==Bars.MITHRIL_BAR){timeoutMultiplier=2;}
        else if (config.getBars()==Bars.STEEL_BAR){timeoutMultiplier=1;}
        lastXpDrop=System.currentTimeMillis();
        bankIsOpen=Rs2Bank.isOpen();
        bankCoal = 0;
        staminaTimer=0;
        initScript = true;
        this.config = config;
        refillCofferAmmount=config.getRefillAmount();
        Microbot.enableAutoRunOn = false;
        state = BANKING;
        secondaryTripCounter=0;
        calculateLoop();
        previousXP=0;
        waitingXpDrop=false;
        waitingnexttick=false;
        firstIteration = true;
        primaryOreEmpty=!Rs2Inventory.hasItem(config.getBars().getPrimaryOre());
        if(config.getBars().getSecondaryOre()!=null){ secondaryOreEmpty = !Rs2Inventory.hasItem(config.getBars().getSecondaryOre()); } else { secondaryOreEmpty=false; }
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                if(System.currentTimeMillis()-lastXpDrop>35000*timeoutMultiplier){
                    if(this.isRunning()) { Rs2Player.logout(); }
                    shutdown();
                }
                switch (state) {
                    case BANKING:
                        Microbot.status = "Banking";
                        if (System.currentTimeMillis() - lastPaymentTime >= PAYMENT_INTERVAL && Rs2Player.getRealSkillLevel(Skill.SMITHING) < 60) {
                            payForeman();
                        }
                        if(!bankIsOpen) {
                            System.out.println("Opening bank");
                            openBank();
                            if(this.isRunning()) { sleepUntil(()-> bankIsOpen,60000); }
                            //System.out.println("bank is open in : "+(System.currentTimeMillis()-startb)+"ms");
                        }
                        firstIteration=false;
                        if(config.useCoalBag()){
                            bankCoal = Rs2Bank.bankItems.stream().filter(item -> item.id == ItemID.COAL).mapToInt(item -> item.quantity).sum();
                            customSleep("high");
                            actions(invInteract, COAL_BAG_12019, "fill"); customSleep("high");}
                        customSleep("high");
                        if(Rs2Inventory.hasItem("bar")){
                            actions(depositAll, "bar");
                            if(this.isRunning()) { sleepUntil(()->!Rs2Inventory.hasItem("bar"),97000); }
                            customSleep("high");
                        }
                        if(!hasItems()){
                            long startTime = System.currentTimeMillis();
                            do { if (hasItems() || !this.isRunning()) { continue; }
                                sleep(5000);
                            } while (!hasItems() && (System.currentTimeMillis() - startTime < 97000) && this.isRunning());
                            sleep(600);
                            if(!hasItems()){
                                if(this.isRunning()) { Rs2Player.logout(); }
                                shutdown();
                            }
                        }
                        if( staminaTimer <= 2 && (Microbot.getClient().getEnergy() < 7500) && config.useStamina()){ useStaminaPotions(); }
                        fillCoffer();
                        smithingBankMethod();
                        customSleep("high");
                        //if(this.isRunning()) { if(config.getBars() == Bars.GOLD_BAR) iceGlovesEquip(); }//TODO old method that wears the ice gloves at the bank, changed it to at the dispenser after xp drop.
                        //customSleep("high");
                        if (bankCoal == Rs2Bank.bankItems.stream().filter(item -> item.id == ItemID.COAL).mapToInt(item -> item.quantity).sum()){
                            actions(invInteract, COAL_BAG_12019, "fill");
                            Rs2Inventory.waitForInventoryChanges(650);
                            customSleep("moderate");
                        }
                        //if(coalBagEmpty){
                        //    actions(invInteract, COAL_BAG_12019, "fill"); customSleep("moderate");
                        //}
                        state = SMITHING;
                        break;
                    case SMITHING:
                        System.out.println("clicking conveyor");
                        smithingMethod();
                        state = BANKING;
                        break;
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 20, TimeUnit.MILLISECONDS);
        return true;
    }
    private void actions(String action, String provider){
        if(this.isRunning()) {
            if (Objects.equals(action, withdrawAll)) { Rs2Bank.withdrawAll(provider); }
            if (Objects.equals(action, depositAll)) { Rs2Bank.depositAll(provider); }
        }
    }
    private void actions(String action, int provider){
        if(this.isRunning()) {
            if (Objects.equals(action, withdrawAll)) { Rs2Bank.withdrawAll(provider); }
            if (Objects.equals(action, withdrawOne)) { Rs2Bank.withdrawOne(provider); }
            if (Objects.equals(action, interact)) { Rs2GameObject.interact(provider); }
        }
    }
    private void actions(String action){
        //if(Objects.equals(action, openBank)){ Rs2Bank.openBank(Rs2GameObject.findObjectById(26707)); }

    }
    private void actions(String action, int ID, String provider){
        if(this.isRunning()) {
            if (Objects.equals(action, invInteract)) { Rs2Inventory.interact(ID, provider); }
        }
    }
    private void openBank() {
        if(this.isRunning()){ Rs2Bank.openBank(Rs2GameObject.findObjectById(26707)); }
    }
    private boolean isCofferEmpty() {
        if(Rs2GameObject.convertGameObjectToObjectComposition(29330)!=null && Rs2GameObject.convertGameObjectToObjectComposition(29330).getImpostorIds()!=null) {
            return Rs2GameObject.convertGameObjectToObjectComposition(29330).getImpostor().getId() == 29328;
        }
        return false;
    }
    private boolean outOfResources(){
        return false;
    }
    private static void useSecondaryOre(){

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
    private void fillCoffer() {
        if (Rs2GameObject.convertGameObjectToObjectComposition(29330) != null && Rs2GameObject.convertGameObjectToObjectComposition(29330).getImpostorIds() != null) {
            if (Rs2GameObject.convertGameObjectToObjectComposition(29330).getImpostor().getId() == 29328) {
                Microbot.status = "Refilling Coffer";
                if (!bankIsOpen) {
                    openBank();
                    if(this.isRunning()) { sleepUntil(() -> bankIsOpen,97000); }
                    customSleep("moderate");
                }
                if(Rs2Inventory.hasItem("ore")) { System.out.println("detected items in inventory"); Rs2Bank.depositAll("ore"); }
                if(Rs2Inventory.hasItem("bar")) { System.out.println("detected items in inventory"); Rs2Bank.depositAll("bar"); }
                customSleep("moderate");
                if(this.isRunning()) { Rs2Bank.withdrawX("coins", refillCofferAmmount); }
                if(this.isRunning()) { sleepUntil(() -> Rs2Inventory.hasItem("coins")); }
                if(this.isRunning()) { Rs2Bank.closeBank(); }
                customSleep("high");
                if(this.isRunning()) { Rs2GameObject.interact(NULL_29330); }
                customSleep("low");
                if(this.isRunning()) { sleepUntil(() -> Rs2Widget.findWidget("Select an option") != null, 97000); }
                customSleep("moderate");
                if(this.isRunning()) { Rs2Keyboard.typeString("1"); }
                if(this.isRunning()) { sleepUntil(() -> Rs2Widget.findWidget("Deposit how much?") != null, 97000); }
                customSleep("moderate");
                if(this.isRunning()) { Rs2Keyboard.typeString(String.valueOf(refillCofferAmmount)); }
                customSleep("high");
                if(this.isRunning()) { Rs2Keyboard.keyPress(KeyEvent.VK_ENTER); }
                if(this.isRunning()) { sleepUntil(() -> Rs2Inventory.hasItem("coins"), 97000); }
                customSleep("high");
                if (!bankIsOpen) {
                    openBank();
                    if(this.isRunning()) { sleepUntil(() -> bankIsOpen, 97000); }
                    customSleep("moderate");
                }
            }
        }
    }
    private void useStaminaPotions(){
        System.out.println("stamina timer : " + staminaTimer);
        if(Microbot.getClient().getEnergy() < 6400) {
            if (this.isRunning()) { Rs2Bank.withdrawOne("Energy potion"); }
            if (this.isRunning()) { sleepUntil(() -> Rs2Inventory.hasItem("Energy potion")); }
            String energyPotion = Rs2Inventory.get("Energy potion").getName();
            if (this.isRunning()) { sleep(61, 97); }
            if (this.isRunning()) { Rs2Inventory.interact("Energy potion", "drink"); }
            if (this.isRunning()) { sleepUntil(() -> !Objects.equals(energyPotion, Rs2Inventory.get("Energy potion").getName()), 97000); }
            if (this.isRunning()) { sleep(161, 197); }
            if (this.isRunning() && Rs2Inventory.hasItem("Energy potion")) {
                if(this.isRunning()) { Rs2Bank.depositOne("Energy potion"); }
                if(this.isRunning()) { sleepUntil(() -> !Rs2Inventory.hasItem("Energy potion"), 97000); }
            }
        }
        if(Microbot.getClient().getEnergy() < 7500) {
            if (this.isRunning()) { Rs2Bank.withdrawOne("Stamina potion"); }
            if (this.isRunning()) { sleepUntil(() -> Rs2Inventory.hasItem("Stamina potion")); }
            String staminaPotion = Rs2Inventory.get("Stamina potion").getName();
            if (this.isRunning()) { sleep(61, 97); }
            if (this.isRunning()) { Rs2Inventory.interact("Stamina potion", "drink"); }
            if (this.isRunning()) { sleepUntil(() -> staminaTimer > 1 && !Objects.equals(staminaPotion, Rs2Inventory.get("Stamina potion").getName()), 97000); }
            if (this.isRunning()) { sleep(161, 197); }
            if (this.isRunning() && (Rs2Inventory.hasItem("Stamina potion") || Rs2Inventory.hasItem(229))) {
                if (this.isRunning() && Rs2Inventory.hasItem("Stamina potion")) {
                    if(this.isRunning()) { Rs2Bank.depositOne("Stamina potion"); }
                    if(this.isRunning()) { sleepUntil(() -> !Rs2Inventory.hasItem("Stamina"), 97000); }
                } else {
                    if(this.isRunning()) { Rs2Bank.depositOne(229); }
                    if(this.isRunning()) { sleepUntil(() -> !Rs2Inventory.hasItem(229)); } }
            }
        }
        if (this.isRunning()) { sleep(161, 197); }
    }
    private void retrieveBars(){
        //customSleep("high");
        if(this.isRunning() && Rs2Player.getWorldLocation().getX()!=1940 && Rs2Player.getWorldLocation().getY()!=4962){
            if (this.isRunning() && config.getBars()!=Bars.GOLD_BAR && config.getBars()!=Bars.SILVER_BAR && config.getBars()!=Bars.STEEL_BAR) { Rs2Walker.walkFastCanvas(new WorldPoint(random(1946, 1948), random(4958, 4960), 0)); }
            else { if(this.isRunning()){ Rs2Walker.walkFastCanvas(new WorldPoint(1940, 4962, 0)); } }
        }
        //TODO config values are 1946, 4959
        //if (this.isRunning()) { Rs2Walker.walkFastCanvas(new WorldPoint(config.tileX(), config.tileY(), 0)); }
        //TODO config values are : 1940x4962

        if(this.isRunning() && config.getBars()!=Bars.GOLD_BAR && config.getBars()!=Bars.SILVER_BAR) {
            long watch = System.currentTimeMillis();
            while(this.isRunning() && Rs2Player.getWorldLocation().getY() > 4962 && System.currentTimeMillis()-watch<random(5000,7000)) {
                customSleep("high");
                if (this.isRunning() && Rs2Player.isMoving()) { watch = System.currentTimeMillis(); } }
        }
        else { while(this.isRunning() && waitingXpDrop){ sleep(97,207); } iceGlovesEquip(); }
        if(this.isRunning()) { if (getBFDispenserState() == 1) { sleepUntil(() -> getBFDispenserState() == 2 || getBFDispenserState() == 3, 97000); } }
        customSleep("high");
        //TODO added this for when bars are less than 27
        System.out.println("Checking for correct bar amount before collecting.");
        //TODO something wrong here, furnace doesn't update for too long and the player skips
        if (calculateBars(config.getBars().getBarID())==0) {
            //TODO change this so that gold still works
            if (Microbot.getVarbitValue(config.getBars().getBFPrimaryOreID()) > 0) {
                if (config.getBars().getBarID()==Bars.STEEL_BAR.getBarID()
                || config.getBars().getBarID()==Bars.MITHRIL_BAR.getBarID()
                || config.getBars().getBarID()==Bars.ADAMANTITE_BAR.getBarID()
                || config.getBars().getBarID()==Bars.RUNITE_BAR.getBarID()) {
                    if (Microbot.getVarbitValue(Varbits.BLAST_FURNACE_COAL) == 0) { return; }
                    }
                sleepUntil(() -> calculateBars(config.getBars().getBarID()) > 0, 10000);
                if (calculateBars(config.getBars().getBarID()) == 0) return;
            }
        }
        if(this.isRunning()) { if (calculateBars(config.getBars().getBarID())<27){ sleepUntil(() -> calculateBars(config.getBars().getBarID())>26); } }
        System.out.println("Detected correct bar amount, proceeding.");
        customSleep("high");


        //TODO need something here in case level up closes widget
        int retry = 0;
        while(this.isRunning() && !Rs2Inventory.hasItem("bar") && !Rs2Widget.hasWidget("bar") && !Rs2Inventory.isFull() && retry<3){
            if(retry>0){ System.out.println("Attempt #"+(retry+1)); }
        actions(interact, 9092);
        long watch = System.currentTimeMillis();
        while(this.isRunning() && !Rs2Widget.hasWidget("bar") && System.currentTimeMillis()-watch<random(5000,7000)){
            customSleep("high");
            if (this.isRunning() && Rs2Player.isMoving()) { watch = System.currentTimeMillis(); } }
        //if(this.isRunning()) { sleepUntil(() -> Rs2Widget.hasWidget("bar"), 97000); }
        customSleep("high");
        if (this.isRunning()) { Rs2Keyboard.keyPress(KeyEvent.VK_SPACE); }
        customSleep("high");
        if (config.getBars() == Bars.GOLD_BAR) { goldGlovesEquip(); customSleep("high"); }
        //TODO bug here where space bar quits working after some time, need something to correct it when it breaks...
            retry++;
        }
        long starti = System.currentTimeMillis();
        do { if (Rs2Inventory.hasItem("bar") || !this.isRunning()) { continue; }
            sleep(60);
            if(System.currentTimeMillis() - starti > 6000 && calculateBars(config.getBars().getBarID())==28){
                if(this.isRunning()) { Rs2Player.logout(); }
                shutdown();
            }
        } while (!Rs2Inventory.hasItem("bar") && (System.currentTimeMillis() - starti < Long.MAX_VALUE) && this.isRunning());
    }
    private void smithingMethod() {
        if(config.getBars().getSecondaryOre()!=null) { if(this.isRunning()) { sleepUntil(()->Rs2Inventory.hasItem(config.getBars().getPrimaryOre()) || Rs2Inventory.hasItem(config.getBars().getSecondaryOre())); }}
        else{ if(this.isRunning()) { sleepUntil(()->Rs2Inventory.hasItem(config.getBars().getPrimaryOre()),97000); }}
        System.out.println("Should be clicking on conveyor belt.");
        if(primaryOreEmpty){System.out.println("Something went wrong with banking detection, primary is empty when it shouldn't be."); primaryOreEmpty=false; }
        //List<Rs2Item> previousInventory = Rs2Inventory.all();
        actions(interact, 9100);//TODO FUCK detect primary ore gone
        //TODO fuckers, doesn't have sleep between checks causing bug.
        if (this.isRunning()) { sleepUntil(() -> Rs2Player.getWorldLocation().getX() < 1939, 20000); }
        if (this.isRunning()) { sleepUntil(() -> Rs2Player.getWorldLocation().getX() > 1941, 20000); }
        if (this.isRunning()) { Rs2Inventory.waitForInventoryChanges(5000); }
        //if (this.isRunning()) { sleepUntil(() -> previousInventory != Rs2Inventory.all(), 20000); }
        if(!waitingXpDrop){ waitingXpDrop=true; }
        System.out.println("Primary empty? "+primaryOreEmpty+", Secondary empty? "+secondaryOreEmpty);
        //boolean b = Rs2Inventory.hasItem(config.getBars().getPrimaryOre());//TODO bug here
        //if(this.isRunning()) { if(b){ sleepUntil(()->primaryOreEmpty,97000); }else{sleepUntil(()-> secondaryOreEmpty,97000);} }
        customSleep("moderate");
        //TODO need inventory capacity checker
        if(!config.useCoalBag() || config.getBars()==Bars.GOLD_BAR || config.getBars()==Bars.SILVER_BAR || config.getBars()==Bars.IRON_BAR) {
            System.out.println("smithing without coalbag config ticked");
            if (secondaryTripCounter == 0 || config.getBars().getSecondaryOre() == null || config.getBars()==Bars.STEEL_BAR) {
                retrieveBars();
            }
        } else {
            System.out.println("smithing with coalbag config ticked");
            if(config.getBars()==Bars.STEEL_BAR){
                if(secondaryTripCounter > 1) {
                    System.out.println("smithing steel bars as per config instructions");
                    actions(invInteract, COAL_BAG_12019, "empty");
                    if (this.isRunning()) { Rs2Inventory.waitForInventoryChanges(650); }
                    //if(this.isRunning()) { boolean c = sleepUntilTrue(()->coalBagEmpty, 100, 5000); System.out.println("empty coal bag? : "+c); }
                    customSleep("moderate");
                    //TODO really need to work on detection for depositing items onto the conveyor... constantly breaks here.
                    actions(interact, 9100);
                    if (this.isRunning()) { Rs2Inventory.waitForInventoryChanges(650); }
                    //customSleep("high");
                    //if(this.isRunning()) { boolean i = sleepUntilTrue(()-> secondaryOreEmpty, 100,17000); System.out.println("sent coal? : "+i); }
                    customSleep("moderate");
                    if(Rs2Inventory.hasItem(COAL)){
                        customSleep("moderate");
                        //TODO put while stuff here
                        final Stopwatch watch = Stopwatch.createStarted();
                        while(this.isRunning() && Rs2Inventory.hasItem(COAL) && watch.elapsed(TimeUnit.MILLISECONDS)<random(5000,7000)){
                            actions(interact, 9100);
                            if (this.isRunning()) { Rs2Inventory.waitForInventoryChanges(650); }
                            //if(this.isRunning()) { boolean a = sleepUntilTrue(() -> secondaryOreEmpty, 100, 17000); System.out.println("sent coal? : "+a); }
                            customSleep("moderate");
                        }
                    }
                }
                System.out.println("Iron ore :" + Microbot.getVarbitValue(Varbits.BLAST_FURNACE_IRON_ORE)+", Steel bars : "+Microbot.getVarbitValue(Varbits.BLAST_FURNACE_STEEL_BAR));
                if(Microbot.getVarbitValue(Varbits.BLAST_FURNACE_IRON_ORE)>0 || Microbot.getVarbitValue(Varbits.BLAST_FURNACE_STEEL_BAR)>0 || secondaryTripCounter>1) { retrieveBars(); }
                if (secondaryTripCounter == 1) { secondaryTripCounter++; }
            }
            if(config.getBars()==Bars.MITHRIL_BAR){//TODO this won't have coal when it deposits mithril by itself - 2 coal, 1 mithril
                System.out.println("smithing mithril bars as per config instructions");
                if (secondaryTripCounter == 0){//TODO if 0 do this
                    System.out.println("Counter is 0 so retrieving mithril bars.");
                    retrieveBars();
                } else {//TODO if 1 do this
                    actions(invInteract, COAL_BAG_12019, "empty");
                    if(this.isRunning()) { sleepUntil(()->coalBagEmpty); }
                    //if(this.isRunning()) { sleepUntil(()->Rs2Inventory.hasItem("coal")); }
                    customSleep("high");
                    actions(interact, 9100);
                    waitForInventorySpace();
                }
            }
            if(config.getBars()==Bars.ADAMANTITE_BAR){
                System.out.println("smithing adamantite bars as per config instructions");
                if (secondaryTripCounter == 0){
                    actions(invInteract, COAL_BAG_12019, "empty");
                    if(this.isRunning()) { sleepUntil(()->coalBagEmpty); }
                    //if(this.isRunning()) { sleepUntil(()->Rs2Inventory.hasItem("coal")); }
                    customSleep("high");
                    actions(interact, 9100);
                    waitForInventorySpace();
                    System.out.println("Counter is 0 so retrieving adamantite bars.");
                    retrieveBars();
                } else {
                    actions(invInteract, COAL_BAG_12019, "empty");
                    if(this.isRunning()) { sleepUntil(()->coalBagEmpty); }
                    //if(this.isRunning()) { sleepUntil(()->Rs2Inventory.hasItem("coal")); }
                    customSleep("high");
                    actions(interact, 9100);
                }
            }
            if(config.getBars()==Bars.RUNITE_BAR){
                actions(invInteract, COAL_BAG_12019, "empty");
                //if(this.isRunning()) { boolean c = sleepUntilTrue(()->coalBagEmpty, 100, 5000); System.out.println("empty coal bag? : "+c); }
                if (this.isRunning()) { Rs2Inventory.waitForInventoryChanges(650); }
                customSleep("moderate");
                actions(interact, 9100);
                if (this.isRunning()) { Rs2Inventory.waitForInventoryChanges(650); }
                //if(this.isRunning()) { boolean i = sleepUntilTrue(()-> secondaryOreEmpty, 100,17000); System.out.println("sent coal? : "+i); }
                customSleep("moderate");
                //TODO don't like this redundancy check, need to ultimately solve why above fails. why is secondaryOreEmpty already true?
                //TODO maybe change it so it checks if the inventory still has coal after another wait before clicking again?
                if(Rs2Inventory.hasItem(COAL)){
                    customSleep("moderate");
                    //TODO put while stuff here
                    final Stopwatch watch = Stopwatch.createStarted();
                    while(this.isRunning() && Rs2Inventory.hasItem(COAL) && watch.elapsed(TimeUnit.MILLISECONDS)<random(5000,7000)){
                        actions(interact, 9100);
                        if (this.isRunning()) { Rs2Inventory.waitForInventoryChanges(650); }
                        //if(this.isRunning()) { boolean a = sleepUntilTrue(() -> secondaryOreEmpty, 100, 17000); System.out.println("sent coal? : "+a); }
                        customSleep("moderate");
                    }
                }
                if (secondaryTripCounter == 1 || secondaryTripCounter == 4){
                    System.out.println("Counter is "+secondaryTripCounter+", so retrieving runite bars.");
                    retrieveBars();
                }
            }
        }
    }
    private void smithingBankMethod() {
        if(!config.useCoalBag() || config.getBars() == Bars.GOLD_BAR || config.getBars() == Bars.SILVER_BAR || config.getBars() == Bars.BRONZE_BAR) {
            System.out.println("banking without coalbag config ticked");
            //TODO VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV
            if (config.getBars().getSecondaryOre() != null) {
                if (secondaryTripCounter < config.getBars().getSecondaryOreNeeded()) {
                    System.out.println("Should increment counter : " + secondaryTripCounter);
                    secondaryTripCounter++;
                    actions(withdrawAll, config.getBars().getSecondaryOre());
                } else {
                    secondaryTripCounter = 0;
                    actions(withdrawAll, config.getBars().getPrimaryOre());
                }
            } else {
                actions(withdrawAll, config.getBars().getPrimaryOre());
            }
            //TODO ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
        } else {//TODO will need changes later for upgraded coal bag?
            //TODO IMPORTANT! need to update to only increment counter after successful bank, wait for inventory full, if not full return / deposit all.
            System.out.println("banking with coalbag config ticked");
            if(config.getBars()==Bars.STEEL_BAR){
                System.out.println("banking for steel bars as per config instructions");
                if(secondaryTripCounter==0){ secondaryTripCounter++; onlyCoal(); }
                //don't need to reset the trip counter because that's just to setup coal always in furnace so we don't accidentally make iron.
                    else if (secondaryTripCounter>0) { oneEach(); }
            }
            if(config.getBars()==Bars.MITHRIL_BAR){
                System.out.println("banking for mithril bars as per config instructions");
                if(secondaryTripCounter<1){ secondaryTripCounter++; doubleCoal();
                } else { secondaryTripCounter = 0; secondaryTripCounter++; actions(withdrawAll, config.getBars().getPrimaryOre());
                    System.out.println("Counter was 1, so changed to 0. withdrawing mithril ore.");
                }
            }
            if(config.getBars()==Bars.ADAMANTITE_BAR){
                System.out.println("banking for adamantite bars as per config instructions");
                if(secondaryTripCounter<1){ secondaryTripCounter++; doubleCoal();
                } else { secondaryTripCounter = 0; secondaryTripCounter++; oneEach();
                    System.out.println("Counter was 1, so changed to 0. withdrawing adamantite ore.");
                }
            }
            if(config.getBars()==Bars.RUNITE_BAR){
                System.out.println("banking for runite bars as per config instructions : "+secondaryTripCounter);
                if (secondaryTripCounter == 0) { secondaryTripCounter++; doubleCoal(); }//TODO on our first loop we have results waiting, and an inventory of 54 with 27 waiting(81)
                else if(secondaryTripCounter == 1){ secondaryTripCounter++; oneEach(); }//TODO on our second loop we should add 27 coal to get 108 coal.
                else if(secondaryTripCounter == 2){ secondaryTripCounter++; doubleCoal(); }//TODO on our third loop we have results waiting to collect, and 0 coal waiting, we add 54.
                else if(secondaryTripCounter == 3){ secondaryTripCounter++; doubleCoal(); }//TODO on our fourth loop we have 54 coal waiting and we add 54 to get 108
                else if(secondaryTripCounter == 4){ secondaryTripCounter = 0; oneEach();//TODO on our fifth loop we have 108 coal waiting, we add our runite, and we deposit 27 coal
                    System.out.println("Counter was 4, so changed to "+secondaryTripCounter+". withdrawing runite ore."); }
            }
        }
    }
    private void doubleCoal(){
        System.out.println("secondary? : "+secondaryOreEmpty);
        actions(withdrawAll, config.getBars().getSecondaryOre());
        System.out.println("withdrawing coal");
        if (this.isRunning()) { Rs2Inventory.waitForInventoryChanges(650); }
        //if(this.isRunning()) { sleepUntil(()->!secondaryOreEmpty); }
        System.out.println("done withdrawing coal");
        //if(this.isRunning()) { sleepUntil(()->Rs2Inventory.hasItem(ItemID.COAL)); }
    }
    private void oneEach(){
        actions(withdrawAll, config.getBars().getPrimaryOre());
        if (this.isRunning()) { Rs2Inventory.waitForInventoryChanges(650); }
        //if(this.isRunning()) { sleepUntil(()->!primaryOreEmpty); }
        //if(this.isRunning()) { sleepUntil(()->Rs2Inventory.hasItem(config.getBars().getPrimaryOre())); }
    }
    private void onlyCoal(){
        actions(withdrawAll, config.getBars().getSecondaryOre());
        if (this.isRunning()) { Rs2Inventory.waitForInventoryChanges(650); }
        //if(this.isRunning()) { sleepUntil(()->Rs2Inventory.hasItem(config.getBars().getSecondaryOre())); }
        //customSleep("moderate");
    }
    private boolean hasItems() {
        if (!bankIsOpen) {
            openBank();
            if(this.isRunning()) { sleepUntil(()->bankIsOpen,97000); }
        }
        if (config.getBars().getSecondaryOre() != null) {
            if (Rs2Bank.hasItem(config.getBars().getSecondaryOre()) && Rs2Bank.hasItem(config.getBars().getPrimaryOre())){
               return true;
            } else { Microbot.showMessage("Unsufficient items found."); return false; }
        } else {
            if (Rs2Bank.hasItem(config.getBars().getPrimaryOre())) {
                return true;
            } else { Microbot.showMessage("Unsufficient items found."); return false; }
        }
    }
    public void waitForInventorySpace(){
        long starti = System.currentTimeMillis();
        System.out.println("Full? : "+Rs2Inventory.isFull()+" || Running? : "+this.isRunning());
        do { if (!Rs2Inventory.isFull() || !this.isRunning()) { System.out.println("WFIS method break condition met "+(System.currentTimeMillis()-starti)); continue; }
            sleep(10);
        } while (Rs2Inventory.isFull() && (System.currentTimeMillis() - starti < 97000) && this.isRunning());
        System.out.println("time elapsed since WFIS method called : "+(System.currentTimeMillis()-starti));
    }
    public boolean waitForCoalBag(){
        return coalBagEmpty;
    }
    public int calculateBars(int a){
        if(a==Bars.SILVER_BAR.getBarID()){ return Microbot.getVarbitValue(Varbits.BLAST_FURNACE_SILVER_BAR); }
        if(a==Bars.GOLD_BAR.getBarID()){ return Microbot.getVarbitValue(Varbits.BLAST_FURNACE_GOLD_BAR); }
        if(a==Bars.IRON_BAR.getBarID()){ return Microbot.getVarbitValue(Varbits.BLAST_FURNACE_IRON_BAR); }
        if(a==Bars.STEEL_BAR.getBarID()){ return Microbot.getVarbitValue(Varbits.BLAST_FURNACE_STEEL_BAR); }
        if(a==Bars.MITHRIL_BAR.getBarID()){ return Microbot.getVarbitValue(Varbits.BLAST_FURNACE_MITHRIL_BAR); }
        if(a==Bars.ADAMANTITE_BAR.getBarID()){ return Microbot.getVarbitValue(Varbits.BLAST_FURNACE_ADAMANTITE_BAR); }
        if(a==Bars.RUNITE_BAR.getBarID()){ return Microbot.getVarbitValue(Varbits.BLAST_FURNACE_RUNITE_BAR); }
        return 0;
    }
    public void calculateLoop(){
        int ironOre = Microbot.getVarbitValue(Varbits.BLAST_FURNACE_IRON_ORE);
        int steelBar = Microbot.getVarbitValue(Varbits.BLAST_FURNACE_STEEL_BAR);
        int runeBar = Microbot.getVarbitValue(Varbits.BLAST_FURNACE_RUNITE_BAR);
        int runeOre = Microbot.getVarbitValue(Varbits.BLAST_FURNACE_RUNITE_ORE);
        int coal = Microbot.getVarbitValue(Varbits.BLAST_FURNACE_COAL);
        if(config.getBars()==Bars.STEEL_BAR){
            System.out.println("Steel bars : "+steelBar+", Iron ore : "+ironOre+", Coal : "+coal);
            if(coal==27){ secondaryTripCounter=2; }
        }
        if(config.getBars()==Bars.RUNITE_BAR){
            System.out.println("Rune bars : "+runeBar+", Rune ore : "+runeOre+", Coal : "+coal);
            //if(runeBar==0 && runeOre == 0){  }
            if(runeBar==27 && coal == 27){ secondaryTripCounter=0; }
            if(runeBar==0 && coal == 81){ secondaryTripCounter=1; }
            if(runeBar==0 && coal == 0){ secondaryTripCounter=2; }
            if(runeBar==0 && coal == 54){ secondaryTripCounter=3; }
            if(runeBar==0 && coal == 108){ secondaryTripCounter=4; }
        }
    }
    public boolean calculateOtherFurnaceItems(int a, int b, int c){//TODO a = primary, b = bars, c = secondary
        int k=0;//TODO runite potential problem with primary ore
        if(a!=COPPER_ORE && Microbot.getVarbitValue(Varbits.BLAST_FURNACE_COPPER_ORE)>0) k++;
        if(c!=COAL && Microbot.getVarbitValue(Varbits.BLAST_FURNACE_COAL)>0) k++;
        if(c!=TIN_ORE && Microbot.getVarbitValue(Varbits.BLAST_FURNACE_TIN_ORE)>0) k++;
        if(b!=BRONZE_BAR && Microbot.getVarbitValue(Varbits.BLAST_FURNACE_BRONZE_BAR)>0) k++;
        if(a!=IRON_ORE && Microbot.getVarbitValue(Varbits.BLAST_FURNACE_IRON_ORE)>0) k++;
        if(b!=STEEL_BAR && Microbot.getVarbitValue(Varbits.BLAST_FURNACE_STEEL_BAR)>0) k++;
        if(a!=MITHRIL_ORE && Microbot.getVarbitValue(Varbits.BLAST_FURNACE_MITHRIL_ORE)>0) k++;
        if(b!=MITHRIL_BAR && Microbot.getVarbitValue(Varbits.BLAST_FURNACE_MITHRIL_BAR)>0) k++;
        if(a!=ADAMANTITE_ORE && Microbot.getVarbitValue(Varbits.BLAST_FURNACE_ADAMANTITE_ORE)>0) k++;
        if(b!=ADAMANTITE_BAR && Microbot.getVarbitValue(Varbits.BLAST_FURNACE_ADAMANTITE_BAR)>0) k++;
        if(a!=RUNITE_ORE && Microbot.getVarbitValue(Varbits.BLAST_FURNACE_RUNITE_ORE)>0) k++;
        if(b!=RUNITE_BAR && Microbot.getVarbitValue(Varbits.BLAST_FURNACE_RUNITE_BAR)>0) k++;
        if(a!=SILVER_ORE && Microbot.getVarbitValue(Varbits.BLAST_FURNACE_SILVER_ORE)>0) k++;
        if(b!=SILVER_BAR && Microbot.getVarbitValue(Varbits.BLAST_FURNACE_SILVER_BAR)>0) k++;
        if(a!=GOLD_ORE && Microbot.getVarbitValue(Varbits.BLAST_FURNACE_GOLD_ORE)>0) k++;
        if(b!=GOLD_BAR && Microbot.getVarbitValue(Varbits.BLAST_FURNACE_GOLD_BAR)>0) k++;
        if(k==0){
            return true;
        } else {
            Microbot.showMessage("unwanted items in furnace, please remove.");
            return false;
        }
    }
    private void payForeman() {

        // Open the bank and deposit all items except the coal bag
        if (!Rs2Bank.isOpen()) {
            if(this.isRunning()) { openBank(); }
            if(this.isRunning()) { sleepUntil(Rs2Bank::isOpen, 60000); }
            if(this.isRunning()) { sleep(100,300); }
        }
        if(this.isRunning()) { Rs2Bank.depositAllExcept(ItemID.COAL_BAG_12019, ItemID.ICE_GLOVES, ItemID.GOLDSMITH_GAUNTLETS); }
        if(this.isRunning()) { Rs2Inventory.waitForInventoryChanges(700); }
        if(this.isRunning()) { sleep(100,300); }
        if(this.isRunning()) { Rs2Bank.withdrawAll("Coins"); }
        if(this.isRunning()) { Rs2Inventory.waitForInventoryChanges(700); }
        if(this.isRunning()) { sleep(100,300); }
        Rs2Npc.interact("blast furnace foreman", "pay");
        if(this.isRunning()) { sleepUntil(
                () -> Rs2Widget.hasWidget("Pay 2,500 coins to use the Blast Furnace"),
                () -> Rs2Player.isMoving(),
                6000
        ); }
        if(this.isRunning()) { sleep(100,300); }
        if(this.isRunning()) { Rs2Widget.clickWidget("Yes"); }
        if(this.isRunning()) { sleep(100,300); }

        // After payment, update the last payment time and go back to banking state
        if(this.isRunning()) { lastPaymentTime = System.currentTimeMillis(); }
        if (!Rs2Bank.isOpen()) {
            if(this.isRunning()) { openBank(); }
                if(this.isRunning()) { sleepUntil(Rs2Bank::isOpen, 60000); }
                if(this.isRunning()) { sleep(100,300); }
        }
            if(this.isRunning()) { Rs2Bank.depositAllExcept(ItemID.COAL_BAG_12019, ItemID.ICE_GLOVES, ItemID.GOLDSMITH_GAUNTLETS); }
            if(this.isRunning()) { Rs2Inventory.waitForInventoryChanges(700); }
    }
    @Override
    public void shutdown() {
        super.shutdown();
    }
}
