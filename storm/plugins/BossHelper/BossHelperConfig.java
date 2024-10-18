package net.runelite.client.plugins.microbot.storm.plugins.BossHelper;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.plugins.microbot.storm.plugins.BossHelper.enums.Combat;
import net.runelite.client.plugins.microbot.storm.plugins.BossHelper.enums.PlayerPrayers;
import net.runelite.client.plugins.microbot.storm.plugins.BossHelper.enums.bossAnimatingBehaviors;

@ConfigGroup("BossHelper")
public interface BossHelperConfig extends Config {
    @ConfigSection(
            name = "General",
            description = "General",
            position = 0
    )
    String generalSection = "General";
    @ConfigSection(
            name = "BossInfo",
            description = "BossInfo",
            position = 1
    )
    String infoSection = "BossInfo";
    @ConfigSection(
            name = "Mechanics",
            description = "Mechanics",
            position = 2
    )
    String mechanicsSection = "Mechanics";

    //TODO ^^^^^^^^^ SECTIONS
    @ConfigItem(
            keyName = "guide",
            name = "How to use",
            description = "How to use this plugin",
            position = 0,
            section = generalSection
    )
    default String GUIDE() {
        return "Start wherever";
    }
    @ConfigItem(
            keyName = "foodItem",
            name = "Food Item",
            description = "what is the name of your healing item",
            position = 1,
            section = generalSection
    )
    default String fooditem() { return ""; }
    @ConfigItem(
            keyName = "healAmount",
            name = "Food Heals",
            description = "input how much health your food heals.",
            position = 2,
            section = generalSection
    )
    default int healamount() { return 0; }
    @ConfigItem(
            keyName = "prayerItem",
            name = "Prayer Item",
            description = "what is the name of your prayer restore item",
            position = 3,
            section = generalSection
    )
    default String prayeritem() { return ""; }
    @ConfigItem(
            keyName = "restoreAmount",
            name = "Prayer Restores",
            description = "input how much your prayer item restores.",
            position = 4,
            section = generalSection
    )
    default int restoreamount() { return 0; }
    @ConfigItem(
            keyName = "combatItem",
            name = "Combat Item",
            description = "what is the name of your combat boost item",
            position = 5,
            section = generalSection
    )
    default String combatitem() { return ""; }
    @ConfigItem(
            keyName = "combatStat",
            name = "Combat Stat",
            description = "what combat stat are you boosting",
            position = 6,
            section = generalSection
    )
    default Combat combatstat() { return Combat.Range; }
    @ConfigItem(
            keyName = "restoreAt",
            name = "Combat Restores",
            description = "What level range would you like to re-use your combat item? leave 0 to disable. ONLY USE NUMBERS! PARSING STRING TO INT FOR NEGATIVES!",
            position = 7,
            section = generalSection
    )
    default String restorecbat() { return "0"; }
    @ConfigItem(
            keyName = "reenablePrayer",
            name = "Re-Enable Prayer",
            description = "Turn prayer back on if the boss turns it off",
            position = 8,
            section = generalSection
    )
    default boolean reenableprayer() { return false; }
    @ConfigItem(
            keyName = "usedPrayer",
            name = "Used Prayer",
            description = "Turn prayer back on if the boss turns it off",
            position = 9,
            section = generalSection
    )
    default PlayerPrayers usedprayer() { return PlayerPrayers.Magic; }
    @ConfigItem(
            keyName = "BossIDS",
            name = "Boss IDS",
            description = "Boss IDS, seperate IDs with a comma.",
            position = 0,
            section = infoSection
    )
    default String bossids() { return ""; }
    @ConfigItem(
            keyName = "AnimationIDs",
            name = "Animation IDs",
            description = "Animation IDs, seperate IDs with a comma.",
            position = 1,
            section = infoSection
    )
    default String animationids() { return ""; }

    @ConfigItem(
            keyName = "ProjectileIDs",
            name = "Projectile IDs",
            description = "Projectile IDs, seperate IDs with a comma.",
            position = 2,
            section = infoSection
    )
    default String projectileids() { return ""; }
    @ConfigItem(
            keyName = "behaviors",
            name = "How to use",
            description = "these are the currently added behaviors;",
            position = 0,
            section = mechanicsSection
    )
    default String BEHAVIORS() {
        return "these are the currently added behaviors;\n" +
        bossAnimatingBehaviors.Protect_Melee.name + "\n" +
        bossAnimatingBehaviors.Protect_Range.name + "\n" +
        bossAnimatingBehaviors.Protect_Magic.name + "\n" +
        bossAnimatingBehaviors.Move3Y.name + "\n" +
        bossAnimatingBehaviors.Zulrah.name + "\n";
    }
    @ConfigItem(
            keyName = "UseMechanics",
            name = "Use Boss Mechanics",
            description = "Should the script use mechanics to help with bosses?",
            position = 1,
            section = mechanicsSection
    )
    default boolean useMechanics() { return false; }
    @ConfigItem(
            keyName = "WhenBossAnimates",
            name = "When Boss Animates",
            description = "When the boss starts an animation, do behaviour. KEEP IN ORDER OF ANIMATIONS. Separate with commas.",
            position = 2,
            section = mechanicsSection
    )
    default String whenbossanimates() { return ""; }
    @ConfigItem(
            keyName = "WhenProjectileExists",
            name = "When Projectile Exists",
            description = "When a projectile exists, do behaviour. KEEP IN ORDER OF PROJECTILES. Separate with commas.",
            position = 3,
            section = mechanicsSection
    )
    default String whenprojectileexists() { return ""; }
}
