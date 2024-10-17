package net.runelite.client.plugins.microbot.storm.plugins.chinning;

import net.runelite.client.config.*;

@ConfigGroup("chinning")
public interface ChinningConfig extends Config {
    @ConfigSection(
            name = "General",
            description = "General",
            position = 0
    )
    String generalSection = "General";
    @ConfigItem(
            keyName = "guide",
            name = "How to use",
            description = "How to use this plugin",
            position = 0,
            section = generalSection
    )
    default String GUIDE() {
        return "Start at chinning location with prayers active";
    }
    @ConfigItem(
            keyName = "prayerItem",
            name = "Prayer Item",
            description = "what is the name of your prayer restore item",
            position = 1,
            section = generalSection
    )
    default String prayeritem() { return ""; }
    @ConfigItem(
            keyName = "restoreAmount",
            name = "Prayer Restores",
            description = "input how much your prayer item restores.",
            position = 2,
            section = generalSection
    )
    default int restoreamount() { return 0; }
    @ConfigItem(
            keyName = "combatItem",
            name = "Combat Item",
            description = "what is the name of your combat boost item",
            position = 3,
            section = generalSection
    )
    default String combatitem() { return ""; }
    @ConfigItem(
            keyName = "restoreAt",
            name = "Combat Restores",
            description = "What level range would you like to re-use your combat item? leave 0 to disable",
            position = 4,
            section = generalSection
    )
    default int restorecbat() { return 0; }
    @ConfigItem(
            keyName = "teleportItem",
            name = "Teleport Item",
            description = "Item used to teleport away",
            position = 5,
            section = generalSection
    )
    default String teleportitem() { return "varrock teleport"; }
    @ConfigItem(
            keyName = "TeleportToAction",
            name = "Teleport away action",
            description = "Action used on the teleport item",
            position = 6,
            section = generalSection
    )
    default String teleportAction() {
        return "break";
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
    @ConfigSection(
            name = "Stacking tiles",
            description = "Set stacking tile coordinates",
            position = 2,
            closedByDefault = false
    )
    String stackSection = "stackSection";
    @ConfigItem(
            keyName = "stacker tile 1",
            name = "first tile for stacking",
            description = "first tile for stacking",
            position = 0,
            section = stackSection
    )
    default String tileOne() { return ""; }
    @ConfigItem(
            keyName = "stacker tile 2",
            name = "second tile for stacking",
            description = "second tile for stacking",
            position = 1,
            section = stackSection
    )
    default String tileTwo() { return ""; }
    @ConfigItem(
            keyName = "tile to attack NPC",
            name = "tile to attack NPC",
            description = "tile to attack NPC at",
            position = 2,
            section = stackSection
    )
    default String npcTile() { return ""; }
}
