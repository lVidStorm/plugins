package net.runelite.client.plugins.microbot.storm.plugins.lunarglassmake.lunarglassmake;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

import javax.inject.Inject;

import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.TitleComponent;

public class GlassMakeOverlay extends OverlayPanel {

    @Inject
    GlassMakeOverlay(GlassMakePlugin plugin)
    {
        super(plugin);
        setPosition(OverlayPosition.TOP_LEFT);
        setNaughty();
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        panelComponent.setPreferredSize(new Dimension(200, 300));
        panelComponent.getChildren().add(TitleComponent.builder()
                .text("GlassMake V" + GlassMakeScript.version)
                .color(Color.GREEN)
                .build());
        return super.render(graphics);
    }
}