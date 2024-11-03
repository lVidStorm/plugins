package net.runelite.client.plugins.microbot.storm.modified.slayer.ui.panels;

import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CurrentSessionPanel extends JPanel {

    private List<SessionCard> sessionCards = new ArrayList<>();

    public CurrentSessionPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
    }

    public void addSessionCard(long startTime, long duration, long xpGained, long lootGained) {
        String formattedStartTime = new SimpleDateFormat("d MMMM yyyy - HH:mm a").format(new Date(startTime));
        SessionCard card = new SessionCard(formattedStartTime, startTime, duration, xpGained, lootGained);
        sessionCards.add(card);
        add(card);
        revalidate();
        repaint();
    }

    public void stopAllTimers() {
        for (SessionCard card : sessionCards) {
            card.stopTimer();
        }
    }

    public static class SessionCard extends JPanel {

        private JLabel idLabel;
        private JLabel durationLabel;
        private JLabel xpGainedLabel;
        private JLabel lootGainedLabel;
        private Timer timer;
        private long startTime;
        private long duration;

        public SessionCard(String startTimeStr, long startTimeMillis, long duration, long xpGained, long lootGained) {
            setLayout(new GridLayout(4, 1));
            setBackground(ColorScheme.DARKER_GRAY_COLOR);
            setBorder(new EmptyBorder(10, 10, 10, 10));

            idLabel = new JLabel("<html><b>" + startTimeStr + "</b></html>");
            idLabel.setForeground(Color.WHITE);

            durationLabel = new JLabel("<html><b>Runtime:</b> " + formatDuration(duration) + "</html>");
            durationLabel.setForeground(Color.WHITE);

            xpGainedLabel = new JLabel("<html><b>XP Gained:</b> " + formatNumber(xpGained) + "</html>");
            xpGainedLabel.setForeground(Color.WHITE);

            lootGainedLabel = new JLabel("<html><b>Loot Gained:</b> " + formatLoot(lootGained) + "</html>");
            lootGainedLabel.setForeground(Color.WHITE);

            add(idLabel);
            add(durationLabel);
            add(xpGainedLabel);
            add(lootGainedLabel);

            this.startTime = startTimeMillis;
            this.duration = duration;

            startTimer();
        }

        private void startTimer() {
            timer = new Timer(1000, e -> {
                long currentTime = System.currentTimeMillis();
                long elapsedTime = (currentTime - startTime) / 1000;
                durationLabel.setText("<html><b>Runtime:</b> " + formatDuration(elapsedTime) + "</html>");
            });
            timer.start();
        }

        public void stopTimer() {
            if (timer != null) {
                timer.stop();
            }
        }

        private String formatDuration(long seconds) {
            return String.format("%02dhr : %02dm : %02ds",
                    (seconds / 3600) % 24, (seconds / 60) % 60, seconds % 60);
        }

        private String formatNumber(long number) {
            return new DecimalFormat("#,###").format(number);
        }

        private String formatLoot(long loot) {
            if (loot < 1000) {
                return String.valueOf(loot);
            } else if (loot < 1_000_000) {
                return new DecimalFormat("#.#k").format(loot / 1000.0);
            } else {
                return new DecimalFormat("#.#m").format(loot / 1_000_000.0);
            }
        }
    }
    public void clearSession() {
        // Stop all timers
        stopAllTimers();

        // Remove all session cards from the panel
        removeAll();

        // Clear the list of session cards
        sessionCards.clear();

        // Revalidate and repaint to update the UI
        revalidate();
        repaint();
    }

}
