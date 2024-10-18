import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import net.runelite.client.input.KeyManager;
import net.runelite.client.input.KeyListener;

import javax.inject.Inject;
import java.awt.*;

import java.awt.event.KeyEvent;

@PluginDescriptor(
        name = "<html>[<font color=#ff00ff>§</font>] " + "UseOnObject",
        description = "Use on Object plugin",
        tags = {"Stormsccript", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class useonobjectPlugin extends Plugin {
    public static int previousKey = 0;
    @Inject
    private useonobjectConfig config;
    @Provides
    useonobjectConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(useonobjectConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private useonobjectOverlay UseOnObjectOverlay;

    @Inject
    useonobjectScript useonobjectScript;

    @Inject
    private KeyManager keyManager;


    // This boolean variable keeps track of whether the hotkey is currently pressed
    private boolean isKey1Pressed = false;
    private boolean isKey2Pressed = false;

    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(UseOnObjectOverlay);
        }
        keyManager.registerKeyListener(hotkeyListener);
        useonobjectScript.run(config);
    }

    protected void shutDown() {
        keyManager.unregisterKeyListener(hotkeyListener);
        useonobjectScript.shutdown();
        overlayManager.remove(UseOnObjectOverlay);
    }
    /*
    int ticks = 10;
    @Subscribe
    public void onGameTick(GameTick tick)
    {
        //System.out.println(getName().chars().mapToObj(i -> (char)(i + 3)).map(String::valueOf).collect(Collectors.joining()));
        if (ticks > 0) {
            ticks--;
        } else {
            ticks = 10;
        }
    }
    */
    private final KeyListener hotkeyListener = new KeyListener() {
        @Override
        public void keyPressed(KeyEvent e) {
            // Check for Ctrl + H key combination
            //System.out.println(e.getKeyCode());
            if (e.getKeyCode() == config.useKey().getKeyCode()) {
                // If the key is not already marked as pressed, start the action loop
                if (!isKey1Pressed) {
                    isKey1Pressed = true;
                    useonobjectScript.key1isdown = true;
                    //something here to start / stop script~
                    //TODO startActionLoop();
                }
            } else if (e.getKeyCode() == config.buryKey().getKeyCode()) {
                if (!isKey2Pressed) {
                    isKey2Pressed = true;
                    useonobjectScript.key2isdown = true;
                }
            }
            if (e.getKeyCode()!=previousKey) { previousKey = e.getKeyCode(); }

        }

        @Override
        public void keyReleased(KeyEvent e) {
            // Check if the key released is the hotkey we are tracking
            if (e.getKeyCode() == config.useKey().getKeyCode()) {
                    isKey1Pressed = false; // Mark the key as not pressed
                    useonobjectScript.key1isdown = false;
            }
            if (e.getKeyCode() == config.buryKey().getKeyCode()) {
                    isKey2Pressed = false;
                    useonobjectScript.key2isdown = false;
            }
        }

        @Override
        public void keyTyped(KeyEvent e) {
            // Not used
        }
    };
}