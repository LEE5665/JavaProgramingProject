package gui.main;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.util.prefs.Preferences;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.FontUIResource;

import gui.main.panel.MemoPanel;
import gui.main.panel.TodoPanel;

public class MainFrame extends JFrame {
	private static final Preferences PREFS = Preferences.userRoot().node("MyAppPrefs");
	private static final String LIGHT = "com.formdev.flatlaf.FlatLightLaf";
	private static final String DARK = "com.formdev.flatlaf.intellijthemes.FlatHiberbeeDarkIJTheme";
	private static final Font GLOBAL_FONT = new Font("Nanum Gothic", Font.PLAIN, 14);

	private int userId;
	private JToggleButton todoButton, memoButton;
	private JPanel contentPanel;
	private MemoPanel memoPanel;
	private TodoPanel todoPanel;

	public MainFrame(int userId) {
		this.userId = userId;
		applySavedLookAndFeel();
		applyGlobalFont(GLOBAL_FONT);

		setTitle("일정 관리");
		setSize(1200, 800);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);

		JPanel headerBar = new JPanel(new BorderLayout());
		headerBar.setBorder(new EmptyBorder(0, 0, 0, 0));
		headerBar.setBackground(UIManager.getColor("Panel.background"));

		JLabel appTitle = new JLabel("일정 관리");
		appTitle.setFont(GLOBAL_FONT.deriveFont(Font.BOLD, 22f));
		appTitle.setBorder(new EmptyBorder(0, 24, 0, 0));
		appTitle.setForeground(UIManager.getColor("Label.foreground"));

		JToggleButton themeToggle = new JToggleButton();
		themeToggle.setFocusPainted(false);
		themeToggle.setBorderPainted(false);
		themeToggle.setContentAreaFilled(false);
		themeToggle.setFont(GLOBAL_FONT.deriveFont(22f));
		themeToggle.setPreferredSize(new Dimension(44, 44));
		boolean dark = PREFS.getBoolean("darkMode", false);
		themeToggle.setSelected(dark);
		themeToggle.setText(dark ? "🌜" : "🌞");
		themeToggle.setToolTipText(dark ? "다크 모드" : "라이트 모드");
		themeToggle.addItemListener(e -> {
			boolean sel = e.getStateChange() == ItemEvent.SELECTED;
			PREFS.putBoolean("darkMode", sel);
			try {
				UIManager.setLookAndFeel(sel ? DARK : LIGHT);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			applyGlobalFont(GLOBAL_FONT);
			SwingUtilities.updateComponentTreeUI(this);

			themeToggle.setText(sel ? "🌜" : "🌞");
			themeToggle.setToolTipText(sel ? "다크 모드" : "라이트 모드");
		});

		JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
		rightPanel.setOpaque(false);
		rightPanel.add(themeToggle);

		headerBar.add(appTitle, BorderLayout.WEST);
		headerBar.add(rightPanel, BorderLayout.EAST);

		JPanel tabPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 8));
		tabPanel.setOpaque(false);

		todoButton = createTypeToggle("To-Do List");
		memoButton = createTypeToggle("메모장");
		ButtonGroup typeGroup = new ButtonGroup();
		typeGroup.add(todoButton);
		typeGroup.add(memoButton);

		Color accent = UIManager.getColor("Component.accentColor");
		if (accent == null)
			accent = UIManager.getColor("Component.focusColor");
		if (accent == null)
			accent = new Color(80, 140, 200);

		todoButton.putClientProperty("accent", accent);
		memoButton.putClientProperty("accent", accent);

		memoButton.setSelected(true);
		todoButton.addActionListener(e -> switchContent("todo"));
		memoButton.addActionListener(e -> switchContent("memo"));

		tabPanel.add(todoButton);
		tabPanel.add(memoButton);

		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.setOpaque(false);
		topPanel.add(headerBar, BorderLayout.NORTH);
		topPanel.add(tabPanel, BorderLayout.CENTER);

		JSeparator separator = new JSeparator();
		separator.setForeground(UIManager.getColor("Separator.foreground"));
		topPanel.add(separator, BorderLayout.SOUTH);

		contentPanel = new JPanel(new CardLayout());
		memoPanel = new MemoPanel(userId);
		todoPanel = new TodoPanel(userId);
		contentPanel.add(memoPanel, "memo");
		contentPanel.add(todoPanel, "todo");

		add(topPanel, BorderLayout.NORTH);
		add(contentPanel, BorderLayout.CENTER);

		switchContent("memo");
	}

	private void applySavedLookAndFeel() {
		boolean dark = PREFS.getBoolean("darkMode", false);
		try {
			UIManager.setLookAndFeel(dark ? DARK : LIGHT);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void applyGlobalFont(Font font) {
		FontUIResource fr = new FontUIResource(font);
		UIDefaults defaults = UIManager.getDefaults();
		for (Object key : defaults.keySet()) {
			Object val = defaults.get(key);
			if (val instanceof FontUIResource) {
				UIManager.put(key, fr);
			}
		}
	}

	private void updateUIAll() {
		SwingUtilities.updateComponentTreeUI(this);
		memoPanel.updateUI();
		todoPanel.updateUI();
		for (AbstractButton btn : new AbstractButton[] { todoButton, memoButton }) {
			for (java.awt.event.ItemListener il : btn.getItemListeners()) {
				il.itemStateChanged(new java.awt.event.ItemEvent(btn, ItemEvent.ITEM_STATE_CHANGED, btn,
						btn.isSelected() ? ItemEvent.SELECTED : ItemEvent.DESELECTED));
			}
		}
	}

	private JToggleButton createTypeToggle(String text) {
		JToggleButton btn = new JToggleButton(text);
		btn.setFocusPainted(false);
		btn.setFont(GLOBAL_FONT.deriveFont(Font.BOLD, 16f));
		btn.setOpaque(false);
		btn.setContentAreaFilled(false);
		btn.setBorder(BorderFactory.createEmptyBorder(7, 30, 7, 30));
		btn.setForeground(UIManager.getColor("Label.foreground"));
		btn.setBackground(new Color(0, 0, 0, 0));

		btn.addItemListener(e -> {
			boolean sel = btn.isSelected();
			Color accent = (Color) btn.getClientProperty("accent");
			btn.setFont(GLOBAL_FONT.deriveFont(sel ? 18f : 16f));
			btn.setForeground(sel ? accent != null ? blend(UIManager.getColor("Label.foreground"), accent, 0.38f)
					: UIManager.getColor("Label.foreground").darker() : UIManager.getColor("Label.foreground"));
			btn.setBorder(sel
					? BorderFactory.createCompoundBorder(
							BorderFactory.createMatteBorder(0, 0, 3, 0,
									accent != null ? accent : new Color(80, 140, 200)),
							BorderFactory.createEmptyBorder(4, 30, 3, 30))
					: BorderFactory.createEmptyBorder(7, 30, 7, 30));
			btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		});
		return btn;
	}

	private static Color blend(Color c1, Color c2, float ratio) {
		if (c1 == null || c2 == null)
			return c1 != null ? c1 : c2;
		float ir = 1.0f - ratio;
		int r = (int) (c1.getRed() * ir + c2.getRed() * ratio);
		int g = (int) (c1.getGreen() * ir + c2.getGreen() * ratio);
		int b = (int) (c1.getBlue() * ir + c2.getBlue() * ratio);
		return new Color(r, g, b);
	}

	private void switchContent(String name) {
		((CardLayout) contentPanel.getLayout()).show(contentPanel, name);
	}
}
