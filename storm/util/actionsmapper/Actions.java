package net.runelite.client.plugins.microbot.storm.util.actionsmapper;

import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;

import java.util.concurrent.ScheduledFuture;

import java.util.Objects;

public class Actions {
    private ScheduledFuture<?> mainScheduledFuture;
    private boolean isRunning = false;
    private int minInterval = 0;
    private long previousAction = 0;
    private final String openBank = "openBank", withdrawAll = "withdrawAll", depositAll="depositAll", interact = "interact", withdrawOne="withdrawOne", invInteract="invInteract", attack="attack";
    public void setScheduledFuture(ScheduledFuture<?> future) {
        this.mainScheduledFuture = future;
    }
    public boolean isRunning() {
        return mainScheduledFuture != null && !mainScheduledFuture.isDone();
    }
    public void action(String action){

    }
    public void action(String action, int ID){
        if(minInterval==0 || System.currentTimeMillis()>(previousAction+minInterval)){
            if(!isRunning) {
                if (Objects.equals(action, withdrawAll)) { Rs2Bank.withdrawAll(ID); }
                if (Objects.equals(action, withdrawOne)) { Rs2Bank.withdrawOne(ID); }
                if (Objects.equals(action, interact)) { Rs2GameObject.interact(ID); }
            }
        }
    }
    public void action(String action, String name){
        if(minInterval==0 || System.currentTimeMillis()>(previousAction+minInterval)) {
            if (!isRunning) {
                if (Objects.equals(action, attack)) { Rs2Npc.attack(name); }
                if (Objects.equals(action, withdrawAll)) { Rs2Bank.withdrawAll(name); }
                if (Objects.equals(action, depositAll)) { Rs2Bank.depositAll(name); }
            }
        }
    }
    public void action(String action, int ID, String menu) {
        if (minInterval == 0 || System.currentTimeMillis() > (previousAction + minInterval)) {
            if (!isRunning) {
                if (Objects.equals(action, invInteract)) { Rs2Inventory.interact(ID, menu); }
            }
        }
    }
    public void action(String action, int ID, int numerical){
        if (minInterval == 0 || System.currentTimeMillis() > (previousAction + minInterval)) {
            if (!isRunning) {

            }
        }
    }
}
