package gui.loginlogout.panel;

import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import api.DB;
import gui.loginlogout.LoginLogoutFrame;
import gui.main.MainFrame;
import net.miginfocom.swing.MigLayout;

public class LoginPanel extends JPanel {
	private static final Preferences PREFS = Preferences.userRoot().node("MyAppPrefs");

	public LoginPanel(LoginLogoutFrame parentFrame) {
		super(new MigLayout("align center center"));

		JPanel panel = new JPanel(new MigLayout("wrap 1", "[350!]"));
		panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

		JLabel title = new JLabel("일정 관리");
		title.setFont(new Font("Dialog", Font.BOLD, 20));
		panel.add(title, "gapbottom 5, center");

		JLabel lblUser = new JLabel("아이디");
		JTextField txtUser = new JTextField();
		txtUser.putClientProperty("JTextField.placeholderText", "아이디 입력");
		// 로그인 상태 유지된 값이 있으면 초기 아이디로 세팅
		boolean remembered = PREFS.getBoolean("rememberMe", false);
		if (remembered) {
			txtUser.setText(PREFS.get("savedUser", ""));
		}

		JLabel lblPass = new JLabel("비밀번호");
		JPasswordField txtPass = new JPasswordField();
		txtPass.putClientProperty("JTextField.placeholderText", "비밀번호 입력");

		JCheckBox rememberMe = new JCheckBox("아이디 저장");
		// 초기 체크박스 상태
		rememberMe.setSelected(remembered);

		JCheckBox lafToggle = new JCheckBox("다크 테마");
		boolean dark = PREFS.getBoolean("darkMode", false);
		lafToggle.setSelected(dark);
		applyLookAndFeel(dark);
		SwingUtilities.updateComponentTreeUI(parentFrame);

		lafToggle.addActionListener(e -> {
			boolean sel = lafToggle.isSelected();
			PREFS.putBoolean("darkMode", sel);
			applyLookAndFeel(sel);
			SwingUtilities.updateComponentTreeUI(parentFrame);
		});

		JButton btnLogin = new JButton("로그인");
		Runnable doLogin = () -> {
			String username = txtUser.getText().trim();
			String password = new String(txtPass.getPassword());
			int userId = DB.login(username, password);
			if (userId > 0) {
				// 로그인 유지 설정 저장
				PREFS.putBoolean("rememberMe", rememberMe.isSelected());
				if (rememberMe.isSelected()) {
					PREFS.put("savedUser", username);
				} else {
					PREFS.remove("savedUser");
				}
				MainFrame todosFrame = new MainFrame(userId);
				todosFrame.setVisible(true);
				SwingUtilities.getWindowAncestor(btnLogin).dispose();
			} else {
				JOptionPane.showMessageDialog(this, "로그인 실패! 아이디 또는 비밀번호를 확인하세요.");
			}
		};
		btnLogin.addActionListener(e -> doLogin.run());

		txtUser.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					txtPass.requestFocusInWindow();
			}
		});
		txtPass.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					doLogin.run();
			}
		});

		SwingUtilities.invokeLater(() -> parentFrame.getRootPane().setDefaultButton(btnLogin));

		JLabel signupText = new JLabel(
				"<html><div style='text-align: center;'>아이디를 등록해야 합니다 <a href='#'>사용자 등록</a></div></html>");
		signupText.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		signupText.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {
				parentFrame.setContentPane(new RegisterPanel(parentFrame));
				parentFrame.revalidate();
			}
		});

		panel.add(lblUser);
		panel.add(txtUser, "growx");
		panel.add(lblPass);
		panel.add(txtPass, "growx");
		panel.add(rememberMe);
		panel.add(lafToggle, "wrap");
		panel.add(btnLogin, "gaptop 15, growx");
		panel.add(signupText, "gaptop 10, center");

		add(panel);
	}

	private void applyLookAndFeel(boolean dark) {
		try {
			if (dark) {
				UIManager.setLookAndFeel(LoginLogoutFrame.DARK);
			} else {
				UIManager.setLookAndFeel(LoginLogoutFrame.LIGHT);
			}
		} catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException
				| IllegalAccessException ex) {
			ex.printStackTrace();
		}
	}
}
