package net.runelite.client.plugins.microbot.storm.modified.slayer.ui.panels;

import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LootPanel extends JPanel {

    private JSpinner minPriceSpinner;
    private JCheckBox prioritizeLootingCheckBox;
    private JTextField importantLootItemsField;
    private JTextField ignoreLootItemsField;

    public LootPanel() {
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
        gbc.fill = GridBagConstraints.HORIZONTAL; // Make components fill horizontally
        gbc.weightx = 1.0; // Allow components to expand horizontally

        // Min Price Spinner
        JLabel minPriceLabel = new JLabel("Min Price of Items to Loot:");
        minPriceLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 0; // Do not expand horizontally
        contentPanel.add(minPriceLabel, gbc);

        minPriceSpinner = new JSpinner(new SpinnerNumberModel(25000, 1, 100000000, 1000)); // Example range
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0; // Expand horizontally
        contentPanel.add(minPriceSpinner, gbc);

        // Prioritize Looting Checkbox
        JLabel prioritizeLootingLabel = new JLabel("Prioritize Looting Over Combat:");
        prioritizeLootingLabel.setForeground(Color.WHITE);
        gbc.gridy = 2;
        gbc.gridwidth = 2; // Span across two columns
        gbc.weightx = 0; // Do not expand horizontally
        contentPanel.add(prioritizeLootingLabel, gbc);

        prioritizeLootingCheckBox = new JCheckBox();
        prioritizeLootingCheckBox.setBackground(ColorScheme.DARK_GRAY_COLOR);
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0; // Do not expand horizontally
        contentPanel.add(prioritizeLootingCheckBox, gbc);

        // Important Loot Items Text Field
        JLabel importantLootItemsLabel = new JLabel("Important Loot Items:");
        importantLootItemsLabel.setForeground(Color.WHITE);
        importantLootItemsLabel.setToolTipText("Comma Delimited e.g. Item1, Item2, Item3");
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.weightx = 0; // Do not expand horizontally
        contentPanel.add(importantLootItemsLabel, gbc);

        importantLootItemsField = new JTextField();
        importantLootItemsField.setPreferredSize(new Dimension(200, 24)); // Make text field larger
        gbc.gridy = 5;
        gbc.weightx = 1.0; // Expand horizontally
        contentPanel.add(importantLootItemsField, gbc);

        // Ignore Loot Items Text Field
        JLabel ignoreLootItemsLabel = new JLabel("Ignore Loot Items:");
        ignoreLootItemsLabel.setForeground(Color.WHITE);
        ignoreLootItemsLabel.setToolTipText("Use * for wildcard, e.g. \"*Bones, *ashes\"");
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.weightx = 0; // Do not expand horizontally
        contentPanel.add(ignoreLootItemsLabel, gbc);

        ignoreLootItemsField = new JTextField();
        ignoreLootItemsField.setPreferredSize(new Dimension(200, 24)); // Make text field larger
        gbc.gridy = 7;
        gbc.weightx = 1.0; // Expand horizontally
        contentPanel.add(ignoreLootItemsField, gbc);
    }

    public int getMinPrice() {
        return (int) minPriceSpinner.getValue();
    }

    public boolean isPrioritizeLooting() {
        return prioritizeLootingCheckBox.isSelected();
    }

    public String getImportantLootItems() {
        return importantLootItemsField.getText();
    }

    public String getIgnoreLootItems() {
        return ignoreLootItemsField.getText();
    }
}
