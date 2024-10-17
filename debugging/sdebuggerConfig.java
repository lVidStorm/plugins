package net.runelite.client.plugins.microbot.storm.debugging;

import net.runelite.client.config.*;
import net.runelite.client.plugins.microbot.storm.debugging.enums.Actions;
import net.runelite.client.plugins.microbot.storm.debugging.enums.Keys;

@ConfigGroup("sdebugger")
public interface sdebuggerConfig extends Config {
    @ConfigItem(keyName = "doAction", name = "Do Action?", description = "do you want this action done?", position = 0)
    default boolean doAction() { return true; }
    @ConfigItem(keyName = "actionName", name = "name of action?", description = "what action to use", position = 1)
    default Actions actionName() { return Actions.OPEN_BANK; }
    @ConfigItem(keyName = "actionIDEntry", name = "ID provided to action?", description = "what ID to send to action?", position = 2)
    default String actionIDEntry() { return "0"; }
    @ConfigItem(keyName = "actionMenu", name = "menu of action?", description = "what action menu", position = 3)
    default String actionMenu() { return ""; }
    @ConfigItem(keyName = "key1", name = "key1", description = "what should key 1 be?", position = 4)
    default Keys key1() { return Keys.VK_ESCAPE; }
    @ConfigItem(keyName = "key2", name = "key2", description = "what should key 2 be?", position = 5)
    default Keys key2() { return Keys.VK_CONTROL; }
    @ConfigItem(keyName = "sleepMin", name = "sleepMin", description = "Minimum sleep time", position = 6)
    default int sleepMin() { return 60; }
    @ConfigItem(keyName = "sleepMax", name = "sleepMax", description = "Maximum sleep time", position = 7)
    default int sleepMax() { return 160; }
}
