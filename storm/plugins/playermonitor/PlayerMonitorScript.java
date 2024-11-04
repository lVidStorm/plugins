package net.runelite.client.plugins.microbot.storm.plugins.playermonitor;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;

import java.util.concurrent.TimeUnit;

public class PlayerMonitorScript extends Script {
    public static String version = "0.0.1";

    public boolean run(PlayerMonitorConfig config) {
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {

            } catch (Exception ex) {
                Microbot.log(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }
}