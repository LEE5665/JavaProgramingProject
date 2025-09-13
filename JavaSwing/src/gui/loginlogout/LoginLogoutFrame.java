package gui.loginlogout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;

import com.formdev.flatlaf.intellijthemes.FlatHiberbeeDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatLightFlatIJTheme;

import gui.loginlogout.panel.LoginPanel;
import jiconfont.icons.font_awesome.FontAwesome;
import jiconfont.swing.IconFontSwing;

public class LoginLogoutFrame extends JFrame {

	private JPanel contentPanel;
	private JToggleButton themeToggle;

	public static final String LIGHT = FlatLightFlatIJTheme.class.getName();
	public static final String DARK = FlatHiberbeeDarkIJTheme.class.getName();

	public LoginLogoutFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(700, 450);
		setLocationRelativeTo(null);
		setTitle("일정 관리");
		setLayout(new BorderLayout());

		addThemeToggleButton();

		contentPanel = new LoginPanel(this);
		getContentPane().add(contentPanel, BorderLayout.CENTER);
	}
	
	private void addThemeToggleButton() { // 상단 토글 버튼
		Preferences prefs = Preferences.userRoot().node("MyAppPrefs");
		boolean dark = prefs.getBoolean("darkMode", false);

		int size = 20;
		themeToggle = new JToggleButton();
		themeToggle.setIcon(IconFontSwing.buildIcon(FontAwesome.MOON_O, size, dark ? Color.WHITE : Color.BLACK));
		themeToggle.setSelectedIcon(IconFontSwing.buildIcon(FontAwesome.SUN_O, size, dark ? Color.WHITE : Color.BLACK));
		themeToggle.setSelected(dark);
		themeToggle.setPreferredSize(new Dimension(50, 30));
		themeToggle.setFocusPainted(false);

		// 경계선 스타일 설정
		Color borderColor = dark ? new Color(80, 80, 80) : new Color(200, 200, 200);
		Border line = BorderFactory.createLineBorder(borderColor, 1, true);
		Border shadow = BorderFactory.createMatteBorder(0, 0, 2, 2, new Color(0, 0, 0, 30));
		themeToggle.setBorder(BorderFactory.createCompoundBorder(line, shadow));

		themeToggle.addActionListener(e -> {
			boolean isDark = themeToggle.isSelected();
			prefs.putBoolean("darkMode", isDark);
			try {
				UIManager.setLookAndFeel(isDark ? DARK : LIGHT);
				SwingUtilities.updateComponentTreeUI(this); // 테마 갱신
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			themeToggle.setIcon(IconFontSwing.buildIcon(FontAwesome.MOON_O, size, isDark ? Color.WHITE : Color.BLACK));
			themeToggle.setSelectedIcon(
					IconFontSwing.buildIcon(FontAwesome.SUN_O, size, isDark ? Color.WHITE : Color.BLACK));

			// 테두리 색 다시 설정
			Color newBorderColor = isDark ? new Color(80, 80, 80) : new Color(200, 200, 200);
			Border newLine = BorderFactory.createLineBorder(newBorderColor, 1, true);
			themeToggle.setBorder(BorderFactory.createCompoundBorder(newLine, shadow));
		});

		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.setOpaque(false);
		topPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 10, 40));
		topPanel.add(themeToggle, BorderLayout.EAST);
		getContentPane().add(topPanel, BorderLayout.NORTH);
	}

	// 패널 교체
	public void setMainContentPanel(JPanel newPanel) {
		getContentPane().remove(contentPanel);
		contentPanel = newPanel;
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		getContentPane().revalidate();
		getContentPane().repaint();
	}

	public static void main(String[] args) {
		Preferences prefs = Preferences.userRoot().node("MyAppPrefs");
		boolean dark = prefs.getBoolean("darkMode", false);
		try {
			UIManager.setLookAndFeel(dark ? DARK : LIGHT);
			IconFontSwing.register(FontAwesome.getIconFont());
		} catch (Exception e) {
			e.printStackTrace();
		}

		SwingUtilities.invokeLater(() -> {
			LoginLogoutFrame frame = new LoginLogoutFrame();
			frame.setVisible(true);
		});
	}
}
