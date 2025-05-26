package gui.loginlogout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.prefs.Preferences;
import javax.swing.*;
import gui.loginlogout.panel.LoginPanel;

public class LoginLogoutFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    public static final String LIGHT = "com.formdev.flatlaf.intellijthemes.FlatLightFlatIJTheme";
    public static final String DARK = "com.formdev.flatlaf.intellijthemes.FlatHiberbeeDarkIJTheme";
    private JToggleButton themeToggle;

    public static void main(String[] args) {
        boolean dark = Preferences.userRoot().node("MyAppPrefs").getBoolean("darkMode", false);
        try {
            UIManager.setLookAndFeel(dark ? DARK : LIGHT);
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            LoginLogoutFrame frame = new LoginLogoutFrame();
            frame.setVisible(true);
        });
    }

    public LoginLogoutFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(20, 20, 20));
        setTitle("일정 관리");

        contentPane = new LoginPanel(this);
        setContentPane(contentPane);

        addThemeToggleButton();
    }

    private void addThemeToggleButton() {
        boolean dark = Preferences.userRoot().node("MyAppPrefs").getBoolean("darkMode", false);
        themeToggle = new JToggleButton(dark ? "🌜" : "🌞");
        themeToggle.setSelected(dark);
        themeToggle.setPreferredSize(new Dimension(50, 30));
        themeToggle.setFocusPainted(false);

        themeToggle.addActionListener(e -> {
            boolean isDark = themeToggle.isSelected();
            Preferences.userRoot().node("MyAppPrefs").putBoolean("darkMode", isDark);
            try {
                UIManager.setLookAndFeel(isDark ? DARK : LIGHT);
                SwingUtilities.updateComponentTreeUI(this);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            themeToggle.setText(isDark ? "🌜" : "🌞");
        });

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(themeToggle, BorderLayout.EAST);
        getContentPane().add(topPanel, BorderLayout.NORTH);
    }
}
