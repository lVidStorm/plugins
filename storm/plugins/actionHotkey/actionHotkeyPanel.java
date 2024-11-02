package net.runelite.client.plugins.microbot.storm.plugins.actionHotkey;

import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class actionHotkeyPanel extends PluginPanel {
    private final actionHotkeyPlugin plugin;
    private final JComboBox<String> firstDropdown;
    private final JComboBox<String> secondDropdown;

    public actionHotkeyPanel(actionHotkeyPlugin plugin) {
        this.plugin = plugin;
        // Initialize drop-down lists
        firstDropdown = new JComboBox<>(new String[]{"Fruits", "Vegetables"});
        secondDropdown = new JComboBox<>();

        // Populate initial items for second drop-down
        updateSecondDropdown("Fruits", secondDropdown);
        setupPanel();
    }

    private void setupPanel() {
        setLayout(new BorderLayout());

        // Add a label at the top
        JLabel titleLabel = new JLabel("ScriptMaker Panel", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);

        // Panel to hold the drop-down lists
        JPanel dropdownPanel = new JPanel();
        dropdownPanel.setLayout(new GridLayout(2, 2, 10, 10)); // 2 rows, 2 columns, with spacing

        // First drop-down to select category
        JComboBox<String> firstDropdown = new JComboBox<>(new String[]{"Fruits", "Vegetables"});
        JComboBox<String> secondDropdown = new JComboBox<>(); // Second drop-down starts empty
        updateSecondDropdown("Fruits", secondDropdown); // Populate initial items for "Fruits"

        // Label and add first dropdown
        dropdownPanel.add(new JLabel("Select Category:", SwingConstants.RIGHT));
        dropdownPanel.add(firstDropdown);

        // Label and add second dropdown
        dropdownPanel.add(new JLabel("Select Item:", SwingConstants.RIGHT));
        dropdownPanel.add(secondDropdown);

        // Add ActionListener to the first dropdown to update the second dropdown
        firstDropdown.addActionListener(e -> {
            String selectedCategory = (String) firstDropdown.getSelectedItem();
            updateSecondDropdown(selectedCategory, secondDropdown);
        });

        // Add the dropdownPanel above the button panels
        add(dropdownPanel, BorderLayout.NORTH);

        // Main panel to hold the two rows of buttons
        JPanel mainButtonPanel = new JPanel();
        mainButtonPanel.setLayout(new BoxLayout(mainButtonPanel, BoxLayout.Y_AXIS));

        // Panel for the top row of buttons
        JPanel upperButtonPanel = new JPanel();
        upperButtonPanel.setLayout(new BoxLayout(upperButtonPanel, BoxLayout.X_AXIS));

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveSequence());
        upperButtonPanel.add(saveButton);

        upperButtonPanel.add(Box.createRigidArea(new Dimension(70, 30))); // Spacer between buttons

        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> deleteSequence());
        upperButtonPanel.add(deleteButton);

        // Panel for the bottom row of buttons
        JPanel lowerButtonPanel = new JPanel();
        lowerButtonPanel.setLayout(new BoxLayout(lowerButtonPanel, BoxLayout.X_AXIS));

        JButton addToSequenceButton = new JButton("Add");
        addToSequenceButton.addActionListener(e -> addToSequence());
        lowerButtonPanel.add(addToSequenceButton);

        lowerButtonPanel.add(Box.createRigidArea(new Dimension(70, 30))); // Spacer between buttons

        JButton removeFromSequenceButton = new JButton("Remove");
        removeFromSequenceButton.addActionListener(e -> removeFromSequence());
        lowerButtonPanel.add(removeFromSequenceButton);

        // Add both button panels to the mainButtonPanel
        mainButtonPanel.add(upperButtonPanel);
        mainButtonPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Spacer between rows
        mainButtonPanel.add(lowerButtonPanel);

        // Add the main button panel to the center of the main layout
        add(mainButtonPanel, BorderLayout.CENTER);

        // Padding for aesthetics
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    // Placeholder methods for button actions
    private void addToSequence() {
        //TODO this will add/modify the current setup to the selected array location
    }

    private void removeFromSequence() {
        //TODO this will remove the selected array location
    }

    private void saveSequence() {
        // TODO: Implement functionality for saving sequence
    }

    private void deleteSequence() {
        // TODO: Implement functionality for deleting sequence
    }
    private void updateSecondDropdown(String category, JComboBox<String> secondDropdown) {
        secondDropdown.removeAllItems(); // Clear current items

        // Define items for each category
        Map<String, String[]> categoryItems = new HashMap<>();
        categoryItems.put("Fruits", new String[]{"Apple", "Banana", "Cherry"});
        categoryItems.put("Vegetables", new String[]{"Carrot", "Broccoli", "Spinach"});

        // Add items based on the selected category
        for (String item : categoryItems.getOrDefault(category, new String[]{})) {
            secondDropdown.addItem(item);
        }
    }
}
