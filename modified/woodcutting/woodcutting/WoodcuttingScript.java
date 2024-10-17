package net.runelite.client.plugins.microbot.storm.modified.woodcutting.woodcutting;

import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.tile.Rs2Tile;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.storm.modified.woodcutting.woodcutting.enums.WoodcuttingWalkBack;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.runelite.client.plugins.microbot.util.math.Random.random;

enum State {
    RESETTING,
    WOODCUTTING,
}

public class WoodcuttingScript extends Script {

    public static String version = "1.6.3";
    public boolean cannotLightFire = false;

    State state = State.WOODCUTTING;
    private static WorldPoint returnPoint;

    public static WorldPoint initPlayerLoc(WoodcuttingConfig config) {
        if (config.walkBack() == WoodcuttingWalkBack.INITIAL_LOCATION) {
            return getInitialPlayerLocation();
        } else {
            return returnPoint;
        }
    }

    public boolean run(WoodcuttingConfig config) {
        if (config.hopWhenPlayerDetected()) {
            Microbot.showMessage("Make sure autologin plugin is enabled and randomWorld checkbox is checked!");
        }
        Rs2Antiban.resetAntibanSettings();
        Rs2Antiban.antibanSetupTemplates.applyWoodcuttingSetup();
        Rs2AntibanSettings.dynamicActivity = true;
        Rs2AntibanSettings.dynamicIntensity = true;
        initialPlayerLocation = null;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                if(Rs2AntibanSettings.actionCooldownActive && config.shouldUseAntiban()) return;
                if (initialPlayerLocation == null) {
                    initialPlayerLocation = Rs2Player.getWorldLocation();
                }
                if (returnPoint == null) {
                    returnPoint = Rs2Player.getWorldLocation();
                }
                if (!config.TREE().hasRequiredLevel()) {
                    Microbot.showMessage("You do not have the required woodcutting level to cut this tree.");
                    shutdown();
                    return;
                }
                //TODO update this so we also check for new highest player count trees~
                //TODO this needs to be changed so that it will continue if we aren't chopping the best tree
                //(isPlayerChoppingBestTree(config) && config.shouldGroup())
                if (!(config.shouldGroup() && !isPlayerChoppingBestTree(config))
                        && (Rs2Player.isMoving() || Rs2Player.isInteracting() || Rs2Player.isAnimating() || Microbot.pauseAllScripts || (Rs2AntibanSettings.actionCooldownActive && config.shouldUseAntiban()))) {
                    return;
                }
                if (Rs2AntibanSettings.actionCooldownActive && config.shouldUseAntiban())
                    return;

                switch (state) {
                    case WOODCUTTING:
                        if (config.hopWhenPlayerDetected()) {
                            if (Rs2Player.logoutIfPlayerDetected(1, 10000))
                                return;
                        }
                        if (Rs2Equipment.isWearing("Dragon axe") || Rs2Equipment.isWearing("Dragon felling axe"))
                            Rs2Combat.setSpecState(true, 1000);
                        if (Rs2Inventory.isFull()) {
                            state = State.RESETTING;
                            return;
                        }
                        //TODO changing this~
                        //GameObject tree = Rs2GameObject.findObject(config.TREE().getName(), true, config.distanceToStray(), false, getInitialPlayerLocation());
                        //TODO Retrieve all nearby game objects
                        GameObject selectedTree = getBestTree(config);
                        //TODO
                        if (selectedTree != null) {
                            if (Rs2GameObject.interact(selectedTree, config.TREE().getAction())) {
                                Rs2Player.waitForAnimation();
                                if(config.shouldUseAntiban()) { Rs2Antiban.actionCooldown(); } else { sleep(127,400); }

                                if (config.walkBack().equals(WoodcuttingWalkBack.LAST_LOCATION)) {
                                    returnPoint = Rs2Player.getWorldLocation();
                                }
                            }
                        } else {
                            System.out.println("Something went wrong, tree is still null.");
                        }
                        break;
                    case RESETTING:
                        resetInventory(config);
                        break;
                }
            } catch (Exception ex) {
                Microbot.log(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }

    private void resetInventory(WoodcuttingConfig config) {
        List<String> itemNames = Arrays.stream(config.itemsToBank().split(",")).map(String::toLowerCase).collect(Collectors.toList());
        String[] allItems = Stream.concat(Stream.of("axe", "tinderbox"),itemNames.stream()).toArray(String[]::new);
        switch (config.resetOptions()) {
            case DROP:
                Rs2Inventory.dropAllExcept(allItems);
                state = State.WOODCUTTING;
                break;
            case BANK:
                if (!Rs2Bank.bankItemsAndWalkBackToOriginalPosition(itemNames, calculateReturnPoint(config)))
                    return;

                state = State.WOODCUTTING;
                break;
            case FIREMAKE:
                burnLog(config);
                
                if (Rs2Inventory.contains(config.TREE().getLog())) return;

                walkBack(config);
                state = State.WOODCUTTING;
                break;
        }
    }

    private void burnLog(WoodcuttingConfig config) {
        WorldPoint fireSpot;
        if (Rs2Player.isStandingOnGameObject() || cannotLightFire) {
            fireSpot = fireSpot(1);
            Rs2Walker.walkFastCanvas(fireSpot);
            cannotLightFire = false;
        }
        if (!isFiremake()) {
            Rs2Inventory.waitForInventoryChanges(() -> {
                Rs2Inventory.use("tinderbox");
                sleep(random(300, 600));
                Rs2Inventory.use(config.TREE().getLog());
            });
        }
        sleepUntil(() -> (!isFiremake() && Rs2Player.waitForXpDrop(Skill.FIREMAKING)) || cannotLightFire, 5000);
    }

    private WorldPoint fireSpot(int distance) {
        List<WorldPoint> worldPoints = Rs2Tile.getWalkableTilesAroundPlayer(distance);
        WorldPoint playerLocation = Rs2Player.getWorldLocation();

        // Create a map to group tiles by their distance from the player
        Map<Integer, List<WorldPoint>> distanceMap = new HashMap<>();

        for (WorldPoint walkablePoint : worldPoints) {
            if (Rs2GameObject.getGameObject(walkablePoint) == null) {
                int tileDistance = playerLocation.distanceTo(walkablePoint);
                distanceMap.computeIfAbsent(tileDistance, k -> new ArrayList<>()).add(walkablePoint);
            }
        }

        // Find the minimum distance that has walkable points
        Optional<Integer> minDistanceOpt = distanceMap.keySet().stream().min(Integer::compare);

        if (minDistanceOpt.isPresent()) {
            List<WorldPoint> closestPoints = distanceMap.get(minDistanceOpt.get());

            // Return a random point from the closest points
            if (!closestPoints.isEmpty()) {
                int randomIndex = random(0, closestPoints.size());
                return closestPoints.get(randomIndex);
            }
        }

        // Recursively increase the distance if no valid point is found
        return fireSpot(distance + 1);
    }

    private boolean isFiremake() {
        return Rs2Player.isAnimating(1800) && Rs2Player.getLastAnimationID() == AnimationID.FIREMAKING;
    }

    private WorldPoint calculateReturnPoint(WoodcuttingConfig config) {
        if (config.walkBack().equals(WoodcuttingWalkBack.LAST_LOCATION)) {
            return returnPoint;
        } else {
            return initialPlayerLocation;
        }
    }

    private void walkBack(WoodcuttingConfig config) {
        Rs2Walker.walkTo(new WorldPoint(calculateReturnPoint(config).getX() - random(-1, 1), calculateReturnPoint(config).getY() - random(-1, 1), calculateReturnPoint(config).getPlane()));
        sleepUntil(() -> Rs2Player.getWorldLocation().distanceTo(calculateReturnPoint(config)) <= 4);
    }
    //TODO make a 2nd method to use as a core, and then reference it from here.
    private boolean isPlayerChoppingBestTree(WoodcuttingConfig config) {
        // Get the best tree with the most players cutting
        GameObject selectedTree = getBestTree(config);

        // If the best tree is null, return false
        if (selectedTree == null) {
            System.out.println("No best tree found.");
            return false;
        }
        /**
         * Represents an in-game orientation that uses fixed point arithmetic.
         * <p>
         * Angles are represented as an int value ranging from 0-2047, where the
         * following is true:
         * <ul>
         *     <li>0 is true South</li>
         *     <li>512 is true West</li>
         *     <li>1024 is true North</li>
         *     <li>1536 is true East</li>
         * </ul>
         */
        // Get the player's current location and orientation
        WorldPoint playerLocation = Rs2Player.getWorldLocation();
        int playerOrientation = Microbot.getClient().getLocalPlayer().getOrientation();
        // Get the selected tree's location
        WorldPoint treeLocation = selectedTree.getWorldLocation();

        // Calculate the angle from the player to the tree
        int deltaX = treeLocation.getX() - playerLocation.getX();
        int deltaY = treeLocation.getY() - playerLocation.getY();
        double angleToTree = Math.toDegrees(Math.atan2(deltaY, deltaX));

        if (angleToTree < 0) {
            angleToTree += 2047;  // Normalize angle to be between 0 and 360 degrees
        }

        // Check if the player is within interaction range (1 tile)
        if (playerLocation.distanceTo(treeLocation) <= 3) {
            //TODO issue here, angle is calculating well above 360.
            // Check if the player's orientation is close to the angle of the tree (within a threshold)
            if (Math.abs(playerOrientation - angleToTree) < 1792) {
                System.out.println("Player is chopping the best tree.");
                return true;
            } else {

                System.out.println("Player is near the best tree but not facing it. : "+convertAngle((int)Math.abs(playerOrientation - angleToTree)));
            }
        } else {
            System.out.println("Player is not near the best tree.");
        }

        return false;  // Player is not chopping the best tree
    }

    private GameObject getBestTree(WoodcuttingConfig config) {
        GameObject selectedTree = null;
            List<GameObject> gameObjects = Rs2GameObject.getGameObjectsWithinDistance(config.distanceToStray());

            WorldPoint playerLocation = Rs2Player.getWorldLocation();  // Get the player's current location
            int maxPlayers = 0;

            // Loop through all game objects to find the best tree
            for (GameObject gameObject : gameObjects) {
                ObjectComposition objComp = Rs2GameObject.convertGameObjectToObjectComposition(gameObject.getId());

                // Check if the object is a tree by its name
                if (objComp != null && objComp.getName().equalsIgnoreCase(config.TREE().getName())) {
                    int playersCutting = 0;

                    // Count players near this tree who are facing it
                    for (Player player : Rs2Player.getPlayers()) {
                        WorldPoint treeLocation = gameObject.getWorldLocation();
                        WorldPoint playerLocationNearby = player.getWorldLocation();

                        // Check if player is near the tree (within 1 tile)
                        if (playerLocationNearby.distanceTo(treeLocation) <= 3) {
                            // Calculate player's facing direction (orientation) compared to the tree's location
                            int playerOrientation = player.getOrientation();
                            int deltaX = treeLocation.getX() - playerLocationNearby.getX();
                            int deltaY = treeLocation.getY() - playerLocationNearby.getY();
                            double angleToTree = Math.toDegrees(Math.atan2(deltaY, deltaX));

                            if (angleToTree < 0) {
                                angleToTree += 360;  // Normalize angle to be between 0 and 360 degrees
                            }

                            // Check if the player's facing angle is within a 30-degree threshold of the tree
                            if (Math.abs(playerOrientation - angleToTree) < 1792) {
                                playersCutting++;  // Player is likely cutting the tree if they are near and facing it
                            }
                        }
                    }

                    // Update the selected tree if this one has more players cutting
                    if (playersCutting > maxPlayers) {
                        maxPlayers = playersCutting;
                        selectedTree = gameObject;
                        //System.out.println("Selected tree updated, set to: " + selectedTree);

                    }
                }
            }
            //System.out.println("Done fetching game objects.");
            // Fallback to the first tree within distance if no players are cutting any
            if (selectedTree == null) {
                System.out.println("For some reason, selectedTree is null...");
                selectedTree = gameObjects.stream()
                        .filter(gameObject -> {
                            ObjectComposition objComp = Rs2GameObject.convertGameObjectToObjectComposition(gameObject.getId());
                            return objComp != null && objComp.getName().equalsIgnoreCase(config.TREE().getName())
                                    && gameObject.getWorldLocation().distanceTo(playerLocation) <= config.distanceToStray();
                        })
                        .findFirst()
                        .orElse(null);
            }


        return selectedTree;
    }
    public static int convertAngle(int angle) {
        int quadrant = angle / 512;
        int offset = angle % 512;
        int angleWithinQuadrant = offset / 512 * 45;
        int startingAngle = quadrant * 90;
        return (startingAngle + angleWithinQuadrant) % 360;
    }
    @Override
    public void shutdown() {
        super.shutdown();
        returnPoint = null;
        initialPlayerLocation = null;
        Rs2Antiban.resetAntibanSettings();
    }
}