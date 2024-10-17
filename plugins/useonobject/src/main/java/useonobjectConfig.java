import enums.Keys;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import static java.awt.event.KeyEvent.*;

@ConfigGroup("useonobject")
public interface useonobjectConfig extends Config {
    @ConfigItem(
            keyName = "itemID",
            name = "Item ID",
            description = "The ID of the item to be used",
            position = 0
    )
    default int itemID() {
        return 11943;
    }// Default value, can be adjusted as needed

    @ConfigItem(
            keyName = "objectID",
            name = "Object ID",
            description = "The ID of the object to use the item on",
            position = 1
    )
    default int objectID() {
        return 411;
    }// Default value, can be adjusted as needed
    @ConfigItem(
            keyName = "sleepMin",
            name = "minimum sleep",
            description = "lowest sleep time",
            position = 2
    )
    default int sleepMin() {
        return 60;
    }// Default value, can be adjusted as needed

    @ConfigItem(
            keyName = "sleepMax",
            name = "maximum sleep",
            description = "highest sleep time",
            position = 3
    )
    default int sleepMax() {
        return 110;
    }// Default value, can be adjusted as needed
    @ConfigItem(
            keyName = "UseLastSlot",
            name = "UseLastSlot",
            description = "only use the last inventory slot?",
            position = 4
    )
    default boolean lastSlot() { return false; }
    @ConfigItem(
            keyName = "UseKey",
            name = "UseKey",
            description = "Which key for Use?",
            position = 5
    )
    default Keys useKey() { return Keys.VK_ESCAPE; }
    @ConfigItem(
            keyName = "BuryKey",
            name = "BuryKey",
            description = "Which key for Bury?",
            position = 6
    )
    default Keys buryKey() { return Keys.VK_CONTROL; }
}