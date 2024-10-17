package net.runelite.client.plugins.microbot.storm.modified.driftnet;

import com.google.common.base.Stopwatch;
import net.runelite.api.ItemID;
import net.runelite.api.MenuAction;
import net.runelite.api.NPC;
import net.runelite.api.ObjectID;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.menu.NewMenuEntry;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static net.runelite.client.plugins.microbot.util.Global.sleepUntilTrue;
import static net.runelite.client.plugins.microbot.util.math.Random.random;

public class iDriftNetScript extends Script {

    public static double version = 1.1;

    private static final int ANNETE_WIDGET = 20250629;
    private static final int BANK_FISH_ICON_WIDGET = 39780359;
    private static final int BANK_ALL_CONFIRM_WIDGET = 39780365;

    public boolean run(iDriftNetConfig config) {
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;

                if (!Rs2Inventory.hasItem(ItemID.DRIFT_NET)) {
                    if (this.isRunning()){ Rs2GameObject.interact(ObjectID.ANNETTE, "Nets"); }
                    if (this.isRunning()){ sleepUntil(() -> Rs2Widget.getWidget(ANNETE_WIDGET) != null, 30000); }
                    if (this.isRunning()){ sleep(2000,4000); }
                    // Rs2Bank.withdrawAll(ItemID.DRIFT_NET); // does not work
                    if (this.isRunning()){ Microbot.doInvoke(new NewMenuEntry(0, ANNETE_WIDGET, MenuAction.CC_OP.getId(), 4, ItemID.DRIFT_NET, "<col=ff9040>Drift net</col>"),
                            new Rectangle(1, 1, Microbot.getClient().getCanvasWidth(), Microbot.getClient().getCanvasHeight())); }
                    if (this.isRunning()){ sleep(800,2000); }
                    if (this.isRunning()){ Rs2Keyboard.keyPress(KeyEvent.VK_ESCAPE); }
                    return;
                }

                if (iDriftNetPlugin.getNETS().stream().anyMatch(x -> x.getStatus() == iDriftNetStatus.FULL || x.getStatus() == iDriftNetStatus.UNSET)) {
                    for (DriftNet net : iDriftNetPlugin.getNETS()) {
                        int distToNet = Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(net.getNet().getWorldLocation());
                        if (net.getStatus() == iDriftNetStatus.UNSET) {
                            if (this.isRunning()){ Rs2GameObject.interact(net.getNet(), "set up"); }
                            if (this.isRunning()){ sleep(600 * distToNet, 800 * distToNet); }
                            break;
                        } else if (net.getStatus() == iDriftNetStatus.FULL) {
                            final Stopwatch netwatch = Stopwatch.createStarted();
                            while(this.isRunning() && netwatch.elapsed(TimeUnit.MILLISECONDS) < 14000 && Rs2Widget.getWidget(BANK_FISH_ICON_WIDGET) == null && !iDriftNetPlugin.netBanked) {
                                if (this.isRunning()) { Rs2GameObject.interact(net.getNet(), "harvest"); }
                                final Stopwatch walkwatch = Stopwatch.createStarted();
                                //TODO bug with isMoving, player is always idle underwater?
                                if(this.isRunning()) { sleepUntil(Rs2Player::isMoving,1200); }
                                while(this.isRunning() && walkwatch.elapsed(TimeUnit.MILLISECONDS) < 10000 && Rs2Player.isMoving() && !iDriftNetPlugin.netBanked){
                                    sleep(202,404);
                                }
                                if (this.isRunning()){ sleep(97,127); }
                                if (this.isRunning()) { sleepUntil(() -> Rs2Widget.getWidget(BANK_FISH_ICON_WIDGET) != null, 20000); }
                            }
                            int numulite = Rs2Inventory.get("Numulite").quantity;
                            if (this.isRunning() && !Rs2Widget.hasWidget("Bank All")) { Rs2Widget.clickWidget(BANK_FISH_ICON_WIDGET); }
                            if (this.isRunning()) { boolean b = sleepUntilTrue(() -> Rs2Widget.hasWidget("Bank All") || numulite!=Rs2Inventory.get("Numulite").quantity, random(97,301), 6000); }
                            if (this.isRunning()) { sleep(800, 1600); }
                            if (this.isRunning() && Rs2Widget.hasWidget("Bank All")) { Rs2Widget.clickWidget(BANK_ALL_CONFIRM_WIDGET); }
                            if (this.isRunning()) { boolean b = sleepUntilTrue(() -> numulite != Rs2Inventory.get("Numulite").quantity, random(97,301), 6000); }
                            if (this.isRunning()) { sleep(800, 1600); }
                            if (this.isRunning()) { Rs2Keyboard.keyPress(KeyEvent.VK_ESCAPE); }
                            if (this.isRunning()) { sleep(203, 407); }
                            if(iDriftNetPlugin.netBanked) {  iDriftNetPlugin.netBanked = false; }
                            return; // return here to avoid moving on to fishing shoal before setting up net (the next loop handles the drift net setup)
                        }
                    }
                    return;
                }

                for (NPC fish : iDriftNetPlugin.getFish().stream().sorted(Comparator.comparingInt(value -> value.getLocalLocation().distanceTo(Microbot.getClient().getLocalPlayer().getLocalLocation()))).collect(Collectors.toList())) {
                    if (!iDriftNetPlugin.getTaggedFish().containsKey(fish) &&  Rs2Npc.getNpcByIndex(fish.getIndex()) != null) {
                        if (this.isRunning()){ Rs2Npc.interact(fish, "Chase"); }
                        if (this.isRunning()){ sleep(97, 131); }
                        final Stopwatch watch = Stopwatch.createStarted();
                        while(this.isRunning() && !iDriftNetPlugin.newChatMessage && watch.elapsed(TimeUnit.MILLISECONDS) < random(5000,10000) && Rs2Player.isInteracting()){
                            sleep(21,67);
                        }
                        if(iDriftNetPlugin.newChatMessage){ iDriftNetPlugin.newChatMessage=false; }
                        if (iDriftNetPlugin.successfulChase) {
                            if (this.isRunning()) { boolean a = sleepUntilTrue(() -> iDriftNetPlugin.getTaggedFish().containsKey(fish),random(37,97),2000); }
                        }
                        break;
                    }
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 600, TimeUnit.MILLISECONDS);

        return true;
    }
}