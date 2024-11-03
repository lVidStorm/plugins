package net.runelite.client.plugins.microbot.storm.modified.slayer;

import lombok.Getter;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.NPCManager;
import net.runelite.client.plugins.inventorysetups.InventorySetup;
import net.runelite.client.plugins.inventorysetups.MInventorySetupsPlugin;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.storm.modified.slayer.Utils.*;
import net.runelite.client.plugins.microbot.storm.modified.slayer.enums.SlayerMasters;
import net.runelite.client.plugins.microbot.storm.modified.slayer.enums.State;
import net.runelite.client.plugins.microbot.util.Rs2InventorySetup;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.camera.Rs2Camera;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.math.Random;
import net.runelite.client.plugins.microbot.util.misc.Rs2Potion;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.security.Login;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class BanksSlayerScript extends Script {
    public ScheduledFuture<?> mainScheduledFuture;

    private Queue<Integer> healthHistory = new LinkedList<>();
    private static final int HEALTH_HISTORY_SIZE = 3; // Number of health checks to store
    private NPC lastInteractingNPC = null;

    private BanksSlayerConfig config;
    private OverlayManager overlayManager;
    private Client client;
    private ConfigManager configManager;
    private ClientThread clientThread;
    public State state = State.IDLE;

    @Inject
    public BanksSlayerScript(OverlayManager overlayManager, ConfigManager configManager, ClientThread clientThread, Client client) {
        this.overlayManager = overlayManager;
        this.configManager = configManager;
        this.clientThread = clientThread;
        this.client = client;
    }

    @Inject
    private NPCManager npcManager;

    @Getter
    private String taskName;

    @Getter
    private AtomicReference<State> currentState = new AtomicReference<>(State.IDLE);
    //Rs2InventorySetup rs2InventorySetup;
    private int streak;
    private int taskId;
    private int amount;
    private int areaId;
    private String taskLocation;
    private boolean loginFlag;
    private List<String> importantLootList;
    private List<String> ignoreLootList;
    private List<String> cannonTaskList;
    private boolean hopping = false;
    private boolean ignoreRestock;
    private boolean superiorHasSpawned = false;
    private boolean isSpecialTravelComplete = false;

    public static List<NPC> attackableNpcs = new ArrayList<>();
    public static Actor currentNpc = null;

    public static String version = "0.2.5";

    private volatile boolean isActive = false;

    InventorySetup slayerInventorySetup;
    Rs2InventorySetup slayerRs2InventorySetup;

    public boolean run(BanksSlayerConfig config) {
        this.config = config;
        slayerInventorySetup = null;
        slayerRs2InventorySetup = null;
        final SlayerMasters selectedSlayerMaster = config.slayerMaster();
        importantLootList = parseLootItems(config.importantLootItems());
        ignoreLootList = parseLootItems(config.ignoreLootItems());
        cannonTaskList = parseLootItems(config.cannonTasks());

        Microbot.enableAutoRunOn = true;
        isActive = true; // Set the plugin as active

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!isActive) {
                    return; // Exit if the plugin is not active
                }

                if (client.getGameState() != GameState.LOGGED_IN) {
                    log("Client not logged in.");
                    return;
                }
                loginFlag = true;
                clientThread.invoke(this::storeCurrentTaskInMemory);

                log("Toggling run energy.");
                Rs2Player.toggleRunEnergy(true);

                determineState();
                log("Current State: " + currentState);

                switch (currentState.get()) {
                    case GET_TASK:
                        handleGetTask(selectedSlayerMaster);
                        break;

                    case SPECIAL_TRANSPORT:
                        handleSpecialTransport();
                        break;

                    case TRAVEL_TO_MONSTER:
                        handleTravelToMonster();
                        break;

                    case FIGHT_MONSTER:
                        handleFightMonster();
                        break;

                    case RESTOCK:
                        handleRestock();
                        break;

                    default:
                        break;
                }

            } catch (Exception ex) {
                log("Error: " + ex.getMessage(), ex);
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);

        return true;
    }

    private void determineState() {
        if (!isActive) {
            return;
        }
        if(hasTask()) {
            log("Determining state... Has task: " + hasTask() + ", Does equipment match: " + slayerRs2InventorySetup.doesEquipmentMatch() + ", Does inventory match: " + slayerRs2InventorySetup.doesInventoryMatch() + ", Ignore restock: " + ignoreRestock);
        } else {
          log("No current task");
        }
        if (!hasTask()) {
            setCurrentState(State.GET_TASK);
        } else {
            clientThread.invoke(this::storeCurrentTaskInMemory);  // Ensure task name is set
            if (!slayerRs2InventorySetup.doesEquipmentMatch() || Rs2Inventory.getInventoryFood().isEmpty()) {
                if (!ignoreRestock) {
                    setCurrentState(State.RESTOCK);
                } else {
                    setCurrentState(State.TRAVEL_TO_MONSTER);
                }
            } else if (SpecialMonsterTravelling.hasSpecialTravel(taskName) && !isSpecialTravelComplete) {
                setCurrentState(State.SPECIAL_TRANSPORT);
            } else if (!isCloseToMonster(taskName)) {
                ignoreRestock = true;
                setCurrentState(State.TRAVEL_TO_MONSTER);
            } else {
                setCurrentState(State.FIGHT_MONSTER);
            }
        }
    }

    private void handleSpecialTransport() {
        if (!isActive) {
            return;
        }

        log("Handling special transport for task: " + taskName);

        if (SpecialMonsterTravelling.handleSpecialTravel(taskName)) {
            log("Handled special interaction for task: " + taskName);
            isSpecialTravelComplete = true;
                setCurrentState(State.TRAVEL_TO_MONSTER); // Proceed to normal travel after special interaction

        }
    }


    private void handleGetTask(SlayerMasters selectedSlayerMaster) {
        if (!isActive) {
            return;
        }

        if (config.isPointSkippingEnabled() && isTuraelTaskLimitReached()) {
            switchToConfiguredMaster();
        }
        if (!isCloseToSlayerMaster()) {
            if (config.isNpcContactEnabled()) {
                handleNpcContact(selectedSlayerMaster);
            } else {
                travelToSlayerMaster(selectedSlayerMaster);
            }
        } else {
            getNewAssignment(selectedSlayerMaster);
            handleRestock();
        }
    }

    private void handleRestock() {
        if (!isActive || ignoreRestock || !hasTask()) {
            return;
        }

        clientThread.invoke(this::storeCurrentTaskInMemory);  // Ensure task name is set

        if (!slayerRs2InventorySetup.doesEquipmentMatch() || !slayerRs2InventorySetup.doesInventoryMatch()) {
            if (!ignoreRestock) {
                log("Restocking as ignoreRestock is false.");
                isSpecialTravelComplete = false;

                WorldPoint currentLocation = Rs2Player.getWorldLocation();
                WorldPoint closestBank = CommonBankLocations.findClosestBank(currentLocation);
                Rs2Walker.walkTo(closestBank);

                boolean hasEquipment = slayerRs2InventorySetup.doesEquipmentMatch();
                boolean hasInventory = slayerRs2InventorySetup.doesInventoryMatch();

                if (!Rs2Bank.isOpen()) {
                    Rs2Bank.openBank();
                }

                if (!hasEquipment) {
                    log("Depositing all items and equipment.");
                    Rs2Bank.depositAll();
                    Rs2Bank.depositEquipment();
                    sleep(600);

                    log("Loading equipment for task: " + taskName);
                    hasEquipment = slayerRs2InventorySetup.loadEquipment();
                    //hasEquipment = MicrobotInventorySetup.loadEquipment(taskName, mainScheduledFuture);
                    log("Equipment match after loading: " + hasEquipment);
                }

                if (!hasInventory) {
                    sleep(600);
                    log("Loading inventory for task: " + taskName);
                    hasInventory = slayerRs2InventorySetup.loadInventory();
                    //hasInventory = MicrobotInventorySetup.loadInventory(taskName, mainScheduledFuture);
                    sleep(1000);
                    log("Inventory match after loading: " + hasInventory);
                }

                log("Restock complete. Equipment match: " + hasEquipment + ", Inventory match: " + hasInventory);

                if (hasEquipment && hasInventory) {
                    log("Restocking successful, transitioning to TRAVEL_TO_MONSTER state.");
                    setCurrentState(State.TRAVEL_TO_MONSTER);
                } else {
                    log("Restocking failed, remaining in RESTOCK state.");
                }
            } else {
                log("Skipping restock as ignoreRestock is true.");
            }
        } else {
            log("Equipment and inventory match, transitioning to TRAVEL_TO_MONSTER state.");
            setCurrentState(State.TRAVEL_TO_MONSTER);
        }
    }

    private void handleTravelToMonster() {
        if (!isActive) {
            return;
        }

        log("Setting ignoreRestock to true for traveling to monster.");
        ignoreRestock = true;

        if (!isCloseToMonster(taskName)) {
            if (travelToMonster(taskName)) {
                log("Successfully traveled to monster location for task: " + taskName);
            } else {
                log("Failed to travel to monster location for task: " + taskName);
            }
        }
        setCurrentState(State.FIGHT_MONSTER);
    }

    private void handleFightMonster() {
        if (!isActive) {
            return;
        }

        ignoreRestock = false;

        if (isCloseToMonster(taskName)) {
            NPC currentTarget = findAndAttackMonster(taskName);
            if (currentTarget != null) {
                handleEat();
                handleSpecialSlayerTask(taskName, currentTarget);
                handleLoot();
                hopIfLocationFull(config.playerThreshold());
            }
        }
    }

    private void setCurrentState(State state) {
        currentState.set(state);
    }

    public State getCurrentState() {
        return currentState.get();
    }

    private boolean isImportantLootAvailable() {
        for (String item : importantLootList) {
            if (Rs2GroundItem.exists(item, 10)) {
                return true;
            }
        }
        return false;
    }

    public void stop() {
        isActive = false; // Set the plugin as inactive
        if (mainScheduledFuture != null) {
            mainScheduledFuture.cancel(true); // Cancel the scheduled task
        }

        // Cancel any ongoing actions
        Rs2Walker.walkTo(Rs2Player.getWorldLocation());
        Rs2Bank.closeBank();
    }

    private void handleEat() {
        if (!Rs2Player.isFullHealth()) {
            if (Rs2Inventory.getInventoryFood().isEmpty()) {
                handleResetTeleport(); // Out of Food
            } else {
                Rs2Player.eatAt(60); // We need to Eat
            }
        }

        if ((Microbot.getClient().getBoostedSkillLevel(Skill.PRAYER) * 100) / Microbot.getClient().getRealSkillLevel(Skill.PRAYER) < Random.random(25, 30)) {
            Rs2Inventory.interact(Rs2Potion.getPrayerPotionsVariants(), "drink");
        }
    }

    private void handleSpecialSlayerTask(String taskName, NPC monster) {
        SpecialSlayerTaskRequirements.SpecialRequirement specialRequirement = SpecialSlayerTaskRequirements.getSpecialRequirement(taskName);

        if (specialRequirement != null) {
            int itemId = specialRequirement.getItemId();
            int healthThreshold = specialRequirement.getHealthThreshold();

            if (Rs2Combat.inCombat()) {
                Actor interacting = Microbot.getClient().getLocalPlayer().getInteracting();
                if (interacting instanceof NPC) {
                    NPC currentNPC = (NPC) interacting;
                    lastInteractingNPC = currentNPC; // Update last interacting NPC
                    int currentHealth = getMonsterHealth(currentNPC);
                    log("Current Health of NPC: " + currentHealth);

                    // Add current health to the history
                    if (healthHistory.size() >= HEALTH_HISTORY_SIZE) {
                        healthHistory.poll(); // Remove the oldest health value
                    }
                    healthHistory.offer(currentHealth);

                    // Ensure the queue is full before making any decisions
                    if (healthHistory.size() == HEALTH_HISTORY_SIZE) {
                        // Check if the last few health values have been -1
                        boolean allNegativeOne = healthHistory.stream().allMatch(h -> h == -1);

                        if (allNegativeOne || (currentHealth <= healthThreshold && currentHealth >= 1)) {
                            Rs2Inventory.useItemOnNpc(itemId, currentNPC);
                            sleep(1200);
                            healthHistory.clear(); // Clear the history when item is used
                        }
                    }
                } else {
                    log("No NPC is currently being interacted with.");
                }
            } else {
                log("Player is not in combat.");
                // Ensure the item is used on the correct NPC if health is critical
                if (lastInteractingNPC != null && !lastInteractingNPC.isDead()) {
                    int currentHealth = getMonsterHealth(lastInteractingNPC);
                    if (currentHealth == -1) {
                        Rs2Inventory.useItemOnNpc(itemId, lastInteractingNPC.getId());
                        sleep(1200);
                        healthHistory.clear(); // Clear the history when item is used
                        lastInteractingNPC = null; // Reset the reference after using the item
                    }
                }
            }
        } else {
            log("No special requirements for task: " + taskName);
        }
    }

    private int getMonsterHealth(NPC monster) {
        int healthRatio = monster.getHealthRatio();
        int healthScale = monster.getHealthScale();

        if (healthRatio == -1 || healthScale == -1) {
            return -1;
        }

        Integer maxHealth = npcManager.getHealth(monster.getId());

        if (maxHealth == null) {
            return -1;
        }

        if (healthRatio > 0) {
            int minHealth = 1;
            int calculatedHealth;

            if (healthScale > 1) {
                if (healthRatio > 1) {
                    minHealth = (maxHealth * (healthRatio - 1) + healthScale - 2) / (healthScale - 1);
                }
                int maxHealthValue = (maxHealth * healthRatio - 1) / (healthScale - 1);
                if (maxHealthValue > maxHealth) {
                    maxHealthValue = maxHealth;
                }
                calculatedHealth = (minHealth + maxHealthValue + 1) / 2;
            } else {
                calculatedHealth = maxHealth;
            }

            return calculatedHealth;
        } else if (healthRatio == 0 && healthScale == 1) {
            // Special case: Health is exactly 0
            return 0;
        }

        return -1;
    }

    private NPC findAndAttackMonster(String taskName) {
        if (hopping) {
            log("Currently hopping worlds, not attacking monsters.");
            return null;
        }

        if (!hopIfLocationFull(config.playerThreshold())) {
            log("Failed to hop worlds.");
            return null;
        }

        if (!Rs2Inventory.isEmpty() && Rs2Inventory.getInventoryFood().isEmpty()) {
            // Full Inv & No Food
            WorldPoint playerLocation = Rs2Player.getWorldLocation();
            WorldPoint closestBank = CommonBankLocations.findClosestBank(playerLocation);

            if (closestBank != null) {
                Rs2Walker.walkTo(closestBank);
                Rs2Walker.walkTo(null);
                Rs2Bank.useBank();
                sleep(1200, 2400);
                Rs2Bank.withdrawAll("lobster");
                sleep(1200, 2400);
                return null; // Return null to indicate we are walking to the bank
            }
        } else {
            // Handle Cannon if required
            if (handleCannonTask(taskName)) {
                if (isCannonBeingPlaced) {
                    // Wait for cannon placement to finish
                    log("Waiting for cannon to be placed.");
                    return null;
                }
            }

            log("Finding and attacking monster for task: " + taskName);
            if (!hasTask()) {
                return null;
            }

            // Handle superior monster if spawned
            if (handleSuperiorMonster(taskName)) {
                return null; // Superior monster found and handled
            }

            List<String> npcNames = TaskNpcMapping.getNpcNames(taskName);
            log("NPC Names: " + npcNames);

            NPC currentInteractingNpc = null;
            if (Microbot.getClient().getLocalPlayer().getInteracting() instanceof NPC) {
                NPC interactingNpc = (NPC) Microbot.getClient().getLocalPlayer().getInteracting();
                if (npcNames.contains(interactingNpc.getName()) && !interactingNpc.isDead()) {
                    currentInteractingNpc = interactingNpc;
                }
            }

            if (currentInteractingNpc != null) {
                log("Already interacting with the correct NPC: " + currentInteractingNpc.getName());
                return currentInteractingNpc;
            }

            List<NPC> attackableNpcs = Microbot.getClient().getNpcs().stream()
                    .filter(x -> !x.isDead()
                            && (!x.isInteracting() || x.getInteracting() == Microbot.getClient().getLocalPlayer())
                            && (x.getInteracting() == null || x.getInteracting() == Microbot.getClient().getLocalPlayer())
                            && x.getAnimation() == -1
                            && npcNames.contains(x.getName()))
                    .sorted(Comparator.comparingInt(value -> value.getLocalLocation().distanceTo(Microbot.getClient().getLocalPlayer().getLocalLocation())))
                    .collect(Collectors.toList());

            if (attackableNpcs.isEmpty()) {
                log("No attackable NPCs found!");
                sleep(2000); // Add a delay before retrying to prevent rapid looping
                return null;
            }

            for (NPC npc : attackableNpcs) {
                if (npc == null
                        || npc.getAnimation() != -1
                        || npc.isDead()
                        || (npc.getInteracting() != null && npc.getInteracting() != Microbot.getClient().getLocalPlayer())
                        || (npc.isInteracting() && npc.getInteracting() != Microbot.getClient().getLocalPlayer())
                        || !npcNames.contains(npc.getName()))
                    continue;

                if (!Rs2Camera.isTileOnScreen(npc.getLocalLocation())) {
                    Rs2Camera.turnTo(npc);
                }

                if (!Rs2Npc.hasLineOfSight(npc)) {
                    continue;
                }

                if (!hopping) {
                    Rs2Npc.interact(npc, "attack");
                    sleepUntil(() -> Microbot.getClient().getLocalPlayer().isInteracting() && Microbot.getClient().getLocalPlayer().getInteracting() instanceof NPC);
                    return npc;
                }
            }
        }

        return null;
    }

    private void storeCurrentTaskInMemory() {
        clientThread.invoke(() -> {
            try {
                log("Storing Task in Memory");
                this.streak = client.getVarbitValue(Varbits.SLAYER_TASK_STREAK);
                this.amount = client.getVarpValue(VarPlayer.SLAYER_TASK_SIZE);
                if (amount > 0) {
                    this.taskId = client.getVarpValue(VarPlayer.SLAYER_TASK_CREATURE);
                    if (taskId == 98) {
                        int structId = client.getEnum(EnumID.SLAYER_TASK)
                                .getIntValue(client.getVarbitValue(Varbits.SLAYER_TASK_BOSS));
                        this.taskName = client.getStructComposition(structId)
                                .getStringValue(ParamID.SLAYER_TASK_NAME);
                            slayerInventorySetup = changeSetup(this.taskName, slayerInventorySetup);
                    } else {
                        this.taskName = client.getEnum(EnumID.SLAYER_TASK_CREATURE)
                                .getStringValue(taskId);
                            slayerInventorySetup = changeSetup(this.taskName, slayerInventorySetup);

                    }
                    this.areaId = client.getVarpValue(VarPlayer.SLAYER_TASK_LOCATION);
                    this.taskLocation = areaId > 0 ? client.getEnum(EnumID.SLAYER_TASK_LOCATION).getStringValue(areaId) : null;

                    log("Current Streak: " + streak);
                    log("Current Task: " + taskName);
                    log("Amount Left: " + amount);
                    if (taskLocation != null) {
                        log("Task Location: " + taskLocation);
                    }
                    log("Task name updated to: " + taskName);
                } else {
                    log("No Task, Need one.");
                }
            } catch (Exception ex) {
                log("Error storing current task: " + ex.getMessage(), ex);
            }
        });
    }

    private InventorySetup changeSetup(String checkSetups, InventorySetup oldSetup) {
        InventorySetup newSetup = MInventorySetupsPlugin.getInventorySetups()
                .stream()
                .filter(Objects::nonNull)
                .filter(x -> x.getName().equalsIgnoreCase(checkSetups))
                .findFirst()
                .orElse(null);
        if(newSetup!=null) {
            if (newSetup.getEquipment() != oldSetup.getEquipment()
                    && newSetup.getInventory() != oldSetup.getInventory()) {
                slayerRs2InventorySetup = new Rs2InventorySetup(checkSetups, mainScheduledFuture);
                return newSetup;
            }
        }
        return oldSetup;
    }

    public boolean isTuraelTaskLimitReached() {
        // Check if the streak ends in 9 for tasks that are at least multiples of 10
        if (streak < 9) {
            return false;
        }
        return (streak % 10 == 9) || (streak % 100 == 49) || (streak % 1000 == 99) || (streak % 10000 == 999);
    }

    public void switchToConfiguredMaster() {
        SlayerMasters configuredMaster = config.slayerMaster();
        log("Switching to configured slayer master: " + configuredMaster);
        // Add logic to switch to the configured slayer master here
    }

    private boolean hasTask() {
        try {
            boolean result = amount > 0;
            log("Has task? " + result + " (Starting amount: " + amount + ")");

            // Check if we do not have a task and if a cannon is placed
            if (!result) {
                boolean cannonIsPlaced = Rs2GameObject.exists(6);
                if (cannonIsPlaced) {
                    GameObject cannon = Rs2GameObject.get("Dwarf multicannon");
                    if (cannon != null) {
                        log("Picking up the cannon as we don't have a task.");
                        Rs2GameObject.interact(cannon, "Pick-up");
                        sleep(3200, 4400); // Wait for the action to complete
                    } else {
                        log("Cannon not found to pick up.");
                    }
                }
            }

            return result;
        } catch (Exception ex) {
            log("Error checking if has task: " + ex.getMessage(), ex);
            return false;
        }
    }

    public boolean isCloseToSlayerMaster() {
        try {
            SlayerMasters selectedSlayerMaster = config.slayerMaster();
            WorldPoint slayerMasterLocation = SlayerMasterLocations.getLocation(selectedSlayerMaster);

            if (slayerMasterLocation == null) {
                log("Error: Slayer master location not found for " + selectedSlayerMaster);
                return false;
            }

            // Get the player's current location
            WorldPoint playerLocation = getPlayerLocation();

            // Calculate the distance between the player's location and the Slayer Master's location
            int distance = playerLocation.distanceTo2D(slayerMasterLocation);
            log("Distance to " + selectedSlayerMaster + ": " + distance);

            // Check if the distance is within a certain threshold (e.g., 5 tiles)
            int threshold = 2; // Change this value as needed
            return distance <= threshold;
        } catch (Exception ex) {
            log("Error checking if close to Slayer Master: " + ex.getMessage(), ex);
            return false;
        }
    }

    private WorldPoint getPlayerLocation() {
        try {
            WorldPoint playerLocation = Microbot.getClient().getLocalPlayer().getWorldLocation();
            log("Player location: " + playerLocation);
            return playerLocation;
        } catch (Exception ex) {
            log("Error getting player location: " + ex.getMessage(), ex);
            return null;
        }
    }

    private boolean travelToSlayerMaster(SlayerMasters slayerMaster) {
        handleResetTeleport();
        ignoreRestock = true;
        try {
            WorldPoint slayerMasterLocation = SlayerMasterLocations.getLocation(slayerMaster);
            if (slayerMasterLocation != null) {
                return Rs2Walker.walkTo(slayerMasterLocation);
            } else {
                log("Invalid Slayer Master Provided.");
                return false;
            }
        } catch (Exception ex) {
            log("Error traveling to Slayer Master: " + ex.getMessage(), ex);
            return false;
        }
    }

    private boolean getNewAssignment(SlayerMasters slayerMaster) {
        try {
            isSpecialTravelComplete = false;
            NPC npc = Rs2Npc.getNpc(slayerMaster.toString());
            if (npc == null) {
                log("No Slayer Master in sight.");
                return false;
            } else {
                Rs2Npc.interact(npc, "Assignment");
                log("Interacting with Slayer Master: " + slayerMaster);
                sleep(3000); // Wait for the interaction to complete
                storeCurrentTaskInMemory(); // Store the task after getting a new assignment
                ignoreRestock = false;
                return true;
            }
        } catch (Exception ex) {
            log("Error getting new assignment: " + ex.getMessage(), ex);
            return false;
        }
    }

    private boolean handleNpcContact(SlayerMasters slayerMaster) {
        handleResetTeleport();
        sleep(1200, 2400);
        MagicAction castNpcContact = MagicAction.NPC_CONTACT;
        if (Rs2Magic.isLunar()) {
            Rs2Magic.cast(castNpcContact);
            sleep(2400, 3000);
            Rs2Widget.clickWidget(slayerMaster.toString());
            sleep(6000, 12000);
            Rs2Dialogue.clickContinue();
            sleep(2400, 3000);
            Rs2Keyboard.keyPress('1');
            sleep(2400, 3000);
            Rs2Dialogue.clickContinue();
            sleep(2400, 3000);
            storeCurrentTaskInMemory();
            handleRestock();
            return true;
        } else {
            log("Not on Lunar Spellbook!");
            return false;
        }
    }

    public boolean isCloseToMonster(String taskName) {
        try {
            WorldPoint monsterLocation = MonsterLocationMapping.getMonsterLocation(taskName);
            if (monsterLocation == null) {
                log("Invalid task name: " + taskName);
                return false;
            }

            WorldPoint playerLocation = getPlayerLocation();
            int distance = playerLocation.distanceTo(monsterLocation);
            int threshold = 10; // Change this value as needed
            return distance <= threshold;
        } catch (Exception ex) {
            log("Error checking if close to monster: " + ex.getMessage(), ex);
            return false;
        }
    }

    private boolean travelToMonster(String taskName) {
        try {
            ignoreRestock = true;
            log("Setting ignoreRestock to true for traveling to monster.");

            WorldPoint monsterLocation = MonsterLocationMapping.getMonsterLocation(taskName);
            if (monsterLocation != null) {
                boolean walkSuccessful = Rs2Walker.walkTo(monsterLocation);
                if (walkSuccessful) {
                    log("Successfully started walking to monster location for task: " + taskName);
                    return true;
                } else {
                    log("Failed to walk to monster location for task: " + taskName);
                    return false;
                }
            } else {
                log("Invalid task name: " + taskName);
                return false;
            }
        } catch (Exception ex) {
            log("Error traveling to monster: " + ex.getMessage(), ex);
            return false;
        }
    }

    private List<String> parseLootItems(String lootItems) {
        return Arrays.stream(lootItems.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    private List<String> parseCannonTasks(String cannonTasks) {
        return Arrays.stream(cannonTasks.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    private boolean handleLoot() {
        boolean lootedAtLeastOneItem = false;

        try {
            if (Rs2GroundItem.lootAtGePrice(config.priceOfItemsToLoot())) {
                sleep(1200, 2400);
            }

            List<String> lootItems = parseLootItems(config.importantLootItems());

            for (String lootItem : lootItems) {
                if (Rs2GroundItem.exists(lootItem, 10)) {
                    Rs2GroundItem.loot(lootItem, 10);
                    lootedAtLeastOneItem = true; // Mark that we have looted at least one item
                }
            }

            sleep(1200, 2400);
        } catch (Exception e) {
            log("An error occurred while handling loot: " + e.getMessage(), e);
            return false;
        }

        if (!lootedAtLeastOneItem) {
            log("No important items were looted.");
        }

        return lootedAtLeastOneItem;
    }

    private void handleResetTeleport() {
        String resetTeleport = config.emergencyTeleport();

        if (Rs2Inventory.contains(resetTeleport)) {
            Rs2Inventory.interact(resetTeleport, "Break");
            sleep(1200, 2400);
        } else {
            // No Teleport Provided or Available, run to a safe location so we can use NPC contact
            // TODO: Implement safe location logic
        }
    }

    private boolean hopIfLocationFull(int threshold) {
        // Check Area for Players
        if (!Rs2Combat.inCombat() && isCloseToMonster(taskName)) {
            long currentPlayers = Rs2Player.getPlayers().stream().count();
            if (currentPlayers >= threshold) {
                log("Current Players in area: " + currentPlayers);
                // Check if cannon is placed
                boolean cannonIsPlaced = Rs2GameObject.exists(6);

                if (cannonIsPlaced) {
                    GameObject cannon = Rs2GameObject.get("Dwarf multicannon");
                    if (cannon != null) {
                        log("Picking up the cannon before hopping worlds.");
                        Rs2GameObject.interact(cannon, "Pick-up");
                        sleep(3200, 4400); // Wait for the action to complete
                    } else {
                        log("Cannon not found to pick up.");
                    }
                }
                hopping = true;
                travelToMonster(taskName);
                sleep(2000); // Sleep for 2s to log out.
                int world = Login.getRandomWorld(true, null);
                boolean hasHopped = Microbot.hopToWorld(world);
                if (!hasHopped) {
                    hopping = false;
                    return false;
                }
                boolean result = sleepUntil(() -> Rs2Widget.findWidget("Switch World") != null);
                if (result) {
                    Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                    sleepUntil(() -> Microbot.getClient().getGameState() == GameState.HOPPING);
                    sleepUntil(() -> Microbot.getClient().getGameState() == GameState.LOGGED_IN);
                }
                hopping = false;
            }
        }
        return true;
    }

    private boolean isCannonBeingPlaced = false;

    private boolean handleCannonTask(String taskName) {
        int[] cannonItems = {2, 6, 8, 10, 12}; // Cannonballs, base, stand, barrels, furnace

        if (cannonTaskList.contains(taskName)) {
            // Use Cannon
            if (Rs2Inventory.containsAll(cannonItems) && !isCannonBeingPlaced) {
                WorldPoint cannonLocation = MonsterCannonMapping.getCannonLocation(taskName);
                if (cannonLocation != null) {
                    log("Walking to Cannon Spot: " + cannonLocation);
                    isCannonBeingPlaced = true;
                    if (Rs2Walker.walkTo(cannonLocation)) {
                        log("Placing Cannon");
                        Rs2Inventory.interact(6, "Set-up");
                        sleep(3200, 4400);
                        Rs2GameObject.interact(6, "Fire");
                        sleep(1200, 2400);
                        isCannonBeingPlaced = false;

                        if (hasTask() && Rs2GameObject.exists(6)) {
                            return true;
                        }
                    } else {
                        log("Failed to walk to cannon location.");
                        isCannonBeingPlaced = false;
                    }
                } else {
                    log("No cannon location found for task: " + taskName);
                }
            }
            return true;
        } else {
            // Don't Use Cannon
            return false;
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        ChatMessageType type = event.getType();
        String message = event.getMessage();

        if (type == ChatMessageType.GAMEMESSAGE) {
            if (message.contains("A superior foe has appeared...")) {
                superiorHasSpawned = true;
            }
        }
    }

    private boolean handleSuperiorMonster(String taskName) {
        if (superiorHasSpawned) {
            List<String> superiorNames = SuperiorMonsterMapping.getSuperiorNames(taskName);

            if (superiorNames.isEmpty()) {
                log("No superior monster mapping found for task: " + taskName);
                superiorHasSpawned = false;
                return false;
            }

            List<NPC> superiorNpcs = Microbot.getClient().getNpcs().stream()
                    .filter(x -> !x.isDead() && superiorNames.contains(x.getName()))
                    .sorted(Comparator.comparingInt(value -> value.getLocalLocation().distanceTo(Microbot.getClient().getLocalPlayer().getLocalLocation())))
                    .collect(Collectors.toList());

            if (!superiorNpcs.isEmpty()) {
                for (NPC npc : superiorNpcs) {
                    if (npc == null || npc.getAnimation() != -1 || npc.isDead()
                            || (npc.getInteracting() != null && npc.getInteracting() != Microbot.getClient().getLocalPlayer())
                            || (npc.isInteracting() && npc.getInteracting() != Microbot.getClient().getLocalPlayer()))
                        continue;

                    if (!Rs2Camera.isTileOnScreen(npc.getLocalLocation())) {
                        Rs2Camera.turnTo(npc);
                    }

                    if (!Rs2Npc.hasLineOfSight(npc)) {
                        continue;
                    }

                    Rs2Npc.interact(npc, "attack");
                    sleepUntil(() -> Microbot.getClient().getLocalPlayer().isInteracting() && Microbot.getClient().getLocalPlayer().getInteracting() instanceof NPC);
                    return true;
                }
            }

            superiorHasSpawned = false; // Reset the flag after handling
        }

        return false;
    }

    private void log(String message) {
        System.out.println(message);
    }

    private void log(String message, Throwable throwable) {
        System.err.println(message);
        throwable.printStackTrace();
    }
}
