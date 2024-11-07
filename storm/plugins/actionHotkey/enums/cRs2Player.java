package net.runelite.client.plugins.microbot.storm.plugins.actionHotkey.enums;

public enum cRs2Player implements Actionable{
    IS_MOVING("is_moving");


    private final String conditionals;
    cRs2Player(String conditionals) {
        this.conditionals = conditionals;
    }

    @Override
    public String getAction() {
        return conditionals;
    }
}
