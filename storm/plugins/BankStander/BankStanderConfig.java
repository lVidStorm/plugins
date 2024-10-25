package net.runelite.client.plugins.microbot.storm.plugins.BankStander;

import net.runelite.client.config.*;

@ConfigGroup("BankStander")
@ConfigInformation("• New version of Bank.js's BankStander<br />" +
        "• Code overhauled by eXioStorm, added features : <br />" +
        "• Withdraw more items from the bank(Super combat potions). <br />" +
        "• Select random items(for things like 1-tick). <br />" +
        "• Withdraw All, & first item not banked. <br />" +
        "• Use menu. <br />" +
        "• Wait for process")
public interface BankStanderConfig extends Config {
    @ConfigItem(
            keyName = "Instructions",
            name = "Instructions",
            description = "Instructions",
            position = 0
    )
    default String basicInstructions() {
        return "This Script will combine items for you" +
                "\n If using a Knife etc. make sure qty is set to 1. and use Item Slot 1." +
                "\nChisel Item ID = 1755" +
                "\nKnife Item ID = 946";
    }
    @ConfigSection(
            name = "Item Settings",
            description = "Set Items to Combine",
            position = 1,
            closedByDefault = false
    )
    String itemSection = "itemSection";
    @ConfigSection(
            name = "Toggles",
            description = "Change plugin behaviour",
            position = 2,
            closedByDefault = false
    )
    String toggles = "toggles";
    @ConfigSection(
            name = "Sleep Settings",
            description = "Set Sleep Settings",
            position = 3,
            closedByDefault = false
    )
    String sleepSection = "sleepSection";
    // Items
    @ConfigItem(
            keyName = "First Item",
            name = "First Item",
            description = "Sets First Item, use either Item ID or Item Name",
            position = 0,
            section = itemSection
    )

    default String firstItemIdentifier() {
        return "Knife";
    }

    @ConfigItem(
            keyName = "First Item Quantity",
            name = "First Item Quantity",
            description = "Sets First Item's Quantity.",
            position = 1,
            section = itemSection
    )
    @Range(
            min = 1,
            max = 27
    )

    default int firstItemQuantity() {
        return 1;
    }

    @ConfigItem(
            keyName = "Second Item",
            name = "Second Item",
            description = "Sets Second Item, use either Item ID or Item Name",
            position = 2,
            section = itemSection
    )

    default String secondItemIdentifier() {
        return "Logs";
    }

    @ConfigItem(
            keyName = "Second Item Quantity",
            name = "Second Item Quantity",
            description = "Sets Second Item's Quantity.",
            position = 3,
            section = itemSection
    )
    @Range(
            min = 1,
            max = 27
    )

    default int secondItemQuantity() {
        return 27;
    }
    @ConfigItem(
            keyName = "Third Item",
            name = "Third Item",
            description = "Sets Third Item, use either Item ID or Item Name",
            position = 4,
            section = itemSection
    )

    default String thirdItemIdentifier() {
        return "Knife";
    }

    @ConfigItem(
            keyName = "Third Item Quantity",
            name = "Third Item Quantity",
            description = "Sets Third Item's Quantity.",
            position = 5,
            section = itemSection
    )
    @Range(
            min = 1,
            max = 27
    )

    default int thirdItemQuantity() {
        return 1;
    }

    @ConfigItem(
            keyName = "Fourth Item",
            name = "Fourth Item",
            description = "Sets Fourth Item, use either Item ID or Item Name",
            position = 6,
            section = itemSection
    )

    default String fourthItemIdentifier() {
        return "Knife";
    }

    @ConfigItem(
            keyName = "Fourth Item Quantity",
            name = "Fourth Item Quantity",
            description = "Sets Fourth Item's Quantity.",
            position = 7,
            section = itemSection
    )
    @Range(
            min = 1,
            max = 27
    )

    default int fourthItemQuantity() {
        return 1;
    }

    @ConfigItem(
            keyName = "pause",
            name = "Pause",
            description = "Pause the script? will pause between states",
            position = 1,
            section = toggles
    )
    default boolean pause() {
        return false;
    }

    @ConfigItem(
            keyName = "useMenu",
            name = "Use Menu?",
            description = "Does this combination need menu entry?",
            position = 2,
            section = toggles
    )
    default boolean needMenuEntry() {
        return false;
    }
    @ConfigItem(
            keyName = "fourItems",
            name = "Four Items",
            description = "does this process need 4 items? e.g. super combat potions",
            position = 3,
            section = toggles
    )
    default boolean fourItems() {
        return false;
    }
    @ConfigItem(
            keyName = "WaitForProcess",
            name = "Wait for process?",
            description = "Does this combination need to wait for animation? ie. wait for inventory to process.",
            position = 5,
            section = toggles
    )
    default boolean waitForAnimation() {
        return false;
    }
    @ConfigItem(
            keyName = "withdrawAll",
            name = "withdraw all",
            description = "for using things like a chisel or knife where the item is not consumed in the process.",
            position = 6,
            section = toggles
    )
    default boolean withdrawAll() {
        return false;
    }
    @ConfigItem(
            keyName = "randomSelection",
            name = "randomSelection",
            description = "select random item in inventory?",
            position = 7,
            section = toggles
    )
    default boolean randomSelection() {
        return false;
    }
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
        return 0;
    }

    @ConfigItem(
            keyName = "Sleep Max",
            name = "Sleep Max",
            description = "Sets the maximum sleep time.",
            position = 0,
            section = sleepSection
    )
    @Range(
            min = 90,
            max = 20000
    )

    default int sleepMax() {
        return 1800;
    }

    @ConfigItem(
            keyName = "Sleep Target",
            name = "Sleep Target",
            description = "This is the Target or Mean of the distribution.",
            position = 0,
            section = sleepSection
    )
    @Range(
            min = 100,
            max = 20000
    )

    default int sleepTarget() {
        return 900;
    }
}
