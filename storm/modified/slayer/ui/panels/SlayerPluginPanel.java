package net.runelite.client.plugins.microbot.storm.modified.slayer.ui.panels;

import net.runelite.client.plugins.microbot.storm.modified.slayer.ui.SessionData;
import net.runelite.client.plugins.microbot.storm.modified.slayer.ui.SessionManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.LinkBrowser;
import net.runelite.client.util.SwingUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Duration;
import java.util.List;

public class SlayerPluginPanel extends PluginPanel {
    private CurrentSessionPanel currentSessionPanel;
    private SessionHistoryPanel sessionHistoryPanel;
    private TaskEquipmentMappingPanel taskEquipmentMappingPanel;
    private UtilityPanel utilityPanel; // Declare UtilityPanel
    private LootPanel lootPanel; // Declare LootPanel
    private SlayerMasterPanel slayerMasterPanel; // Declare SlayerMasterPanel
    private JPanel dynamicPanel; // New dynamic panel area
    private JButton startScriptButton;
    private boolean isScriptRunning = false; // To track the script state
    private long sessionStartTime;

    public SlayerPluginPanel() {
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new BorderLayout());

        // Create and initialize panels
        currentSessionPanel = new CurrentSessionPanel();
        sessionHistoryPanel = new SessionHistoryPanel();
        taskEquipmentMappingPanel = new TaskEquipmentMappingPanel();
        utilityPanel = new UtilityPanel(); // Initialize UtilityPanel
        lootPanel = new LootPanel(); // Initialize LootPanel
        slayerMasterPanel = new SlayerMasterPanel(); // Initialize SlayerMasterPanel

        // Load session history
        loadSessionHistory();

        // Dynamic panel setup using CardLayout
        dynamicPanel = new JPanel(new CardLayout());
        dynamicPanel.add(createTitledPanel(currentSessionPanel, "Current Session"), "Session");
        dynamicPanel.add(createTitledPanel(sessionHistoryPanel, "Session History"), "History");
        dynamicPanel.add(createTitledPanel(taskEquipmentMappingPanel, "Task Equipment Mapping"), "TaskEquipment");
        dynamicPanel.add(createTitledPanel(utilityPanel, "Utility Settings"), "Utility"); // Add UtilityPanel
        dynamicPanel.add(createTitledPanel(lootPanel, "Loot Settings"), "Loot"); // Add LootPanel
        dynamicPanel.add(createTitledPanel(slayerMasterPanel, "Slayer Masters"), "Masters"); // Add SlayerMasterPanel

        // Main Panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS)); // Use vertical box layout
        mainPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

        // Options panel containing the buttons
        JPanel viewControls = new JPanel(new GridLayout(1, 3, 10, 0)); // Adjust grid columns as needed
        viewControls.setBackground(ColorScheme.DARK_GRAY_COLOR);
        viewControls.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Create buttons
        JButton[] buttons = {
                createIconButton("discord.png", "Get help with the Plugin or make suggestions on Discord", "https://discord.com/channels/1087718903985221642/1241330685680287746"),
                createIconButton("github.png", "Report issues or contribute on GitHub", "https://github.com/bankjs"),
                createIconButton("pp.png", "Support developer via Donation", "https://www.paypal.com/donate/?hosted_button_id=QGDBBQZRVNBRA"),
        };

        // Add buttons to viewControls panel
        for (JButton button : buttons) {
            viewControls.add(button);
        }

        // Add viewControls panel to the main panel
        mainPanel.add(viewControls);

        // Icon grid panel
        JPanel iconGridPanel = new JPanel(new GridLayout(1, 6, 5, 5)); // 1 row and 6 columns for buttons
        iconGridPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        iconGridPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Create grid buttons
        JButton[] gridButtons = {
                createActionButton("equipment.png", "Manage task and equipment mapping", e -> showPanel("TaskEquipment")),
                createActionButton("utility.png", "Configure utility settings", e -> showPanel("Utility")),
                createActionButton("loot.png", "Adjust loot settings", e -> showPanel("Loot")), // Add action for Loot Panel
                createActionButton("masters.png", "Configure Slayer Master settings", e -> showPanel("Masters")),
                createActionButton("history.png", "View Session History", e -> showPanel("History"))
        };

        // Add grid buttons to iconGridPanel
        for (JButton button : gridButtons) {
            iconGridPanel.add(button);
        }

        // Add icon grid panel to the main panel
        mainPanel.add(iconGridPanel);

        // Add dynamicPanel to the main panel
        mainPanel.add(dynamicPanel);

        // Add the mainPanel to the center of the PluginPanel
        add(mainPanel, BorderLayout.CENTER);

        // Add startScriptButton to the bottom of the PluginPanel
        startScriptButton = new JButton("Start Script");
        startScriptButton.addActionListener(e -> toggleScript());
        startScriptButton.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        startScriptButton.setUI(new BasicButtonUI());
        startScriptButton.addMouseListener(createButtonHoverListener(startScriptButton));
        add(startScriptButton, BorderLayout.SOUTH);
    }

    private void toggleScript() {
        if (isScriptRunning) {
            stopScript();
        } else {
            startScript();
        }
    }

    private void startScript() {
        startScriptButton.setText("Stop Script");
        isScriptRunning = true;
        showPanel("Session");

        sessionStartTime = System.currentTimeMillis(); // Current time in milliseconds
        currentSessionPanel.addSessionCard(sessionStartTime, 0, 12230, 1500); // Placeholder values
    }

    private void stopScript() {
        startScriptButton.setText("Start Script");
        isScriptRunning = false;

        // Stop timers for all session cards
        currentSessionPanel.stopAllTimers(); // Ensure this method is implemented to stop timers

        // Calculate duration
        long endTime = System.currentTimeMillis();
        long duration = endTime - sessionStartTime; // Duration in milliseconds

        // Example values
        long xpGained = 12230; // Example XP Gained
        long lootGained = 1500; // Example Loot Gained

        // Add current session to session history
        sessionHistoryPanel.addSession(sessionStartTime, duration, xpGained, lootGained);

        // Create SessionData object
        SessionData sessionData = new SessionData(
                Duration.ofMillis(duration),
                5, // Example number of tasks completed
                (int) xpGained,
                (int) lootGained
        );

        // Save the session data
        saveSessionData(sessionData);

        // Clear the current session panel
        currentSessionPanel.clearSession(); // Add this method to clear the session data
    }

    private void saveSessionData(SessionData sessionData) {
        List<SessionData> sessions = SessionManager.loadSessions(); // Load existing sessions
        sessions.add(sessionData); // Add the new session
        SessionManager.saveSessions(sessions); // Save all sessions
    }

    private void loadSessionHistory() {
        try {
            List<SessionData> sessions = SessionManager.loadSessions(); // Ensure this method returns the expected data

            if (sessions != null) {
                for (SessionData session : sessions) {
                    if (session != null) {
                        long duration = session.getDuration().toMillis();
                        long xpGained = session.getXpGained();
                        long lootGained = session.getLootValue();

                        long startTime = System.currentTimeMillis() - duration;

                        System.out.println("Loading session: " + startTime + ", " + duration + ", " + xpGained + ", " + lootGained);

                        sessionHistoryPanel.addSession(startTime, duration, xpGained, lootGained);
                        System.out.println("Added session: " + startTime + ", " + duration + ", " + xpGained + ", " + lootGained);
                    } else {
                        System.err.println("Encountered null SessionData object.");
                    }
                }
                sessionHistoryPanel.revalidate();
                sessionHistoryPanel.repaint();
            } else {
                System.err.println("SessionManager.loadSessions() returned null.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void showPanel(String panelName) {
        CardLayout cl = (CardLayout) (dynamicPanel.getLayout());
        cl.show(dynamicPanel, panelName);
    }

    private JPanel createTitledPanel(JPanel panel, String title) {
        JPanel titledPanel = new JPanel(new BorderLayout());
        titledPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

        JLabel titleLabel = new JLabel("~ " + title + " ~", SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(new EmptyBorder(5, 0, 5, 0));

        titledPanel.add(titleLabel, BorderLayout.NORTH);
        titledPanel.add(panel, BorderLayout.CENTER);

        return titledPanel;
    }

    private JButton createIconButton(String iconPath, String tooltip, String url) {
        ImageIcon icon = loadIcon(iconPath);
        ImageIcon resizedIcon = resizeIcon(icon, 16, 16); // Resize to 16x16
        JButton button = new JButton(resizedIcon);
        SwingUtil.removeButtonDecorations(button);
        button.setToolTipText(tooltip);
        button.setBackground(ColorScheme.DARK_GRAY_COLOR);
        button.setUI(new BasicButtonUI());
        button.addMouseListener(createButtonHoverListener(button));
        button.addActionListener(e -> LinkBrowser.browse(url));
        return button;
    }

    private JButton createActionButton(String iconPath, String tooltip, ActionListener actionListener) {
        ImageIcon icon = loadIcon(iconPath);
        ImageIcon resizedIcon = resizeIcon(icon, 32, 32); // Resize to 32x32 or any other size you prefer
        JButton button = new JButton(resizedIcon);
        button.setToolTipText(tooltip);
        button.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        button.setUI(new BasicButtonUI());
        button.addMouseListener(createButtonHoverListener(button));
        if (actionListener != null) {
            button.addActionListener(actionListener);
        }
        return button;
    }

    private ImageIcon loadIcon(String path) {
        java.net.URL imgURL = getClass().getResource("/net/runelite/client/microbot/storm/modified/slayer/icons/" + path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return new ImageIcon(); // Return an empty icon or a placeholder if the icon is not found
        }
    }

    private ImageIcon resizeIcon(ImageIcon icon, int width, int height) {
        Image img = icon.getImage();
        Image resizedImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImg);
    }

    private MouseAdapter createButtonHoverListener(JButton button) {
        return new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(ColorScheme.DARK_GRAY_HOVER_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(ColorScheme.DARKER_GRAY_COLOR);
            }
        };
    }
}
