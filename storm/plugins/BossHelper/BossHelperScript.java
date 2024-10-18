package net.runelite.client.plugins.microbot.storm.plugins.BossHelper;

import net.runelite.api.*;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer;
import net.runelite.client.plugins.microbot.util.prayer.Rs2PrayerEnum;
import net.runelite.client.ui.ClientUI;


import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static net.runelite.client.plugins.microbot.util.camera.Rs2Camera.isTileOnScreen;
import static net.runelite.client.plugins.microbot.util.player.Rs2Player.eatAt;
import static net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer.isPrayerActive;


public class BossHelperScript extends Script {
    public static double version = 2.0;
    static boolean switcher1 = false;
    private static boolean switcher2 = false;
    private static boolean bossAnimating = false;
    private static boolean lowHealth = false;
    private static boolean outOfFood = false;
    private static boolean lowPrayer = false;
    private static boolean outOfPrayerPotions = false;
    private static boolean lowCombat = false;
    private static boolean outOfCombatPotions = false;
    private static boolean newLocation = false;
    BossHelperConfig config;
    boolean initScript = false;
    static Rs2PrayerEnum playersPrayer;
    static boolean behaviorPrayerSwitch = false;
    //public static String bossIDs;
    //public static String animationIDs;
    //public static String projectileIDs;
    //public String[] split(String regex);
    //TODO could use a hashmap here instead to have a map of actions and cooldowns
    static Instant previousMechanic = Instant.now();

    public boolean run(BossHelperConfig config) {
        this.config = config;
        playersPrayer = config.usedprayer().name;
        String[] bossIDs = config.bossids().split(",");
        String[] animationIDs = config.animationids().split(",");
        String[] projectileIDs = config.projectileids().split(",");
        String[] bossbehaviors = config.whenbossanimates().split(",");
        String[] projectilebehaviors = config.whenprojectileexists().split(",");
        initScript = true;

        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run()) return;
            try {
                if (initScript) {

                    initScript = false;
                }
                //System.out.println("start of code");
                long startTime = System.currentTimeMillis();
                //System.out.println("before boss checks");
                if(config.useMechanics()) {
                    //System.out.println("after boss check config");
                    if (Instant.now().compareTo(previousMechanic) >= 0) {
                        //System.out.println("after 'previous mechanic' check");
                        if(bossIDs!=null) {
                            //System.out.println("after boss null check");
                            for (int i = 0; i < bossIDs.length; i++) {
                                for (int j = 0; j < animationIDs.length; j++) {
                                    if (checkAnimationById(Integer.parseInt(bossIDs[i]), Integer.parseInt(animationIDs[j]))) {
                                        //System.out.println("boss id: "+Integer.parseInt(bossIDs[i]));
                                        Mechanics.doBehaviorWhenEvent(Integer.parseInt(bossIDs[i]), Integer.parseInt(animationIDs[j]), 0, bossbehaviors[j]);
                                        //break;
                                        //return;
                                    }
                                }
                            }
                        }

                        System.out.println("before projectile checks");
                        if(projectileIDs!=null) {
                            System.out.println("after projectile null check");
                            for (int i = 0; i < projectileIDs.length; i++) {
                                if (doesProjectileExistById(Integer.parseInt(projectileIDs[i]))) {
                                    Mechanics.doBehaviorWhenEvent(i,0,Integer.parseInt(projectileIDs[i]),projectilebehaviors[i]);
                                    //return;
                                }
                            }
                        }
                    }
                }
                System.out.println("after boss checks");
                //TODO auto eat, auto restore, auto range potion, auto prayer back on, auto run from projectile id,
                if(!outOfFood || Rs2Inventory.hasItem(config.fooditem())) {
                    if (config.healamount() != 0) {
                        if ((Microbot.getClient().getRealSkillLevel(Skill.HITPOINTS) - Microbot.getClient().getBoostedSkillLevel(Skill.HITPOINTS))
                                > (config.healamount())) {
                            if (Rs2Inventory.hasItem(config.fooditem())) {
                                lowHealth = true;
                                outOfFood = false;
                                //System.out.println("healing");
                            } else {
                                outOfFood = true;
                            }
                        }
                    }
                }
                if(!outOfPrayerPotions || Rs2Inventory.hasItem(config.prayeritem())) {
                    if (config.restoreamount() != 0) {
                        if ((Microbot.getClient().getRealSkillLevel(Skill.PRAYER) - Microbot.getClient().getBoostedSkillLevel(Skill.PRAYER))
                                > (config.restoreamount())) {
                            if (Rs2Inventory.hasItem(config.prayeritem())) {
                                lowPrayer = true;
                                outOfPrayerPotions = false;
                                //System.out.println("restore prayer");
                            } else {
                                outOfPrayerPotions = true;
                            }
                        }
                    }
                }
                if(!outOfCombatPotions || Rs2Inventory.hasItem(config.combatitem())) {
                    if (Integer.parseInt(config.restorecbat()) != 0) {
                        if (Microbot.getClient().getBoostedSkillLevel(config.combatstat().name) - (Microbot.getClient().getRealSkillLevel(config.combatstat().name))
                                < (Integer.parseInt(config.restorecbat()))) {
                            if (Rs2Inventory.hasItem(config.combatitem())) {
                                //TODO double check that this is correct
                                lowCombat = true;
                                outOfCombatPotions = false;
                                //System.out.println("re-use combat potion");
                            } else {
                                outOfCombatPotions = true;
                            }
                        }
                    }
                }
                //System.out.println("end of stat checks");
                if (lowHealth || lowPrayer || lowCombat) {
                    NPC npc=null;
                    if(Microbot.getClient().getLocalPlayer().isInteracting()) {
                        npc = (NPC) Microbot.getClient().getLocalPlayer().getInteracting();
                    }
                    ClientUI.getClient().setEnabled(false);
                    sleep(60, 120);
                    if (lowHealth) {
                        System.out.println("Low Health");
                        if(config.fooditem().contains("potion")||config.fooditem().contains("brew")){
                            Rs2Inventory.interact(config.fooditem(), "drink");
                        } else {
                            eatAt(99);
                        }
                        lowHealth = false;
                        sleep(130, 200);
                    }
                    //System.out.println("after heal action");
                    if (lowPrayer || lowCombat) {
                            if (lowPrayer) {
                                System.out.println("Low Prayer");
                                if (Rs2Inventory.hasItem(config.prayeritem())) {
                                    System.out.println("attempting to drink \""+ config.prayeritem() +"\"");
                                    Rs2Inventory.interact(config.prayeritem(), "drink");
                                    lowPrayer = false;
                                    sleep(130, 200);
                                }
                            }
                            if (lowCombat) {
                                System.out.println("Low Combat");
                                if (Rs2Inventory.hasItem(config.combatitem())) {
                                    Rs2Inventory.interact(config.combatitem(), "drink");
                                    lowCombat = false;
                                    sleep(130, 200);
                                }
                            }

                        //System.out.println("after potions");
                    }
                    if(npc!=null) {
                        Rs2Npc.attack(npc);
                        sleep(60, 140);
                    }
                    ClientUI.getClient().setEnabled(true);
                    //System.out.println("after restore actions");
                }

                //System.out.println("before prayer check");
                if(config.reenableprayer()) {
                    //System.out.println("check enable prayer config button");
                    if (!Rs2Prayer.isOutOfPrayer()) {
                        //System.out.println("player is not out of prayer");
                        if(!isPrayerActive(playersPrayer)) {
                            if(!behaviorPrayerSwitch) {
                                if (isPrayerActive(Rs2PrayerEnum.PROTECT_MELEE) ||
                                        isPrayerActive(Rs2PrayerEnum.PROTECT_RANGE) ||
                                        isPrayerActive(Rs2PrayerEnum.PROTECT_MAGIC)) {
                                    if (isPrayerActive(Rs2PrayerEnum.PROTECT_MAGIC) && playersPrayer != Rs2PrayerEnum.PROTECT_MAGIC) {
                                        playersPrayer = Rs2PrayerEnum.PROTECT_MAGIC;
                                    }
                                    if (isPrayerActive(Rs2PrayerEnum.PROTECT_MELEE) && playersPrayer != Rs2PrayerEnum.PROTECT_MELEE) {
                                        playersPrayer = Rs2PrayerEnum.PROTECT_MELEE;
                                    }
                                    if (isPrayerActive(Rs2PrayerEnum.PROTECT_RANGE) && playersPrayer != Rs2PrayerEnum.PROTECT_RANGE) {
                                        playersPrayer = Rs2PrayerEnum.PROTECT_RANGE;
                                    }
                                }
                            }
                            NPC npc=null;
                            if(Microbot.getClient().getLocalPlayer().isInteracting()) {
                                npc = (NPC) Microbot.getClient().getLocalPlayer().getInteracting();
                            }

                            System.out.println("attempt to toggle prayer");
                            ClientUI.getClient().setEnabled(false);
                            //System.out.println("if prayer is turned off");
                            sleep(60, 140);
                            Rs2Prayer.toggle(playersPrayer, true);
                            sleep(60, 140);
                            if(npc!=null) {
                                Rs2Npc.attack(npc);
                                sleep(60, 140);
                            }
                            if(behaviorPrayerSwitch){
                                behaviorPrayerSwitch = false;
                            }
                            ClientUI.getClient().setEnabled(true);
                        }
                    }

                } else if (playersPrayer != config.usedprayer().name) {
                    playersPrayer = config.usedprayer().name;
                }
                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }
    private boolean checkAnimationById(int npc, int animation) {
        System.out.println("checking boss for animation");
        if(Rs2Npc.getNpc(npc)!=null) {
            return animation == Rs2Npc.getNpc(npc).getAnimation();
        } else {
            return false;
        }
    }
    static boolean doesProjectileExistById(int id) {
        for (Projectile projectile : Microbot.getClient().getProjectiles()) {
            if (projectile.getId() == id) {
                //println("Projectile $id found")
                return true;
            }
        }
        return false;
    }
    @Override
    public void shutdown() {
        super.shutdown();
    }
}
