package net.runelite.client.plugins.microbot.storm.modified.zshadeskiller.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Area {
Phrin_Shades(3482, 9715, 3503,9726),
Riyl_Shades(3468,9694,3518,9713),
//TODO Asyn shades have many doors in the way, need to exclude areas for them.
Asyn_Shades(3464,9665,3481,9692),
Fiyr_Shades(3462, 9700, 3467,9716),
Urium_Shades(3496, 9666, 3517,9691),
Barrows(3522, 9666, 3579,9724),
Burgh_Bank(3487, 3204, 3504,3221);

    public final int ax;
    public final int ay;
    public final int bx;
    public final int by;
@Override
    public String toString() {
        return super.toString();
    }
}
