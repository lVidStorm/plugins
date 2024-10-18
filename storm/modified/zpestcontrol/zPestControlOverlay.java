package net.runelite.client.plugins.microbot.storm.modified.zpestcontrol;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.pestcontrol.Portal;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

import static net.runelite.client.plugins.microbot.pestcontrol.PestControlScript.portals;

public class zPestControlOverlay  extends OverlayPanel {
    @Inject
    zPestControlOverlay(zPestControlPlugin plugin)
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
                    .text("Micro zPestControl V" + zPestControlScript.version)
                    .color(Color.GREEN)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder().build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left(Microbot.status)
                    .build());

            if (zPestControlScript.DEBUG) {
                for(Portal portal: portals) {
                    if (portal.getHitPoints() == null) continue;
                    panelComponent.getChildren().add(LineComponent.builder()
                            .left(portal.toString() + " - ❤️" + portal.getHitPoints().getText() + " - ⚔️ " + portal.isAttackAble())
                            .build());
                }
            }
        } catch(Exception ex) {
            System.out.println(ex.getMessage());
        }
        return super.render(graphics);
    }
}
