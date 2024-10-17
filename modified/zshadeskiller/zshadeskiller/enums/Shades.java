package net.runelite.client.plugins.microbot.storm.modified.zshadeskiller.zshadeskiller.enums;

import lombok.AllArgsConstructor;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;

import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
public enum Shades {
    PHRIN("Phrin", Arrays.asList("Phrin Shade", "Phrin Shadow"), new WorldPoint(3493, 9723, 0),
            new WorldArea(3493, 9723, 6, 6, 0), net.runelite.client.plugins.microbot.storm.modified.zshadeskiller.zshadeskiller.enums.Area.Phrin_Shades, "Bronze"),
    RIYL("Riyl", Arrays.asList("Riyl Shade", "Riyl Shadow"), new WorldPoint(3493, 9707, 0),
            new WorldArea(3493, 9707, 6, 6, 0), net.runelite.client.plugins.microbot.storm.modified.zshadeskiller.zshadeskiller.enums.Area.Riyl_Shades, "Steel"),
    //TODO Asyn shades have many doors in the way, need to exclude areas for them.
    ASYN("Asyn", Arrays.asList("Asyn Shade", "Asyn Shadow"), new WorldPoint(3479, 9687, 0),
            new WorldArea(3479, 9687, 6, 6, 0), net.runelite.client.plugins.microbot.storm.modified.zshadeskiller.zshadeskiller.enums.Area.Asyn_Shades, "Black"),
    FIYR("Fiyr", Arrays.asList("Fiyr Shade", "Fiyr Shadow"), new WorldPoint(3466, 9708, 0),
            new WorldArea(3459, 9700, 8, 83, 0), net.runelite.client.plugins.microbot.storm.modified.zshadeskiller.zshadeskiller.enums.Area.Fiyr_Shades, "Silver"),
    URIUM("Urium", Arrays.asList("Urium Shade", "Urium Shadow"), new WorldPoint(3507, 9689, 0),
            new WorldArea(3507, 9677, 10, 14, 0), net.runelite.client.plugins.microbot.storm.modified.zshadeskiller.zshadeskiller.enums.Area.Urium_Shades, "Gold");


    public final String displayName;
    public final List<String> names;// we don't have a contain method atm, so just use a list
    public final WorldPoint location;
    public final WorldArea shadeArea;
    public final Area Area;
    public final String requiredKeys;
    @Override
    public String toString() {
        return super.toString();
    }

}
