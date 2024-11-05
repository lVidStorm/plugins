package net.runelite.client.plugins.microbot.storm.plugins.mouseclickcounter;

import com.google.inject.Provides;
import java.io.IOException;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.MouseListener;
import net.runelite.client.input.MouseManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PluginDescriptor(name = "Mouse Click Counter")
public class MouseClickCounterPlugin extends Plugin {
  private static final Logger log = LoggerFactory.getLogger(MouseClickCounterPlugin.class);
  
  @Inject
  private Client client;
  
  @Inject
  private MouseClickCounterConfig config;
  
  @Inject
  private MouseManager mouseManager;
  
  @Inject
  private OverlayManager overlayManager;
  
  @Inject
  private MouseClickCounterOverlay overlay;
  
  private MouseClickCounterListener mouseListener;
  
  protected void startUp() throws Exception {
    this.mouseListener = new MouseClickCounterListener(this.client);
    this.mouseManager.registerMouseListener((MouseListener)this.mouseListener);
    this.overlayManager.add(this.overlay);
  }
  
  protected void shutDown() throws Exception {
    this.mouseListener.saveMouseClicks();
    this.mouseManager.unregisterMouseListener((MouseListener)this.mouseListener);
    this.mouseListener = null;
    this.overlayManager.remove(this.overlay);
  }
  
  public void resetConfiguration() {
    this.mouseListener.resetMouseClickCounterListener();
  }
  
  @Provides
  MouseClickCounterConfig provideConfig(ConfigManager configManager) {
    return (MouseClickCounterConfig)configManager.getConfig(MouseClickCounterConfig.class);
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
  
  public int getRightClickCounter() {
    return this.mouseListener.getRightClickCounter();
  }
  
  public int getMiddleClickCounter() {
    return this.mouseListener.getMiddleClickCounter();
  }
  
  public int getTotalClickCounter() {
    return this.mouseListener.getTotalClickCounter();
  }
}
