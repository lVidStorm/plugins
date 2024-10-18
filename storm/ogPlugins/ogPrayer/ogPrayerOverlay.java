package net.runelite.client.plugins.microbot.storm.ogPlugins.ogPrayer;

import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class ogPrayerOverlay extends OverlayPanel {
    @Inject
    ogPrayerOverlay(ogPrayerPlugin plugin)
    {
        super(plugin);
        setPosition(OverlayPosition.TOP_LEFT);
        setNaughty();
    }
    @Override
    public Dimension render(Graphics2D graphics) {
            try {
                panelComponent.setPreferredSize(new Dimension(200, 300));
                panelComponent.getChildren().add(TitleComponent.builder()
                        .text("OG Prayer V" + ogPrayerScript.version)
                        .color(Color.GREEN)
                        .build());

                panelComponent.getChildren().add(LineComponent.builder().build());

                panelComponent.getChildren().add(LineComponent.builder()
                        .left(ogPrayerScript.status.toString())
                        .build());

//            for (Point point: Microbot.getMouse().mousePositions) {
//                graphics.setColor(Color.RED);
//                graphics.drawString("x", point.getX(), point.getY());
//            }

            } catch(Exception ex) {
                System.out.println(ex.getMessage());
            }
        return super.render(graphics);
    }
}
