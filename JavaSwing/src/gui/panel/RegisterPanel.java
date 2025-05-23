package gui.panel;

import javax.swing.*;

import api.DB;
import gui.TestFrame;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import net.miginfocom.swing.MigLayout;

public class RegisterPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	public RegisterPanel(TestFrame parentFrame) {
		super(new MigLayout("align center center")); // ✅ wrapper 역할
		setBackground(new Color(20, 20, 20));

		JPanel panel = new JPanel(new MigLayout("wrap 1", "[350!]"));
		panel.setBackground(new Color(30, 30, 30));
		panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

		JLabel title = new JLabel("사용자 등록");
		title.setFont(new Font("Dialog", Font.BOLD, 20));
		title.setForeground(Color.WHITE);
		panel.add(title, "gapbottom 15, center");

		JLabel lblId = new JLabel("아이디");
		lblId.setForeground(Color.WHITE);
		JTextField txtId = new JTextField();

		JLabel lblPw = new JLabel("비밀번호");
		lblPw.setForeground(Color.WHITE);
		JPasswordField txtPw = new JPasswordField();

		JButton btnRegister = new JButton("등록");
		btnRegister.addActionListener((ActionEvent e) -> {
			String username = txtId.getText();
			String password = new String(txtPw.getPassword());

			String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
			boolean success = DB.executeUpdate(sql, new Object[]{username, password}) > 0;

			if (success) {
				JOptionPane.showMessageDialog(this, "등록 성공!", "성공", JOptionPane.INFORMATION_MESSAGE);
				parentFrame.setContentPane(new LoginPanel(parentFrame));
				parentFrame.revalidate();
			} else {
				JOptionPane.showMessageDialog(this, "등록 실패 (아이디 중복)", "오류", JOptionPane.ERROR_MESSAGE);
			}
		});
		
		JLabel backText = new JLabel("<html><div style='text-align: center;'>돌아가시겠습니까? <a href='#'>로그인</a></div></html>");
		backText.setForeground(Color.LIGHT_GRAY);
		backText.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		backText.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
            	parentFrame.setContentPane(new LoginPanel(parentFrame)); // 로그인 화면으로 전환
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
