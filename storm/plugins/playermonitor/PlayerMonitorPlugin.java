package net.runelite.client.plugins.microbot.storm.plugins.playermonitor;

import com.google.inject.Provides;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.MouseListener;
import net.runelite.client.input.MouseManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.storm.plugins.playermonitor.zMouseClickCounterListener;
import net.runelite.client.plugins.microbot.storm.plugins.playermonitor.ClickCounterOverlay;
import net.runelite.client.ui.overlay.OverlayManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

 @PluginDescriptor(
         name = PluginDescriptor.eXioStorm + "Player Monitor",
         enabledByDefault = false

 )
 public class PlayerMonitorPlugin extends Plugin {
   private static final Logger log = LoggerFactory.getLogger(PlayerMonitorPlugin.class);
 
   
   @Inject
   private Client client;
   @Inject
   private PlayerMonitorConfig config;
   @Inject
   private OverlayManager overlayManager;
   @Inject
   private ClickCounterOverlay clickCounterOverlay;
   @Inject
   private PlayerAlarmOverlay playerAlarmOverlay;
   @Inject
   private Notifier notifier;
   @Inject
   private MouseManager mouseManager;
   private zMouseClickCounterListener mouseListener;
   
   private boolean overlayOn = false;
   private final HashMap<String, Integer> playerNameToTimeInRange = new HashMap<>();


   protected void startUp() throws Exception {
     overlayManager.add(clickCounterOverlay);
     this.mouseListener = new zMouseClickCounterListener(this.client);
     this.mouseManager.registerMouseListener((MouseListener) this.mouseListener);
   }

   @Subscribe
   public void onClientTick(ClientTick clientTick) {
     List<Player> dangerousPlayers = getPlayersInRange().stream().filter(this::shouldPlayerTriggerAlarm).collect(Collectors.toList());
     if (this.config.timeoutToIgnore() > 0)
     { updatePlayersInRange(); }
     boolean shouldAlarm = (dangerousPlayers.size() > 0);
     if (shouldAlarm && !this.overlayOn) {
       if (this.config.desktopNotification())
       { this.notifier.notify("Player spotted!"); }
       this.overlayOn = true;
       this.overlayManager.add(this.playerAlarmOverlay);
     }
     if (!shouldAlarm) {
       this.overlayOn = false;
       this.overlayManager.remove(this.playerAlarmOverlay);
     } 
   }
   private List<Player> getPlayersInRange() {
     LocalPoint currentPosition = this.client.getLocalPlayer().getLocalLocation();
     return this.client.getPlayers()
       .stream()
       .filter(player -> (player.getLocalLocation().distanceTo(currentPosition) / 128 <= this.config.alarmRadius()))
       .collect(Collectors.toList());
   }
   private boolean shouldPlayerTriggerAlarm(Player player) {
     if (player.getId() == this.client.getLocalPlayer().getId())
     { return false; }
     if (this.config.ignoreClan() && player.isClanMember())
     { return false; }
     if (this.config.ignoreFriends() && player.isFriend())
     { return false; }
     if (this.config.ignoreFriendsChat() && player.isFriendsChatMember())
     { return false; }
     if (this.config.ignoreIgnored() && this.client.getIgnoreContainer().findByName(player.getName()) != null)
     { return false; }
     if (this.config.timeoutToIgnore() > 0) {
       int timePlayerIsOnScreen = this.playerNameToTimeInRange.getOrDefault(player.getName(), Integer.valueOf(0)).intValue();
        return timePlayerIsOnScreen <= this.config.timeoutToIgnore() * 1000;
     }
     return true;
   }
   private void updatePlayersInRange() {
     List<Player> playersInRange = getPlayersInRange();
     for (Player player : playersInRange) {
       String playerName = player.getName();
       int timeInRange = this.playerNameToTimeInRange.containsKey(playerName) ? (this.playerNameToTimeInRange.get(playerName).intValue() + 20) : 20;
       this.playerNameToTimeInRange.put(playerName, Integer.valueOf(timeInRange));
     }
     List<String> playerNames = playersInRange.stream().map(Actor::getName).collect(Collectors.toList());
     List<String> playersToReset = this.playerNameToTimeInRange.keySet().stream().filter(playerName -> !playerNames.contains(playerName)).collect(Collectors.toList());
     for (String playerName : playersToReset) {
       this.playerNameToTimeInRange.remove(playerName);
     }
   }
   protected void shutDown() throws Exception {
     this.overlayManager.remove(this.clickCounterOverlay);
     this.mouseListener.saveMouseClicks();
     this.mouseManager.unregisterMouseListener((MouseListener)this.mouseListener);
     this.mouseListener = null;
       if (this.overlayOn) {
       this.overlayOn = false;
       this.overlayManager.remove(this.playerAlarmOverlay);

     } 
   }
   @Subscribe
   public void onGameStateChanged(GameStateChanged event) throws IOException {
     GameState state = event.getGameState();
     if (state == GameState.LOGIN_SCREEN || state == GameState.UNKNOWN)
       this.mouseListener.saveMouseClicks();
   }
   public int getLeftClickCounter() {
     return this.mouseListener.getLeftClickCounter();
   }
   @Provides
   PlayerMonitorConfig provideConfig(ConfigManager configManager) {
     return configManager.getConfig(PlayerMonitorConfig.class);
   }
 }


