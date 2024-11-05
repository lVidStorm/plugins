package net.runelite.client.plugins.microbot.storm.plugins.mouseclickcounter;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import javax.inject.Inject;
import net.runelite.api.MenuAction;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;

public class MouseClickCounterOverlay extends Overlay {
  private final PanelComponent panelComponent = new PanelComponent();
  
  private final MouseClickCounterConfig config;
  
  private final MouseClickCounterPlugin plugin;
  
  private int size;
  
  private ArrayList<Integer> previousSelections;
  
  @Inject
  private MouseClickCounterOverlay(MouseClickCounterConfig config, MouseClickCounterPlugin plugin) {
    setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
    this.config = config;
    this.size = 0;
    this.plugin = plugin;
    this.previousSelections = new ArrayList<>();
    getMenuEntries().add(new OverlayMenuEntry(MenuAction.RUNELITE_OVERLAY_CONFIG, "Configure", "Mouse click counter overlay"));
  }
  
  private int getSize(Graphics2D graphics) {
    int configSize = 0;
    ArrayList<Integer> currentSelections = new ArrayList<>();
    if (this.config.showTotalClick()) {
      String totalString = "Clicks: " + this.plugin.getTotalClickCounter();
      int totalSize = graphics.getFontMetrics().stringWidth(totalString);
      configSize = Math.max(configSize, totalSize);
      currentSelections.add(Integer.valueOf(0));
    } 
    if (this.config.showLeftClick()) {
      String leftString = "Left: " + this.plugin.getLeftClickCounter();
      int leftSize = graphics.getFontMetrics().stringWidth(leftString);
      configSize = Math.max(configSize, leftSize);
      currentSelections.add(Integer.valueOf(1));
    } 
    if (this.config.showRightClick()) {
      String rightString = "Right: " + this.plugin.getRightClickCounter();
      int rightSize = graphics.getFontMetrics().stringWidth(rightString);
      configSize = Math.max(configSize, rightSize);
      currentSelections.add(Integer.valueOf(2));
    } 
    if (this.config.showMiddleClick()) {
      String middleString = "Middle: " + this.plugin.getMiddleClickCounter();
      int middleSize = graphics.getFontMetrics().stringWidth(middleString);
      configSize = Math.max(configSize, middleSize);
      currentSelections.add(Integer.valueOf(3));
    } 
    Collections.sort(currentSelections);
    if (!currentSelections.equals(this.previousSelections)) {
      this.size = configSize;
      this.previousSelections = currentSelections;
    } else if (this.size + 5 < configSize) {
      this.size = configSize;
    } 
    return this.size;
  }
  
  public Dimension render(Graphics2D graphics) {
    this.panelComponent.getChildren().clear();
    this.panelComponent.setPreferredSize(new Dimension(getSize(graphics) + 15, 0));
    if (!this.config.hide()) {
      if (this.config.showTotalClick())
        this.panelComponent.getChildren().add(LineComponent.builder()
            .left("Clicks: ")
            .right("" + this.plugin.getTotalClickCounter())
            .build()); 
      if (this.config.showLeftClick())
        this.panelComponent.getChildren().add(LineComponent.builder()
            .left("Left: ")
            .right("" + this.plugin.getLeftClickCounter())
            .build()); 
      if (this.config.showRightClick())
        this.panelComponent.getChildren().add(LineComponent.builder()
            .left("Right: ")
            .right("" + this.plugin.getRightClickCounter())
            .build()); 
      if (this.config.showMiddleClick())
        this.panelComponent.getChildren().add(LineComponent.builder()
            .left("Middle: ")
            .right("" + this.plugin.getMiddleClickCounter())
            .build()); 
    } 
    return this.panelComponent.render(graphics);
  }
}
