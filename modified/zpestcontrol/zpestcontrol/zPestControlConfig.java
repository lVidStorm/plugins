package net.runelite.client.plugins.microbot.storm.modified.zpestcontrol.zpestcontrol;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("zpestcontrol")
public interface zPestControlConfig extends Config {
    @ConfigItem(
            keyName = "guide",
            name = "How to use",
            description = "How to use this plugin",
            position = 1
    )
    default String GUIDE() {
        return "Start near a boat of your combat level";
    }
    @ConfigItem(
            keyName = "disclaimer",
            name = "disclaimer",
            description = "",
            position = 0
    )
    default String disclaimer() {
        return "Originally MOCROSOFT's Pest Control plugin, modified\n" +
                "to \n" +
                "";
    }
    @ConfigItem(
            keyName = "Ignore Splatters",
            name = "Ignore Splatters",
            description = "should the script ignore splatters",
            position = 2
    )
    default boolean ignoreSplatters() {
        return true;
    }

    @ConfigItem(
            keyName = "NPC Priority 1",
            name = "NPC Priority 1",
            description = "What npc to attack as first option",
            position = 3
    )
    default zPestControlNpc Priority1() {
        return zPestControlNpc.PORTAL;
    }
    @ConfigItem(
            keyName = "NPC Priority 2",
            name = "NPC Priority 2",
            description = "What npc to attack as second option",
            position = 4
    )
    default zPestControlNpc Priority2() {
        return zPestControlNpc.SPINNER;
    }
    @ConfigItem(
            keyName = "NPC Priority 3",
            name = "NPC Priority 3",
            description = "What npc to attack as third option",
            position = 5
    )
    default zPestControlNpc Priority3() {
        return zPestControlNpc.BRAWLER;
    }

    @ConfigItem(
            keyName = "Alch in boat",
            name = "Alch while waiting",
            description = "Alch while waiting",
            position = 6
    )
    default boolean alchInBoat() {
        return false;
    }

    @ConfigItem(
            keyName = "itemToAlch",
            name = "Item to alch",
            description = "Item to alch",
            position = 7
    )
    default String alchItem() {
        return "";
    }

    @ConfigItem(
            keyName = "QuickPrayer",
            name = "Enable QuickPrayer",
            description = "Enables quick prayer",
            position = 8
    )
    default boolean quickPrayer() {
        return false;
    }

    @ConfigItem(
            keyName = "Special Attack",
            name = "Use Special Attack on %",
            description = "What percentage to use Special Attack",
            position = 9
    )
    default int specialAttackPercentage() {
        return 100;
    }
}
