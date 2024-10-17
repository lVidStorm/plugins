package net.runelite.client.plugins.microbot.storm.modified.zpestcontrol;

import com.google.common.collect.ImmutableSet;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.ObjectID;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.math.Random;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.plugins.pestcontrol.Portal;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Collectors;

import static net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer.isQuickPrayerEnabled;
import static net.runelite.client.plugins.microbot.util.walker.Rs2Walker.distanceToRegion;
import static net.runelite.client.plugins.pestcontrol.Portal.*;

public class zPestControlScript extends Script {
    public static double version = 1.0;
    public static final boolean DEBUG = false;

    boolean walkToCenter = false;

    private static final Set<Integer> SPINNER_IDS = ImmutableSet.of(
            NpcID.SPINNER,
            NpcID.SPINNER_1710,
            NpcID.SPINNER_1711,
            NpcID.SPINNER_1712,
            NpcID.SPINNER_1713
    );

    private static final Set<Integer> BRAWLER_IDS = ImmutableSet.of(
            NpcID.BRAWLER,
            NpcID.BRAWLER_1736,
            NpcID.BRAWLER_1738,
            NpcID.BRAWLER_1737,
            NpcID.BRAWLER_1735
    );

    final int distanceToPortal = 8;
    public static List<Portal> portals = List.of(PURPLE, BLUE, RED, YELLOW);
    private void resetPortals() {
        for (Portal portal : portals) {
            portal.setHasShield(true);
        }
    }
    static Instant lastAnimating;
    private final Duration actionDelay = Duration.ofMillis(1200);
    static WorldPoint previousDestination;
    static int pestControlPlane;
    static boolean destination = false;

    public boolean run(zPestControlConfig config) {
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run()) return;
            try {
                if (Microbot.getClient().getMinimapZoom() != 2.0) {
                    Microbot.getClient().setMinimapZoom(2.0);
                }
                final boolean isInzPestControl = Microbot.getClient().getWidget(WidgetInfo.PEST_CONTROL_BLUE_SHIELD) != null;
                final boolean isInBoat = Microbot.getClient().getWidget(WidgetInfo.PEST_CONTROL_BOAT_INFO) != null;
                if (isInzPestControl) {
                    if (!isQuickPrayerEnabled() && Microbot.getClient().getBoostedSkillLevel(Skill.PRAYER) != 0) {
                        final Widget prayerOrb = Rs2Widget.getWidget(ComponentID.MINIMAP_QUICK_PRAYER_ORB);
                        if (prayerOrb != null) {
                            sleep(60, 1200);
                            Microbot.getMouse().click(prayerOrb.getCanvasLocation());
                            sleep(1000, 1500);
                        }
                    }
                    if (!walkToCenter) {
                        WorldPoint centerLocation = WorldPoint.fromRegion(Rs2Player.getWorldLocation().getRegionID(), 32, 17, Microbot.getClient().getPlane());
                        Rs2Walker.walkTo(centerLocation, 3);
                        sleepUntil(() -> Instant.now().compareTo(lastAnimating.plus(actionDelay).minus(Duration.ofMillis(600))) >= 0);
                        lastAction();
                        if (centerLocation.distanceTo(Rs2Player.getWorldLocation()) > 4) {
                            return;
                        } else {
                            walkToCenter = true;
                        }
                    }

                    Widget purpleHealth = PURPLE.getHitPoints();
                    Widget blueHealth = BLUE.getHitPoints();
                    Widget redHealth = RED.getHitPoints();
                    Widget yellowHealth = YELLOW.getHitPoints();

                    Rs2Combat.setSpecState(true, 550);
                    if(destination){
                        if(Microbot.getClient().getPlane()==pestControlPlane) {
                            if (Rs2Walker.isCloseToRegion(5, previousDestination.getRegionX(), previousDestination.getRegionY())) {
                                destination = false;
                            }
                            if(!Rs2Player.isMoving() && Instant.now().compareTo(lastAnimating.plus(Duration.ofMillis(4000))) >= 0){
                                destination=false;
                            }
                        }
                    }
                    for (int brawler : BRAWLER_IDS) {
                        if (!Microbot.getClient().getLocalPlayer().isInteracting()
                                && Instant.now().compareTo(lastAnimating.plus(actionDelay)) >= 0
                                && !destination) {
                            if (Rs2Npc.interact(brawler, "attack")) {
                                lastAction();
                                sleepUntil(() -> !Microbot.getClient().getLocalPlayer().isInteracting());
                                return;
                            }
                        }
                    }
                    for (int spinner : SPINNER_IDS) {
                        if (!Microbot.getClient().getLocalPlayer().isInteracting()
                                && Instant.now().compareTo(lastAnimating.plus(actionDelay)) >= 0
                                && !destination) {
                            if (Rs2Npc.interact(spinner, "attack")) {
                                lastAction();
                                sleepUntil(() -> !Microbot.getClient().getLocalPlayer().isInteracting());
                                return;
                            }
                        }
                    }
                    if (!Microbot.getClient().getLocalPlayer().isInteracting()
                        && Instant.now().compareTo(lastAnimating.plus(actionDelay)) >= 0
                        && !destination) {
                        if(checkContribution()>35) {
                            Portal targetPortal = checkPortalShields();
                            for (Portal portal : portals) {
                                if (!portal.isHasShield() && !portal.getHitPoints().getText().trim().equals("0") && checkPortalShields() == portal) {
                                    if (!Rs2Walker.isCloseToRegion(distanceToPortal, portal.getRegionX(), portal.getRegionY())) {
                                        runTo(portal.getRegionX(), portal.getRegionY());
                                    } else {
                                        Rs2Npc.attack("portal");
                                        lastAction();
                                        sleepUntil(() -> !Microbot.getClient().getLocalPlayer().isInteracting());
                                    }
                                }
                            }
                        }
                    }

                    if (Microbot.getClient().getLocalPlayer().isInteracting()) {
                        return;
                    }
                    if (!Microbot.getClient().getLocalPlayer().isInteracting()
                            && Instant.now().compareTo(lastAnimating.plus(actionDelay)) >= 0
                            && !destination) {
                        net.runelite.api.NPC portal = Arrays.stream(Rs2Npc.getPestControlPortals()).findFirst().orElse(null);
                        if (portal != null) {
                            if (Rs2Npc.attack(portal.getId())) {
                                lastAction();
                                sleepUntil(() -> !Microbot.getClient().getLocalPlayer().isInteracting());
                            }
                        } else {
                            if (!Microbot.getClient().getLocalPlayer().isInteracting()
                                && Instant.now().compareTo(lastAnimating.plus(actionDelay)) >= 0
                                && !destination) {
                                //TODO CHECK FOR LINE OF SIGHT
                                //TODO should probably change this to a configurable object from the config that can loop through a list? ideas...
                                //net.runelite.api.NPC[] npcs = Rs2Npc.getNpcs();
                                net.runelite.api.NPC[] npcs = getNpcs();
                                if (npcs.length!=0) {
                                    net.runelite.api.NPC desirable;
                                    if(config.ignoreSplatters()) {
                                        desirable = Arrays.stream(npcs).filter(x ->
                                                x.getHealthRatio() > -1 && x.getHealthRatio() > x.getHealthScale() / 3).filter(x ->
                                                !Objects.requireNonNull(x.getName()).contains("Splatter")).filter(x ->
                                                !Objects.requireNonNull(x.getName()).contains("Squire")).filter(x ->
                                                !Objects.requireNonNull(x.getName()).contains("Void Knight")).filter(x ->
                                                !Objects.requireNonNull(x.getName()).contains("Portal")).findFirst().orElse(null);
                                    } else {

                                        desirable = Arrays.stream(npcs).filter(x ->
                                                x.getHealthRatio() > -1 && x.getHealthRatio() > x.getHealthScale() / 3).filter(x ->
                                                !Objects.requireNonNull(x.getName()).contains("Squire")).filter(x ->
                                                !Objects.requireNonNull(x.getName()).contains("Void Knight")).filter(x ->
                                                !Objects.requireNonNull(x.getName()).contains("Portal")).findFirst().orElse(null);
                                    }
                                    if (desirable != null) {
                                        Rs2Npc.attack(desirable.getId());
                                        lastAction();
                                    }
                                }
                            }
                        }
                    }
                    if (!Microbot.getClient().getLocalPlayer().isInteracting() && Instant.now().compareTo(lastAnimating.plus(actionDelay)) >= 0 && !destination) {
                        destination=true;
                        walkToPortal();
                    }
                } else {
                    resetPortals();
                    destination=false;
                    walkToCenter = false;
                    if (!isInBoat) {
                        final Widget prayerOrb = Rs2Widget.getWidget(ComponentID.MINIMAP_QUICK_PRAYER_ORB);
                        sleep(20, 300);
                        if(Rs2Player.getWorldLocation().distanceTo(WorldPoint.fromRegion(Rs2Player.getWorldLocation().getRegionID(), 14, 29, Microbot.getClient().getPlane()))>3) {
                            if (Microbot.getClient().getMinimapZoom() != 2.0) {
                                Microbot.getClient().setMinimapZoom(2.0);
                            }
                            Rs2Walker.walkTo(WorldPoint.fromRegion(Rs2Player.getWorldLocation().getRegionID(), 14, 29, Microbot.getClient().getPlane()), 3);
                            sleep(20, 800);
                        }
                        if (Microbot.getClient().getLocalPlayer().getCombatLevel() >= 100) {
                            Rs2GameObject.interact(ObjectID.GANGPLANK_25632);
                        } else if (Microbot.getClient().getLocalPlayer().getCombatLevel() >= 70) {
                            Rs2GameObject.interact(ObjectID.GANGPLANK_25631);
                        } else {
                            Rs2GameObject.interact(ObjectID.GANGPLANK_14315);
                        }
                        if (isQuickPrayerEnabled() && Microbot.getClient().getBoostedSkillLevel(Skill.PRAYER) != 0) {
                            sleep(300, 1200);
                            Microbot.getMouse().click(prayerOrb.getCanvasLocation());
                            lastAction();
                        }
                        lastAction();
                        sleep(3000);
                    }
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }
    public void lastAction() {
        lastAnimating = (Instant.now().plusMillis(Random.random(20, 400)));
    }
    public void runTo(int x, int y){
        lastAction();
        pestControlPlane=Microbot.getClient().getPlane();
        destination=true;
        previousDestination=WorldPoint.fromRegion(Rs2Player.getWorldLocation().getRegionID(), x, y, Microbot.getClient().getPlane());
        Rs2Walker.walkTo(WorldPoint.fromRegion(Rs2Player.getWorldLocation().getRegionID(), x, y, Microbot.getClient().getPlane()), 5);
    }
    public Portal checkPortalShields() {
        List<Pair<Portal, Integer>> distancesToPortal = new ArrayList();
        for (Portal portal : portals) {
            if (!portal.isHasShield() && !portal.getHitPoints().getText().trim().equals("0")) {
                distancesToPortal.add(Pair.of(portal, distanceToRegion(portal.getRegionX(), portal.getRegionY())));
            }
        }
        if(!distancesToPortal.isEmpty()) {
            Pair<Portal, Integer> closestPortal = distancesToPortal.stream().min(Map.Entry.comparingByValue()).get();
            return closestPortal.getKey();
        } else {
            return getClosestLivePortal();
        }
    }
    public Portal getClosestLivePortal() {
        List<Pair<Portal, Integer>> distancesToPortal = new ArrayList();
        for (Portal portal : portals) {
            if (portal.getHitPoints().getText().trim().equals("250")) {
                distancesToPortal.add(Pair.of(portal, distanceToRegion(portal.getRegionX(), portal.getRegionY())));
            }
        }

        Pair<Portal, Integer> closestPortal = distancesToPortal.stream().min(Map.Entry.comparingByValue()).get();

        return closestPortal.getKey();
    }
    public void walkToPortal() {
        Portal closestLivePortal = getClosestLivePortal();
        for (Portal portal : portals) {
            if (portal.getHitPoints().getText().trim().equals("250") && closestLivePortal == portal) {
                if (!Rs2Walker.isCloseToRegion(distanceToPortal, portal.getRegionX(), portal.getRegionY())) {
                    runTo(portal.getRegionX(), portal.getRegionY());
                    previousDestination=WorldPoint.fromRegion(Rs2Player.getWorldLocation().getRegionID(), portal.getRegionX(), portal.getRegionY(), Microbot.getClient().getPlane());
                } else {
                    destination=false;
                }
            }
        }
    }
    public int checkContribution(){
        Widget bar = Microbot.getClient().getWidget(ComponentID.PEST_CONTROL_ACTIVITY_BAR).getChild(0);
        Rectangle2D bounds = bar.getBounds().getBounds2D();
        Widget prgs = Microbot.getClient().getWidget(ComponentID.PEST_CONTROL_ACTIVITY_PROGRESS).getChild(0);
        int perc = (int) ((prgs.getBounds().getWidth() / bounds.getWidth()) * 100);
        return perc;
    }
    public static NPC[] getNpcs() {
        List<NPC> npcs = Microbot.getClient().getNpcs().stream()
                .sorted(Comparator.comparingInt(value -> value.getLocalLocation().distanceTo(Microbot.getClient().getLocalPlayer().getLocalLocation())))
                .collect(Collectors.toList());
        return npcs.toArray(new NPC[npcs.size()]);
    }
    public void shutDown() {
        super.shutdown();
    }
}
