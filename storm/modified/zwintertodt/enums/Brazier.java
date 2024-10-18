package net.runelite.client.plugins.microbot.storm.modified.zwintertodt.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.coords.WorldPoint;

@AllArgsConstructor
public enum Brazier {
    SOUTH_EAST(new WorldPoint(1638, 3996, 0), new WorldPoint(1639, 3998, 0));

    @Getter
    public final WorldPoint BRAZIER_LOCATION;
    @Getter
    public final WorldPoint OBJECT_BRAZIER_LOCATION;
}
