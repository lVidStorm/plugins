package net.runelite.client.plugins.microbot.storm.plugins.thievingstalls;

import net.runelite.client.config.*;

@ConfigGroup("thievingstalls")
public interface thievingstallsConfig extends Config {
    @ConfigSection(
            name = "General",
            description = "General",
            position = 0,
            closedByDefault = false
    )
    String generalSection = "generalSection";
    @ConfigItem(
            keyName = "turbo",
            name = "turbo",
            description = "remove click delay to compete with other bots?",
            position = 0,
            section = generalSection
    )
    default boolean turbo() { return false; }
    @ConfigItem(
            keyName = "GameObjectID",
            name = "gameObjectID",
            description = "ID of the stall you are trying to steal from.",
            position = 1,
            section = generalSection
    )
    default String gameObjectID()
    {
        return "28823";
    }
    @ConfigItem(
            keyName = "dropEa",
            name = "dropEa",
            description = "Drop items after every steal?",
            position = 2,
            section = generalSection
    )
    default boolean dropEa() { return false; }
    @ConfigItem(
            keyName = "DoNotDropitemList",
            name = "Do not drop item list",
            description = "Do not drop item list comma seperated",
            position = 3,
            section = generalSection
    )
    default String DoNotDropItemList()
    {
        return "";
    }
    @ConfigItem(
            keyName = "KeepItem",
            name = "Keep items above value",
            description = "Keep items above the gp value",
            position = 4,
            section = generalSection
    )
    default int keepItemsAboveValue()
    {
        return 10000;
    }

    @ConfigSection(
            name = "Sleep Settings",
            description = "Set Sleep Settings",
            position = 1,
            closedByDefault = false
    )
    String sleepSection = "sleepSection";
    @ConfigItem(
            keyName = "Sleep Min",
            name = "Sleep Min",
            description = "Sets the minimum sleep time.",
            position = 0,
            section = sleepSection
    )
    @Range(
            min = 60,
            max = 20000
    )

    default int sleepMin() {
        return 61;
    }

    @ConfigItem(
            keyName = "Sleep Max",
            name = "Sleep Max",
            description = "Sets the maximum sleep time.",
            position = 0,
            section = sleepSection
    )
    @Range(
            min = 60,
            max = 20000
    )

    default int sleepMax() {
        return 297;
    }

    @ConfigItem(
            keyName = "Sleep Target",
            name = "Sleep Target",
            description = "This is the Target or Mean of the distribution.",
            position = 0,
            section = sleepSection
    )
    @Range(
            min = 60,
            max = 20000
    )

    default int sleepTarget() {
        return 123;
    }
}
