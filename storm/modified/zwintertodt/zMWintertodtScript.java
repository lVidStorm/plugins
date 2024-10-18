package net.runelite.client.plugins.microbot.storm.modified.zwintertodt;

import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.enums.Activity;
import net.runelite.client.plugins.microbot.util.antiban.enums.PlayStyle;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.plugins.microbot.storm.modified.zwintertodt.enums.State;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.runelite.api.Constants.GAME_TICK_LENGTH;
import static net.runelite.api.ObjectID.BRAZIER_29312;
import static net.runelite.api.ObjectID.BURNING_BRAZIER_29314;
import static net.runelite.client.plugins.microbot.util.Global.sleepGaussian;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;
import static net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory.items;
import static net.runelite.client.plugins.microbot.util.math.Random.random;
import static net.runelite.client.plugins.microbot.util.player.Rs2Player.eatAt;


/**
 * 27/04/2024 - Reworking the MWintertodtScript.java
 */

public class zMWintertodtScript extends Script {
    public static String version = "1.4.6";

    public static State state = State.BANKING;
    public static boolean resetActions = false;
    static zMWintertodtConfig config;
    static zMWintertodtPlugin plugin;
    private static boolean lockState = false;
    final WorldPoint BOSS_ROOM = new WorldPoint(1630, 3982, 0);
    String axe = "";
    final String SUPPLY_CRATE = "supply crate";
    int wintertodtHp = -1;
    static int requiredBurnableItems = 1;
    private GameObject brazier;

    private static void changeState(State scriptState) {
        changeState(scriptState, false);
    }

    private static void changeState(State scriptState, boolean lock) {
        requiredBurnableItems = random(2,6);
        if (state == scriptState || lockState) return;
        System.out.println("Changing current script state from: " + state + " to " + scriptState);
        state = scriptState;
        resetActions = true;
        setLockState(scriptState, lock);
        lockState = lock;
    }

    private static void setLockState(State state, boolean lock) {
        if (lockState == lock) return;
        lockState = lock;
        System.out.println("State " + state.toString() + " has set lockState to " + lockState);
    }

    private static boolean shouldFletchRoots() {
        if (!config.fletchRoots()) return false;
        if (!Rs2Inventory.hasItem(ItemID.BRUMA_ROOT)) {
            setLockState(State.FLETCH_LOGS, false);
            return false;
        }
        changeState(State.FLETCH_LOGS, true);
        return true;
    }

    public static void onHitsplatApplied(HitsplatApplied hitsplatApplied) {
        Actor actor = hitsplatApplied.getActor();

        if (actor != Microbot.getClient().getLocalPlayer()) {
            return;
        }

        resetActions = true;

    }

    public boolean run(zMWintertodtConfig config, zMWintertodtPlugin plugin) {
        zMWintertodtScript.config = config;
        zMWintertodtScript.plugin = plugin;
        Rs2Antiban.resetAntibanSettings();
        Rs2Antiban.antibanSetupTemplates.applyGeneralBasicSetup();
        Rs2Antiban.setActivity(Activity.GENERAL_WOODCUTTING);
        Rs2Antiban.setPlayStyle(PlayStyle.EXTREME_AGGRESSIVE);
        state = State.BANKING;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;

                long startTime = System.currentTimeMillis();

                if (config.axeInInventory()) {
                    if (!Rs2Inventory.hasItem("axe")) {
                        Microbot.showMessage("It seems that you selected axeInInventory option but no axe was found in your inventory.");
                        sleep(5000);
                        return;
                    }
                    axe = Rs2Inventory.get("axe").name;
                } else if (!Rs2Equipment.isWearing("axe")){
                    if (Rs2Inventory.hasItem("axe")) {
                        Rs2Inventory.wear("axe");
                        sleepUntil(() -> Rs2Equipment.isWearing("axe"));
                        return;
                    }
                    Microbot.showMessage("Please wear an axe OR If you'd like to have an axe in your inventory, you can enable the setting 'Axe In Inventory'.");
                    sleep(5000);
                    return;
                }


                boolean wintertodtRespawning = Rs2Widget.hasWidget("returns in");
                boolean isWintertodtAlive = Rs2Widget.hasWidget("Wintertodt's Energy");
                //TODO do something to have this change when the pyromancer dies.
                brazier = Rs2GameObject.findObject(BRAZIER_29312, config.brazierLocation().getOBJECT_BRAZIER_LOCATION());
                GameObject brokenBrazier = Rs2GameObject.findObject(ObjectID.BRAZIER_29313, config.brazierLocation().getOBJECT_BRAZIER_LOCATION());
                GameObject fireBrazier = Rs2GameObject.findObject(ObjectID.BURNING_BRAZIER_29314, config.brazierLocation().getOBJECT_BRAZIER_LOCATION());
                boolean playerIsLowWarmth = getWarmthLevel() < config.warmthTreshhold();
                boolean needBanking = !Rs2Inventory.hasItemAmount(config.food().getName(), config.minFood(), false, false)
                        && playerIsLowWarmth || !Rs2Inventory.hasItemAmount(config.food().getName(), config.minFood(), false, false)
                        && !isWintertodtAlive;
                Widget wintertodtHealthbar = Rs2Widget.getWidget(396, 26);

                if (wintertodtHealthbar != null && isWintertodtAlive) {
                    String widgetText = wintertodtHealthbar.getText();
                    wintertodtHp = Integer.parseInt(widgetText.split("\\D+")[1]);
                } else {
                    wintertodtHp = -1;
                }

                if (Rs2Widget.hasWidget("Leave and lose all progress")) {
                    Rs2Keyboard.typeString("1");
                    sleep(1600, 2000);
                    return;
                }

                dropUnnecessaryItems();
                shouldLightBrazier(isWintertodtAlive, needBanking, fireBrazier, brazier);
                shouldBank(needBanking);
                shouldEat();
                dodgeOrbDamage();

                if (!needBanking) {
                    if (!isWintertodtAlive) {
                        if (state != State.ENTER_ROOM && state != State.WAITING && state != State.BANKING) {
                            setLockState(State.GLOBAL, false);
                            changeState(State.WAITING);
                        }
                    } else {
                        handleMainLoop();
                    }
                } else {
                    setLockState(State.BANKING, false);
                }


                switch (state) {
                    case BANKING:
                        if (!handleBankLogic(config)) return;
                        if (Rs2Player.isFullHealth() && Rs2Inventory.hasItemAmount(config.food().getName(), config.foodAmount(), false, true)) {
                            zMWintertodtScript.plugin.setTimesBanked(plugin.getTimesBanked() + 1);
                            changeState(State.ENTER_ROOM);
                        }
                        break;
                    case ENTER_ROOM:
                        if (!wintertodtRespawning && !isWintertodtAlive) {
                            //TODO issue here?
                            if(Rs2Player.getWorldLocation().getY() < 3982) { Rs2Walker.walkTo(BOSS_ROOM, 12); }
                        } else {
                            state = State.WAITING;
                        }
                        break;


                    case WAITING:
                        walkToBrazier();
                        shouldLightBrazier(isWintertodtAlive, needBanking, fireBrazier, brazier);
                        break;
                    case LIGHT_BRAZIER:
                        //TODO need to add something here to detect down pyromancer
                        if (brazier != null && !Rs2Player.isAnimating() && !Rs2Player.isMoving()) {
                            sleep(67,97);
                            Rs2GameObject.interact(brazier, "light");
                            sleep(1000);
                            return;
                        }
                        break;
                    case CHOP_ROOTS:
                        Rs2Combat.setSpecState(true, 1000);
                        if (!Rs2Player.isAnimating() && !Rs2Player.isMoving() || resetActions) {
                            sleep(67,97);
                            Rs2GameObject.interact(ObjectID.BRUMA_ROOTS, "Chop");
                            sleepUntil(Rs2Player::isAnimating, 2000);
                            resetActions = false;
                        }
                        break;
                    case FLETCH_LOGS:
                        if (Rs2Player.getAnimation() != AnimationID.FLETCHING_BOW_CUTTING || resetActions) {
                            walkToBrazier();
                            Rs2Inventory.combine(ItemID.KNIFE, ItemID.BRUMA_ROOT);
                            Rs2Player.waitForAnimation();
                            resetActions = false;
                        }
                        break;
                    case BURN_LOGS:
                        if (!Microbot.isGainingExp || resetActions) {
                            TileObject burningBrazier = Rs2GameObject.findObjectById(BURNING_BRAZIER_29314);
                            sleep(67,97);
                            if (brokenBrazier != null && config.fixBrazier()) {
                                Rs2GameObject.interact(brokenBrazier, "fix");
                                Microbot.log("Fixing brazier");
                                sleep(1500,1800);
                                return;
                            }
                            // this extra check is needed in case all braziers are broken or not burning
                            if (burningBrazier == null && brazier != null && config.relightBrazier()) {
                                Rs2GameObject.interact(brazier, "light");
                                Microbot.log("Lighting brazier");
                                sleep(1500,1800);
                                return;
                            } else {
                                if (burningBrazier.getWorldLocation().distanceTo(Rs2Player.getWorldLocation()) > 10 && brazier != null && config.relightBrazier()) {
                                    Rs2GameObject.interact(brazier, "light");
                                    Microbot.log("Lighting brazier");
                                    sleep(1500,1800);
                                    return;
                                }
                            }
                            if (burningBrazier.getWorldLocation().distanceTo(Rs2Player.getWorldLocation()) < 10 && hasItemsToBurn()) {
                                Rs2GameObject.interact(burningBrazier, "feed");
                                Microbot.log("Feeding brazier");
                                resetActions = false;
                                sleep(1500, 1800);

                            }

                        }
                        break;
                }

                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                 Microbot.log(ex.getMessage());
                ex.printStackTrace();
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
        return true;
    }

    private void handleMainLoop() {
        if (isWintertodtAlmostDead() && hasItemsToBurn()) {
            setLockState(State.BURN_LOGS, false);
            if (shouldBurnLogs()) {
            }
        } else {
            if (shouldChopRoots()) return;
            if (shouldFletchRoots()) return;
            if (shouldBurnLogs()) {
            }
        }
    }

    private boolean shouldBurnLogs() {
        if (!hasItemsToBurn()) {
            setLockState(State.BURN_LOGS, false);
            return false;
        }
        changeState(State.BURN_LOGS, true);
        return true;
    }

    private boolean shouldEat() {
        if (getWarmthLevel() <= config.eatAtWarmthLevel()) {
            Rs2Player.useFood();
            sleepGaussian(600, 150);
            plugin.setFoodConsumed(plugin.getFoodConsumed() + 1);
            Rs2Inventory.dropAll("jug");
            resetActions = true;
            return true;
        }
        return false;
    }

    private boolean shouldBank(boolean needBanking) {
        if (!needBanking) return false;
        changeState(State.BANKING);
        return true;
    }

    private boolean shouldChopRoots() {
        if (Rs2Inventory.isFull()) {
            if (state == State.CHOP_ROOTS) {
                setLockState(State.CHOP_ROOTS, false);
            }
            return false;
        }
        if (hasItemsToBurn()) return false;
        changeState(State.CHOP_ROOTS, true);
        return true;
    }

    private boolean shouldLightBrazier(boolean isWintertodtAlive, boolean needBanking, GameObject fireBrazier, GameObject brazier) {
        if (!isWintertodtAlive) return false;
        if (needBanking) return false;
        if (state == State.CHOP_ROOTS) return false;// we are most likely to far from the brazier to light it in time

        if (brazier == null || fireBrazier != null) {
            setLockState(State.LIGHT_BRAZIER, false);
            return false;
        }

        changeState(State.LIGHT_BRAZIER, true);
        return true;
    }

    private boolean isWintertodtAlmostDead() {
        return wintertodtHp > 0 && wintertodtHp < 15;
    }

    //TODO hasItemsToBurn method changed to get more logs when we run out when wintertodt is almost dead, will decide a random amount from 1~6 needed to burn more.
    private boolean hasItemsToBurn() {
        int i = (int) (items().stream().filter(x -> x.id==ItemID.BRUMA_KINDLING).count()
                        +items().stream().filter(x -> x.id==ItemID.BRUMA_ROOT).count());
        //System.out.println("hasItemsToBurn == " + i);
        if ((Rs2GameObject.findObjectById(BURNING_BRAZIER_29314) != null
                ? Rs2GameObject.findObjectById(BURNING_BRAZIER_29314).getWorldLocation().distanceTo(Rs2Player.getWorldLocation())
                : 10) < 4 && i > 0 ){
            //System.out.println("Player is standing next to Brazier w/ things to burn");
            return true;
        } else return (Rs2GameObject.findObjectById(BURNING_BRAZIER_29314) != null
                ? Rs2GameObject.findObjectById(BURNING_BRAZIER_29314).getWorldLocation().distanceTo(Rs2Player.getWorldLocation())
                : 10) > 2 && i >= requiredBurnableItems;
    }

    private void dropUnnecessaryItems() {
        if (!config.fletchRoots() && Rs2Inventory.hasItem(ItemID.KNIFE)) {
            Rs2Inventory.drop(ItemID.KNIFE);
        }
        if (!config.fixBrazier() && Rs2Inventory.hasItem(ItemID.HAMMER)) {
            Rs2Inventory.drop(ItemID.HAMMER);
        }
        if ((Rs2Equipment.hasEquipped(ItemID.BRUMA_TORCH) || Rs2Equipment.hasEquipped(ItemID.BRUMA_TORCH_OFFHAND)) && Rs2Inventory.hasItem(ItemID.TINDERBOX)) {
            Rs2Inventory.drop(ItemID.TINDERBOX);
        }
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }

    private void walkToBrazier() {
        if (Rs2Player.getWorldLocation().distanceTo(config.brazierLocation().getBRAZIER_LOCATION()) > 6) {
            Rs2Walker.walkTo(config.brazierLocation().getBRAZIER_LOCATION(), 2);
        } else if (!Rs2Player.getWorldLocation().equals(config.brazierLocation().getBRAZIER_LOCATION())) {
            Rs2Walker.walkFastCanvas(config.brazierLocation().getBRAZIER_LOCATION());
            sleep(GAME_TICK_LENGTH);
//            if (Rs2Player.getWorldLocation().distanceTo(config.brazierLocation().getBRAZIER_LOCATION()) > 4) {
//                Rs2Player.waitForWalking();
//            } else {
//                //sleep(3000);
//            }
        } else if (Rs2Player.getWorldLocation().equals(config.brazierLocation().getBRAZIER_LOCATION()) && state == State.WAITING) {
            Rs2GameObject.hoverOverObject(brazier);
        }
    }

    private void dodgeOrbDamage() {
        for (GraphicsObject graphicsObject : Microbot.getClient().getGraphicsObjects()) {
            if (!resetActions && graphicsObject.getId() == 502
                    && WorldPoint.fromLocalInstance(Microbot.getClient(),
                    graphicsObject.getLocation()).distanceTo(Rs2Player.getWorldLocation()) == 1) {
                System.out.println(WorldPoint.fromLocalInstance(Microbot.getClient(),
                        graphicsObject.getLocation()).distanceTo(Rs2Player.getWorldLocation()));
                //walk south
                List<GameObject> gameObjects = new ArrayList<>(Rs2GameObject.getGameObjectsWithinDistance(5));
                Microbot.log("Game objects: " + gameObjects.size());
                // we only need to dodge if there are 2 or more snow fall objects
                if (gameObjects.size() > 2) {
                    Rs2Walker.walkFastCanvas(new WorldPoint(Rs2Player.getWorldLocation().getX(), Rs2Player.getWorldLocation().getY() - 1, Rs2Player.getWorldLocation().getPlane()));
                    Rs2Player.waitForWalking(1000);
                    sleep(GAME_TICK_LENGTH * 2);
                    resetActions = true;
                }

            }
        }
    }

    private boolean handleBankLogic(zMWintertodtConfig config) {
        boolean i;//this just used for sleepUntilTrue
        if (!Rs2Player.isFullHealth() && Rs2Inventory.hasItem(config.food().getName(), false)) {
            eatAt(99);
            return true;
        }
        if (Rs2Inventory.hasItemAmount(config.food().getName(), config.foodAmount())) {
            state = State.ENTER_ROOM;
            return true;
        }
        WorldPoint bankLocation = new WorldPoint(1640, 3944, 0);
        if (Rs2Player.getWorldLocation().distanceTo(bankLocation) > 6) {
            Rs2Walker.walkTo(bankLocation);
            Rs2Player.waitForWalking();
        }
        Rs2Bank.openBank(Rs2GameObject.findObjectById(29321));//changed previous openBank method to stop the initial freeze
        if (!Rs2Bank.isOpen()) return true;
        if(!config.fixBrazier()) {
            Rs2Bank.depositAll();
        }
        else {
            Rs2Bank.depositAllExcept("hammer", "tinderbox", "knife", config.food().getName());
        }
        int foodCount = (int) Rs2Inventory.getInventoryFood().stream().count();
        if (config.fixBrazier() && !Rs2Inventory.hasItem("hammer")) {
            Rs2Bank.withdrawX(true, "hammer", 1);
            i = sleepUntilTrue(() -> Rs2Inventory.hasItem("hammer"), random(47,187), random(3000,7000));
        }
        if (!Rs2Equipment.hasEquipped(ItemID.BRUMA_TORCH) && !Rs2Equipment.hasEquipped(ItemID.BRUMA_TORCH_OFFHAND)) {
            Rs2Bank.withdrawX(true, "tinderbox", 1, true);
            i = sleepUntilTrue(() -> Rs2Inventory.hasItem("tinderbox"), random(47,187), random(3000,7000));
        }
        if (config.fletchRoots()) {
            Rs2Bank.withdrawX(true, "knife", 1, true);
            i = sleepUntilTrue(() -> Rs2Inventory.hasItem("knife"), random(47,187), random(3000,7000));
        }
        if (config.axeInInventory()) {
            Rs2Bank.withdrawX(true, axe, 1);
            i = sleepUntilTrue(() -> Rs2Inventory.hasItem(axe), random(47,187), random(3000,7000));
        }
        if (!Rs2Bank.hasBankItem(config.food().getName(), config.foodAmount(), true)) {
            Microbot.showMessage("Insufficient food supply");
            Microbot.pauseAllScripts = true;
            return true;
        }
        Rs2Bank.withdrawX(config.food().getId(), config.foodAmount() - foodCount);
        return sleepUntilTrue(() -> Rs2Inventory.hasItemAmount(config.food().getName(), config.foodAmount(), false, true), 100, 5000);
    }

    public int getWarmthLevel() {
        String warmthWidgetText = Rs2Widget.getChildWidgetText(396, 20);

        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(warmthWidgetText);

        if (matcher.find()) {
            int warmthLevel = Integer.parseInt(matcher.group());
            return warmthLevel;
        }
        return 100;
    }
}