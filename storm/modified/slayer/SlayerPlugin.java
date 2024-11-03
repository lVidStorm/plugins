package net.runelite.client.plugins.microbot.storm.modified.slayer;

import com.google.inject.Provider;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.storm.modified.slayer.ui.panels.BanksSlayerPluginPanel;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@PluginDescriptor(
        name = PluginDescriptor.Bank + "Bank's AIO Slayer",
        description = "Bank's AIO Slayer",
        tags = {"slayer", "bank.js"},
        enabledByDefault = false
)
@Slf4j
public class SlayerPlugin extends Plugin {
    @Inject
    private SlayerConfig config;

    @Provides
    SlayerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(SlayerConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private SlayerOverlay slayerOverlay;

    @Inject
    SlayerScript slayerScript;

    @Inject
    private ClientThread clientThread;

    @Inject
    private Client client;

    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private Provider<BanksSlayerPluginPanel> uiPanel;

    private NavigationButton uiNavigationButton;


    private static int tickCounter = 0;
    private Instant startOfLastTick;

    // Widgets for Prayer Flicking
    private static final int PRAYER_ORB = 10485781;
    private final Random random = new Random();
    private ScheduledExecutorService scheduler;
    private boolean initialQuickPrayerEnabled;

    @Override
    protected void startUp() throws AWTException {
        // Correct the path to the image resource
        final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/skill_icons/slayer.png");

        uiNavigationButton = NavigationButton.builder()
                .tooltip("Banks AIO Slayer")
                .icon(icon)
                .priority(10)
                .panel(uiPanel.get())
                .build();

        clientToolbar.addNavigation(uiNavigationButton);

        slayerScript.run(config);
        overlayManager.add(slayerOverlay);
        scheduler = Executors.newScheduledThreadPool(1);
        initialQuickPrayerEnabled = false; // Reset the state
    }



    @Override
    protected void shutDown() {
        slayerScript.stop(); // Stop the script and cancel scheduled tasks
        overlayManager.remove(slayerOverlay);
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
        initialQuickPrayerEnabled = false; // Reset the state
    }


    @Subscribe
    public void onGameTick(GameTick event) {
        startOfLastTick = Instant.now();
        tickCounter++;

        if (isPrayerFlickingEnabled(config)) {
            if (isInCombat() && slayerScript.isCloseToMonster(slayerScript.getTaskName())) {
                if (!initialQuickPrayerEnabled) {
                    Microbot.status = "Enabling Quick Prayers";
                    enableQuickPrayer();
                    initialQuickPrayerEnabled = true;
                } else {
                    clientThread.invoke(() -> {
                        try {
                            Microbot.status = "Flicking Quick Prayers";
                            togglePrayer();
                        } catch (Exception e) {
                            log.error("Error in togglePrayer: ", e);
                        }
                    });
                }
            } else {
                if (initialQuickPrayerEnabled) {
                    Microbot.status = "Disabling Quick Prayers";
                    disableQuickPrayer();
                    Microbot.status = "Idle";
                    initialQuickPrayerEnabled = false;
                }
            }
        }
    }


    public boolean isPrayerActive() {
        boolean active = Rs2Prayer.isQuickPrayerEnabled();
        //log.info("Quick Prayers are " + (active ? "Enabled" : "Disabled") + ".");
        return active;
    }

    public void clickPrayerOrb() {
        // log.info("Attempting to click the prayer orb");
        Widget prayerOrbWidget = client.getWidget(PRAYER_ORB);
        if (prayerOrbWidget != null) {
            Rs2Widget.clickWidget(PRAYER_ORB);
            //log.info("Clicked the prayer orb");
        } else {
            log.warn("Prayer orb widget not found");
        }
    }

    public void enableQuickPrayer() {
        if (!isPrayerActive()) {
            clickPrayerOrb();
            // log.info("Enabled Quick Prayers");
        }
    }

    public void disableQuickPrayer() {
        if (isPrayerActive()) {
            clickPrayerOrb();
            //  log.info("Disabled Quick Prayers");
        }
    }

    public boolean isInCombat() {
        return Rs2Combat.inCombat();
    }

    public boolean isPrayerFlickingEnabled(SlayerConfig config) {
        return config.prayerFlicking();
    }

    private void togglePrayer() {
        clickPrayerOrb(); // First click to turn off

        // Adding delay of 150ms +/- 30ms
        int delay = 150 + random.nextInt(60) - 30;
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.schedule(() -> clientThread.invokeLater(() -> {
                clickPrayerOrb(); // Second click to turn back on
            }), delay, TimeUnit.MILLISECONDS);
        }
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged varbitChanged) {
        if (varbitChanged.getVarpId() == VarPlayer.CANNON_AMMO) {
            int cballThreshold = getCannonBallThreshold();
            int cballsLeft = varbitChanged.getValue();
            System.out.println("Balls Left: " + cballsLeft);
            System.out.println("Ball Threshold: " + cballThreshold);
            boolean cannonIsPlaced = Rs2GameObject.exists(6);

            if (cannonIsPlaced) {
                if (cballsLeft <= cballThreshold) {
                    GameObject cannon = Rs2GameObject.get("Dwarf multicannon");
                    if (cannon != null) {
                        System.out.println("Firing cannon because balls left (" + cballsLeft + ") is below or equal to threshold (" + cballThreshold + ").");
                        Rs2GameObject.interact(cannon, "Fire");
                    } else {
                        System.out.println("No cannon found to interact with.");
                    }
                } else {
                    System.out.println("Not firing cannon. Balls left (" + cballsLeft + ") is above threshold (" + cballThreshold + ").");
                }
            } else {
                System.out.println("Cannon is not placed.");
            }
        }
    }


    public static int getCannonBallThreshold() {
        Random random = new Random();
        double[] weights = {0.05, 0.05, 0.05, 0.05, 0.05, 0.1, 0.15, 0.15, 0.15, 0.2, 0.2}; // Higher weights for higher thresholds
        double cumulativeProbability = 0.0;
        double p = random.nextDouble();

        for (int i = 0; i < weights.length; i++) {
            cumulativeProbability += weights[i];
            if (p <= cumulativeProbability) {
                return i;
            }
        }
        // fallback (shouldn't reach)
        return 10;
    }


}
