package net.runelite.client.plugins.microbot.storm.modified.gotr;

import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.ProgressPieComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;



public class zGotrOverlay extends OverlayPanel {

    private final zGotrPlugin plugin;
    public static Color PUBLIC_TIMER_COLOR = Color.YELLOW;
    public static int TIMER_OVERLAY_DIAMETER = 20;
    private final ProgressPieComponent progressPieComponent = new ProgressPieComponent();

    int sleepingCounter;

    @Inject
    zGotrOverlay(zGotrPlugin plugin)
    {
        super(plugin);
        this.plugin = plugin;
        setPosition(OverlayPosition.TOP_LEFT);
        setNaughty();
    }
    @Override
    public Dimension render(Graphics2D graphics) {
        try {
            panelComponent.setPreferredSize(new Dimension(200, 300));
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Guardians of the rift V" + zGotrScript.version)
                    .color(PluginDescriptor.stormColor)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder().build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("STATE: " + zGotrScript.state)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Elemental points: " + zGotrScript.elementalRewardPoints)
                    .build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Catalytic points: " + zGotrScript.catalyticRewardPoints)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Total time script loop: " + zGotrScript.totalTime + "ms")
                    .build());

        } catch(Exception ex) {
            System.out.println(ex.getMessage());
        }
        return super.render(graphics);
    }
}
