package net.runelite.client.plugins.microbot.storm.debugging;

import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.storm.debugging.enums.Actions;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class sdebuggerScript extends Script {
    public static double version = 1.0;
    public static boolean key1isdown;
    public static boolean key2isdown;
    private int minInterval;
    private int previousAction;
    private sdebuggerConfig config;
    private Actions actions;
    //private final String openBank = "openBank", withdrawAll = "withdrawAll", depositAll="depositAll", interact = "interact", withdrawOne="withdrawOne", invInteract="invInteract", attack="attack", getWidget="getWidget", println="println", walkFastCanvas="walkFastCanvas";
    public boolean run(sdebuggerConfig config){
        previousAction = 0;
        minInterval = 0;
        key1isdown = false;
        key2isdown = false;
        //actions = new Actions();
        this.config = config;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if(key1isdown) {
                while (key1isdown && this.isRunning()) {
                    System.out.println("Should be doing thing");
                    if (Pattern.compile("[0-9]+").matcher(config.actionMenu()).matches()) {
                        if (Pattern.compile("[0-9]+").matcher(config.actionIDEntry()).matches()) {
                            action(config.actionName().getAction(), Integer.parseInt(config.actionIDEntry()), Integer.parseInt(config.actionMenu()));
                        } else {
                            action(config.actionName().getAction(), config.actionIDEntry(), Integer.parseInt(config.actionMenu()));
                        }
                    } else {
                        if (Pattern.compile("[0-9]+").matcher(config.actionIDEntry()).matches()) {
                            action(config.actionName().getAction(), Integer.parseInt(config.actionIDEntry()), config.actionMenu());
                        } else {
                            action(config.actionName().getAction(), config.actionIDEntry(), config.actionMenu());
                        }
                    }
                    sleep(config.sleepMin(), config.sleepMax());
                }
            } else if(!config.doAction())  {
                isRunningDemo();
            }
        }, 0, 30, TimeUnit.MILLISECONDS);
        return true;
    }

    public void isRunningDemo(){
        sleepUntil(this::isRunning,2000);
        if(this.isRunning()) { System.out.println("1"); }
        sleep(1000);
        if(this.isRunning()) { System.out.println("2"); }
        sleep(1000);
        if(this.isRunning()) { System.out.println("3"); }
        sleep(1000);
        if(this.isRunning()) { System.out.println("4"); }
        sleep(1000);
        if(this.isRunning()) { System.out.println("5"); }
        sleep(1000);
        if(this.isRunning()) { System.out.println("6"); }
        sleep(1000);
        if(this.isRunning()) { System.out.println("7"); }
        sleep(1000);
        if(this.isRunning()) { System.out.println("8"); }
        sleep(1000);
        if(this.isRunning()) { System.out.println("9"); }
        sleep(1000);
        if(this.isRunning()) { System.out.println("you should see this if the script is running"); }
    }
    public void action(String action, int ID){
        if(minInterval==0 || System.currentTimeMillis()>(previousAction+minInterval)){
            if(this.isRunning()) {
                if (Objects.equals(action, Actions.WITHDRAW_ALL.getAction())) { Rs2Bank.withdrawAll(ID); }
                if (Objects.equals(action, Actions.WITHDRAW_ONE.getAction())) { Rs2Bank.withdrawOne(ID); }
                if (Objects.equals(action, Actions.INTERACT.getAction())) { Rs2GameObject.interact(ID); }
            }
        }
    }
    public void action(String action, String name){
        if(minInterval==0 || System.currentTimeMillis()>(previousAction+minInterval)) {
            if (this.isRunning()) {
                if (Objects.equals(action, Actions.ATTACK.getAction())) { Rs2Npc.attack(name); }
                if (Objects.equals(action, Actions.WITHDRAW_ALL.getAction())) { Rs2Bank.withdrawAll(name); }
                if (Objects.equals(action, Actions.DEPOSIT_ALL.getAction())) { Rs2Bank.depositAll(name); }
            }
        }
    }
    public void action(String action, int ID, String menu) {
        if (minInterval == 0 || System.currentTimeMillis() > (previousAction + minInterval)) {
            if (this.isRunning()) {
                if (Objects.equals(action, Actions.INV_INTERACT.getAction())) { Rs2Inventory.interact(ID, menu); }
                if (Objects.equals(action, Actions.PRINTLN.getAction()) && Objects.equals(menu, Actions.GET_WIDGET.getAction())) { System.out.println(Rs2Widget.getWidget(ID)); }
            }
        }
    }
    public void action(String action, int ID, int numerical){
        if (minInterval == 0 || System.currentTimeMillis() > (previousAction + minInterval)) {
            if (this.isRunning()) {
                if (Objects.equals(action, Actions.WALK_FAST_CANVAS.getAction())) { Rs2Walker.walkFastCanvas(new WorldPoint(ID, numerical, Rs2Player.getWorldLocation().getPlane())); }
            }
        }
    }
    public void action(String action, String menu, String ID){

    }
    public void action(String action, String menu, int ID){

    }
    @Override
    public void shutdown() {
        super.shutdown();
    }
}
