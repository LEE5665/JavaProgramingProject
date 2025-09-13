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

import api.model.UserDAO;
import gui.loginlogout.LoginLogoutFrame;
import gui.main.MainFrame;
import net.miginfocom.swing.MigLayout;

public class LoginPanel extends JPanel {
	private static final Preferences PREFS = Preferences.userRoot().node("MyAppPrefs"); // 로컬 저장소 사용

	public LoginPanel(LoginLogoutFrame parentFrame) {
		super(new MigLayout("align center center"));

		JPanel panel = new JPanel(new MigLayout("wrap 1", "[350!]"));
		panel.setBorder(BorderFactory.createEmptyBorder(0, 30, 30, 30));

		JLabel title = new JLabel("로그인");
		title.setFont(new Font("Dialog", Font.BOLD, 20));
		panel.add(title, "gapbottom 5, center");

		JLabel lblUser = new JLabel("아이디");
		JTextField txtUser = new JTextField();
		txtUser.putClientProperty("JTextField.placeholderText", "아이디 입력");
		boolean remembered = PREFS.getBoolean("rememberMe", false);
		if (remembered) {
			txtUser.setText(PREFS.get("savedUser", ""));
		}

		JLabel lblPass = new JLabel("비밀번호");
		JPasswordField txtPass = new JPasswordField();
		txtPass.putClientProperty("JTextField.placeholderText", "비밀번호 입력");

		JCheckBox rememberMe = new JCheckBox("아이디 저장");
		rememberMe.setSelected(remembered);

		JButton btnLogin = new JButton("로그인");
		Runnable doLogin = () -> { // 로그인 함수
			String username = txtUser.getText().trim();
			String password = new String(txtPass.getPassword());
			int userId = UserDAO.login(username, password);
			if (userId > 0) {
				PREFS.putBoolean("rememberMe", rememberMe.isSelected());
				if (rememberMe.isSelected()) {
					PREFS.put("savedUser", username);
				} else {
					PREFS.remove("savedUser");
				}
				MainFrame todosFrame = new MainFrame(userId);
				todosFrame.setVisible(true);
				SwingUtilities.getWindowAncestor(btnLogin).dispose(); // 로그인 창 최상위 객체 제거
			} else {
				JOptionPane.showMessageDialog(this, "로그인 실패! 아이디 또는 비밀번호를 확인하세요.");
			}
		};
		btnLogin.addActionListener(e -> doLogin.run());

		SwingUtilities.invokeLater(() -> parentFrame.getRootPane().setDefaultButton(btnLogin)); // 엔터 클릭 시 로그인 실행

		JLabel signupText = new JLabel(
				"<html><div style='text-align: center;'>아이디를 등록해야 합니다 <a href='#'>사용자 등록</a></div></html>");
		signupText.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // 커서 모양 변경
		signupText.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {
				parentFrame.setMainContentPanel(new RegisterPanel(parentFrame));
				parentFrame.revalidate();
			}
		});

		panel.add(lblUser);
		panel.add(txtUser, "growx");
		panel.add(lblPass);
		panel.add(txtPass, "growx");
		panel.add(rememberMe);
		panel.add(btnLogin, "gaptop 15, growx");
		panel.add(signupText, "gaptop 10, center");

		add(panel);
	}

	private void applyLookAndFeel(boolean dark) { // 테마 적용
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
