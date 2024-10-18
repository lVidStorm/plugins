package net.runelite.client.plugins.microbot.storm.plugins.idler;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;

import java.util.concurrent.TimeUnit;

import static net.runelite.client.plugins.microbot.util.math.Random.random;


public class IdlerScript extends Script {

    public static boolean test = false;
    public boolean run(IdlerConfig config) {
        Microbot.enableAutoRunOn = false;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                long startTime = System.currentTimeMillis();

                test = false;
                idlerSleep(180000,1080000);
                if(this.isRunning()){
                    int i = random(1,12);
                    if (i==1) {
                        if (this.isRunning()) { Rs2Tab.switchToCombatOptionsTab(); }
                    } else if (i==2) {
                        if (this.isRunning()) { Rs2Tab.switchToSkillsTab(); }
                    } else if (i==3) {
                        if (this.isRunning()) { Rs2Tab.switchToQuestTab(); }
                    } else if (i==4) {
                        if (this.isRunning()) { Rs2Tab.switchToEquipmentTab(); }
                    } else if (i==5) {
                        if (this.isRunning()) { Rs2Tab.switchToPrayerTab(); }
                    } else if (i==6) {
                        if (this.isRunning()) { Rs2Tab.switchToMagicTab(); }
                    } else if (i==7) {
                        if (this.isRunning()) { Rs2Tab.switchToGroupingTab(); }
                    } else if (i==8) {
                        if (this.isRunning()) { Rs2Tab.switchToFriendsTab(); }
                    } else if (i==9) {
                        if (this.isRunning()) { Rs2Tab.switchToAccountManagementTab(); }
                    } else if (i==10) {
                        if (this.isRunning()) { Rs2Tab.switchToSettingsTab(); }
                    } else if (i==11) {
                        if (this.isRunning()) { Rs2Tab.switchToEmotesTab(); }
                    } else {
                        if (this.isRunning()) { Rs2Tab.switchToMusicTab(); }
                    }
                    idlerSleep(2000,9000);
                    if (this.isRunning()) { Rs2Tab.switchToInventoryTab(); }
                    idlerSleep(2000,9000);
                }

                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
    private void idlerSleep(int min, int max){
        long startTime = System.currentTimeMillis();
        int randomTime = random(min, max);
        while(this.isRunning() && (System.currentTimeMillis()-startTime)<randomTime){
            sleep(600);
        }
        if(this.isRunning()) { sleep(100,1200); }
    }
}
