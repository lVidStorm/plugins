package net.runelite.client.plugins.microbot.storm.plugins.BossHelper.enums;

import lombok.AllArgsConstructor;
import net.runelite.api.Skill;

@AllArgsConstructor
public enum Combat {
    Melee(Skill.STRENGTH),
    Range(Skill.RANGED),
    Magic(Skill.MAGIC);
    public Skill name;
    @Override
    public String toString() { return super.toString(); }


}
