package net.runelite.client.plugins.microbot.storm.modified.gotr;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.qualityoflife.scripts.pouch.PouchOverlay;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;
import java.time.Instant;
import java.util.Optional;
import java.util.regex.Matcher;

@PluginDescriptor(
        name = PluginDescriptor.eXioStorm + "zGuardiansOfTheRift",
        description = "zGuardians of the rift plugin",
        tags = {"runecrafting", "guardians of the rift", "gotr", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class zGotrPlugin extends Plugin {
    @Inject
    private zGotrConfig config;

    @Provides
    zGotrConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(zGotrConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private zGotrOverlay zGotrOverlay;
    @Inject
    private PouchOverlay pouchOverlay;
    @Inject
    zGotrScript zGotrScript;

    public zGotrConfig getConfig() {
        return config;
    }


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(pouchOverlay);
            overlayManager.add(zGotrOverlay);
        }
        zGotrScript.run(config);
    }

    protected void shutDown() {
        zGotrScript.shutdown();
        overlayManager.remove(zGotrOverlay);
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState() == GameState.LOADING) {
            zGotrScript.resetPlugin();
        } else if (event.getGameState() == GameState.LOGIN_SCREEN) {
            zGotrScript.isInMiniGame = false;
        }
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned npcSpawned) {
        NPC npc = npcSpawned.getNpc();
        if (npc.getId() == zGotrScript.greatGuardianId) {
            zGotrScript.greatGuardian = npc;
        }
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned npcDespawned) {
        NPC npc = npcDespawned.getNpc();
        if (npc.getId() == zGotrScript.greatGuardianId) {
            zGotrScript.greatGuardian = null;
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage) {
        if (chatMessage.getType() != ChatMessageType.SPAM && chatMessage.getType() != ChatMessageType.GAMEMESSAGE) {
            return;
        }

        String msg = chatMessage.getMessage();

        if (msg.contains("You step through the portal")) {
            Microbot.getClient().clearHintArrow();
            zGotrScript.nextGameStart = Optional.empty();
        }

        if (msg.contains("The rift becomes active!")) {
            zGotrScript.nextGameStart = Optional.empty();
            zGotrScript.state = zGotrState.ENTER_GAME;
        } else if (msg.contains("The rift will become active in 30 seconds.")) {
            zGotrScript.shouldMineGuardianRemains = true;//30s
            zGotrScript.nextGameStart = Optional.of(Instant.now().plusSeconds(30));
        } else if (msg.contains("The rift will become active in 10 seconds.")) {
            zGotrScript.shouldMineGuardianRemains = true;//10s
            zGotrScript.nextGameStart = Optional.of(Instant.now().plusSeconds(10));
        } else if (msg.contains("The rift will become active in 5 seconds.")) {
            zGotrScript.shouldMineGuardianRemains = true;//5s
            zGotrScript.nextGameStart = Optional.of(Instant.now().plusSeconds(5));
        } else if (msg.contains("The Portal Guardians will keep their rifts open for another 30 seconds.")) {
            zGotrScript.shouldMineGuardianRemains = true;//start
            zGotrScript.nextGameStart = Optional.of(Instant.now().plusSeconds(60));
        }else if (msg.toLowerCase().contains("closed the rift!") || msg.toLowerCase().contains("The great guardian was defeated!")) {
            zGotrScript.shouldMineGuardianRemains = true;//end
        }

        Matcher rewardPointMatcher = zGotrScript.rewardPointPattern.matcher(msg);
        if (rewardPointMatcher.find()) {
            zGotrScript.elementalRewardPoints = Integer.parseInt(rewardPointMatcher.group(1).replaceAll(",", ""));
            zGotrScript.catalyticRewardPoints = Integer.parseInt(rewardPointMatcher.group(2).replaceAll(",", ""));
        }
    }

    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned event) {
        GameObject gameObject = event.getGameObject();
        if (zGotrScript.isGuardianPortal(gameObject)) {
            zGotrScript.guardians.add(gameObject);
        }

        if (gameObject.getId() == zGotrScript.portalId) {
            Microbot.getClient().setHintArrow(gameObject.getWorldLocation());
        }
    }

    @Subscribe
    public void onGameObjectDespawned(GameObjectDespawned event) {
        GameObject gameObject = event.getGameObject();

        zGotrScript.guardians.remove(gameObject);
        zGotrScript.activeGuardianPortals.remove(gameObject);

        if (gameObject.getId() == zGotrScript.portalId) {
            Microbot.getClient().clearHintArrow();
        }
    }

}
