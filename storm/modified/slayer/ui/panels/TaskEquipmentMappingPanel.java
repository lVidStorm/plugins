package net.runelite.client.plugins.microbot.storm.modified.slayer.ui.panels;

import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TaskEquipmentMappingPanel extends JPanel {
    private JTextField searchField;
    private JList<String> taskList;
    private DefaultListModel<String> taskListModel;

    private List<String> allTasks; // This should be populated with all possible tasks

    public TaskEquipmentMappingPanel() {
        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Initialize tasks
        allTasks = new ArrayList<>(); // Populate with actual tasks
        allTasks.add("Task 1");
        allTasks.add("Task 2");
        allTasks.add("Task 3");
        // Add more tasks as needed

        // Search bar
        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(200, 30));
        searchField.setToolTipText("Search for a Slayer task");

        // Task list
        taskListModel = new DefaultListModel<>();
        taskList = new JList<>(taskListModel);
        taskList.setBackground(ColorScheme.DARK_GRAY_COLOR);
        taskList.setForeground(Color.WHITE);
        taskList.setBorder(BorderFactory.createLineBorder(ColorScheme.MEDIUM_GRAY_COLOR));

        // Populate task list with all tasks initially
        updateTaskList(allTasks);

        // Add components to panel
        add(searchField, BorderLayout.NORTH);
        add(new JScrollPane(taskList), BorderLayout.CENTER);

        // Add document listener for search field
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterTasks();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterTasks();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterTasks();
            }
        });
    }

    private void filterTasks() {
        String searchText = searchField.getText().toLowerCase();
        List<String> filteredTasks = new ArrayList<>();

        for (String task : allTasks) {
            if (task.toLowerCase().contains(searchText)) {
                filteredTasks.add(task);
            }
        }

        updateTaskList(filteredTasks);
    }

    private void updateTaskList(List<String> tasks) {
        taskListModel.clear();
        for (String task : tasks) {
            taskListModel.addElement(task);
        }
    }

    // You can add methods here to handle the mapping logic if needed
}
