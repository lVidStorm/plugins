package net.runelite.client.plugins.microbot.storm.modified.slayer.ui.panels;

import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class UtilityPanel extends JPanel {

    private JCheckBox prayerFlickingCheckBox;
    private JSpinner playerThresholdSpinner;

    public UtilityPanel() {
        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        // Main Content Panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(contentPanel, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Set padding
        gbc.anchor = GridBagConstraints.WEST; // Align components to the left

        // Prayer Flicking Checkbox
        JLabel prayerFlickingLabel = new JLabel("Enable Prayer Flicking:");
        prayerFlickingLabel.setForeground(Color.WHITE);
        prayerFlickingLabel.setToolTipText("Enable or disable prayer flicking.");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        contentPanel.add(prayerFlickingLabel, gbc);

        prayerFlickingCheckBox = new JCheckBox();
        prayerFlickingCheckBox.setBackground(ColorScheme.DARK_GRAY_COLOR);
        gbc.gridx = 1;
        contentPanel.add(prayerFlickingCheckBox, gbc);

        // Player Threshold Number Input
        JLabel playerThresholdLabel = new JLabel("Player Threshold:");
        playerThresholdLabel.setForeground(Color.WHITE);
        playerThresholdLabel.setToolTipText("The max number of players in a slayer area to trigger a world hop.");
        gbc.gridx = 0;
        gbc.gridy = 1;
        contentPanel.add(playerThresholdLabel, gbc);

        playerThresholdSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) playerThresholdSpinner.getEditor();
        Dimension spinnerDimension = new Dimension(50, editor.getPreferredSize().height);
        playerThresholdSpinner.setPreferredSize(spinnerDimension);
        gbc.gridx = 1;
        contentPanel.add(playerThresholdSpinner, gbc);
    }

    public boolean isPrayerFlickingEnabled() {
        return prayerFlickingCheckBox.isSelected();
    }

    public int getPlayerThreshold() {
        return (int) playerThresholdSpinner.getValue();
    }
}
