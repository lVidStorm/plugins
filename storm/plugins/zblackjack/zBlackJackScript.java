package net.runelite.client.plugins.microbot.storm.plugins.zblackjack;

import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.storm.plugins.zblackjack.enums.Area;
import net.runelite.client.plugins.microbot.storm.plugins.zblackjack.enums.State;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.shortestpath.ShortestPathPlugin;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.shop.Rs2Shop;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static net.runelite.client.plugins.microbot.storm.plugins.zblackjack.enums.State.*;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;
import static net.runelite.client.plugins.microbot.util.math.Random.random;
import static net.runelite.client.plugins.microbot.util.walker.Rs2Walker.getTile;

public class zBlackJackScript extends Script {
    public static double version = 3.2;
    public static State state = BANKING;
    zBlackJackConfig config;
    static boolean firstHit=false;
    boolean firstPlayerOpenCurtain = false;
    boolean initScript = false;
    boolean npcIsTrapped = false;
    boolean knockout = false;
    static boolean koPassed=false;
    static boolean isPlayerNearby = false;
    static boolean npcsCanSeeEachother = false;
    NPC npc;
    public static List<NPC> npcsInArea = new ArrayList();
    static int playerHit=0;
    int lureFailed=0;
    int previousHP;
    static int hitsplatXP;
    int koXpDrop;
    int xpDrop;
    int hitReactTime = 110;
    int pickpomin = 200;
    int pickpomax = 365;
    static int bjCycle = 0;
    int emptyJug = 1935;
    int notedWine = 1994;
    int unnotedWine= 1993;
    int pollniveachTeleport = 11743;
    static long hitsplatStart;
    long hitReactStart;
    long xpdropstartTime;
    long startTime;
    long endTime;
    long previousAction;
    WorldPoint shopsLocation = new WorldPoint(3360, 2988, 0);
    private boolean hasRequiredItems() {
        return Rs2Inventory.hasItem("Coins")
                && Rs2Equipment.isWearing("blackjack")
                && (Rs2Equipment.isWearing(config.teleportItemToBank()) || Rs2Inventory.hasItem(config.teleportItemToBank()))
                && Rs2Inventory.hasItem(notedWine);
    }
    private boolean withdrawRequiredItems() {
        Rs2Bank.depositAll();
        if (this.isRunning()) sleep(600, 1000);
        Rs2Bank.withdrawX("Coins", 1000);
        if (this.isRunning()) sleepUntil(() -> Rs2Inventory.hasItem("Coins"));
        if (this.isRunning()) sleep(80, 120);
        Rs2Bank.withdrawAll(notedWine);//noted wines
        if (this.isRunning()) sleepUntil(() -> Rs2Inventory.hasItem(notedWine));
        if (this.isRunning()) sleep(80, 120);
        if(!Rs2Equipment.isWearing(config.teleportItemToBank())) {
            Rs2Bank.withdrawX(config.teleportItemToBank(), 1);
            if (this.isRunning()) sleepUntil(() -> Rs2Inventory.hasItem(config.teleportItemToBank()));
        }
        if (this.isRunning()) sleep(80, 120);
        Rs2Bank.withdrawX(pollniveachTeleport, 1);//Pollnivneach teleport(make with redirect scroll
        if (this.isRunning()) sleepUntil(() -> Rs2Inventory.hasItem(pollniveachTeleport));
        if (this.isRunning()) sleep(800, 1200);
        return true;
    }

    public boolean run(zBlackJackConfig config) {
        this.config = config;
        hitReactTime = config.maxReactTime();
        pickpomin = config.minTime();
        pickpomax = config.maxTime();
        initScript = true;
        state = BANKING;
        Microbot.enableAutoRunOn = false;
        useStaminaPotsIfNeeded = false;

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {

            try {
                if (!super.run()) return;
                if (!Microbot.isLoggedIn()){sleep(1000); return;}
                startTime = System.currentTimeMillis();
                if (initScript) {
                    previousHP = Microbot.getClient().getBoostedSkillLevel(Skill.HITPOINTS);
                    if(hasRequiredItems()) {
                        if (Rs2Player.getWorldLocation().distanceTo(config.THUGS().location) < 30) {
                            state = WALK_TO_THUGS;
                        } else {
                            if(Rs2Inventory.hasItem(pollniveachTeleport)){
                                if (this.isRunning()) Rs2Inventory.interact(pollniveachTeleport, "break");
                                if (this.isRunning()) Rs2Player.waitForAnimation();
                                if (this.isRunning()) sleep(300,900);
                            }
                        }
                    } else {
                        state = BANKING;
                    }
                    initScript = false;
                }
                if (!this.isRunning()) {
                   Rs2Walker.setTarget(null);
                }
                if(state==BLACKJACK){
                    if(knockout&&Microbot.getClient().getLocalPlayer().getAnimation()!=401&&!koPassed){
                        hitReactStart=System.currentTimeMillis();
                        if (this.isRunning()) sleepUntil(() ->Microbot.getClient().getLocalPlayer().getAnimation()==401, (hitReactTime-10));
                        if(Microbot.getClient().getLocalPlayer().getAnimation()==401) {
                            koPassed = true;
                        }
                    }
                }
                handlePlayerHit();
                if(state==BLACKJACK){
                    if(!checkCurtain(config.THUGS().door)) {
                        if (!isPlayerNearby) {
                            if (this.isRunning()) sleep(120, 240);
                            if (this.isRunning()) Rs2GameObject.interact(config.THUGS().door, "Close");
                            if (this.isRunning()) sleepUntil(() -> checkCurtain(config.THUGS().door), 5000);
                            bjCycle = 0;
                            if (this.isRunning()) sleep(120, 240);
                            if (state == BLACKJACK) {
                                state = WALK_TO_THUGS;
                            }
                        } else {
                            int r = random(1,4);
                            if(r==4 && bjCycle==0 && firstPlayerOpenCurtain){
                                if (this.isRunning()) sleep(400,600);
                                if (this.isRunning()) Rs2GameObject.interact(config.THUGS().door, "Close");
                                if (this.isRunning()) sleepUntil(() -> checkCurtain(config.THUGS().door), 3000);
                                bjCycle = 0;
                                if (this.isRunning()) sleep(400, 600);
                                if (state == BLACKJACK) {
                                    state = WALK_TO_THUGS;
                                }
                            }
                            if (npcsCanSeeEachother) {
                                int e=0;
                                while (e < 3){
                                    //TODO :cries: sure hope this is right
                                    npcsInArea = Microbot.getClient().getNpcs().stream().filter(x ->
                                                    Objects.requireNonNull(x.getName()).contains(config.THUGS().displayName)).filter(x ->
                                                    x.getWorldLocation().getX() >= config.THUGS().thugArea.ax && x.getWorldLocation().getY() >= config.THUGS().thugArea.ay &&
                                                    x.getWorldLocation().getX() <= config.THUGS().thugArea.bx && x.getWorldLocation().getY() <= config.THUGS().thugArea.by &&
                                                    x.getCombatLevel()==config.THUGS().thugLevel)
                                            .collect(Collectors.toList());
                                    if ((npcsInArea.size() == 1) || (checkCurtain(config.THUGS().door) && npcsInArea.size() == 1)) {
                                        npcsCanSeeEachother=false;
                                        break;
                                    }
                                    if (this.isRunning()) sleep(3000);
                                    e++;
                                }
                                if (e==3){
                                    npcsCanSeeEachother=false;
                                    state = BANKING;
                                }
                            }
                            if(bjCycle==0 && !firstPlayerOpenCurtain) {
                                firstPlayerOpenCurtain = true;
                            }
                        }
                    } else {
                        if(firstPlayerOpenCurtain){
                            firstPlayerOpenCurtain=false;
                        }
                    }
                }
                if (Microbot.getClient().getBoostedSkillLevel(Skill.HITPOINTS) <= config.healAt() || !Rs2Inventory.hasItem(unnotedWine)) {
                    if (!Rs2Inventory.hasItem(unnotedWine)) {
                        if (!Rs2Inventory.hasItem(notedWine)) {
                            state = BANKING;
                        } else {
                            state = UN_NOTING;
                        }
                    } else {
                        if (this.isRunning()) sleep(120,240);
                        if (this.isRunning()) Rs2Inventory.interact(unnotedWine, "drink");
                        if (this.isRunning()) sleep(120,240);
                    }
                }
                switch (state) {
                    case BANKING:
                        //System.out.println("state == BANKING");
                        if(inArea(Rs2Player.getWorldLocation(), config.THUGS().thugArea) && isPlayerNearby){
                            if (this.isRunning()) sleep(120,240);
                            if (this.isRunning()) Rs2Equipment.interact(config.teleportItemToBank(), config.teleportActionToBank());
                            if (this.isRunning()) Rs2Player.waitForAnimation();
                            if (this.isRunning()) sleep(1000, 3000);
                        }
                        // need to teleport to bank
                            if(!hasRequiredItems()){
                                // add config item teleport here
                                boolean foundBank = Rs2Bank.openBank();
                                if (!foundBank) {
                                    if (this.isRunning()) sleep(120,240);
                                    if (this.isRunning()) Rs2Equipment.interact(config.teleportItemToBank(), config.teleportActionToBank());
                                    if (this.isRunning()) Rs2Player.waitForAnimation();
                                    if (this.isRunning()) sleep(80, 120);
                                    if(!Rs2Bank.walkToBank()) {
                                        if (this.isRunning()) Rs2Bank.walkToBank();
                                        return;
                                    }
                                    return;
                                }
                                if (this.isRunning()) sleepUntil(() -> Rs2Bank.isOpen());
                                if (this.isRunning()) sleep(80, 120);
                                if (Rs2Bank.isOpen()) {
                                    boolean result = withdrawRequiredItems();
                                    if (!result) return;
                                    Rs2Bank.closeBank();
                                    if (this.isRunning()) sleepUntil(() -> !Rs2Bank.isOpen());
                                    if (this.isRunning()) sleep(80, 120);
                                    if(config.wearTeleportItem() && !Rs2Equipment.isWearing(config.teleportItemToBank())){
                                        if(Rs2Inventory.hasItem(config.teleportItemToBank())){
                                            if (this.isRunning()) Rs2Inventory.wear(config.teleportItemToBank());
                                        }
                                    }
                                }
                                // teleport to pollniveach
                                if(Rs2Inventory.hasItem(pollniveachTeleport)){
                                    if (this.isRunning()) Rs2Inventory.interact(pollniveachTeleport, "break");
                                    if (this.isRunning()) Rs2Player.waitForAnimation();
                                    if (this.isRunning()) sleep(300,900);
                                    return;
                                }
                            }

                        state = UN_NOTING;
                        break;
                    case UN_NOTING:
                        if (Microbot.getClient().getLocalPlayer().hasSpotAnim(245)) {
                            if (this.isRunning()) sleepUntil(() -> !Microbot.getClient().getLocalPlayer().hasSpotAnim(245),5000);
                        }
                            if(!inArea(Rs2Player.getWorldLocation(), Area.ShopsArea)){
                              if(inArea(Rs2Player.getWorldLocation(), config.THUGS().thugArea)){
                                if (this.isRunning()) sleep(120,240);
                                if(checkCurtain(config.THUGS().door)){
                                    if (this.isRunning()) sleep(120,240);
                                    if (this.isRunning()) Rs2GameObject.interact(config.THUGS().door, "Open");
                                    if (this.isRunning()) sleepUntil(() -> !checkCurtain(config.THUGS().door), 10000);
                                    if (this.isRunning()) sleep(160,320);
                                    if (this.isRunning()) Rs2Walker.walkFastCanvas(new WorldPoint(config.THUGS().escapeTiles[0],config.THUGS().escapeTiles[1],Rs2Player.getWorldLocation().getPlane()));
                                    if (this.isRunning()) sleepUntil(() -> Rs2Player.getWorldLocation().getX()==config.THUGS().escapeTiles[0] && Rs2Player.getWorldLocation().getY()==config.THUGS().escapeTiles[1],10000);
                                    if (this.isRunning()) sleep(160,320);
                                    if (this.isRunning()) Rs2GameObject.interact(config.THUGS().door, "Close");
                                    if (this.isRunning()) sleepUntil(() -> checkCurtain(config.THUGS().door), 5000);
                                    if (this.isRunning()) sleep(120,240);
                                    if (this.isRunning()) Rs2Player.toggleRunEnergy(true);
                                    if (this.isRunning()) sleep(220,360);
                                } else {
                                    if (this.isRunning()) Rs2Walker.walkFastCanvas(new WorldPoint(config.THUGS().escapeTiles[0],config.THUGS().escapeTiles[1],Rs2Player.getWorldLocation().getPlane()));
                                    if (this.isRunning()) sleepUntil(() -> Rs2Player.getWorldLocation().getX()==config.THUGS().escapeTiles[0] && Rs2Player.getWorldLocation().getX()==config.THUGS().escapeTiles[1],2000);
                                    if (this.isRunning()) sleep(220,360);
                                    if (this.isRunning()) Rs2GameObject.interact(config.THUGS().door, "Close");
                                    if (this.isRunning()) sleepUntil(() -> checkCurtain(config.THUGS().door), 5000);
                                    if (this.isRunning()) sleep(220,360);
                                }
                            }
                            Rs2Walker.walkTo(shopsLocation, 2);
                            if (this.isRunning()) sleepUntil(() -> inArea(Rs2Player.getWorldLocation(),Area.ShopsArea), 10000);
                            ShortestPathPlugin.getPathfinder().cancel();
                            if (this.isRunning()) sleep(300,400);
                            return;
                        }
                        if(!Rs2Inventory.hasItem(unnotedWine)){
                            if(Rs2Inventory.hasItem(emptyJug)) {
                                if (this.isRunning()) sleep(220,340);
                                if (this.isRunning()) Rs2Npc.interact(3537, "trade");
                                if (this.isRunning()) sleepUntil(() -> Rs2Shop.isOpen(), 5000);
                                if (this.isRunning()) sleep(620, 860);
                                if(Rs2Shop.isOpen()){
                                    if (Rs2Inventory.hasItem(Rs2Inventory.get(emptyJug).name)) {
                                        if (this.isRunning()) Rs2Inventory.sellItem(Rs2Inventory.get(emptyJug).name, "50");
                                        if (this.isRunning()) sleepUntil(() -> !Rs2Inventory.hasItem(Rs2Inventory.get(emptyJug).name));
                                        if (this.isRunning()) sleep(400, 860);
                                        if (this.isRunning()) Rs2Shop.closeShop();
                                        if (this.isRunning()) sleepUntil(() -> !Rs2Shop.isOpen(), 5000);
                                        if (this.isRunning()) sleep(400, 860);
                                    }
                                }
                            }
                            if(Rs2Inventory.hasItem(notedWine) && !Rs2Inventory.hasItem(emptyJug)){
                                if (!Rs2Inventory.isItemSelected()) {
                                    if (this.isRunning()) Rs2Inventory.use(notedWine);
                                    if (this.isRunning()) sleep(280, 360);
                                } else {
                                    if (this.isRunning()) sleep(120,240);
                                    if (this.isRunning()) Rs2Npc.interact(1615, "Use");
                                    if (this.isRunning()) sleepUntil(() -> Microbot.getClient().getWidget(14352385) != null,2000);
                                    if (this.isRunning()) sleep(120,240);
                                    if (Microbot.getClient().getWidget(14352385) != null) {
                                        if (this.isRunning()) Rs2Keyboard.keyPress('3');
                                        if (this.isRunning()) sleepUntil(() -> Rs2Inventory.hasItem(unnotedWine),2000);
                                        if (this.isRunning()) sleep(240, 450);
                                    }
                                }
                            }
                        }
                        if(!Rs2Inventory.hasItem(notedWine)){
                            state = BANKING;
                            return;
                        }
                        if(!Rs2Inventory.hasItem(unnotedWine)){
                            return;
                        }
                        state = WALK_TO_THUGS;
                        break;
                    case WALK_TO_THUGS:
                        if (inArea(Rs2Player.getWorldLocation(), config.THUGS().thugArea)) {
                            npcsInArea = Microbot.getClient().getNpcs().stream().filter(x ->
                            Objects.requireNonNull(x.getName()).contains(config.THUGS().displayName)).filter(x ->
                            x.getWorldLocation().getX() >= config.THUGS().thugArea.ax && x.getWorldLocation().getY() >= config.THUGS().thugArea.ay &&
                            x.getWorldLocation().getX() <= config.THUGS().thugArea.bx && x.getWorldLocation().getY() <= config.THUGS().thugArea.by &&
                            x.getCombatLevel()==config.THUGS().thugLevel)
                            .collect(Collectors.toList());
                            if(npcsInArea.isEmpty()){
                                if (this.isRunning()) sleep(120,240);
                                if(checkCurtain(config.THUGS().door)) {
                                    if (this.isRunning()) Rs2GameObject.interact(config.THUGS().door, "Open");
                                    if (this.isRunning()) sleepUntil(() -> !checkCurtain(config.THUGS().door), 5000);
                                    if (this.isRunning()) sleep(120, 240);
                                }
                                npcIsTrapped=false;
                                state = TRAP_NPC;
                                npc = Microbot.getClientThread().runOnClientThread(() -> Microbot.getClient().getNpcs().stream()
                                        .filter(x -> x != null && x.getName() != null && !x.isDead()
                                        && Objects.requireNonNull(x.getName()).contains(config.THUGS().displayName)
                                        && x.getCombatLevel()==config.THUGS().thugLevel)
                                        .sorted(Comparator.comparingInt(value -> value.getLocalLocation()
                                        .distanceTo(new LocalPoint(3337,2950,0))))).findFirst().get();
                                return;
                            } else {
                                if(npcsInArea.size()>1){
                                    state = LURE_AWAY;
                                    return;
                                }
                                if (this.isRunning()) sleep(120,240);
                                  if(checkCurtain(config.THUGS().door)){
                                    if (this.isRunning()) sleep(120,240);
                                    if (!npcIsTrapped) {
                                        npcIsTrapped = true;
                                    }
                                      state = BLACKJACK;
                                } else {
                                    npcIsTrapped = true;
                                    state = BLACKJACK;
                                }
                                  npc = npcsInArea.stream().findFirst().get();
                            }
                        } else {
                            if (this.isRunning()) {
                                if (!Rs2Walker.walkFastCanvas(config.THUGS().location)) {
                                    Rs2Walker.walkTo(config.THUGS().location, 1);
                                }
                            }
                            if (this.isRunning()) sleepUntil(() -> inArea(Rs2Player.getWorldLocation(),config.THUGS().thugArea), 10000);
                            ShortestPathPlugin.getPathfinder().cancel();
                            if (this.isRunning()) sleep(120,200);
                            return;
                        }
                        break;
                    case LURE_AWAY:
                        npc = Rs2Npc.getNpcs().findFirst().get();
                        if (this.isRunning()) sleep(120,240);
                        if (lure_NPC(npc)){
                            if (this.isRunning()) sleep(60, 180);
                            state = RUN_AWAY;
                        }

                        break;
                    case TRAP_NPC:
                        //System.out.println("state == TRAP_NPC");
                        if(!npcIsTrapped){
                            if(lure_NPC(npc)) {
                                if(lureFailed>0){
                                    lureFailed=0;
                                }
                                if (this.isRunning()) sleep(60, 180);
                                state = WALK_TO_THUGS;
                                return;
                            } else {
                                lureFailed++;
                                if(lureFailed==5){
                                    lureFailed=0;
                                    state = BANKING;
                                }
                            }
                        } else {
                            state = BLACKJACK;
                            return;
                        }
                        //break;
                    case RUN_AWAY:
                        //System.out.println("state == RUN_AWAY");
                        if (this.isRunning()) Rs2Player.toggleRunEnergy(true);
                        if (config.THUGS().needsToLeaveHut) {
                        if(checkCurtain(config.THUGS().door)) {
                            if (this.isRunning()) sleep(240, 290);
                                if (this.isRunning()) Rs2GameObject.interact(config.THUGS().door, "Open");
                                if (this.isRunning()) sleepUntil(() -> !checkCurtain(config.THUGS().door), 3000);
                                if (this.isRunning()) sleep(220, 280);
                            }
                            Rs2Player.getWorldLocation().distanceTo(config.THUGS().door);
                            if (this.isRunning()) {
                                if (!Rs2Walker.walkFastCanvas(new WorldPoint(config.THUGS().escapeTiles[0], config.THUGS().escapeTiles[1], 0))) {
                                    Rs2Walker.walkTo(new WorldPoint(config.THUGS().escapeTiles[0], config.THUGS().escapeTiles[1], 0), 1);
                                }
                            }
                            if (this.isRunning()) sleepUntil(() -> Rs2Player.getWorldLocation().getX() == config.THUGS().escapeTiles[0] && Rs2Player.getWorldLocation().getY() == config.THUGS().escapeTiles[1], 5000);
                            if (this.isRunning()) sleep(320, 380);
                            if (this.isRunning()) Rs2GameObject.interact(config.THUGS().door, "Close");
                            if (this.isRunning()) sleepUntil(() -> checkCurtain(config.THUGS().door), 5000);
                            if (this.isRunning()) sleep(320, 380);
                            if (this.isRunning()) {
                                if (!Rs2Walker.walkFastCanvas(new WorldPoint(config.THUGS().escapeTiles[2], config.THUGS().escapeTiles[3], 0))) {
                                    Rs2Walker.walkTo(new WorldPoint(config.THUGS().escapeTiles[2], config.THUGS().escapeTiles[3], 0), 1);
                                }
                            }
                            if (this.isRunning()) sleepUntil(() -> Rs2Player.getWorldLocation().getX() == config.THUGS().escapeTiles[2] && Rs2Player.getWorldLocation().getY() == config.THUGS().escapeTiles[3], 8000);
                        }
                            if (this.isRunning()) sleep(300, 400);
                        if (this.isRunning()) Rs2GameObject.interact(config.THUGS().escapeObjectTile[0],true);
                        if (this.isRunning()) sleepUntil(() -> Microbot.getClient().getLocalPlayer().getWorldLocation().getPlane()==1,8000);
                        if (this.isRunning()) sleep(1201,2401);
                        if (this.isRunning()) Rs2GameObject.interact(config.THUGS().escapeObjectTile[1],true);
                        if (this.isRunning()) sleepUntil(() -> Microbot.getClient().getLocalPlayer().getWorldLocation().getPlane()==0,8000);
                        if (this.isRunning()) sleep(320,380);
                        state = WALK_TO_THUGS;
                        return;

                    case BLACKJACK:
                        //System.out.println("state == BLACKJACK");
                        if(!npcIsTrapped){
                            state = TRAP_NPC;
                            return;
                        }
                        if (bjCycle == 0){
                            previousHP = Microbot.getClient().getBoostedSkillLevel(Skill.HITPOINTS);
                            xpdropstartTime = System.currentTimeMillis();
                            koXpDrop = Microbot.getClient().getSkillExperience(Skill.THIEVING);
                            if(System.currentTimeMillis()>(previousAction+random(500,700)) || !knockout ) {
                                if (this.isRunning()) Rs2Npc.interact(npc, "Knock-Out");
                            }
                            previousAction=System.currentTimeMillis();
                            knockout = true;
                            endTime = System.currentTimeMillis();
                            ++bjCycle;
                            return;
                        }
                        if (bjCycle <= 2){
                            if(knockout && !firstHit){
                                if(npc.getAnimation() != 838) { if (this.isRunning()) sleepUntil(() -> npc.getAnimation() == 838, 600); }
                            }
                            xpDrop = Microbot.getClient().getSkillExperience(Skill.THIEVING);
                            xpdropstartTime = System.currentTimeMillis();
                            // 360ms is good.370ms starts to miss.350ms decent. 350~365
                            if((previousAction+1140+pickpomin)>System.currentTimeMillis()) {
                                if (this.isRunning()) sleep((int) ((previousAction + 840 + random(pickpomin, pickpomax)) - System.currentTimeMillis()));
                            }
                            if(npc.getAnimation()==838) {
                                if (this.isRunning()) Rs2Npc.interact(npc, "Pickpocket");
                                knockout=false;
                                if (this.isRunning()) sleepUntil(() -> xpDrop < Microbot.getClient().getSkillExperience(Skill.THIEVING), 1000);
                            } else {
                                if (this.isRunning()) sleep(90,140);
                                bjCycle=0;
                                return;
                            }
                            previousAction=System.currentTimeMillis();
                            endTime = System.currentTimeMillis();
                            ++bjCycle;
                            return;
                        }
                        if(npc.getAnimation()==838) {
                            if (this.isRunning()) sleepUntil(() -> npc.getAnimation() != 838, 800);
                        }
                        if (this.isRunning()) sleep(120,180);
                        bjCycle=0;
                        break;

                }
                endTime = System.currentTimeMillis();
                //long totalTime = endTime - startTime;
                //System.out.println("Total time for loop " + totalTime);
                if (!this.isRunning()) {
                    Rs2Walker.setTarget(null);
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 10, TimeUnit.MILLISECONDS);
        return true;
    }
    public void handlePlayerHit(){
        if(playerHit>=1) {
            int j = 0;
            int i = random(2, 3);
            int c = 120;
            if (playerHit == 1 && firstHit) {
                if((hitReactStart+hitReactTime)>System.currentTimeMillis()) {
                    if (this.isRunning()) sleep(60, (int) ((hitReactStart+hitReactTime) - System.currentTimeMillis()));
                }
                while (j < i) {
                    if (this.isRunning()) Rs2Npc.interact(npc, "Pickpocket");
                    if (this.isRunning()) sleep(c, (int) (c * 1.3));
                    c = (int) (c * 1.4);
                    ++j;
                }
                knockout=false;
                firstHit = false;
                bjCycle = 0;
            }
            boolean hasStars = Microbot.getClient().getLocalPlayer().hasSpotAnim(245);
            if (!hasStars) {
                if (playerHit <= 1 || Microbot.getClient().getSkillExperience(Skill.THIEVING)> zBlackJackScript.hitsplatXP) {
                    playerHit = 0;
                } else {
                    playerHit = 0;
                    state = RUN_AWAY;
                    knockout = false;
                    bjCycle = 0;
                }
                previousHP = Microbot.getClient().getBoostedSkillLevel(Skill.HITPOINTS);
            }
        }
    }
    public static boolean checkCurtain(WorldPoint a) {
        Tile currentTile = getTile(a);
        WallObject wallObject;
        if (currentTile != null) {
            wallObject = currentTile.getWallObject();
        } else {
            wallObject = null;
        }

        if (wallObject != null) {
            ObjectComposition objectComposition = Rs2GameObject.getObjectComposition(wallObject.getId());
            if (objectComposition == null) {
                return false;
            }
            for (String action : objectComposition.getActions()) {
                return action != null && (action.equals("Open"));
            }
        }
        return false;
    }
    public boolean lure_NPC(NPC npc){
        if (this.isRunning()) sleep(200,260);
        if (this.isRunning()) Rs2Npc.interact(npc, "Lure");
        boolean lureStarted = sleepUntilTrue(() -> Rs2Widget.hasWidget("Psst. Come here, I want to show you something."), 300, 15000);
        if (this.isRunning()) Rs2Player.toggleRunEnergy(false);
        if(lureStarted){
            if (this.isRunning()) sleep(120,160);
            if (this.isRunning()) Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
            if (this.isRunning()) sleepUntilTrue(() -> !Rs2Widget.hasWidget("Psst. Come here, I want to show you something."), 300, 3000);
            if (this.isRunning()) sleep(120,160);
        } else {
            return false;
        }
        boolean lureResult = Rs2Widget.hasWidget("What is it?");
        if(lureResult){
            if (this.isRunning()) Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
            if (this.isRunning()) sleepUntilTrue(() -> !Rs2Widget.hasWidget("What is it?"), 300, 3000);
            if (this.isRunning()) sleep(320,460);
            if (this.isRunning()) Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
            if (this.isRunning()) sleep(320,460);
            if (this.isRunning()) {
                if (!Rs2Walker.walkFastCanvas(new WorldPoint(3346, 2955, 0))) {
                Rs2Walker.walkTo(new WorldPoint(3346, 2955, 0), 1);
                }
            }
            if (this.isRunning()) sleepUntil(() -> Rs2Player.getWorldLocation().getX()==3346 && Rs2Player.getWorldLocation().getY()==2955);
            if (this.isRunning()) sleep(120,160);
            if (this.isRunning()) waitForNPC(npc);
            if(npc.getWorldLocation().getY()<Rs2Player.getWorldLocation().getY()){
                if (this.isRunning()) {
                    if (!Rs2Walker.walkFastCanvas(new WorldPoint(3346, 2959, 0))) {
                        Rs2Walker.walkTo(new WorldPoint(3346, 2959, 0), 1);
                    }
                }
                if (this.isRunning()) sleepUntil(() -> Rs2Player.getWorldLocation().getY()>=2958, 3000);
                if (this.isRunning()) waitForNPC(npc);
                if (this.isRunning()) {
                    if (!Rs2Walker.walkFastCanvas(new WorldPoint(3346, 2955, 0))) {
                        Rs2Walker.walkTo(new WorldPoint(3346, 2955, 0), 1);
                    }
                }
                if (this.isRunning()) sleepUntil(() -> Rs2Player.getWorldLocation().getX()==3346 && Rs2Player.getWorldLocation().getY()==2955);
                if (this.isRunning()) waitForNPC(npc);
                if (this.isRunning()) {
                    if (!Rs2Walker.walkFastCanvas(new WorldPoint(3343, 2954, 0))) {
                        Rs2Walker.walkTo(new WorldPoint(3343, 2954, 0), 1);
                    }
                }
                if (this.isRunning()) sleepUntil(() -> Rs2Player.getWorldLocation().getX()==3343 && Rs2Player.getWorldLocation().getY()==2954, 3000);
                if (this.isRunning()) Rs2Player.toggleRunEnergy(true);
            } else {
                if (this.isRunning()) {
                    if (!Rs2Walker.walkFastCanvas(new WorldPoint(3343, 2954, 0))) {
                    Rs2Walker.walkTo(new WorldPoint(3343, 2954, 0), 1); }
                }
                if (this.isRunning()) sleepUntil(() -> Rs2Player.getWorldLocation().getX()==3343 && Rs2Player.getWorldLocation().getY()==2954, 3000);
                if (this.isRunning()) waitForNPC(npc);
                if (this.isRunning()) Rs2Player.toggleRunEnergy(true);
            }
            npcIsTrapped=true;
            return true;
        } else {
            if (this.isRunning()) sleep(300,600);
            return false;
        }
    }
    public void waitForNPC(NPC npc){
        long movingStart = System.currentTimeMillis();
        while(npc.getWorldLocation().distanceTo(Rs2Player.getWorldLocation())!=1){
            WorldPoint isMoving = npc.getWorldLocation();
            if (this.isRunning()) sleep(1000);
            if(npc.getWorldLocation()==isMoving){
                break;
            }
            if((System.currentTimeMillis()-movingStart)>=15000){
                break;
            }
        }
    }
    public static boolean inArea(WorldPoint entity, Area area){
        return (entity.getX() >= area.ax && entity.getY() >= area.ay) && (entity.getX() <= area.bx && entity.getY() <= area.by);
    }
    @Override
    public void shutdown() {
        super.shutdown();
    }
}
