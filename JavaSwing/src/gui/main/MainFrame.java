package gui.main;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import gui.main.panel.MemoPanel;
import gui.main.panel.TodoPanel;
import net.miginfocom.swing.MigLayout;

public class MainFrame extends JFrame {
	private static final Preferences PREFS = Preferences.userRoot().node("MyAppPrefs");
	private final String LIGHT = "com.formdev.flatlaf.FlatLightLaf";
	private final String DARK = "com.formdev.flatlaf.FlatDarculaLaf";

	private int userId;
	private JToggleButton todoButton, memoButton, themeToggle;
	private ButtonGroup typeGroup;
	private JPanel contentPanel;
	private MemoPanel memoPanel;
	private TodoPanel todoPanel;

	public MainFrame(int userId) {
		this.userId = userId;
		applySavedLookAndFeel();

		setTitle("일정 관리");
		setSize(1200, 800);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);

		todoButton = createTypeToggle("To-Do List");
		memoButton = createTypeToggle("메모장");
		typeGroup = new ButtonGroup();
		typeGroup.add(todoButton);
		typeGroup.add(memoButton);
		memoButton.setSelected(true);

		todoButton.addActionListener(e -> switchContent("todo"));
		memoButton.addActionListener(e -> switchContent("memo"));

		themeToggle = new JToggleButton();
		themeToggle.setFocusPainted(false);
		themeToggle.setFont(new Font("Dialog", Font.PLAIN, 16));
		themeToggle.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		themeToggle.setPreferredSize(new Dimension(40, 30));
		themeToggle.setMargin(new Insets(0, 0, 0, 0));
		themeToggle.setHorizontalTextPosition(SwingConstants.CENTER);
		themeToggle.setVerticalTextPosition(SwingConstants.CENTER);

		boolean dark = PREFS.getBoolean("darkMode", false);
		themeToggle.setSelected(dark);
		themeToggle.setText(dark ? "🌜" : "🌞");
		themeToggle.setToolTipText(dark ? "다크 모드" : "라이트 모드");
		Color fg = UIManager.getColor("ToggleButton.foreground");
		Color selFg = UIManager.getColor("ToggleButton.select");
		Color bg = UIManager.getColor("ToggleButton.background");
		Color selBg = UIManager.getColor("ToggleButton.focus");
		themeToggle.setForeground(dark ? selFg : fg);
		themeToggle.setBackground(dark ? selBg : bg);
		themeToggle.addItemListener(e -> {
			boolean sel = e.getStateChange() == ItemEvent.SELECTED;
			PREFS.putBoolean("darkMode", sel);
			try {
				UIManager.setLookAndFeel(sel ? DARK : LIGHT);
			} catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException
					| IllegalAccessException ex) {
				ex.printStackTrace();
			}
			themeToggle.setText(sel ? "🌜" : "🌞");
			themeToggle.setToolTipText(sel ? "다크 모드" : "라이트 모드");
			Color fg2 = UIManager.getColor("ToggleButton.foreground");
			Color selFg2 = UIManager.getColor("ToggleButton.select");
			Color bg2 = UIManager.getColor("ToggleButton.background");
			Color selBg2 = UIManager.getColor("ToggleButton.focus");
			themeToggle.setForeground(sel ? selFg2 : fg2);
			themeToggle.setBackground(sel ? selBg2 : bg2);
			SwingUtilities.updateComponentTreeUI(this);
			memoPanel.updateUI();
			todoPanel.updateUI();
		});

		JPanel typePanel = new JPanel(new MigLayout("insets 0, wrap 3", "[50%][50%][pref!]"));
		typePanel.add(todoButton, "growx");
		typePanel.add(memoButton, "growx");
		typePanel.add(themeToggle, "align right");

		JSeparator separator = new JSeparator();
		separator.setForeground(UIManager.getColor("Separator.foreground"));

		JPanel header = new JPanel(new BorderLayout());
		header.add(typePanel, BorderLayout.CENTER);
		header.add(separator, BorderLayout.SOUTH);

		contentPanel = new JPanel(new CardLayout());
		memoPanel = new MemoPanel(userId);
		todoPanel = new TodoPanel(userId);
		contentPanel.add(memoPanel, "memo");
		contentPanel.add(todoPanel, "todo");

		add(header, BorderLayout.NORTH);
		add(contentPanel, BorderLayout.CENTER);

		switchContent("memo");
	}

	private void applySavedLookAndFeel() {
		boolean dark = PREFS.getBoolean("darkMode", false);
		try {
			UIManager.setLookAndFeel(dark ? DARK : LIGHT);
		} catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException
				| IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	private JToggleButton createTypeToggle(String text) {
		JToggleButton btn = new JToggleButton("<html><div style='text-align:center;'>" + text + "</div></html>");
		btn.setFocusPainted(false);
		btn.setFont(new Font("Dialog", Font.BOLD, 16));
		btn.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		Color fgNormal = UIManager.getColor("ToggleButton.foreground");
		Color fgSelected = UIManager.getColor("ToggleButton.select");
		Color bgNormal = UIManager.getColor("ToggleButton.background");
		Color bgSelected = UIManager.getColor("ToggleButton.focus");
		boolean initiallySelected = btn.isSelected();
		btn.setForeground(initiallySelected ? fgSelected : fgNormal);
		btn.setBackground(initiallySelected ? bgSelected : bgNormal);
		btn.addItemListener(e -> {
			boolean sel = btn.isSelected();
			Color fg2 = UIManager.getColor("ToggleButton.foreground");
			Color selFg2 = UIManager.getColor("ToggleButton.select");
			Color bg2 = UIManager.getColor("ToggleButton.background");
			Color selBg2 = UIManager.getColor("ToggleButton.focus");
			btn.setForeground(sel ? selFg2 : fg2);
			btn.setBackground(sel ? selBg2 : bg2);
		});
		return btn;
	}

	private void switchContent(String name) {
		((CardLayout) contentPanel.getLayout()).show(contentPanel, name);
	}
}
