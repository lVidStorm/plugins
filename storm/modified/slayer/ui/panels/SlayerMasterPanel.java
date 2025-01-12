package net.runelite.client.plugins.microbot.storm.modified.slayer.ui.panels;

import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SlayerMasterPanel extends JPanel {

    private JCheckBox turaelSkippingCheckBox;

    public SlayerMasterPanel() {
        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        // Main Content Panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridLayout(2, 2, 10, 10)); // 2 rows and 2 columns
        contentPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(contentPanel, BorderLayout.CENTER);

        // Create buttons for Slayer Masters
        JButton turaelButton = createActionButton("turael.png", "Turael");
        JButton steveButton = createActionButton("steve.png", "Steve");
        JButton nieveButton = createActionButton("nieve.png", "Nieve");
        JButton duradelButton = createActionButton("duradel.png", "Duradel");

        // Add buttons to the content panel
        contentPanel.add(turaelButton);
        contentPanel.add(steveButton);
        contentPanel.add(nieveButton);
        contentPanel.add(duradelButton);

        // Checkbox for "Use Turael Skipping"
        turaelSkippingCheckBox = new JCheckBox("Use Turael Skipping");
        turaelSkippingCheckBox.setToolTipText("Does Turael Tasks to Boost Points");
        turaelSkippingCheckBox.setBackground(ColorScheme.DARK_GRAY_COLOR);
        turaelSkippingCheckBox.setForeground(Color.WHITE);
        turaelSkippingCheckBox.setBorder(new EmptyBorder(5, 0, 5, 0));

        // Panel for checkbox
        JPanel checkboxPanel = new JPanel();
        checkboxPanel.setLayout(new FlowLayout(FlowLayout.CENTER)); // Center the checkbox
        checkboxPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        checkboxPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        checkboxPanel.add(turaelSkippingCheckBox);

        // Title Label
        JLabel titleLabel = new JLabel("More Masters coming soon...", SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(new EmptyBorder(10, 0, 10, 0));

        // Add content panel, checkbox panel, and title label to the main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(checkboxPanel, BorderLayout.SOUTH);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JButton createActionButton(String iconPath, final String masterName) {
        ImageIcon icon = loadIcon(iconPath);
        ImageIcon resizedIcon = resizeIcon(icon, 48, 48); // Resize to 48x48
        JButton button = new JButton(resizedIcon);
        button.setToolTipText(masterName);
        button.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        button.setUI(new BasicButtonUI());

        // Add ActionListener to show dialog
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(
                        SlayerMasterPanel.this,
                        "Change Selected Slayer Master to " + masterName + "?",
                        "Select Slayer Master",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        });

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
}
