package net.runelite.client.plugins.microbot.storm.modified.slayer.ui.panels;

import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.DecimalFormat;

public class SessionCard extends JPanel {

    private JLabel idLabel;
    private JLabel durationLabel;
    private JLabel xpGainedLabel;
    private JLabel lootGainedLabel;

    public SessionCard(String startTime, long duration, long xpGained, long lootGained) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColorScheme.DARK_GRAY_COLOR, 1),
                new EmptyBorder(10, 15, 10, 15)
        ));
        setMaximumSize(new Dimension(Short.MAX_VALUE, 120)); // Adjusted max height

        idLabel = createStyledLabel("<html><b>" + startTime + "</b></html>", 14, Font.BOLD);
        durationLabel = createStyledLabel("<html><b>Runtime:</b> " + formatDuration(duration) + "</html>", 12, Font.PLAIN);
        xpGainedLabel = createStyledLabel("<html><b>XP Gained:</b> " + formatNumber(xpGained) + "</html>", 12, Font.PLAIN);
        lootGainedLabel = createStyledLabel("<html><b>Loot Gained:</b> " + formatLoot(lootGained) + "</html>", 12, Font.PLAIN);

        add(idLabel);
        add(Box.createRigidArea(new Dimension(0, 5))); // Add spacing between rows
        add(durationLabel);
        add(xpGainedLabel);
        add(lootGainedLabel);
    }

    private JLabel createStyledLabel(String text, int fontSize, int fontStyle) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", fontStyle, fontSize));
        return label;
    }

    private String formatDuration(long seconds) {
        return String.format("%02d hr : %02d min : %02d sec",
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
