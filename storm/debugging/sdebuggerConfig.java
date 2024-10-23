package net.runelite.client.plugins.microbot.storm.debugging;

import net.runelite.client.config.*;
import net.runelite.client.plugins.microbot.storm.debugging.enums.Actions;
import net.runelite.client.plugins.microbot.storm.debugging.enums.Keys;

@ConfigGroup("sdebugger")
public interface sdebuggerConfig extends Config {
    @ConfigSection(
            name = "first",
            description = "first action",
            position = 5
    )
    String firstSection = "First Action";
    @ConfigSection(
            name = "second",
            description = "second action",
            position = 6
    )
    String secondSection = "Second Action";
    @ConfigItem(keyName = "doAction", name = "Do Action?", description = "do you want this action done?", position = 0)
    default boolean doAction() { return true; }
    @ConfigItem(keyName = "key1", name = "key1", description = "what should key 1 be?", position = 1)
    default Keys key1() { return Keys.VK_ESCAPE; }
    @ConfigItem(keyName = "key2", name = "key2", description = "what should key 2 be?", position = 2)
    default Keys key2() { return Keys.VK_CONTROL; }
    @ConfigItem(keyName = "sleepMin", name = "sleepMin", description = "Minimum sleep time", position = 3)
    default int sleepMin() { return 60; }
    @ConfigItem(keyName = "sleepMax", name = "sleepMax", description = "Maximum sleep time", position = 4)
    default int sleepMax() { return 160; }

    @ConfigItem(keyName = "firstActionName", name = "name of action?", description = "what action to use", position = 0, section = firstSection)
    default Actions firstActionName() { return Actions.OPEN_BANK; }
    @ConfigItem(keyName = "firstActionIDEntry", name = "ID provided to action?", description = "what ID to send to action?", position = 1, section = firstSection)
    default String firstActionIDEntry() { return "0"; }
    @ConfigItem(keyName = "firstActionMenu", name = "menu of action?", description = "what action menu", position = 2, section = firstSection)
    default String firstActionMenu() { return ""; }

    @ConfigItem(keyName = "secondActionName", name = "name of action?", description = "what action to use", position = 0, section = secondSection)
    default Actions secondActionName() { return Actions.OPEN_BANK; }
    @ConfigItem(keyName = "secondActionIDEntry", name = "ID provided to action?", description = "what ID to send to action?", position = 1, section = secondSection)
    default String secondActionIDEntry() { return "0"; }
    @ConfigItem(keyName = "secondActionMenu", name = "menu of action?", description = "what action menu", position = 2, section = secondSection)
    default String secondActionMenu() { return ""; }
}
