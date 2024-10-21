package net.runelite.client.plugins.microbot.storm.plugins.ModifBankStander;

import net.runelite.client.config.*;

@ConfigGroup("example")
public interface zBanksBankStanderConfig extends Config {

    @ConfigSection(
            name = "Item Settings",
            description = "Set Items to Combine",
            position = 0,
            closedByDefault = false
    )
    String itemSection = "itemSection";

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
            min = 0,
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
            min = 0,
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
            min = 0,
            max = 20000
    )

    default int sleepTarget() {
        return 900;
    }

    @ConfigItem(
            keyName = "Instructions",
            name = "Instructions",
            description = "Instructions",
            position = 1,
            section = sleepSection
    )
    default String basicInstructions() {
        return "This Script will combine items for you" +
                "\n If using a Knife etc. make sure qty is set to 1. and use Item Slot 1." +
                "\nChisel Item ID = 1755" +
                "\nKnife Item ID = 946";
    }
    @ConfigItem(
            keyName = "NeedMenuEntry",
            name = "Need Menu Entry?",
            description = "Does this combination need menu entry?",
            position = 2,
            section = sleepSection
    )
    default boolean needMenuEntry() {
        return false;
    }
    @ConfigItem(
            keyName = "SuperCombatPotions",
            name = "SuperCombatPotions",
            description = "Does this combination need menu entry?",
            position = 3,
            section = sleepSection
    )
    default boolean supercombat() {
        return false;
    }
    @ConfigItem(
            keyName = "bankall",
            name = "bankall",
            description = "Should we always bank all?",
            position = 4,
            section = sleepSection
    )
    default boolean bankall() {
        return false;
    }
    @ConfigItem(
            keyName = "WaitForAnimation",
            name = "Wait for animation?",
            description = "Does this combination need to wait for animation? ie. wait for inventory to process.",
            position = 5,
            section = sleepSection
    )
    default boolean waitForAnimation() {
        return false;
    }
    @ConfigItem(
            keyName = "withdrawAll",
            name = "is item 1 not consumed?",
            description = "is Item 1 not consumed such as a chisel?",
            position = 6,
            section = sleepSection
    )
    default boolean withdrawAll() {
        return false;
    }
    @ConfigItem(
            keyName = "oneTick",
            name = "oneTick",
            description = "is combination to be 1-ticked?",
            position = 7,
            section = sleepSection
    )
    default boolean oneTick() {
        return false;
    }

}
