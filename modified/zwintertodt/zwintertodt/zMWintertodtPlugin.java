package net.runelite.client.plugins.microbot.storm.modified.zwintertodt.zwintertodt;

import com.google.inject.Provides;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.ItemID;
import net.runelite.api.MessageNode;
import net.runelite.api.Skill;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.StatChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.misc.TimeUtils;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;
import java.time.Instant;

@PluginDescriptor(
        name = PluginDescriptor.Default + "zWintertodt",
        description = "Wintertodt Minigame Bot",
        tags = {"Wintertodt", "microbot", "firemaking", "minigame"},
        enabledByDefault = false
)
@Slf4j
public class zMWintertodtPlugin extends Plugin {
    @Inject
    zMWintertodtScript zwintertodtScript;
    @Inject
    private zMWintertodtConfig config;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private zMWintertodtOverlay zwintertodtOverlay;

    @Getter(AccessLevel.PACKAGE)
    private int won;

    @Getter(AccessLevel.PACKAGE)
    private int lost;

    @Getter(AccessLevel.PACKAGE)
    private int logsCut;

    @Getter(AccessLevel.PACKAGE)
    private int logsFletched;

    @Getter(AccessLevel.PACKAGE)
    private int braziersFixed;

    @Getter(AccessLevel.PACKAGE)
    private int braziersLit;

    @Getter
    @Setter
    private int foodConsumed;

    @Getter
    @Setter
    private int timesBanked;

    @Getter(AccessLevel.PACKAGE)
    private boolean scriptStarted;

    private Instant scriptStartTime;

    @Provides
    zMWintertodtConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(zMWintertodtConfig.class);
    }

    protected String getTimeRunning() {
        return scriptStartTime != null ? TimeUtils.getFormattedDurationBetween(scriptStartTime, Instant.now()) : "";
    }

    private void reset() {
        this.won = 0;
        this.lost = 0;
        this.logsCut = 0;
        this.logsFletched = 0;
        this.braziersFixed = 0;
        this.braziersLit = 0;
        this.foodConsumed = 0;
        this.timesBanked = 0;
        this.scriptStartTime = null;
        this.scriptStarted = false;
    }

    @Override
    protected void startUp() throws AWTException {
        reset();
        this.scriptStartTime = Instant.now();
        this.scriptStarted = true;
        if (overlayManager != null) {
            overlayManager.add(zwintertodtOverlay);
        }
        zwintertodtScript.run(config, this);
    }

    protected void shutDown() {
        zwintertodtScript.shutdown();
        overlayManager.remove(zwintertodtOverlay);
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage) {
        ChatMessageType chatMessageType = chatMessage.getType();
        MessageNode messageNode = chatMessage.getMessageNode();

        if (!scriptStarted
                || !isInWintertodtRegion()
                || chatMessageType != ChatMessageType.GAMEMESSAGE
                && chatMessageType != ChatMessageType.SPAM) {
            return;
        }


        if (messageNode.getValue().startsWith("You fix the brazier")) {
            braziersFixed++;
        }

        if (messageNode.getValue().startsWith("You light the brazier")) {
            braziersLit++;
        }

        if (messageNode.getValue().startsWith("You have helped enough to earn a supply crate")) {
            won++;
        }

        if (messageNode.getValue().startsWith("You did not earn enough points")) {
            lost++;
        }

    }

    private boolean isInWintertodtRegion() {
        return Microbot.getClient().getLocalPlayer().getWorldLocation().getRegionID() == 6462;
    }

    private int getResourcesInInventory() {
        return Rs2Inventory.count(ItemID.BRUMA_ROOT) + Rs2Inventory.count(ItemID.BRUMA_KINDLING);
    }

    @Subscribe
    public void onHitsplatApplied(HitsplatApplied hitsplatApplied)
    {
        zMWintertodtScript.onHitsplatApplied(hitsplatApplied);
    }

    @Subscribe
    public void onStatChanged(StatChanged event) {


        if (event.getSkill() == Skill.WOODCUTTING) {
            logsCut++;
        }

        if (event.getSkill() == Skill.FLETCHING) {
            logsFletched++;
        }
    }
}
