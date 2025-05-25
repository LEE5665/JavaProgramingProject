package gui.loginlogout;

import java.awt.Color;
import java.util.prefs.Preferences;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import gui.loginlogout.panel.LoginPanel;

public class LoginLogoutFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	public static final String LIGHT = "com.formdev.flatlaf.FlatIntelliJLaf";
	public static final String DARK = "com.formdev.flatlaf.FlatDarkLaf";

	public static void main(String[] args) {
		boolean dark = Preferences.userRoot().node("MyAppPrefs").getBoolean("darkMode", false);
		try {
			if (dark) {
				UIManager.setLookAndFeel(DARK);
			} else {
				UIManager.setLookAndFeel(LIGHT);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		SwingUtilities.invokeLater(() -> {
			LoginLogoutFrame frame = new LoginLogoutFrame();
			frame.setVisible(true);
		});
	}

	/**
	 * Create the frame.
	 */
	public LoginLogoutFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1000, 600);
		setLocationRelativeTo(null);
		getContentPane().setBackground(new Color(20, 20, 20));
		setTitle("일정 관리");
		contentPane = new LoginPanel(this);
		setContentPane(contentPane);
	}

}
