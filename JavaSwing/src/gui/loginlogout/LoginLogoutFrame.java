package gui.loginlogout;

import java.awt.*;
import java.util.prefs.Preferences;
import javax.swing.*;
import gui.loginlogout.panel.LoginPanel;
import gui.loginlogout.panel.RegisterPanel;

public class LoginLogoutFrame extends JFrame {

    private JPanel contentPanel; 
    private JToggleButton themeToggle;

    public static final String LIGHT = "com.formdev.flatlaf.intellijthemes.FlatLightFlatIJTheme";
    public static final String DARK = "com.formdev.flatlaf.intellijthemes.FlatHiberbeeDarkIJTheme";

    public LoginLogoutFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setTitle("일정 관리");
        setLayout(new BorderLayout());

        
        addThemeToggleButton();

        
        contentPanel = new LoginPanel(this);
        getContentPane().add(contentPanel, BorderLayout.CENTER);
    }

    /** 상단에 테마 토글 버튼 패널 추가 (고정) */
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
        topPanel.setOpaque(false); 
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 10, 40));
        topPanel.add(themeToggle, BorderLayout.EAST);

        getContentPane().add(topPanel, BorderLayout.NORTH);
    }

    /**
     * 중앙 contentPanel을 교체(로그인/회원가입 등 화면 전환)
     * 새 패널로 바꾸면, 이전 패널은 제거 후 새 패널로 교체
     */
    public void setMainContentPanel(JPanel newPanel) {
        getContentPane().remove(contentPanel);
        contentPanel = newPanel;
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        getContentPane().revalidate();
        getContentPane().repaint();
    }

    /** main 진입점 */
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
}
