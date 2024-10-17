package net.runelite.client.plugins.microbot.storm.plugins.BossHelper.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum bossAnimatingBehaviors {
    Move3Y("3-y-axis"),
    Protect_Melee("protectMelee"),
    Protect_Range("protectRange"),
    Protect_Magic("protectMagic"),
    Zulrah("Zulrah");
    public final String name;

    @Override
    public String toString() {
        return super.toString();
    }
}
