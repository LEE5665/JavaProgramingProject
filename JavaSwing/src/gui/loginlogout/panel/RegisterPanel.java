package gui.loginlogout.panel;

import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import api.DB;
import gui.loginlogout.LoginLogoutFrame;
import net.miginfocom.swing.MigLayout;

public class RegisterPanel extends JPanel {
	public RegisterPanel(LoginLogoutFrame parentFrame) {
		super(new MigLayout("align center center"));
		JPanel panel = new JPanel(new MigLayout("wrap 1", "[350!]"));
		panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

		JLabel title = new JLabel("사용자 등록");
		title.setFont(new Font("Dialog", Font.BOLD, 20));
		panel.add(title, "gapbottom 15, center");

		JLabel lblId = new JLabel("아이디");
		JTextField txtId = new JTextField();

		JLabel lblPw = new JLabel("비밀번호");
		JPasswordField txtPw = new JPasswordField();

		JButton btnRegister = new JButton("등록");
		Runnable doRegister = () -> {
			String username = txtId.getText().trim();
			String password = new String(txtPw.getPassword());
			if (username.isEmpty() || password.isEmpty()) {
				JOptionPane.showMessageDialog(this, "아이디와 비밀번호를 모두 입력하세요.", "경고", JOptionPane.WARNING_MESSAGE);
				return;
			}
			boolean success = DB.registerUser(username, password);
			if (success) {
				JOptionPane.showMessageDialog(this, "등록 성공! 로그인 페이지로 이동합니다.", "성공", JOptionPane.INFORMATION_MESSAGE);
				parentFrame.setContentPane(new LoginPanel(parentFrame));
				parentFrame.revalidate();
			} else {
				JOptionPane.showMessageDialog(this, "등록 실패 (아이디 중복 혹은 오류)", "오류", JOptionPane.ERROR_MESSAGE);
			}
		};
		btnRegister.addActionListener(e -> doRegister.run());

		txtId.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					txtPw.requestFocusInWindow();
				}
			}
		});
		txtPw.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					doRegister.run();
				}
			}
		});

		SwingUtilities.invokeLater(() -> parentFrame.getRootPane().setDefaultButton(btnRegister));

		JLabel backText = new JLabel(
				"<html><div style='text-align: center;'>돌아가시겠습니까? <a href='#'>로그인</a></div></html>");
		backText.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		backText.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {
				parentFrame.setContentPane(new LoginPanel(parentFrame));
				parentFrame.revalidate();
			}
		});

		panel.add(lblId);
		panel.add(txtId, "growx");
		panel.add(lblPw, "gaptop 10");
		panel.add(txtPw, "growx");
		panel.add(btnRegister, "gaptop 15, growx");
		panel.add(backText, "gaptop 10, center");

		add(panel);
	}
}
