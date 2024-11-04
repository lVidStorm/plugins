package net.runelite.client.plugins.microbot.storm.modified.slayer;

import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class SlayerOverlay extends OverlayPanel {
    private final SlayerPlugin plugin;
    private final SlayerScript script;

    @Inject
    SlayerOverlay(SlayerPlugin plugin, SlayerScript script) {
        super(plugin);
        this.plugin = plugin;
        this.script = script;
        setPosition(OverlayPosition.BOTTOM_LEFT);
        setPreferredSize(new Dimension(800, 300));
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        try {
            panelComponent.getChildren().clear();

            // Add title with custom color
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("AIO Slayer")
                    .color(PluginDescriptor.stormColor)
                    .build());

            // Add separator line
            panelComponent.getChildren().add(LineComponent.builder().build());

            // Add status line
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Current Action:")
                    .right(Microbot.status)
                    .leftColor(Color.WHITE)
                    .rightColor(Color.GREEN)
                    .build());

            // Add Script State
            panelComponent.getChildren().add(LineComponent.builder()
                    .left(plugin.slayerScript.state.toString())
                    .build());

            // Add Task
            String currentTask = script.getTaskName();
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Task:")
                    .right(currentTask != null && !currentTask.isEmpty() ? currentTask : "No task")
                    .leftColor(Color.WHITE)
                    .rightColor(Color.WHITE)
                    .build());

            // Add combat status
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("In Combat:")
                    .right(plugin.isInCombat() ? "Yes" : "No")
                    .leftColor(Color.WHITE)
                    .rightColor(plugin.isInCombat() ? Color.GREEN : Color.RED)
                    .build());

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return super.render(graphics);
    }
}
