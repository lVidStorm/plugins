package net.runelite.client.plugins.microbot.storm.debugging.enums;

public enum Actions {
    OPEN_BANK("openBank"),
    WITHDRAW_ALL("withdrawAll"),
    DEPOSIT_ALL("depositAll"),
    INTERACT("interact"),
    WITHDRAW_ONE("withdrawOne"),
    INV_INTERACT("invInteract"),
    ATTACK("attack"),
    GET_WIDGET("getWidget"),
    PRINTLN("println"),
    WALK_FAST_CANVAS("walkFastCanvas");

    private final String actions;
    Actions(String actions) {
        this.actions = actions;
    }

    public String getAction() {
        return actions;
    }
}
