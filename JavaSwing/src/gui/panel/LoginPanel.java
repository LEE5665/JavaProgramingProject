package gui.panel;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

import com.formdev.flatlaf.FlatClientProperties;
import net.miginfocom.swing.MigLayout;

import gui.TestFrame;

public class LoginPanel extends JPanel {

    public LoginPanel(TestFrame parentFrame) {
    	super(new MigLayout("align center center"));
    	
    	JPanel panel = new JPanel(new MigLayout("wrap 1", "[350!]"));
        panel.putClientProperty(FlatClientProperties.STYLE,
                "arc:20;" +
                "[light]background:darken(@background,3%);" +
                "[dark]background:lighten(@background,3%)");
        panel.setBackground(new Color(30, 30, 30));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel title = new JLabel("일정 관리");
        title.putClientProperty(FlatClientProperties.STYLE,
                "font:bold +10;" +
                "[light]foreground:lighten(@foreground,30%);" +
                "[dark]foreground:darken(@foreground,30%)");
        title.setFont(new Font("Dialog", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        panel.add(title, "gapbottom 5, center");

        JLabel subtitle = new JLabel("사용자 등록 후 사용 해 주세요");
        subtitle.setFont(new Font("Dialog", Font.PLAIN, 13));
        subtitle.setForeground(Color.LIGHT_GRAY);
        panel.add(subtitle, "gapbottom 15, center");

        JLabel lblUser = new JLabel("아이디");
        lblUser.setForeground(Color.WHITE);
        JTextField txtUser = new JTextField();
        txtUser.putClientProperty("JTextField.placeholderText", "아이디 입력");

        JLabel lblPass = new JLabel("비밀번호");
        lblPass.setForeground(Color.WHITE);
        JPasswordField txtPass = new JPasswordField();
        txtPass.putClientProperty("JTextField.placeholderText", "비밀번호 입력");

        JCheckBox rememberMe = new JCheckBox("로그인 상태 유지");

        JButton btnLogin = new JButton("로그인");

        JLabel signupText = new JLabel("<html><div style='text-align: center;'>아이디를 등록해야 합니다 <a href='#'>사용자 등록</a></div></html>");
        signupText.setForeground(Color.LIGHT_GRAY);
        signupText.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signupText.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                parentFrame.setContentPane(new RegisterPanel(parentFrame));
                parentFrame.revalidate();
            }
        });

        panel.add(lblUser, "gaptop 10");
        panel.add(txtUser, "growx");
        panel.add(lblPass, "gaptop 10");
        panel.add(txtPass, "growx");
        panel.add(rememberMe, "gaptop 5");
        panel.add(btnLogin, "gaptop 15, growx");
        panel.add(signupText, "gaptop 10, center");

        setBackground(new Color(20, 20, 20));
        
        add(panel);
    }
}