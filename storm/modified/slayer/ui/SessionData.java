package net.runelite.client.plugins.microbot.storm.modified.slayer.ui;

import java.io.Serializable;
import java.time.Duration;

public class SessionData implements Serializable {
    private static final long serialVersionUID = 1L; // Ensure version compatibility

    private Duration duration;
    private int tasksCompleted;
    private int xpGained;
    private int lootValue;

    public SessionData(Duration duration, int tasksCompleted, int xpGained, int lootValue) {
        this.duration = duration;
        this.tasksCompleted = tasksCompleted;
        this.xpGained = xpGained;
        this.lootValue = lootValue;
    }

    // Getters and Setters

    public Duration getDuration() {

        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public int getTasksCompleted() {
        return tasksCompleted;
    }

    public void setTasksCompleted(int tasksCompleted) {
        this.tasksCompleted = tasksCompleted;
    }

    public int getXpGained() {
        return xpGained;
    }

    public void setXpGained(int xpGained) {
        this.xpGained = xpGained;
    }

    public int getLootValue() {
        return lootValue;
    }

    public void setLootValue(int lootValue) {
        this.lootValue = lootValue;
    }
}
