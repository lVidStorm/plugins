package net.runelite.client.plugins.microbot.storm.modified.zshadeskiller.zshadeskiller;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.TileItem;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.ItemSpawned;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.storm.modified.zshadeskiller.zshadeskiller.enums.Area;
import net.runelite.client.plugins.microbot.storm.modified.zshadeskiller.zshadeskiller.enums.State;
import net.runelite.client.plugins.microbot.util.models.RS2Item;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

@PluginDescriptor(
        name = "<html>[<font color=#ff00ff>§</font>] " + "zShadesKiller",
        description = "Microbot zShadesKiller plugin",
        tags = {"Shades", "microbot", "Moneymaking"},
        enabledByDefault = false
)
@Slf4j
public class zShadesKillerPlugin extends Plugin {
    @Inject
    private zShadesKillerConfig config;
    @Provides
    zShadesKillerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(zShadesKillerConfig.class);
    }
    @Inject
    private Notifier notifier;
    @Inject
    private Client client;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private zShadesKillerOverlay shadesKillerOverlay;

    @Inject
    zShadesKillerScript shadesKillerScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(shadesKillerOverlay);
        }
        shadesKillerScript.run(config);
    }
    @Subscribe
    public void onItemSpawned(ItemSpawned itemSpawned)
    {
        //TODO check that player has ownership of item.
        if(itemSpawned.getItem().getOwnership()== TileItem.OWNERSHIP_SELF &&
           zShadesKillerScript.inArea(itemSpawned.getTile().getWorldLocation(), config.SHADES().Area) &&
           (Microbot.getItemManager().getItemPrice(itemSpawned.getItem().getId()) * itemSpawned.getItem().getQuantity())>=config.priceOfItemsToLoot()) {
                //zShadesKillerScript.previousItemSpawn=System.currentTimeMillis();
                RS2Item rs2Item = new RS2Item(Microbot.getItemManager().getItemComposition(itemSpawned.getItem().getId()), itemSpawned.getTile(), itemSpawned.getItem());
                zShadesKillerScript.temp.add(rs2Item);

        }
    }
    @Subscribe
    public void onClientTick(ClientTick clientTick) {
        List<Player> dangerousPlayers = this.client.getPlayers()
                .stream()
                .filter(player -> (player.getLocalLocation().distanceTo(this.client.getLocalPlayer().getLocalLocation()) / 128 <= 32) && this.shouldPlayerTriggerAlarm(player))
                .collect(Collectors.toList());
        if (zShadesKillerScript.state == State.FIGHT_SHADES) {
            if (dangerousPlayers.size() > 0) {
                this.notifier.notify("Player spotted!");
                zShadesKillerScript.state=State.WORLD_HOP;
            }
        }
    }
    private boolean shouldPlayerTriggerAlarm(Player player) {
        if (player.getId() == this.client.getLocalPlayer().getId())
        { return false; }
        if (zShadesKillerScript.inArea(player.getWorldLocation(), Area.Barrows))
        { return false; }
        return true;
    }
    protected void shutDown() {
        shadesKillerScript.shutdown();
        overlayManager.remove(shadesKillerOverlay);
    }
}
