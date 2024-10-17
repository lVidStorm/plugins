package net.runelite.client.plugins.microbot.storm.modified.thieving;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.plugins.microbot.thieving.enums.iThievingNpc;
import net.runelite.client.plugins.microbot.util.misc.Rs2Food;

@ConfigGroup("iThieving")
public interface iThievingConfig extends Config {

    @ConfigItem(
            keyName = "guide",
            name = "How to use",
            description = "How to use this plugin",
            position = 0,
            section = generalSection
    )
    default String GUIDE() {
        return "Start near any of the npc";
    }
    @ConfigSection(
            name = "general",
            description = "general",
            position = 0
    )
    String generalSection = "general";
    @ConfigItem(
            keyName = "disclaimer",
            name = "disclaimer",
            description = "",
            position = 0
    )
    default String disclaimer() {
        return "Originally MOCROSOFT's Thieving plugin, modified\n" +
                "to \n" +
                "";
    }
    @ConfigItem(
            keyName = "Npc",
            name = "Npc",
            description = "Choose the npc to start thieving from",
            position = 0,
            section = generalSection
    )
    default iThievingNpc THIEVING_NPC()
    {
        return iThievingNpc.NONE;
    }

    @ConfigItem(
            keyName = "Shadow Veil",
            name = "Shadow Veil",
            description = "Are you using Shadow veil?",
            position = 1,
            section = generalSection
    )
    default boolean useShadowVeil(){return false;}

    @ConfigSection(
            name = "Food",
            description = "Food",
            position = 1
    )
    String food = "Food";

    @ConfigItem(
            keyName = "Hitpoints",
            name = "Hitpoints treshhold %",
            description = "Use food at certain hitpoint treshhold",
            position = 1,
            section = food
    )
    default int hitpoints()
    {
        return 20;
    }

    @ConfigItem(
            keyName = "Food",
            name = "Food",
            description = "type of food",
            position = 2,
            section = food
    )
    default Rs2Food food()
    {
        return Rs2Food.MONKFISH;
    }

    @ConfigItem(
            keyName = "FoodAmount",
            name = "Food Amount",
            description = "Amount of food to withdraw from bank",
            position = 2,
            section = food
    )
    default int foodAmount()
    {
        return 5;
    }

    @ConfigSection(
            name = "Coin pouch & Items",
            description = "Coin pouch & Items",
            position = 2
    )
    String coinPouchSection = "Coin pouch & Items";

    @ConfigItem(
            keyName = "Coin Pouch TreshHold",
            name = "How many coinpouches in your inventory before opening?",
            description = "How many coinpouches do you need in your inventory before opening them?",
            position = 1,
            section = coinPouchSection
    )
    default int coinPouchTreshHold()
    {
        return 28;
    }

    @ConfigItem(
            keyName = "KeepItem",
            name = "Keep items above value",
            description = "Keep items above the gp value",
            position = 1,
            section = coinPouchSection
    )
    default int keepItemsAboveValue()
    {
        return 10000;
    }

    @ConfigItem(
            keyName = "DodgyNecklaceAmount",
            name = "Dodgy necklace Amount",
            description = "Amount of dodgy necklace to withdraw from bank",
            position = 1,
            section = coinPouchSection
    )
    default int dodgyNecklaceAmount()
    {
        return 5;
    }

    @ConfigItem(
            keyName = "DoNotDropitemList",
            name = "Do not drop item list",
            description = "Do not drop item list comma seperated",
            position = 1,
            section = coinPouchSection
    )
    default String DoNotDropItemList()
    {
        return "";
    }

}
