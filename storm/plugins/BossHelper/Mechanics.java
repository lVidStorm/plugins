package net.runelite.client.plugins.microbot.storm.plugins.BossHelper;

import net.runelite.api.NPC;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.math.Random;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.prayer.Rs2PrayerEnum;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.ui.ClientUI;

import java.time.Instant;
import java.util.Objects;

import static net.runelite.client.plugins.microbot.util.Global.sleep;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;

public class Mechanics {
    static void doBehaviorWhenEvent(int npc, int animation, int projectile, String action){
        System.out.println("before behavior action");
        NPC monster=null;
        if(Microbot.getClient().getLocalPlayer().isInteracting()) {
            monster = (NPC) Microbot.getClient().getLocalPlayer().getInteracting();
        }
        if(Objects.equals(action, "3-y-axis")){
            Mechanics.BallWalk();
            sleep(80,150);
            //Rs2Npc.attack("Galvek");
        }
        if(Objects.equals(action, "protectRange")){
            if(BossHelperScript.playersPrayer != Rs2PrayerEnum.PROTECT_RANGE) {
                System.out.println("protectRange");
                BossHelperScript.behaviorPrayerSwitch = true;
                BossHelperScript.playersPrayer = Rs2PrayerEnum.PROTECT_RANGE;
            }
        }
        if(Objects.equals(action, "protectMelee")){
            if(BossHelperScript.playersPrayer != Rs2PrayerEnum.PROTECT_MELEE) {
                System.out.println("protectMelee");
                BossHelperScript.behaviorPrayerSwitch = true;
                BossHelperScript.playersPrayer = Rs2PrayerEnum.PROTECT_MELEE;
            }
        }
        if(Objects.equals(action, "protectMagic")){
            if(BossHelperScript.playersPrayer != Rs2PrayerEnum.PROTECT_MAGIC) {
                System.out.println("protectMagic");
                BossHelperScript.behaviorPrayerSwitch = true;
                BossHelperScript.playersPrayer = Rs2PrayerEnum.PROTECT_MAGIC;
            }
        }
        if(Objects.equals(action, "Zulrah")){
            ZulrahPrayerSwitch(npc);
        }
        if(monster!=null && Microbot.getClient().getLocalPlayer().getInteracting()!=monster) {
            Rs2Npc.attack(monster);
            sleep(60, 140);
        }
        System.out.println("after behavior action");
    }
    static void ZulrahPrayerSwitch(int id){
        ClientUI.getClient().setEnabled(false);
        System.out.println("ZPS provided ID : " + id);
        if(id==2042) {
            //TODO check for hybrid cycle?
            if(BossHelperScript.doesProjectileExistById(1046)){
                if (BossHelperScript.playersPrayer != Rs2PrayerEnum.PROTECT_MAGIC) {
                    BossHelperScript.behaviorPrayerSwitch = true;
                    BossHelperScript.playersPrayer = Rs2PrayerEnum.PROTECT_MAGIC;
                }
            } else {
                if (BossHelperScript.playersPrayer != Rs2PrayerEnum.PROTECT_RANGE) {
                    System.out.println("should be protect from range");
                    BossHelperScript.behaviorPrayerSwitch = true;
                    BossHelperScript.playersPrayer = Rs2PrayerEnum.PROTECT_RANGE;
                }
            }
        }
        if(id==2043) {
            System.out.println("Melee Zulrah?...");
        }
        if(id==2044) {
            if(BossHelperScript.playersPrayer != Rs2PrayerEnum.PROTECT_MAGIC) {
                System.out.println("should be protect from magic");
                BossHelperScript.behaviorPrayerSwitch = true;
                BossHelperScript.playersPrayer = Rs2PrayerEnum.PROTECT_MAGIC;
            }
        }

        ClientUI.getClient().setEnabled(true);
    }
    static void BallWalk() {
        ClientUI.getClient().setEnabled(false);
        WorldPoint currentPlayerLocation = Microbot.getClient().getLocalPlayer().getWorldLocation();
        WorldPoint sideStepLocation;
        if(BossHelperScript.switcher1) {
            sideStepLocation = new WorldPoint(currentPlayerLocation.getX(), currentPlayerLocation.getY() + 3, 0);
            BossHelperScript.switcher1 = false;
        } else {
            sideStepLocation = new WorldPoint(currentPlayerLocation.getX(), currentPlayerLocation.getY() - 3, 0);
            BossHelperScript.switcher1 = true;
        }
        final WorldPoint _sideStepLocation = sideStepLocation;
        Rs2Walker.walkFastLocal(LocalPoint.fromWorld(Microbot.getClient(), _sideStepLocation));
        Rs2Player.waitForWalking();
        sleepUntil(() -> Microbot.getClient().getLocalPlayer().getWorldLocation().equals(_sideStepLocation));
        BossHelperScript.previousMechanic = (Instant.now().plusMillis(6000));
        ClientUI.getClient().setEnabled(true);
    }
    private void redBallWalk() {
        WorldPoint currentPlayerLocation = Microbot.getClient().getLocalPlayer().getWorldLocation();
        WorldPoint sideStepLocation = new WorldPoint(currentPlayerLocation.getX() + 2, currentPlayerLocation.getY(), 0);
        if (Random.random(0, 2) == 1) {
            sideStepLocation = new WorldPoint(currentPlayerLocation.getX() - 2, currentPlayerLocation.getY(), 0);
        }
        final WorldPoint _sideStepLocation = sideStepLocation;
        Rs2Walker.walkFastLocal(LocalPoint.fromWorld(Microbot.getClient(), _sideStepLocation));
        Rs2Player.waitForWalking();
        sleepUntil(() -> Microbot.getClient().getLocalPlayer().getWorldLocation().equals(_sideStepLocation));
    }
}
