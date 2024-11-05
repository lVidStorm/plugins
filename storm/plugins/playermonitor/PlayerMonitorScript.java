package net.runelite.client.plugins.microbot.storm.plugins.playermonitor;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;
import java.util.concurrent.TimeUnit;

public class PlayerMonitorScript extends Script {
    public static String version = "0.0.1";
    @Inject
    private PlayerMonitorConfig config;
    @Inject
    private PlayerMonitorPlugin plugin; // Access overlayOn status
    @Inject
    private OverlayManager overlayManager;
    private static FlashOverlay flashOverlay;
    boolean playAlarm;
    Color offColor = new Color(0, 0, 0, 0);

    public boolean run(PlayerMonitorConfig config, OverlayManager overlayManager) {
        this.config = config;
        this.overlayManager = overlayManager;
        playAlarm = false;
        // Initialize FlashOverlay and set initial color
        flashOverlay = new FlashOverlay();
        flashOverlay.setFlashColor(offColor);
        overlayManager.add(flashOverlay);
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (Microbot.getClient().getGameCycle() % 20 >= 10) {
                    if (plugin.isOverlayOn() && !playAlarm) {
                        if (config.playAlarmSound()) {
                            Microbot.getClientThread().invokeLater(() -> Microbot.getClient().playSoundEffect(config.alarmSoundID().getId(), 127));
                        }
                        playAlarm = true;
                        flashOverlay.setFlashColor(config.flashColor());
                    }
                } else {
                    if (playAlarm) {
                        playAlarm = false;
                        flashOverlay.setFlashColor(offColor);
                    }
                }
            } catch (Exception ex) {
                Microbot.log(ex.getMessage());
            }
        }, 0, 1, TimeUnit.MILLISECONDS);
        return true;
    }
    @Override
    public void shutdown() {
        // Cancel the scheduled task
        if (mainScheduledFuture != null) {
            mainScheduledFuture.cancel(true);
        }
        // Ensure the overlay is fully cleared and removed
        if (flashOverlay != null) {
            flashOverlay.setFlashColor(offColor); // Clear the overlay
            overlayManager.remove(flashOverlay); // Remove from OverlayManager
        }
        super.shutdown();
    }
}