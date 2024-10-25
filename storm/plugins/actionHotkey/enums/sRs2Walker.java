package net.runelite.client.plugins.microbot.storm.plugins.actionHotkey.enums;

public enum sRs2Walker implements Actionable {
    WALK_FAST_CANVAS("walkFastCanvas");

    private final String actions;
    sRs2Walker(String actions) {
        this.actions = actions;
    }

    @Override
    public String getAction() {
        return actions;
    }
}
