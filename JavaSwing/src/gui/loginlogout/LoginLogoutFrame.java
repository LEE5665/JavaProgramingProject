package gui.loginlogout;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.intellijthemes.FlatArcDarkIJTheme;

import gui.loginlogout.panel.LoginPanel;
import gui.loginlogout.panel.RegisterPanel;
import net.miginfocom.swing.MigLayout;

public class LoginLogoutFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(new FlatArcDarkIJTheme());
					LoginLogoutFrame frame = new LoginLogoutFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
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
