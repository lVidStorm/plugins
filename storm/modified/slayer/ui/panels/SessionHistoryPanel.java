package net.runelite.client.plugins.microbot.storm.modified.slayer.ui.panels;

import net.runelite.client.plugins.microbot.storm.modified.slayer.ui.SessionData;
import net.runelite.client.plugins.microbot.storm.modified.slayer.ui.SessionHistoryManager;

import javax.swing.*;
import java.awt.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SessionHistoryPanel extends JPanel {
    private final JPanel historyContainer;
    private final JLabel totalRuntimeLabel;
    private final JLabel totalXpLabel;
    private final JLabel totalLootLabel;

    private long totalRuntime;
    private long totalXpGained;
    private long totalLootGained;

    public SessionHistoryPanel() {
        setLayout(new BorderLayout());
        historyContainer = new JPanel();
        historyContainer.setLayout(new BoxLayout(historyContainer, BoxLayout.Y_AXIS));
        add(new JScrollPane(historyContainer), BorderLayout.CENTER);

        JPanel totalsPanel = new JPanel();
        totalsPanel.setLayout(new GridLayout(3, 1));
        totalsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        totalRuntimeLabel = new JLabel("Total Runtime: 0 mins");
        totalXpLabel = new JLabel("Total XP Gained: 0");
        totalLootLabel = new JLabel("Total Loot Gained: 0");

        totalsPanel.add(totalRuntimeLabel);
        totalsPanel.add(totalXpLabel);
        totalsPanel.add(totalLootLabel);

        add(totalsPanel, BorderLayout.SOUTH);

        loadSessionHistory(); // Load session history when the panel is initialized
    }

    // Track added sessions to prevent duplicates
    private final Set<Long> addedSessionIds = new HashSet<>();

    public void addSession(long startTime, long duration, long xpGained, long lootGained) {
        long sessionId = startTime; // Or any unique identifier you can use
        if (addedSessionIds.contains(sessionId)) {
            return; // Session already added
        }

        addedSessionIds.add(sessionId);
        SessionHistoryCard card = new SessionHistoryCard(startTime, duration, xpGained, lootGained);
        historyContainer.add(card);
        updateTotals(duration, xpGained, lootGained);
        SwingUtilities.invokeLater(() -> {
            historyContainer.revalidate();
            historyContainer.repaint();
        });
    }



    public void removeSession(SessionHistoryCard card) {
        historyContainer.remove(card);
        updateTotals(-card.getDuration(), -card.getXpGained(), -card.getLootGained());
        revalidate();
        repaint();
        saveSessionHistory(); // Save session history after removal
    }

    private void updateTotals(long duration, long xpGained, long lootGained) {
        totalRuntime += duration;
        totalXpGained += xpGained;
        totalLootGained += lootGained;

        SwingUtilities.invokeLater(() -> {
            totalRuntimeLabel.setText("Total Runtime: " + (totalRuntime / 60000) + " mins");
            totalXpLabel.setText("Total XP Gained: " + totalXpGained);
            totalLootLabel.setText("Total Loot Gained: " + totalLootGained);
        });
    }

    public void loadSessionHistory() {
        historyContainer.removeAll();
        List<SessionData> sessionDataList = SessionHistoryManager.loadSessionHistory();
        if (sessionDataList != null) {
            for (SessionData data : sessionDataList) {
                long startTime = System.currentTimeMillis() - data.getDuration().toMillis();
                long duration = data.getDuration().toMillis();
                long xpGained = data.getXpGained();
                long lootGained = data.getLootValue();
                addSession(startTime, duration, xpGained, lootGained);
            }
        }
    }


    void saveSessionHistory() {
        List<SessionData> sessionDataList = new ArrayList<>();
        for (Component component : historyContainer.getComponents()) {
            if (component instanceof SessionHistoryCard) {
                SessionHistoryCard card = (SessionHistoryCard) component;
                SessionData data = new SessionData(
                        Duration.ofMillis(card.getDuration()),
                        0, // Placeholder for tasks completed
                        (int) card.getXpGained(),
                        (int) card.getLootGained()
                );
                sessionDataList.add(data);
            }
        }
        SessionHistoryManager.saveSessionHistory(sessionDataList);
    }
}
