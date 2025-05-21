package gui;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import javax.swing.*;
import javax.swing.UIManager;
import com.formdev.flatlaf.FlatClientProperties;

import com.formdev.flatlaf.intellijthemes.FlatArcDarkIJTheme;
import net.miginfocom.swing.MigLayout;

public class LoginLogoutMain {

	private JFrame frame;

	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				UIManager.setLookAndFeel(new FlatArcDarkIJTheme());
				LoginLogoutMain window = new LoginLogoutMain();
				window.frame.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	public LoginLogoutMain() {
		initialize();
	}

	private void initialize() {
		this.frame = new JFrame("FlatLaf Login"); // ✅ 필드 초기화
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.setSize(1000,600);
	    frame.setLocationRelativeTo(null);
	    frame.getContentPane().setBackground(new Color(20, 20, 20));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new MigLayout("wrap 1", "[350!]"));
        panel.putClientProperty(FlatClientProperties.STYLE,
            "arc:20;" +
            "[light]background:darken(@background,3%);" +
            "[dark]background:lighten(@background,3%)");
        panel.setBackground(new Color(30, 30, 30));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel title = new JLabel("Welcome back!");
        title.putClientProperty(FlatClientProperties.STYLE,
            "font:bold +10;" +
            "[light]foreground:lighten(@foreground,30%);" +
            "[dark]foreground:darken(@foreground,30%)");
        title.setFont(new Font("Dialog", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        panel.add(title, "gapbottom 5, center");

        JLabel subtitle = new JLabel("Please sign in to access your account");
        subtitle.setFont(new Font("Dialog", Font.PLAIN, 13));
        subtitle.setForeground(Color.LIGHT_GRAY);
        panel.add(subtitle, "gapbottom 15, center");

        JLabel lblUser = new JLabel("Username");
        lblUser.setForeground(Color.WHITE);
        JTextField txtUser = new JTextField();
        txtUser.putClientProperty("JTextField.placeholderText", "Enter your username or email");

        JLabel lblPass = new JLabel("Password");
        lblPass.setForeground(Color.WHITE);
        JPasswordField txtPass = new JPasswordField();
        txtPass.putClientProperty("JTextField.placeholderText", "Enter your password");

        JCheckBox rememberMe = new JCheckBox("Remember me");

        JButton btnLogin = new JButton("Login");

        JLabel signupText = new JLabel("<html><div style='text-align: center;'>Don't have an account? <a href='#'>Sign up</a></div></html>");
        signupText.setForeground(Color.LIGHT_GRAY);

        // 컴포넌트 추가
        panel.add(lblUser, "gaptop 10");
        panel.add(txtUser, "growx");
        panel.add(lblPass, "gaptop 10");
        panel.add(txtPass, "growx");
        panel.add(rememberMe, "gaptop 5");
        panel.add(btnLogin, "gaptop 15, growx");
        panel.add(signupText, "gaptop 10, center");

        // 전체를 감싸는 중앙 패널
        JPanel wrapper = new JPanel(new MigLayout("align center center"));
        wrapper.setBackground(new Color(20, 20, 20));
        wrapper.add(panel);

        frame.setContentPane(wrapper);
        frame.setVisible(true);
	}
}
