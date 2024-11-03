package net.runelite.client.plugins.microbot.storm.modified.slayer.ui.panels;

import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SessionHistoryCard extends JPanel {
    private long startTime;
    private long duration; // Duration in milliseconds
    private long xpGained;
    private long lootGained;

    public SessionHistoryCard(long startTime, long duration, long xpGained, long lootGained) {
        this.startTime = startTime;
        this.duration = duration;
        this.xpGained = xpGained;
        this.lootGained = lootGained;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout()); // Use BorderLayout to position the close button
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        setBorder(createCardBorder());

        // Create a panel for the content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridLayout(4, 1));
        contentPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        JLabel startTimeLabel = new JLabel("<html><b>" + formatDate(startTime) + "</b></html>", SwingConstants.LEFT);
        JLabel durationLabel = new JLabel("<html><b> Duration: " + formatDuration(duration) + "</b></html>", SwingConstants.LEFT);
        JLabel xpLabel = new JLabel("<html><b> XP Gained: " + xpGained + "</b></html>", SwingConstants.LEFT);
        JLabel lootLabel = new JLabel("<html><b> Loot Gained: " + lootGained + "</b></html>", SwingConstants.LEFT);

        contentPanel.add(startTimeLabel);
        contentPanel.add(durationLabel);
        contentPanel.add(xpLabel);
        contentPanel.add(lootLabel);

        add(contentPanel, BorderLayout.CENTER);

        // Create the close button
        JButton closeButton = createCloseButton();
        add(closeButton, BorderLayout.LINE_END);
    }

    private JButton createCloseButton() {
        JButton closeButton = new JButton("X");
        closeButton.setPreferredSize(new Dimension(20, 20));
        closeButton.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder());
        closeButton.setContentAreaFilled(false);
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Handle the deletion of this card
                Container parent = getParent();
                if (parent != null) {
                    // Remove this card from the parent panel
                    ((JPanel) parent).remove(SessionHistoryCard.this);
                    // Update the UI
                    ((JPanel) parent).revalidate();
                    ((JPanel) parent).repaint();

                    // Optionally, update the parent panel (SessionHistoryPanel) if needed
                    Container grandParent = parent.getParent();
                    if (grandParent instanceof SessionHistoryPanel) {
                        ((SessionHistoryPanel) grandParent).saveSessionHistory();
                    }
                } else {
                    System.err.println("Parent container is null.");
                }
            }
        });
        return closeButton;
    }

    private Border createCardBorder() {
        Border lineBorder = BorderFactory.createMatteBorder(0, 0, 2, 0, ColorScheme.LIGHT_GRAY_COLOR); // Bottom border
        Border paddingBorder = new EmptyBorder(10, 10, 10, 10);
        return BorderFactory.createCompoundBorder(paddingBorder, lineBorder);
    }

    private String formatDate(long startTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(startTime));
    }

    private String formatDuration(long duration) {
        long seconds = duration / 1000; // Convert milliseconds to seconds
        long minutes = seconds / 60;
        seconds %= 60;
        return String.format("%d min %d sec", minutes, seconds);
    }

    public long getDuration() {
        return duration;
    }

    public long getXpGained() {
        return xpGained;
    }

    public long getLootGained() {
        return lootGained;
    }
}
