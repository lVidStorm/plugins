package net.runelite.client.plugins.microbot.storm.debugging;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Player;
import net.runelite.api.events.*;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Arrays;

import static net.runelite.client.plugins.microbot.util.Global.sleep;

@PluginDescriptor(
        name = "<html>[<font color=#ff00ff>§</font>] " + "sdebugger",
        description = "Storm's test plugin",
        tags = {"testing", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class sdebuggerPlugin extends Plugin {
    public static int previousKey = 0;
    @Inject
    private sdebuggerConfig config;
    @Provides
    sdebuggerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(sdebuggerConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private sdebuggerOverlay sdebuggerOverlay;

    @Inject
    sdebuggerScript SdebuggerScript;

    @Inject
    private KeyManager keyManager;


    // This boolean variable keeps track of whether the hotkey is currently pressed
    private boolean isKey1Pressed = false;
    private boolean isKey2Pressed = false;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
        }
        keyManager.registerKeyListener(hotkeyListener);
        SdebuggerScript.run(config);
    }
    protected void shutDown() {
        keyManager.unregisterKeyListener(hotkeyListener);
        SdebuggerScript.shutdown();
    }
    private final KeyListener hotkeyListener = new KeyListener() {
        @Override
        public void keyPressed(KeyEvent e) {
            // Check for Ctrl + H key combination
            //System.out.println(e.getKeyCode());
            if (e.getKeyCode() == config.key1().getKeyCode()) {
                // If the key is not already marked as pressed, start the action loop
                if (!isKey1Pressed) {
                    isKey1Pressed = true;
                    sdebuggerScript.key1isdown = true;
                    //something here to start / stop script~
                    //TODO startActionLoop();
                }
            } else if (e.getKeyCode() == config.key2().getKeyCode()) {
                if (!isKey2Pressed) {
                    isKey2Pressed = true;
                    sdebuggerScript.key2isdown = true;
                }
            }
            if (e.getKeyCode()!=previousKey) { previousKey = e.getKeyCode(); }

        }

        @Override
        public void keyReleased(KeyEvent e) {
            // Check if the key released is the hotkey we are tracking
            if (e.getKeyCode() == config.key1().getKeyCode()) {
                isKey1Pressed = false; // Mark the key as not pressed
                sdebuggerScript.key1isdown = false;
            }
            if (e.getKeyCode() == config.key2().getKeyCode()) {
                isKey2Pressed = false;
                sdebuggerScript.key2isdown = false;
            }
        }

        @Override
        public void keyTyped(KeyEvent e) {
            // Not used
        }
    };
}
