package net.runelite.client.plugins.microbot.storm.ogPlugins.ogBlastFurnace;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class ogBlastFurnaceOverlay extends OverlayPanel {
    @Inject
    ogBlastFurnaceOverlay(ogBlastFurnacePlugin plugin)
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
                        .text("OG Blast Furnace BETA")
                        .color(Color.GREEN)
                        .build());

                panelComponent.getChildren().add(LineComponent.builder().build());

                panelComponent.getChildren().add(LineComponent.builder()
                        .left(Microbot.status)
                        .build());


            } catch(Exception ex) {
                System.out.println(ex.getMessage());
        }
        return super.render(graphics);
    }
}
