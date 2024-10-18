package net.runelite.client.plugins.microbot.storm.modified.driftnet;

import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.*;

import javax.inject.Inject;
import java.awt.*;

class iDriftNetOverlay extends Overlay
{
    private final iDriftNetConfig config;
    private final iDriftNetPlugin plugin;

    @Inject
    private iDriftNetOverlay(iDriftNetConfig config, iDriftNetPlugin plugin)
    {
        this.config = config;
        this.plugin = plugin;
        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.LOW);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setNaughty();
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!plugin.isInDriftNetArea())
        {
            return null;
        }

        if (config.highlightUntaggedFish())
        {
            renderFish(graphics);
        }
        if (config.showNetStatus())
        {
            renderNets(graphics);
        }
        if (config.tagAnnetteWhenNoNets())
        {
            renderAnnette(graphics);
        }

        return null;
    }

    private void renderFish(Graphics2D graphics)
    {
        for (NPC fish : iDriftNetPlugin.getFish())
        {
            if (!iDriftNetPlugin.getTaggedFish().containsKey(fish))
            {
                OverlayUtil.renderActorOverlay(graphics, fish, "", config.untaggedFishColor());
            }
        }
    }

    private void renderNets(Graphics2D graphics)
    {
        for (DriftNet net : iDriftNetPlugin.getNETS())
        {
            final Shape polygon = net.getNet().getConvexHull();

            if (polygon != null)
            {
                OverlayUtil.renderPolygon(graphics, polygon, net.getStatus().getColor());
            }

            String text = net.getFormattedCountText();
            Point textLocation = net.getNet().getCanvasTextLocation(graphics, text, 0);
            if (textLocation != null)
            {
                OverlayUtil.renderTextLocation(graphics, textLocation, text, config.countColor());
            }
        }
    }

    private void renderAnnette(Graphics2D graphics)
    {
        GameObject annette = plugin.getAnnette();
        if (annette != null && !plugin.isDriftNetsInInventory())
        {
            OverlayUtil.renderPolygon(graphics, annette.getConvexHull(), config.annetteTagColor());
        }
    }
}

