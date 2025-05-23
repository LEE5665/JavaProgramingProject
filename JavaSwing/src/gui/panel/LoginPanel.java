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

        JLabel title = new JLabel("���� ����");
        title.putClientProperty(FlatClientProperties.STYLE,
                "font:bold +10;" +
                "[light]foreground:lighten(@foreground,30%);" +
                "[dark]foreground:darken(@foreground,30%)");
        title.setFont(new Font("Dialog", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        panel.add(title, "gapbottom 5, center");

        JLabel subtitle = new JLabel("����� ��� �� ��� �� �ּ���");
        subtitle.setFont(new Font("Dialog", Font.PLAIN, 13));
        subtitle.setForeground(Color.LIGHT_GRAY);
        panel.add(subtitle, "gapbottom 15, center");

        JLabel lblUser = new JLabel("���̵�");
        lblUser.setForeground(Color.WHITE);
        JTextField txtUser = new JTextField();
        txtUser.putClientProperty("JTextField.placeholderText", "���̵� �Է�");

        JLabel lblPass = new JLabel("��й�ȣ");
        lblPass.setForeground(Color.WHITE);
        JPasswordField txtPass = new JPasswordField();
        txtPass.putClientProperty("JTextField.placeholderText", "��й�ȣ �Է�");

        JCheckBox rememberMe = new JCheckBox("�α��� ���� ����");

        JButton btnLogin = new JButton("�α���");

        JLabel signupText = new JLabel("<html><div style='text-align: center;'>���̵� ����ؾ� �մϴ� <a href='#'>����� ���</a></div></html>");
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