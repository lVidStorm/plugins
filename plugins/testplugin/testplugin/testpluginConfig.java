package net.runelite.client.plugins.microbot.storm.plugins.testplugin.testplugin;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("testplugin")
public interface testpluginConfig extends Config {
    @ConfigSection(
            name = "General",
            description = "General",
            position = 0,
            closedByDefault = false
    )
    String generalSection = "generalSection";
    @ConfigItem(
            keyName = "DoNotDropitemList",
            name = "Do not drop item list",
            description = "Do not drop item list comma seperated",
            position = 0,
            section = generalSection
    )
    default String DoNotDropItemList()
    {
        return "";
    }
    @ConfigSection(
            name = "Stacking tiles",
            description = "Set stacking tile coordinates",
            position = 1,
            closedByDefault = false
    )
    String stackSection = "stackSection";
    @ConfigItem(
            keyName = "walktile",
            name = "walk to tile",
            description = "walk to tile",
            position = 0,
            section = stackSection
    )
    default String walktile() { return ""; }
}
