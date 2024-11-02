package net.runelite.client.plugins.microbot.storm.plugins.actionHotkey.enums;

public enum cNone implements Actionable{
    NONE("none");


    private final String conditionals;
    cNone(String conditionals) {
        this.conditionals = conditionals;
    }

    @Override
    public String getAction() {
        return conditionals;
    }
}
