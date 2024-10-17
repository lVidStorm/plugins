package net.runelite.client.plugins.microbot.storm.modified.nmz;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("nmz")
public interface iNmzConfig extends Config {
    String GROUP = "iNmz";

    @ConfigItem(
            keyName = "guide",
            name = "How to use",
            description = "How to use this plugin",
            position = 0,
            section = generalSection
    )
    default String GUIDE() {
        return "Start outside NMZ with gp in Dominic's coffer and make sure bankpin has been unlocked\n" +
                "Make sure you have already set up your hard dream once!\n" +
                "Make sure to turn on AUTO RETALIATE!";
    }
    @ConfigItem(
            keyName = "disclaimer",
            name = "disclaimer",
            description = "",
            position = 0
    )
    default String disclaimer() {
        return "Originally MOCROSOFT's NMZ plugin, modified\n" +
                "to \n" +
                "";
    }
    @ConfigSection(
            name = "General",
            description = "General",
            position = 0,
            closedByDefault = false
    )
    String generalSection = "general";

    @ConfigItem(
            keyName = "How many overload potions to use",
            name = "How many overload potions to use",
            description = "How many overload potions to use",
            position = 3,
            section = generalSection
    )
    default int overloadPotionAmount()
    {
        return 8;
    }

    @ConfigItem(
            keyName = "How many absorption potions to use",
            name = "How many absorption potions to use",
            description = "How many absorption potions to use",
            position = 4,
            section = generalSection
    )
    default int absorptionPotionAmount()
    {
        return 19;
    }

    @ConfigItem(
            keyName = "Stop after death",
            name = "Stop after death",
            description = "Stop after death",
            position = 4,
            section = generalSection
    )
    default boolean stopAfterDeath()
    {
        return true;
    }

    @ConfigItem(
            keyName = "Use Zapper",
            name = "Use Zapper",
            description = "Use Zapper to increase nightmare zone points",
            position = 4,
            section = generalSection
    )
    default boolean useZapper()
    {
        return false;
    }

    @ConfigItem(
            keyName = "Use Reccurent damage",
            name = "Use Reccurent damage",
            description = "Use reccurent damage to increase nightmare zone points",
            position = 4,
            section = generalSection
    )
    default boolean useReccurentDamage()
    {
        return false;
    }

    @ConfigItem(
            keyName = "Use Power Surge",
            name = "Use Power Surge",
            description = "Use power surge for infinite special attack",
            position = 4,
            section = generalSection
    )
    default boolean usePowerSurge()
    {
        return false;
    }
    @ConfigItem(
            keyName = "Auto Prayer Potion",
            name = "Auto drink prayer potion",
            description = "Automatically drinks prayer potions",
            position = 5,
            section = generalSection
    )
    default boolean togglePrayerPotions()
    {
        return false;
    }
    @ConfigItem(
            keyName = "Random Mouse Movements",
            name = "Random Mouse Movements",
            description = "Random Mouse Movements",
            position = 6,
            section = generalSection
    )
    default boolean randomMouseMovements()
    {
        return true;
    }
    @ConfigItem(
            keyName = "Walk to center",
            name = "Walk to center",
            description = "Walk to center of nmz",
            position = 7,
            section = generalSection
    )
    default boolean walkToCenter()
    {
        return true;
    }
    @ConfigItem(
            keyName = "Use Absorption potions",
            name = "Use Absorption potions",
            description = "toggle use of absorption potions",
            position = 8,
            section = generalSection
    )
    default boolean useAbsorption()
    {
        return true;
    }
    @ConfigItem(
            keyName = "Minimum absorption",
            name = "Minimum absorption",
            description = "minimum number for random amount",
            position = 9,
            section = generalSection
    )
    default int minAbsorption()
    {
        return 43;
    }

    @ConfigItem(
            keyName = "Maximum absorption",
            name = "Maximum absorption",
            description = "maximum number for random amount",
            position = 10,
            section = generalSection
    )
    default int maxAbsorption()
    {
        return 107;
    }
}
