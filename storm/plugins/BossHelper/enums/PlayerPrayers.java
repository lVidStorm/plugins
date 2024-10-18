package net.runelite.client.plugins.microbot.storm.plugins.BossHelper.enums;
import lombok.AllArgsConstructor;
import net.runelite.api.Skill;
import net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer;
import net.runelite.client.plugins.microbot.util.prayer.Rs2PrayerEnum;

import static net.runelite.client.plugins.microbot.util.prayer.Rs2PrayerEnum.*;


@AllArgsConstructor
public enum PlayerPrayers {
    Melee(PROTECT_MELEE),
    Range(PROTECT_RANGE),
    Magic(PROTECT_MAGIC);
    public Rs2PrayerEnum name;
}
