package net.runelite.client.plugins.microbot.storm.plugins.playermonitor;

import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;

import javax.inject.Inject;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

public class PlayerAlarmOverlay
   extends OverlayPanel {
   private final PlayerMonitorConfig config;
   private final Client client;
   private boolean playAlarm = false;
   private ArrayList<Integer> previousSelections;
   private PanelComponent clickCounter;

   @Inject
   private PlayerAlarmOverlay(PlayerMonitorConfig config, Client client) {
     this.config = config;
     this.client = client;
     this.previousSelections = new ArrayList<>();
   }
   public Dimension render(Graphics2D graphics) {
     this.panelComponent.getChildren().clear();
     this.panelComponent.setPreferredSize(new Dimension(this.client.getCanvasWidth(), this.client.getCanvasHeight()));
     for (int i = 0; i < 100; i++)
     {
       this.panelComponent.getChildren().add(LineComponent.builder()
           .left(" ")
           .build());
     }
     if (this.client.getGameCycle() % 20 >= 10) {
    if (!playAlarm) {
        if(config.playSound()){ client.playSoundEffect(config.paSoundID().getId(), 127); }
        playAlarm = true;
    }
       this.panelComponent.setBackgroundColor(this.config.flashColor());
     } else {
       if(playAlarm){
           playAlarm = false;
       }
       this.panelComponent.setBackgroundColor(new Color(0, 0, 0, 0));
     }
     return this.panelComponent.render(graphics);
   }
 }